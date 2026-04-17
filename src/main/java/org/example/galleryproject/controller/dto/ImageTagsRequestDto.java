package org.example.galleryproject.controller.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ImageTagsRequestDto(
        @NotEmpty(message = "at least one tag is required")
        List<
                @NotBlank(message = "tag cannot be blank")
                @Size(min = 1, max = 40, message = "tag must be between 1 and 40 characters")
                String> tags
) {
}
