package tg.edtch.activEducation.shared.minio.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@Component
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    private String url;
    private String accessKey;
    private String secretKey;
    private Bucket bucket = new Bucket();

    @Data
    public static class Bucket {
        private String images = "activeducation-images";
        private String videos = "activeducation-videos";
        private String documents = "activeducation-documents";

        public String getImages() {
            return images;
        }

        public String getVideos() {
            return videos;
        }

        public String getDocuments() {
            return documents;
        }
    }

    public String getEndpoint() {
        return url;
    }

    public String getAccessKey() {
        return accessKey != null ? accessKey : "minioadmin";
    }

    public String getSecretKey() {
        return secretKey != null ? secretKey : "minioadmin123";
    }

    public Bucket getBucket() {
        return bucket;
    }

    public String getUrl() {
        return url != null ? url : "http://localhost:9000";
    }
}
