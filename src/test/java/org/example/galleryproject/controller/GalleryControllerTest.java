package org.example.galleryproject.controller;

import org.example.galleryproject.model.GalleryImage;
import org.example.galleryproject.service.GalleryService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GalleryControllerTest {

    @Test
    void getAllImagesReturnsOkAndBody() {
        List<GalleryImage> expected = List.of(sampleImage(1L));
        GalleryController controller = new GalleryController(new StubGalleryService(expected, Optional.empty(), false));

        ResponseEntity<List<GalleryImage>> response = controller.getAllImages();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    @Test
    void getImagesByIdReturnsOkWhenImageExists() {
        GalleryImage expected = sampleImage(10L);
        GalleryController controller = new GalleryController(new StubGalleryService(List.of(), Optional.of(expected), false));

        ResponseEntity<GalleryImage> response = controller.getImagesById(10);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    @Test
    void getImagesByIdReturnsNotFoundWhenImageDoesNotExist() {
        GalleryController controller = new GalleryController(new StubGalleryService(List.of(), Optional.empty(), false));

        ResponseEntity<GalleryImage> response = controller.getImagesById(999);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void uploadLocalImagesReturnsOkOnSuccess() {
        GalleryController controller = new GalleryController(new StubGalleryService(List.of(), Optional.empty(), false));

        ResponseEntity<Object> response = controller.uploadLocalImages();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void uploadLocalImagesReturnsServerErrorOnFailure() {
        GalleryController controller = new GalleryController(
                new StubGalleryService(List.of(), Optional.empty(), true)
        );

        ResponseEntity<Object> response = controller.uploadLocalImages();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    private static GalleryImage sampleImage(long id) {
        return new GalleryImage(
                id,
                "image.jpg",
                "title",
                "description",
                "image-gallery/image.jpg",
                false,
                "2026-01-01T00:00:00Z",
                "2026-01-01T00:00:00Z",
                "https://example.com/image.jpg",
                List.of("nature")
        );
    }

    private static class StubGalleryService extends GalleryService {
        private final List<GalleryImage> allImages;
        private final Optional<GalleryImage> imageById;
        private final boolean failUpload;

        StubGalleryService(List<GalleryImage> allImages, Optional<GalleryImage> imageById, boolean failUpload) {
            super(null);
            this.allImages = allImages;
            this.imageById = imageById;
            this.failUpload = failUpload;
        }

        @Override
        public List<GalleryImage> getAllImages() {
            return allImages;
        }

        @Override
        public Optional<GalleryImage> getImageById(int id) {
            return imageById;
        }

        @Override
        public void uploadLocalImages() {
            if (failUpload) {
                throw new RuntimeException("upload failed");
            }
        }
    }
}
