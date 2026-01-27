package com.health.spry.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.health.spry.dto.BookRequest;
import com.health.spry.dto.BookResponse;
import com.health.spry.dto.PagedResponse;
import com.health.spry.dto.WishlistRequest;
import com.health.spry.service.BookService;
import com.health.spry.service.WishlistService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Books", description = "Book inventory management APIs with CRUD, search, pagination and wishlist")
public class BookController {

	@Autowired
    private final BookService bookService;
	
	@Autowired
    private final WishlistService wishlistService;

    @PostMapping
    @Operation(summary = "Create a new book", description = "Add a new book to the library inventory")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Book created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate ISBN",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
            
                    content = @Content)
    })
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
        log.info("Received request to create book: {}", request.getTitle());
        BookResponse response = bookService.createBook(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all books", description = "Retrieve a paginated list of books with optional (id, title, author, published-year) filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Books retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content)
    })
    public ResponseEntity<PagedResponse<BookResponse>> getAllBooks(
            @Parameter(description = "Filter by author name") @RequestParam(required = false) String author,
            @Parameter(description = "Filter by published year") @RequestParam(required = false) Integer publishedYear,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") @Min(1) int size,
            @Parameter(description = "Sort by field (id, title, author, publishedYear)") @RequestParam(defaultValue = "id") String sortBy) {
        log.info("Received request to get all books");
        PagedResponse<BookResponse> response = bookService.getAllBooks(author, publishedYear, page, size, sortBy);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get book by ID", description = "Retrieve a specific book by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "404", description = "Book not found",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content)
    })
    public ResponseEntity<BookResponse> getBookById(
            @Parameter(description = "Book ID", required = true) @PathVariable @Min(1) Long id) {
        log.info("Received request to get book with ID: {}", id);
        BookResponse response = bookService.getBookById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update book", description = "Update an existing book's details. Updates to availabilityStatus trigger Kafka notifications.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "404", description = "Book not found",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content)
    })
    public ResponseEntity<BookResponse> updateBook(
            @Parameter(description = "Book ID", required = true) @PathVariable @Min(1) Long id,
            @Valid @RequestBody BookRequest request) {
        log.info("Received request to update book with ID: {}", id);
        BookResponse response = bookService.updateBook(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete book", description = "Soft delete a book from the inventory (sets deleted flag)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Book deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Book not found",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content)
    })
    public ResponseEntity<String> deleteBook(
            @Parameter(description = "Book ID", required = true) @PathVariable @Min(1) Long id) {
        log.info("Received request to delete book with ID: {}", id);
        bookService.deleteBook(id);
        return new ResponseEntity<>("Book deleted successfully", HttpStatus.NO_CONTENT);
    }

    @GetMapping("/search")
    @Operation(summary = "Search books", description = "Search for books by title or author using partial matching")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid search query",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content)
    })
    public ResponseEntity<List<BookResponse>> searchBooks(
            @Parameter(description = "Search query for title or author", required = true)
            @RequestParam @NotBlank(message = "Search query is required") String query) {
        log.info("Received request to search books with query: {}", query);
        List<BookResponse> response = bookService.searchBooks(query);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/wishlist")
    @Operation(summary = "Add book to wishlist", description = "Add a book to user's wishlist for availability notifications")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book added to wishlist"),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content)
    })
    public ResponseEntity<String> addToWishlist(@Valid @RequestBody WishlistRequest request) {
        log.info("Received request to add book to wishlist");
        wishlistService.addToWishlist(request);
        return ResponseEntity.ok("Book Added to the wishlist");
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the book service is running")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service is healthy",
                    content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Book Service is running");
    }
}
