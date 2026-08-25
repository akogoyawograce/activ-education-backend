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
    private ImageProcessing imageProcessing = new ImageProcessing();

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

    /** Redimensionnement / compression automatique des images à l'upload. */
    @Data
    public static class ImageProcessing {
        /** Active le traitement d'image à l'upload. */
        private boolean enabled = true;
        /** Largeur max d'une bannière (paysage). */
        private int maxBannerWidth = 1280;
        /** Hauteur max d'une bannière (paysage). */
        private int maxBannerHeight = 400;
        /** Dimension max d'un logo (carré). */
        private int maxLogoSize = 512;
        /** Taille max générique (dépassée → downscale proportionnel, ratio préservé). */
        private int maxDimension = 2560;
        /** Qualité JPEG (0-1). */
        private float jpgQuality = 0.85f;
        /** Formats d'image traités (par content-type). */
        private String[] supportedTypes = {"image/jpeg", "image/png", "image/bmp", "image/gif"};
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
