package org.example.galleryproject.client;

import org.example.galleryproject.client.dto.GalleryImagePath;
import org.example.galleryproject.client.dto.SignedUrlResponse;
import org.example.galleryproject.model.GalleryImage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class SupabaseClient {

    private final String SUPABASE_STORAGE_NAME;
    private final String SUPABASE_DB_NAME;
    private final String BASE_URL;
    private final WebClient webClient;
    private final int URL_TTL = 3600;

    public SupabaseClient(@Value("${supabase.bucket-name}")
                          String supabaseStorageName, @Value("${supabase.db-name}") String supabaseDbName, @Value("${supabase.url}") String baseUrl, @Value("${supabase.api-key}") String apiKey) {
        SUPABASE_STORAGE_NAME = supabaseStorageName;
        SUPABASE_DB_NAME = supabaseDbName;
        this.BASE_URL = baseUrl;
        this.webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("apikey", apiKey)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }


    public Mono<Void> uploadImage(byte[] bytes, String fileName) {
        String bucketPath = SUPABASE_STORAGE_NAME + "/" + fileName;

        Mono<String> uploadMono = webClient.put()
                .uri("/storage/v1/object/" + bucketPath)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .bodyValue(bytes)
                .retrieve()
                .bodyToMono(String.class);

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        String insertJson = String.format("""
                {
                  "file_name": "%s",
                  "bucket_path": "%s",
                  "created_at": "%s"
                }
                """, fileName, bucketPath, now);

        Mono<String> insertMono = webClient.post()
                .uri("/rest/v1/" + SUPABASE_DB_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(insertJson)
                .retrieve()
                .bodyToMono(String.class);

        return uploadMono.flatMap(uploaded ->
                insertMono.onErrorResume(e ->
                        webClient.delete()
                                .uri("/storage/v1/object/" + bucketPath)
                                .retrieve()
                                .bodyToMono(String.class)
                                .then(Mono.error(new RuntimeException("Failed DB insert, rolled back storage", e)))
                )
        ).then();
    }

    private Mono<String> fetchBucketPathById(int id) {
        return webClient.get()
                .uri("/rest/v1/" + SUPABASE_DB_NAME + "?id=eq." + id + "&select=bucket_path")
                .retrieve()
                .bodyToFlux(GalleryImagePath.class) // map to DTO
                .next()
                .map(GalleryImagePath::bucket_path);
    }

    private Mono<String> generateSignedUrl(String bucketPath) {
        return webClient.post()
                .uri("/storage/v1/object/sign/" + bucketPath)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"expiresIn\":" + URL_TTL + "}")
                .retrieve()
                .bodyToMono(SignedUrlResponse.class)
                .map(res -> BASE_URL + "/storage/v1" + res.signedURL());
    }

    public Mono<GalleryImage> fetchImageByID(int id) {
        return fetchBucketPathById(id)
                .flatMap(this::generateSignedUrl)
                .map(url -> new GalleryImage(id, url, Collections.emptyList()));
    }

    public List<GalleryImage> fetchAllImages() {
        List<GalleryImage> images = new ArrayList<>();

        return images;
    }


}
