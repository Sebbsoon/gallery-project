package org.example.galleryproject.config;

import java.util.List;
import java.util.Optional;

import org.example.galleryproject.controller.dto.ImageRequestDto;
import org.example.galleryproject.controller.dto.ImageTagsRequestDto;
import org.example.galleryproject.controller.dto.ImageVisibilityRequestDto;
import org.example.galleryproject.model.GalleryImage;
import org.example.galleryproject.service.GalleryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GalleryService galleryService;

    @Test
    void getImagesIsAccessibleWithoutAuthentication() throws Exception {
        when(galleryService.getVisibleImages(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/images"))
                .andExpect(status().isOk());
    }

    @Test
    void getAdminImagesRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/images/admin"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAdminImagesForGuestTokenIsForbidden() throws Exception {
        mockMvc.perform(get("/api/images/admin")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAdminImagesForAdminTokenIsAllowed() throws Exception {
        when(galleryService.getAllImages()).thenReturn(List.of(sampleImage(1L)));

        mockMvc.perform(get("/api/images/admin")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void uploadRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/images/upload"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void uploadForGuestTokenIsForbidden() throws Exception {
        mockMvc.perform(post("/api/images/upload")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void uploadForAdminTokenIsAllowed() throws Exception {
        doNothing().when(galleryService).uploadLocalImages();

        mockMvc.perform(post("/api/images/upload")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void updateMetadataRequiresAuthentication() throws Exception {
        mockMvc.perform(put("/api/images/1")
                        .contentType("application/json")
                        .content("{\"title\":\"Updated\",\"description\":\"Desc\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateMetadataForGuestTokenIsForbidden() throws Exception {
        mockMvc.perform(put("/api/images/1")
                        .with(SecurityMockMvcRequestPostProcessors.jwt())
                        .contentType("application/json")
                        .content("{\"title\":\"Updated\",\"description\":\"Desc\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateMetadataForAdminTokenIsAllowed() throws Exception {
        when(galleryService.updateImageMetadata(anyInt(), any(ImageRequestDto.class)))
                .thenReturn(Optional.of(sampleImage(1L)));

        mockMvc.perform(put("/api/images/1")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType("application/json")
                        .content("{\"title\":\"Updated\",\"description\":\"Desc\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void updateVisibilityRequiresAuthentication() throws Exception {
        mockMvc.perform(patch("/api/images/1/visibility")
                        .contentType("application/json")
                        .content("{\"hidden\":true}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateVisibilityForGuestTokenIsForbidden() throws Exception {
        mockMvc.perform(patch("/api/images/1/visibility")
                        .with(SecurityMockMvcRequestPostProcessors.jwt())
                        .contentType("application/json")
                        .content("{\"hidden\":true}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateVisibilityForAdminTokenIsAllowed() throws Exception {
        when(galleryService.updateImageVisibility(anyInt(), any(ImageVisibilityRequestDto.class)))
                .thenReturn(Optional.of(sampleImage(1L)));

        mockMvc.perform(patch("/api/images/1/visibility")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType("application/json")
                        .content("{\"hidden\":true}"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteImageRequiresAuthentication() throws Exception {
        mockMvc.perform(delete("/api/images/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteImageForGuestTokenIsForbidden() throws Exception {
        mockMvc.perform(delete("/api/images/1")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteImageForAdminTokenIsAllowed() throws Exception {
        when(galleryService.deleteImage(anyInt())).thenReturn(true);

        mockMvc.perform(delete("/api/images/1")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void addTagsRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/images/1/tags")
                        .contentType("application/json")
                        .content("{\"tags\":[\"nature\"]}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addTagsForGuestTokenIsForbidden() throws Exception {
        mockMvc.perform(post("/api/images/1/tags")
                        .with(SecurityMockMvcRequestPostProcessors.jwt())
                        .contentType("application/json")
                        .content("{\"tags\":[\"nature\"]}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void addTagsForAdminTokenIsAllowed() throws Exception {
        when(galleryService.addImageTags(anyInt(), any(ImageTagsRequestDto.class)))
                .thenReturn(Optional.of(sampleImage(1L)));

        mockMvc.perform(post("/api/images/1/tags")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType("application/json")
                        .content("{\"tags\":[\"nature\"]}"))
                .andExpect(status().isOk());
    }

    @Test
    void removeTagRequiresAuthentication() throws Exception {
        mockMvc.perform(delete("/api/images/1/tags/nature"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void removeTagForGuestTokenIsForbidden() throws Exception {
        mockMvc.perform(delete("/api/images/1/tags/nature")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void removeTagForAdminTokenIsAllowed() throws Exception {
        when(galleryService.removeImageTag(anyInt(), any(String.class)))
                .thenReturn(Optional.of(sampleImage(1L)));

        mockMvc.perform(delete("/api/images/1/tags/nature")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    private GalleryImage sampleImage(long id) {
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
}
