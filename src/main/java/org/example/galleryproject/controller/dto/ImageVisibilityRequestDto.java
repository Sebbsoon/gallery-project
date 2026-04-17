package org.example.galleryproject.controller.dto;

import jakarta.validation.constraints.NotNull;

public record ImageVisibilityRequestDto(
        @NotNull(message = "hidden is required")
        Boolean hidden
) {
}
