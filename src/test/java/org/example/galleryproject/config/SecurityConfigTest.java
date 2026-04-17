package org.example.galleryproject.config;

import java.util.List;

import org.example.galleryproject.service.GalleryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        when(galleryService.getAllImages()).thenReturn(List.of());

        mockMvc.perform(get("/api/images"))
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
}
