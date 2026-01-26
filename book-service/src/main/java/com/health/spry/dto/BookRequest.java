package com.health.spry.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.health.spry.annotations.YearConstraint;
import com.health.spry.model.AvailabilityStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request object for creating or updating a book")
public class BookRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    @Schema(description = "Book title", example = "Clean Code: A Handbook of Agile Software Craftsmanship", required = true)
    private String title;

    @NotBlank(message = "Author is required")
    @Size(max = 100, message = "Author name cannot exceed 100 characters")
    @Schema(description = "Author name", example = "Robert C. Martin", required = true)
    private String author;

    @NotBlank(message = "ISBN is required")
    @Size(min = 5, max = 10, message = "ISBN must be between 5 and 10 characters")
    @Schema(description = "ISBN number (10 or 13 digits)", example = "978-0132350884", required = true)
    private String isbn;

    @NotNull(message = "Published year is required")
    @Min(value = 1000, message = "Year must be at least 1000")
    @YearConstraint(message = "Publication year cannot be in the future") // This is where the year validation comes in place
    @Schema(description = "Year the book was published", example = "2008", required = true, minimum = "1000")
    @JsonProperty("published-year")
    private Integer publishedYear;

    @NotNull(message = "Availability status is required")
    @Schema(description = "Current availability status of the book", example = "AVAILABLE", required = true, 
            allowableValues = {"AVAILABLE", "BORROWED"})
    @JsonProperty("availability-status")
    private AvailabilityStatus availabilityStatus;
}
