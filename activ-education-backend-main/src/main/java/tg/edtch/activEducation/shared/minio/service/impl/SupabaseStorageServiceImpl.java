package tg.edtch.activEducation.shared.minio.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import tg.edtch.activEducation.shared.minio.config.SupabaseStorageProperties;
import tg.edtch.activEducation.shared.minio.dto.FileDownloadResponse;
import tg.edtch.activEducation.shared.minio.dto.FileMetadata;
import tg.edtch.activEducation.shared.minio.dto.FileUploadResponse;
import tg.edtch.activEducation.shared.minio.enums.FileType;
import tg.edtch.activEducation.shared.minio.exception.FileNotFoundException;
import tg.edtch.activEducation.shared.minio.exception.MinioException;
import tg.edtch.activEducation.shared.minio.service.MinioService;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@Slf4j
@Service
@org.springframework.context.annotation.Primary
public class SupabaseStorageServiceImpl implements MinioService {

    private final String storageUrl;
    private final String anonKey;
    private final String serviceRoleKey;
    private final RestTemplate restTemplate;

    public SupabaseStorageServiceImpl(SupabaseStorageProperties props) {
        this.storageUrl = props.getUrl() + "/storage/v1";
        this.anonKey = props.getAnonKey();
        this.serviceRoleKey = props.getServiceRoleKey();
        this.restTemplate = new RestTemplate();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeBuckets() {
        log.info("Supabase Storage ready (buckets created via API)");
    }

    @Override
    public FileUploadResponse uploadFile(MultipartFile file, FileType fileType) {
        return uploadFile(file, fileType, null);
    }

    @Override
    public FileUploadResponse uploadFile(MultipartFile file, FileType fileType, String customFileName) {
        try {
            String bucketId = getBucketId(fileType);
            String fileName = customFileName != null ? customFileName : generateFileName(file);
            String path = bucketId + "/" + fileName;

            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", anonKey);
            headers.set("Authorization", "Bearer " + serviceRoleKey);
            headers.setContentType(MediaType.valueOf(Objects.requireNonNull(file.getContentType(), "Unknown content type")));

            HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(), headers);
            String uploadUrl = storageUrl + "/object/" + path;

            ResponseEntity<String> response = restTemplate.exchange(uploadUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String publicUrl = storageUrl + "/object/public/" + path;
                log.info("File uploaded to Supabase: {}", publicUrl);
                return FileUploadResponse.builder()
                        .fileName(fileName)
                        .fileUrl(publicUrl)
                        .fileSize(file.getSize())
                        .contentType(file.getContentType())
                        .uploadedAt(LocalDateTime.now())
                        .build();
            }
            throw new MinioException("Upload failed: " + response.getStatusCode());
        } catch (MinioException e) {
            throw e;
        } catch (Exception e) {
            throw new MinioException("Upload failed: " + e.getMessage(), e);
        }
    }

    @Override
    public FileDownloadResponse downloadFile(String fileName, FileType fileType) {
        try {
            String bucketId = getBucketId(fileType);
            String url = storageUrl + "/object/authenticated/" + bucketId + "/" + fileName;

            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", anonKey);
            headers.set("Authorization", "Bearer " + serviceRoleKey);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                byte[] data = response.getBody();
                String contentType = response.getHeaders().getContentType() != null
                        ? response.getHeaders().getContentType().toString()
                        : "application/octet-stream";

                return FileDownloadResponse.builder()
                        .inputStream(new ByteArrayInputStream(data))
                        .contentType(contentType)
                        .fileName(fileName)
                        .fileSize((long) data.length)
                        .build();
            }
            throw new FileNotFoundException("File not found: " + fileName);
        } catch (HttpClientErrorException.NotFound e) {
            throw new FileNotFoundException("File not found: " + fileName);
        } catch (Exception e) {
            throw new MinioException("Download failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteFile(String fileName, FileType fileType) {
        try {
            String bucketId = getBucketId(fileType);
            String url = storageUrl + "/object/" + bucketId + "/" + fileName;

            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", anonKey);
            headers.set("Authorization", "Bearer " + serviceRoleKey);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Delete failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public FileMetadata getFileMetadata(String fileName, FileType fileType) {
        try {
            String bucketId = getBucketId(fileType);
            String url = storageUrl + "/object/info/public/" + bucketId + "/" + fileName;

            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", anonKey);
            headers.set("Authorization", "Bearer " + serviceRoleKey);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            if (response.getBody() != null) {
                Map<String, Object> info = response.getBody();
                    return FileMetadata.builder()
                            .fileName(fileName)
                            .fileSize((Long) info.getOrDefault("size", 0L))
                            .contentType((String) info.getOrDefault("mimetype", "application/octet-stream"))
                            .lastModified(LocalDateTime.now())
                            .build();
            }
            throw new FileNotFoundException("File not found: " + fileName);
        } catch (HttpClientErrorException.NotFound e) {
            throw new FileNotFoundException("File not found: " + fileName);
        } catch (Exception e) {
            throw new MinioException("Failed to get metadata: " + e.getMessage(), e);
        }
    }

    @Override
    public List<FileMetadata> listFiles(FileType fileType) {
        try {
            String bucketId = getBucketId(fileType);
            String url = storageUrl + "/object/list/" + bucketId;

            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", anonKey);
            headers.set("Authorization", "Bearer " + serviceRoleKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("prefix", "");
            body.put("limit", 100);
            body.put("offset", 0);
            body.put("sortBy", Map.of("column", "name", "order", "asc"));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.POST, entity, List.class);

            List<FileMetadata> result = new ArrayList<>();
            if (response.getBody() != null) {
                for (Object item : response.getBody()) {
                    if (item instanceof Map<?, ?> obj) {
                        String updatedAt = (String) obj.get("updated_at");
                        LocalDateTime lastModified = LocalDateTime.now();
                        if (updatedAt != null) {
                            try {
                                lastModified = ZonedDateTime.parse(updatedAt).toLocalDateTime();
                            } catch (DateTimeParseException e) {
                                log.debug("Could not parse updated_at: {}", updatedAt);
                            }
                        }
                        result.add(FileMetadata.builder()
                                .fileName((String) obj.get("name"))
                                .fileSize(obj.get("metadata") instanceof Map m
                                        ? ((Number) m.getOrDefault("size", 0)).longValue()
                                        : 0L)
                                .contentType(obj.get("metadata") instanceof Map m
                                        ? (String) m.getOrDefault("mimetype", "application/octet-stream")
                                        : "application/octet-stream")
                                .lastModified(lastModified)
                                .build());
                    }
                }
            }
            return result;
        } catch (Exception e) {
            log.error("List files failed: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public boolean fileExists(String fileName, FileType fileType) {
        try {
            String bucketId = getBucketId(fileType);
            String url = storageUrl + "/object/info/public/" + bucketId + "/" + fileName;

            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", anonKey);
            headers.set("Authorization", "Bearer " + serviceRoleKey);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (Exception e) {
            log.debug("Error checking file existence: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getFileUrl(String fileName, FileType fileType) {
        String bucketId = getBucketId(fileType);
        return storageUrl + "/object/public/" + bucketId + "/" + fileName;
    }

    @Override
    public List<FileUploadResponse> uploadMultipleFiles(List<MultipartFile> files, FileType fileType) {
        return files.stream()
                .map(file -> uploadFile(file, fileType))
                .toList();
    }

    @Override
    public String getPresignedUrl(String fileName, FileType fileType, int expiryInMinutes) {
        return getFileUrl(fileName, fileType);
    }

    @Override
    public byte[] getFileContentAsBytes(String fileName, FileType fileType) {
        try {
            String bucketId = getBucketId(fileType);
            String url = storageUrl + "/object/authenticated/" + bucketId + "/" + fileName;

            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", anonKey);
            headers.set("Authorization", "Bearer " + serviceRoleKey);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            throw new FileNotFoundException("File not found: " + fileName);
        } catch (HttpClientErrorException.NotFound e) {
            throw new FileNotFoundException("File not found: " + fileName);
        } catch (Exception e) {
            throw new MinioException("Failed to get file content: " + e.getMessage(), e);
        }
    }

    @Override
    public String extractFileNameFromUrl(String url) {
        if (url == null || url.isEmpty()) return "";
        int lastSlash = url.lastIndexOf('/');
        return lastSlash >= 0 ? url.substring(lastSlash + 1) : url;
    }

    private String getBucketId(FileType fileType) {
        return fileType.getBucketSuffix();
    }

    private String generateFileName(MultipartFile file) {
        String originalName = Objects.requireNonNull(file.getOriginalFilename(), "File must have a name");
        String ext = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex > 0) {
            ext = originalName.substring(dotIndex);
        }
        return UUID.randomUUID() + ext;
    }
}
