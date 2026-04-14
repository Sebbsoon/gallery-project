package org.example.galleryproject.controller;

import java.net.URI;
import java.util.List;

import org.example.galleryproject.controller.dto.ImageRequestDto;
import org.example.galleryproject.model.GalleryImage;
import org.example.galleryproject.service.GalleryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

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
    public Mono<ResponseEntity<GalleryImage>> getImagesById(@PathVariable int id) {
        return galleryService.getImageById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/upload")
    public Mono<ResponseEntity<Object>> uploadLocalImages() {
        return galleryService.uploadLocalImages()
                .then(Mono.just(ResponseEntity.ok().build()))
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<GalleryImage>> updateImage(@PathVariable int id, @RequestParam ImageRequestDto image) {
        return galleryService.updateImage(id, image)
                .map(updated -> ResponseEntity
                        .created(URI.create("/images/" + updated.id()))
                        .body(updated))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
