package com.health.spry.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.health.spry.annotations.YearConstraint;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "books",indexes = {
	    @Index(name = "idx_books_deleted_status", columnList = "deleted,availability_status"),
	    @Index(name = "idx_books_deleted_author", columnList = "deleted,author"),
	    @Index(name = "idx_books_deleted_year", columnList = "deleted,published_year"),
	    @Index(name = "idx_books_deleted_title", columnList = "deleted,title")
	}, uniqueConstraints = {
    @UniqueConstraint(columnNames = "isbn")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank(message = "Author is required")
    @Size(max = 100, message = "Author name cannot exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String author;

    @NotBlank(message = "ISBN is required")
    @Size(min = 5, max = 10, message = "ISBN must be between 5 and 10 characters")
    @Column(nullable = false, unique = true, length = 13)
    private String isbn;

    @NotNull(message = "Published year is required")
    @Min(value = 1000, message = "Year must be at least 1000")
    @YearConstraint(message = "Publication year cannot be in the future") // This is where the year validation comes in place
    @Column(name = "published_year", nullable = false)
    private Integer publishedYear;

    @NotNull(message = "Availability status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status", nullable = false, length = 20)
    private AvailabilityStatus availabilityStatus;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
