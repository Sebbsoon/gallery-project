package org.example.galleryproject.client;

import org.example.galleryproject.client.dto.SignedUrlResponse;
import org.example.galleryproject.client.dto.SupabaseImageRecord;
import org.example.galleryproject.client.dto.SupabaseImageTagRecord;
import org.example.galleryproject.model.GalleryImage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SupabaseClient {

    private final String SUPABASE_STORAGE_NAME;
    private final String SUPABASE_DB_NAME;
    private final String BASE_URL;
    private final RestClient restClient;
    private static final int URL_TTL = 3600;
    private static final ParameterizedTypeReference<List<SupabaseImageRecord>> IMAGE_LIST_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<SupabaseImageTagRecord>> IMAGE_TAG_LIST_TYPE =
            new ParameterizedTypeReference<>() {};

    public SupabaseClient(@Value("${supabase.bucket-name}")
                          String supabaseStorageName, @Value("${supabase.db-name}") String supabaseDbName, @Value("${supabase.url}") String baseUrl, @Value("${supabase.api-key}") String apiKey) {
        SUPABASE_STORAGE_NAME = supabaseStorageName;
        SUPABASE_DB_NAME = supabaseDbName;
        this.BASE_URL = baseUrl;
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("apikey", apiKey)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }


    public void uploadImage(byte[] bytes, String fileName, String title, String description) {
        String bucketPath = SUPABASE_STORAGE_NAME + "/" + fileName;

        restClient.put()
                .uri("/storage/v1/object/{bucketPath}", bucketPath)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes)
                .retrieve()
                .toBodilessEntity();

        Map<String, Object> insertPayload = new LinkedHashMap<>();
        insertPayload.put("file_name", fileName);
        insertPayload.put("title", title);
        insertPayload.put("description", description);
        insertPayload.put("bucket_path", bucketPath);

        try {
            restClient.post()
                    .uri("/rest/v1/{table}", SUPABASE_DB_NAME)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Prefer", "return=minimal")
                    .body(insertPayload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException ex) {
            rollbackStorageUpload(bucketPath);
            throw new RuntimeException("Failed DB insert, rolled back storage upload", ex);
        }
    }

    private void rollbackStorageUpload(String bucketPath) {
        restClient.delete()
                .uri("/storage/v1/object/{bucketPath}", bucketPath)
                .retrieve()
                .toBodilessEntity();
    }

    public Optional<GalleryImage> fetchImageById(int id) {
        URI uri = UriComponentsBuilder.fromPath("/rest/v1/{table}")
                .queryParam("id", "eq." + id)
                .queryParam("select", "id,file_name,title,description,bucket_path,is_hidden,created_at,updated_at")
                .buildAndExpand(SUPABASE_DB_NAME)
                .toUri();

        List<SupabaseImageRecord> rows = restClient.get()
                .uri(uri)
                .retrieve()
                .body(IMAGE_LIST_TYPE);

        if (rows == null || rows.isEmpty()) {
            return Optional.empty();
        }

        SupabaseImageRecord row = rows.getFirst();
        Map<Long, List<String>> tagsByImageId = fetchTagsByImageIds(List.of(row.id()));
        return Optional.of(toGalleryImage(row, tagsByImageId.getOrDefault(row.id(), Collections.emptyList())));
    }

    public Optional<GalleryImage> updateImageMetadata(int id, String title, String description) {
        URI uri = UriComponentsBuilder.fromPath("/rest/v1/{table}")
                .queryParam("id", "eq." + id)
                .queryParam("select", "id,file_name,title,description,bucket_path,is_hidden,created_at,updated_at")
                .buildAndExpand(SUPABASE_DB_NAME)
                .toUri();

        Map<String, Object> patchPayload = new LinkedHashMap<>();
        patchPayload.put("title", title);
        patchPayload.put("description", description);

        List<SupabaseImageRecord> rows = restClient.patch()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Prefer", "return=representation")
                .body(patchPayload)
                .retrieve()
                .body(IMAGE_LIST_TYPE);

        if (rows == null || rows.isEmpty()) {
            return Optional.empty();
        }

        SupabaseImageRecord row = rows.getFirst();
        Map<Long, List<String>> tagsByImageId = fetchTagsByImageIds(List.of(row.id()));
        return Optional.of(toGalleryImage(row, tagsByImageId.getOrDefault(row.id(), Collections.emptyList())));
    }

    public List<GalleryImage> fetchAllImages() {
        URI uri = UriComponentsBuilder.fromPath("/rest/v1/{table}")
                .queryParam("select", "id,file_name,title,description,bucket_path,is_hidden,created_at,updated_at")
                .queryParam("order", "id.asc")
                .buildAndExpand(SUPABASE_DB_NAME)
                .toUri();

        List<SupabaseImageRecord> rows = restClient.get()
                .uri(uri)
                .retrieve()
                .body(IMAGE_LIST_TYPE);

        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> imageIds = rows.stream().map(SupabaseImageRecord::id).toList();
        Map<Long, List<String>> tagsByImageId = fetchTagsByImageIds(imageIds);

        List<GalleryImage> images = new ArrayList<>(rows.size());
        for (SupabaseImageRecord row : rows) {
            images.add(toGalleryImage(row, tagsByImageId.getOrDefault(row.id(), Collections.emptyList())));
        }
        return images;
    }

    private Map<Long, List<String>> fetchTagsByImageIds(List<Long> imageIds) {
        if (imageIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String idsCsv = imageIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        URI uri = UriComponentsBuilder.fromPath("/rest/v1/image_tags")
                .queryParam("select", "image_id,tags(name)")
                .queryParam("image_id", "in.(" + idsCsv + ")")
                .build(true)
                .toUri();

        List<SupabaseImageTagRecord> rows = restClient.get()
                .uri(uri)
                .retrieve()
                .body(IMAGE_TAG_LIST_TYPE);

        if (rows == null || rows.isEmpty()) {
            return Collections.emptyMap();
        }

        return rows.stream()
                .filter(row -> row.tags() != null && row.tags().name() != null && !row.tags().name().isBlank())
                .collect(Collectors.groupingBy(
                        SupabaseImageTagRecord::imageId,
                        Collectors.mapping(row -> row.tags().name(), Collectors.toList())
                ));
    }

    private GalleryImage toGalleryImage(SupabaseImageRecord row, List<String> tags) {
        return new GalleryImage(
                row.id(),
                row.fileName(),
                row.title(),
                row.description(),
                row.bucketPath(),
                row.hidden(),
                row.createdAt(),
                row.updatedAt(),
                generateSignedUrl(row.bucketPath()),
                tags
        );
    }

    private String generateSignedUrl(String bucketPath) {
        SignedUrlResponse response = restClient.post()
                .uri("/storage/v1/object/sign/{bucketPath}", bucketPath)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("expiresIn", URL_TTL))
                .retrieve()
                .body(SignedUrlResponse.class);

        if (response == null || response.signedURL() == null || response.signedURL().isBlank()) {
            throw new IllegalStateException("Supabase did not return a signed URL");
        }

        if (response.signedURL().startsWith("http://") || response.signedURL().startsWith("https://")) {
            return response.signedURL();
        }
        return BASE_URL + "/storage/v1" + response.signedURL();
    }
}
