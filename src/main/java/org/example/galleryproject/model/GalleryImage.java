package org.example.galleryproject.model;

import java.util.List;

public record GalleryImage(int id, String url, List<String> tags) {
}
