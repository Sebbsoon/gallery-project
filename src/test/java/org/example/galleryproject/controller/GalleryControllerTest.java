package org.example.galleryproject.controller;

import org.example.galleryproject.controller.dto.ImageRequestDto;
import org.example.galleryproject.controller.dto.ImageTagsRequestDto;
import org.example.galleryproject.controller.dto.ImageVisibilityRequestDto;
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
        GalleryController controller = new GalleryController(
                new StubGalleryService(expected, Optional.empty(), Optional.empty(), Optional.empty(), false)
        );

        ResponseEntity<List<GalleryImage>> response = controller.getAllImages();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    @Test
    void getImagesByIdReturnsOkWhenImageExists() {
        GalleryImage expected = sampleImage(10L);
        GalleryController controller = new GalleryController(
                new StubGalleryService(List.of(), Optional.of(expected), Optional.empty(), Optional.empty(), false)
        );

        ResponseEntity<GalleryImage> response = controller.getImagesById(10);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    @Test
    void getImagesByIdReturnsNotFoundWhenImageDoesNotExist() {
        GalleryController controller = new GalleryController(
                new StubGalleryService(List.of(), Optional.empty(), Optional.empty(), Optional.empty(), false)
        );

        ResponseEntity<GalleryImage> response = controller.getImagesById(999);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void uploadLocalImagesReturnsOkOnSuccess() {
        GalleryController controller = new GalleryController(
                new StubGalleryService(List.of(), Optional.empty(), Optional.empty(), Optional.empty(), false)
        );

        ResponseEntity<Object> response = controller.uploadLocalImages();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void uploadLocalImagesReturnsServerErrorOnFailure() {
        GalleryController controller = new GalleryController(
                new StubGalleryService(List.of(), Optional.empty(), Optional.empty(), Optional.empty(), true)
        );

        ResponseEntity<Object> response = controller.uploadLocalImages();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void updateImageMetadataReturnsOkWhenImageExists() {
        GalleryImage expected = sampleImage(10L);
        GalleryController controller = new GalleryController(
                new StubGalleryService(List.of(), Optional.empty(), Optional.of(expected), Optional.empty(), false)
        );

        ResponseEntity<GalleryImage> response = controller.updateImageMetadata(10, new ImageRequestDto("Title", "Desc"));

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    @Test
    void updateImageMetadataReturnsNotFoundWhenImageDoesNotExist() {
        GalleryController controller = new GalleryController(
                new StubGalleryService(List.of(), Optional.empty(), Optional.empty(), Optional.empty(), false)
        );

        ResponseEntity<GalleryImage> response = controller.updateImageMetadata(10, new ImageRequestDto("Title", "Desc"));

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updateImageVisibilityReturnsOkWhenImageExists() {
        GalleryImage expected = sampleImage(11L);
        GalleryController controller = new GalleryController(
                new StubGalleryService(List.of(), Optional.empty(), Optional.empty(), Optional.of(expected), false)
        );

        ResponseEntity<GalleryImage> response = controller.updateImageVisibility(
                11,
                new ImageVisibilityRequestDto(Boolean.TRUE)
        );

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    @Test
    void updateImageVisibilityReturnsNotFoundWhenImageDoesNotExist() {
        GalleryController controller = new GalleryController(
                new StubGalleryService(List.of(), Optional.empty(), Optional.empty(), Optional.empty(), false)
        );

        ResponseEntity<GalleryImage> response = controller.updateImageVisibility(
                11,
                new ImageVisibilityRequestDto(Boolean.FALSE)
        );

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteImageReturnsNoContentWhenImageExists() {
        GalleryController controller = new GalleryController(new GalleryService(null) {
            @Override
            public boolean deleteImage(int id) {
                return true;
            }
        });

        ResponseEntity<Void> response = controller.deleteImage(1);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void deleteImageReturnsNotFoundWhenImageDoesNotExist() {
        GalleryController controller = new GalleryController(new GalleryService(null) {
            @Override
            public boolean deleteImage(int id) {
                return false;
            }
        });

        ResponseEntity<Void> response = controller.deleteImage(1);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteImageReturnsServerErrorWhenDeletionFails() {
        GalleryController controller = new GalleryController(new GalleryService(null) {
            @Override
            public boolean deleteImage(int id) {
                throw new RuntimeException("delete failed");
            }
        });

        ResponseEntity<Void> response = controller.deleteImage(1);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void addImageTagsReturnsOkWhenImageExists() {
        GalleryImage expected = sampleImage(12L);
        GalleryController controller = new GalleryController(new GalleryService(null) {
            @Override
            public Optional<GalleryImage> addImageTags(int id, ImageTagsRequestDto tagsRequest) {
                return Optional.of(expected);
            }
        });

        ResponseEntity<GalleryImage> response = controller.addImageTags(12, new ImageTagsRequestDto(List.of("nature")));

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    @Test
    void addImageTagsReturnsNotFoundWhenImageDoesNotExist() {
        GalleryController controller = new GalleryController(new GalleryService(null) {
            @Override
            public Optional<GalleryImage> addImageTags(int id, ImageTagsRequestDto tagsRequest) {
                return Optional.empty();
            }
        });

        ResponseEntity<GalleryImage> response = controller.addImageTags(12, new ImageTagsRequestDto(List.of("nature")));

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void removeImageTagReturnsOkWhenTagExists() {
        GalleryImage expected = sampleImage(13L);
        GalleryController controller = new GalleryController(new GalleryService(null) {
            @Override
            public Optional<GalleryImage> removeImageTag(int id, String tagName) {
                return Optional.of(expected);
            }
        });

        ResponseEntity<GalleryImage> response = controller.removeImageTag(13, "nature");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    @Test
    void removeImageTagReturnsNotFoundWhenTagMissing() {
        GalleryController controller = new GalleryController(new GalleryService(null) {
            @Override
            public Optional<GalleryImage> removeImageTag(int id, String tagName) {
                return Optional.empty();
            }
        });

        ResponseEntity<GalleryImage> response = controller.removeImageTag(13, "nature");

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
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
        private final Optional<GalleryImage> updatedImage;
        private final Optional<GalleryImage> visibilityUpdatedImage;
        private final boolean failUpload;

        StubGalleryService(
                List<GalleryImage> allImages,
                Optional<GalleryImage> imageById,
                Optional<GalleryImage> updatedImage,
                Optional<GalleryImage> visibilityUpdatedImage,
                boolean failUpload
        ) {
            super(null);
            this.allImages = allImages;
            this.imageById = imageById;
            this.updatedImage = updatedImage;
            this.visibilityUpdatedImage = visibilityUpdatedImage;
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

        @Override
        public Optional<GalleryImage> updateImageMetadata(int id, ImageRequestDto imageRequest) {
            return updatedImage;
        }

        @Override
        public Optional<GalleryImage> updateImageVisibility(int id, ImageVisibilityRequestDto visibilityRequest) {
            return visibilityUpdatedImage;
        }
    }
}
