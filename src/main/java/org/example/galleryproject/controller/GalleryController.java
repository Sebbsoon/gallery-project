package org.example.galleryproject.controller;

import java.util.List;

import org.example.galleryproject.controller.dto.ImageRequestDto;
import org.example.galleryproject.controller.dto.ImageVisibilityRequestDto;
import org.example.galleryproject.model.GalleryImage;
import org.example.galleryproject.service.GalleryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/images")
public class GalleryController {

    private final GalleryService galleryService;

    public GalleryController(GalleryService galleryService) {
        this.galleryService = galleryService;
    }

    @GetMapping
    public ResponseEntity<List<GalleryImage>> getAllImages() {
        List<GalleryImage> images = galleryService.getAllImages();
        return ResponseEntity.ok().body(images);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GalleryImage> getImagesById(@PathVariable int id) {
        return galleryService.getImageById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/upload")
    public ResponseEntity<Object> uploadLocalImages() {
        try {
            galleryService.uploadLocalImages();
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<GalleryImage> updateImageMetadata(
            @PathVariable int id,
            @Valid @RequestBody ImageRequestDto imageRequest
    ) {
        return galleryService.updateImageMetadata(id, imageRequest)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/visibility")
    public ResponseEntity<GalleryImage> updateImageVisibility(
            @PathVariable int id,
            @Valid @RequestBody ImageVisibilityRequestDto visibilityRequest
    ) {
        return galleryService.updateImageVisibility(id, visibilityRequest)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable int id) {
        try {
            boolean deleted = galleryService.deleteImage(id);
            if (!deleted) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
