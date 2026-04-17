package org.example.galleryproject.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.example.galleryproject.client.SupabaseClient;
import org.example.galleryproject.controller.dto.ImageRequestDto;
import org.example.galleryproject.controller.dto.ImageTagsRequestDto;
import org.example.galleryproject.controller.dto.ImageVisibilityRequestDto;
import org.example.galleryproject.model.GalleryImage;
import org.springframework.stereotype.Service;

@Service
public class GalleryService {

    private final SupabaseClient client;

    public GalleryService(SupabaseClient client) {
        this.client = client;
    }

    public void uploadLocalImages() {
        File folder = new File("uploads/images/");
        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            return;
        }

        for (File file : files) {
            try {
                upload(file);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload file: " + file.getName(), e);
            }
        }
    }

    private void upload(File file) throws IOException {
        String id = UUID.randomUUID().toString();
        String fileName = id + "-" + file.getName();
        byte[] bytes = Files.readAllBytes(file.toPath());
        String title = deriveTitle(file.getName());

        client.uploadImage(bytes, fileName, title, null);
    }

    private String deriveTitle(String originalName) {
        int dotIndex = originalName.lastIndexOf('.');
        String nameWithoutExtension = dotIndex > 0 ? originalName.substring(0, dotIndex) : originalName;
        return nameWithoutExtension.replace('_', ' ').trim();
    }

    public List<GalleryImage> getAllImages() {
        return client.fetchAllImages();
    }

    public Optional<GalleryImage> getImageById(int id) {
        return client.fetchImageById(id);
    }

    public List<GalleryImage> getVisibleImages(List<String> tags) {
        List<GalleryImage> visibleImages = client.fetchAllImages().stream()
                .filter(image -> !image.hidden())
                .collect(Collectors.toList());

        List<String> normalizedTags = normalizeTags(tags);
        if (normalizedTags.isEmpty()) {
            return visibleImages;
        }

        return visibleImages.stream()
                .filter(image -> {
                    List<String> imageTags = image.tags() == null ? Collections.emptyList() : image.tags();
                    return imageTags.stream()
                            .map(tag -> tag == null ? "" : tag.trim().toLowerCase(Locale.ROOT))
                            .anyMatch(normalizedTags::contains);
                })
                .collect(Collectors.toList());
    }

    public Optional<GalleryImage> getVisibleImageById(int id) {
        return client.fetchImageById(id).filter(image -> !image.hidden());
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> normalized = new ArrayList<>();
        for (String rawTag : tags) {
            if (rawTag == null || rawTag.isBlank()) {
                continue;
            }
            String[] splitByComma = rawTag.split(",");
            for (String part : splitByComma) {
                String cleaned = part.trim().toLowerCase(Locale.ROOT);
                if (!cleaned.isBlank() && !normalized.contains(cleaned)) {
                    normalized.add(cleaned);
                }
            }
        }
        return normalized;
    }

    public Optional<GalleryImage> updateImageMetadata(int id, ImageRequestDto imageRequest) {
        return client.updateImageMetadata(id, imageRequest.title(), imageRequest.description());
    }

    public Optional<GalleryImage> updateImageVisibility(int id, ImageVisibilityRequestDto visibilityRequest) {
        return client.updateImageVisibility(id, visibilityRequest.hidden());
    }

    public boolean deleteImage(int id) {
        return client.deleteImage(id);
    }

    public Optional<GalleryImage> addImageTags(int id, ImageTagsRequestDto tagsRequest) {
        List<String> normalizedTags = tagsRequest.tags().stream()
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
        return client.addTagsToImage(id, normalizedTags);
    }

    public Optional<GalleryImage> removeImageTag(int id, String tagName) {
        return client.removeTagFromImage(id, tagName.trim());
    }

}
