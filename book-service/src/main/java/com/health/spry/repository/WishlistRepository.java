package com.health.spry.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.health.spry.model.Wishlist;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByBookId(Long bookId);
    Boolean existsByUserIdAndBookId(Long userId, Long bookId);
}
