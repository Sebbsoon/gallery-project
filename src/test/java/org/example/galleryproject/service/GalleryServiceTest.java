package org.example.galleryproject.service;

import org.example.galleryproject.client.SupabaseClient;
import org.example.galleryproject.controller.dto.ImageRequestDto;
import org.example.galleryproject.controller.dto.ImageVisibilityRequestDto;
import org.example.galleryproject.model.GalleryImage;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GalleryServiceTest {

    @Test
    void getAllImagesDelegatesToClient() {
        List<GalleryImage> expected = List.of(sampleImage(1L), sampleImage(2L));
        StubSupabaseClient client = new StubSupabaseClient(
                expected,
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
        GalleryService service = new GalleryService(client);

        List<GalleryImage> actual = service.getAllImages();

        assertEquals(expected, actual);
    }

    @Test
    void getImageByIdDelegatesToClient() {
        GalleryImage expected = sampleImage(42L);
        StubSupabaseClient client = new StubSupabaseClient(
                List.of(),
                Optional.of(expected),
                Optional.empty(),
                Optional.empty()
        );
        GalleryService service = new GalleryService(client);

        GalleryImage actual = service.getImageById(42).orElseThrow();

        assertEquals(expected, actual);
    }

    @Test
    void updateImageMetadataDelegatesToClient() {
        GalleryImage expected = sampleImage(42L);
        StubSupabaseClient client = new StubSupabaseClient(
                List.of(),
                Optional.empty(),
                Optional.of(expected),
                Optional.empty()
        );
        GalleryService service = new GalleryService(client);

        GalleryImage actual = service.updateImageMetadata(42, new ImageRequestDto("Updated", "New desc")).orElseThrow();

        assertEquals(expected, actual);
    }

    @Test
    void updateImageVisibilityDelegatesToClient() {
        GalleryImage expected = sampleImage(42L);
        StubSupabaseClient client = new StubSupabaseClient(
                List.of(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(expected)
        );
        GalleryService service = new GalleryService(client);

        GalleryImage actual = service.updateImageVisibility(42, new ImageVisibilityRequestDto(Boolean.TRUE)).orElseThrow();

        assertEquals(expected, actual);
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

    private static class StubSupabaseClient extends SupabaseClient {
        private final List<GalleryImage> allImages;
        private final Optional<GalleryImage> imageById;
        private final Optional<GalleryImage> updatedImage;
        private final Optional<GalleryImage> visibilityUpdatedImage;

        StubSupabaseClient(
                List<GalleryImage> allImages,
                Optional<GalleryImage> imageById,
                Optional<GalleryImage> updatedImage,
                Optional<GalleryImage> visibilityUpdatedImage
        ) {
            super("image-gallery", "images", "https://example.com", "key");
            this.allImages = allImages;
            this.imageById = imageById;
            this.updatedImage = updatedImage;
            this.visibilityUpdatedImage = visibilityUpdatedImage;
        }

        @Override
        public List<GalleryImage> fetchAllImages() {
            return allImages;
        }

        @Override
        public Optional<GalleryImage> fetchImageById(int id) {
            return imageById;
        }

        @Override
        public Optional<GalleryImage> updateImageMetadata(int id, String title, String description) {
            return updatedImage;
        }

        @Override
        public Optional<GalleryImage> updateImageVisibility(int id, boolean hidden) {
            return visibilityUpdatedImage;
        }
    }
}
