package org.example.galleryproject.model;

import java.util.List;

public record GalleryImage(
        long id,
        String fileName,
        String title,
        String description,
        String bucketPath,
        boolean hidden,
        String createdAt,
        String updatedAt,
        String url,
        List<String> tags
) {
}
