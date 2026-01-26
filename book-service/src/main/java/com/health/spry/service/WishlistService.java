package com.health.spry.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.health.spry.dto.WishlistRequest;
import com.health.spry.exception.BookNotFoundException;
import com.health.spry.exception.DuplicateWishlistException;
import com.health.spry.model.Wishlist;
import com.health.spry.repository.BookRepository;
import com.health.spry.repository.WishlistRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final BookRepository bookRepository;

    @Transactional
    public void addToWishlist(WishlistRequest request) throws DuplicateWishlistException {
    	
    	// Update logic to get user from user auth service 
        log.info("Adding book {} to wishlist for user {}", request.getBookId(), request.getUserId());
        

        // Verify book exists and is not deleted
        bookRepository.findByIdAndDeletedFalse(request.getBookId())
                .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + request.getBookId()));

        // Check if already in wishlist
        if (wishlistRepository.existsByUserIdAndBookId(request.getUserId(), request.getBookId())) {
            log.warn("Book {} already in wishlist for user {}", request.getBookId(), request.getUserId());
            throw new DuplicateWishlistException("Book already preesent in User's Wishlist");
        }

        Wishlist wishlist = Wishlist.builder()
                .userId(request.getUserId())
                .bookId(request.getBookId())
                .build();

        wishlistRepository.save(wishlist);
        log.info("Book added to wishlist successfully");
    }
}
