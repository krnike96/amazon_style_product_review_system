package com.niket.productreviewsystem.model;

import com.niket.productreviewsystem.validator.NoUrl;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ReviewFormDTO {

    @NotNull(message = "Rating is required.")
    @Min(value = 1, message = "Rating must be at least 1 star.")
    @Max(value = 5, message = "Rating cannot exceed 5 stars.")
    private Integer rating;

    @Size(max = 5000, message = "Comment must not exceed 5000 characters.")
    @NoUrl // APPLYING THE CUSTOM VALIDATION HERE
    private String comment;

    // This field is for file upload and is not persisted directly
    private MultipartFile imageFile;

    private Long productId; // Hidden field to know which product the review is for
}