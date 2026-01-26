package com.health.spry.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistRequest {

    //@NotNull(message = "User ID is required") 
	// Intentionally removed the condition as we only want to wishlist against the validated user not any user 
    private Long userId;

    @NotNull(message = "Book ID is required")
    private Long bookId;
}
