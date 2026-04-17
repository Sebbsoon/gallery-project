package org.example.galleryproject.service;

import org.example.galleryproject.client.SupabaseClient;
import org.example.galleryproject.controller.dto.ImageRequestDto;
import org.example.galleryproject.controller.dto.ImageTagsRequestDto;
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
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                false
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
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                false
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
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                false
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
                Optional.of(expected),
                Optional.empty(),
                Optional.empty(),
                false
        );
        GalleryService service = new GalleryService(client);

        GalleryImage actual = service.updateImageVisibility(42, new ImageVisibilityRequestDto(Boolean.TRUE)).orElseThrow();

        assertEquals(expected, actual);
    }

    @Test
    void deleteImageDelegatesToClient() {
        StubSupabaseClient client = new StubSupabaseClient(
                List.of(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                true
        );
        GalleryService service = new GalleryService(client);

        boolean deleted = service.deleteImage(42);

        assertEquals(true, deleted);
    }

    @Test
    void addImageTagsDelegatesToClient() {
        GalleryImage expected = sampleImage(42L);
        StubSupabaseClient client = new StubSupabaseClient(
                List.of(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(expected),
                Optional.empty(),
                false
        );
        GalleryService service = new GalleryService(client);

        GalleryImage actual = service.addImageTags(42, new ImageTagsRequestDto(List.of("nature", "indoor"))).orElseThrow();

        assertEquals(expected, actual);
    }

    @Test
    void removeImageTagDelegatesToClient() {
        GalleryImage expected = sampleImage(42L);
        StubSupabaseClient client = new StubSupabaseClient(
                List.of(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(expected),
                false
        );
        GalleryService service = new GalleryService(client);

        GalleryImage actual = service.removeImageTag(42, "nature").orElseThrow();

        assertEquals(expected, actual);
    }

    @Test
    void getVisibleImagesFiltersOutHiddenImages() {
        GalleryImage visible = sampleImage(1L);
        GalleryImage hidden = new GalleryImage(
                2L,
                "hidden.jpg",
                "hidden",
                "hidden",
                "image-gallery/hidden.jpg",
                true,
                "2026-01-01T00:00:00Z",
                "2026-01-01T00:00:00Z",
                "https://example.com/hidden.jpg",
                List.of("private")
        );
        StubSupabaseClient client = new StubSupabaseClient(
                List.of(visible, hidden),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                false
        );
        GalleryService service = new GalleryService(client);

        List<GalleryImage> actual = service.getVisibleImages(null);

        assertEquals(List.of(visible), actual);
    }

    @Test
    void getVisibleImagesFiltersByTag() {
        GalleryImage nature = sampleImage(1L);
        GalleryImage city = new GalleryImage(
                2L,
                "city.jpg",
                "city",
                "city",
                "image-gallery/city.jpg",
                false,
                "2026-01-01T00:00:00Z",
                "2026-01-01T00:00:00Z",
                "https://example.com/city.jpg",
                List.of("urban")
        );
        StubSupabaseClient client = new StubSupabaseClient(
                List.of(nature, city),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                false
        );
        GalleryService service = new GalleryService(client);

        List<GalleryImage> actual = service.getVisibleImages(List.of("nature"));

        assertEquals(List.of(nature), actual);
    }

    @Test
    void getVisibleImageByIdReturnsEmptyWhenHidden() {
        GalleryImage hidden = new GalleryImage(
                2L,
                "hidden.jpg",
                "hidden",
                "hidden",
                "image-gallery/hidden.jpg",
                true,
                "2026-01-01T00:00:00Z",
                "2026-01-01T00:00:00Z",
                "https://example.com/hidden.jpg",
                List.of("private")
        );
        StubSupabaseClient client = new StubSupabaseClient(
                List.of(),
                Optional.of(hidden),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                false
        );
        GalleryService service = new GalleryService(client);

        Optional<GalleryImage> actual = service.getVisibleImageById(2);

        assertEquals(Optional.empty(), actual);
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
        private final Optional<GalleryImage> tagsAddedImage;
        private final Optional<GalleryImage> tagRemovedImage;
        private final boolean deleteResult;

        StubSupabaseClient(
                List<GalleryImage> allImages,
                Optional<GalleryImage> imageById,
                Optional<GalleryImage> updatedImage,
                Optional<GalleryImage> visibilityUpdatedImage,
                Optional<GalleryImage> tagsAddedImage,
                Optional<GalleryImage> tagRemovedImage,
                boolean deleteResult
        ) {
            super("image-gallery", "images", "https://example.com", "key");
            this.allImages = allImages;
            this.imageById = imageById;
            this.updatedImage = updatedImage;
            this.visibilityUpdatedImage = visibilityUpdatedImage;
            this.tagsAddedImage = tagsAddedImage;
            this.tagRemovedImage = tagRemovedImage;
            this.deleteResult = deleteResult;
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

        @Override
        public boolean deleteImage(int id) {
            return deleteResult;
        }

        @Override
        public Optional<GalleryImage> addTagsToImage(int id, List<String> tags) {
            return tagsAddedImage;
        }

        @Override
        public Optional<GalleryImage> removeTagFromImage(int id, String tagName) {
            return tagRemovedImage;
        }
    }
}
