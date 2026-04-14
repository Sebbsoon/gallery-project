package org.example.galleryproject.service;

import org.example.galleryproject.client.SupabaseClient;
import org.example.galleryproject.controller.dto.ImageRequestDto;
import org.example.galleryproject.model.GalleryImage;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

@Service
public class GalleryService {

    private final SupabaseClient client;

    public GalleryService(SupabaseClient client) {
        this.client = client;
    }

    public Mono<Void> uploadLocalImages() {
        try {
            File folder = new File("uploads/images/");
            File[] files = folder.listFiles();
            if (files == null || files.length == 0) return Mono.empty();

            Mono<Void> uploads = Mono.empty();
            for (File file : files) {
                uploads = uploads.then(upload(file));
            }
            return uploads;

        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    private Mono<Void> upload(File file) throws IOException {
        String id = UUID.randomUUID().toString();
        String fileName = id + "-" + file.getName();
        byte[] bytes = Files.readAllBytes(file.toPath());

        return client.uploadImage(bytes, fileName);
    }

    public List<GalleryImage> getAllImages() {
        return client.fetchAllImages();
    }

    public Mono<GalleryImage> getImageById(int id) {
        return client.fetchImageByID(id);
    }

    public Mono<ResponseEntity<GalleryImage>> updateImage(int id, ImageRequestDto image) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateImage'");
    }
}
