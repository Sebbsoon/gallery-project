package org.example.galleryproject.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SupabaseImageRecord(
        @JsonProperty("id") long id,
        @JsonProperty("file_name") String fileName,
        @JsonProperty("title") String title,
        @JsonProperty("description") String description,
        @JsonProperty("bucket_path") String bucketPath,
        @JsonProperty("is_hidden") boolean hidden,
        @JsonProperty("created_at") String createdAt,
        @JsonProperty("updated_at") String updatedAt
) {
}
