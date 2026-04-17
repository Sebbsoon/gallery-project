package org.example.galleryproject.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SupabaseImageTagRecord(
        @JsonProperty("image_id") long imageId,
        @JsonProperty("tags") TagRecord tags
) {
    public record TagRecord(@JsonProperty("name") String name) {
    }
}
