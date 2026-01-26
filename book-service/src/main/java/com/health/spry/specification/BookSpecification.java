package com.health.spry.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.health.spry.model.Book;

import jakarta.persistence.criteria.Predicate;

public class BookSpecification {

    public static Specification<Book> filterBooks(String author, Integer publishedYear, String search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always exclude soft-deleted books
            predicates.add(criteriaBuilder.equal(root.get("deleted"), false));

            // Filter by author
            if (author != null && !author.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("author")),
                        "%" + author.toLowerCase() + "%"
                ));
            }

            // Filter by published year
            if (publishedYear != null) {
                predicates.add(criteriaBuilder.equal(root.get("publishedYear"), publishedYear));
            }

            // Search by title or author (partial matching)
            if (search != null && !search.isEmpty()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                Predicate titlePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        searchPattern
                );
                Predicate authorPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("author")),
                        searchPattern
                );
                predicates.add(criteriaBuilder.or(titlePredicate, authorPredicate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
