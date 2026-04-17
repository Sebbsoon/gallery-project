package org.example.galleryproject.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.example.galleryproject.client.SupabaseClient;
import org.example.galleryproject.controller.dto.ImageRequestDto;
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

    public Optional<GalleryImage> updateImageMetadata(int id, ImageRequestDto imageRequest) {
        return client.updateImageMetadata(id, imageRequest.title(), imageRequest.description());
    }

    public Optional<GalleryImage> updateImageVisibility(int id, ImageVisibilityRequestDto visibilityRequest) {
        return client.updateImageVisibility(id, visibilityRequest.hidden());
    }

}
