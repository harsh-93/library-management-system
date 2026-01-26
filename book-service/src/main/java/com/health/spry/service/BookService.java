package com.health.spry.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.health.spry.dto.BookRequest;
import com.health.spry.dto.BookResponse;
import com.health.spry.dto.PagedResponse;
import com.health.spry.exception.BookNotFoundException;
import com.health.spry.exception.DuplicateIsbnException;
import com.health.spry.kafka.BookNotificationEvent;
import com.health.spry.kafka.BookNotificationProducer;
import com.health.spry.model.AvailabilityStatus;
import com.health.spry.model.Book;
import com.health.spry.model.Wishlist;
import com.health.spry.repository.BookRepository;
import com.health.spry.repository.WishlistRepository;
import com.health.spry.specification.BookSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final WishlistRepository wishlistRepository;
    private final BookNotificationProducer notificationProducer;

    @Transactional
    public BookResponse createBook(BookRequest request) {
        log.info("Creating book with ISBN: {}", request.getIsbn());

        // Check for duplicate ISBN
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateIsbnException("Book with ISBN '" + request.getIsbn() + "' already exists");
        }

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .publishedYear(request.getPublishedYear())
                .availabilityStatus(request.getAvailabilityStatus())
                .deleted(false)
                .build();

        Book savedBook = bookRepository.save(book);
        log.info("Book created successfully with ID: {}", savedBook.getId());

        return mapToResponse(savedBook);
    }

    @Transactional(readOnly = true)
    public PagedResponse<BookResponse> getAllBooks(String author, Integer publishedYear, 
                                                     int page, int size, String sortBy) {
        log.info("Fetching books - author: {}, year: {}, page: {}, size: {}", author, publishedYear, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        Specification<Book> spec = BookSpecification.filterBooks(author, publishedYear, null);

        Page<Book> bookPage = bookRepository.findAll(spec, pageable);

        List<BookResponse> content = bookPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PagedResponse.<BookResponse>builder()
                .content(content)
                .pageNumber(bookPage.getNumber())
                .pageSize(bookPage.getSize())
                .totalElements(bookPage.getTotalElements())
                .totalPages(bookPage.getTotalPages())
                .last(bookPage.isLast())
                .first(bookPage.isFirst())
                .build();
    }

    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        log.info("Fetching book with ID: {}", id);
        Book book = bookRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + id));
        return mapToResponse(book);
    }

    @Transactional
    public BookResponse updateBook(Long id, BookRequest request) {
        log.info("Updating book with ID: {}", id);

        Book book = bookRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + id));

        // Check for duplicate ISBN (excluding current book)
        if (!book.getIsbn().equals(request.getIsbn()) && 
            bookRepository.existsByIsbnAndIdNotAndDeletedFalse(request.getIsbn(), id)) {
            throw new DuplicateIsbnException("Book with ISBN '" + request.getIsbn() + "' already exists");
        }

        // Store previous status for notification check
        AvailabilityStatus previousStatus = book.getAvailabilityStatus();

        // Update book fields
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setPublishedYear(request.getPublishedYear());
        book.setAvailabilityStatus(request.getAvailabilityStatus());

        Book updatedBook = bookRepository.save(book);
        log.info("Book updated successfully with ID: {}", updatedBook.getId());

        // Trigger async notification if status changed from BORROWED to AVAILABLE
        if (previousStatus == AvailabilityStatus.BORROWED && 
            request.getAvailabilityStatus() == AvailabilityStatus.AVAILABLE) {
            log.info("Book status changed from BORROWED to AVAILABLE. Triggering notifications.");
            sendWishlistNotifications(updatedBook);
        }

        return mapToResponse(updatedBook);
    }

    @Transactional
    public void deleteBook(Long id) {
        log.info("Soft deleting book with ID: {}", id);

        Book book = bookRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + id));

        //This is where soft-delete is implemented
        book.setDeleted(true);
        book.setDeletedAt(LocalDateTime.now());
        bookRepository.save(book);

        log.info("Book soft deleted successfully with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public List<BookResponse> searchBooks(String query) {
        log.info("Searching books with query: {}", query);

        Specification<Book> spec = BookSpecification.filterBooks(null, null, query);
        List<Book> books = bookRepository.findAll(spec);

        return books.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void sendWishlistNotifications(Book book) {
        // This runs asynchronously via Kafka
        List<Wishlist> wishlists = wishlistRepository.findByBookId(book.getId());
        
        log.info("Found {} users with book {} on wishlist", wishlists.size(), book.getId());

        for (Wishlist wishlist : wishlists) {
            BookNotificationEvent event = BookNotificationEvent.builder()
                    .bookId(book.getId())
                    .bookTitle(book.getTitle())
                    .userId(wishlist.getUserId())
                    .eventType("BOOK_AVAILABLE")
                    .message("Book '" + book.getTitle() + "' is now available")
                    .build();

            notificationProducer.sendNotification(event);
        }
    }

    private BookResponse mapToResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .publishedYear(book.getPublishedYear())
                .availabilityStatus(book.getAvailabilityStatus())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }
}
