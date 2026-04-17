package org.example.galleryproject.controller.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

public record ImageRequestDto(
        @NotBlank(message = "title is required")
        @Size(min = 1, max = 120, message = "title must be between 1 and 120 characters")
        String title,
        @Size(max = 1_000, message = "description must be at most 1000 characters")
        String description
) {
}
