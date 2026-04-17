package org.example.galleryproject.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SupabaseImageTagLinkRecord(
        @JsonProperty("image_id") long imageId,
        @JsonProperty("tag_id") long tagId
) {
}
