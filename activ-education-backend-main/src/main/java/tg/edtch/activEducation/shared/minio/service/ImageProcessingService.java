package tg.edtch.activEducation.shared.minio.service;

import org.springframework.web.multipart.MultipartFile;
import tg.edtch.activEducation.shared.minio.service.impl.ImageProcessResult;

/**
 * Traitement automatique des images à l'upload : redimensionnement ciblé
 * (bannière paysage / logo carré) + compression (JPEG/PNG).
 */
public interface ImageProcessingService {

    /**
     * Traite une image selon l'usage visé.
     *
     * @return le résultat optimisé, ou {@code null} si l'image ne peut pas
     *         être traitée (format non supporté, GIF animé, fichier trop
     *         volumineux) — dans ce cas l'original doit être conservé tel quel.
     */
    ImageProcessResult process(MultipartFile file, ImagePurpose purpose);

    /** Usage de l'image : détermine les dimensions cibles. */
    enum ImagePurpose {
        /** Usage générique : downscale proportionnel si très grande image. */
        GENERIC,
        /** Bannière paysage (cover 1280x400). */
        BANNER,
        /** Logo carré (contain 512x512). */
        LOGO;

        /** Parse un paramètre « purpose » (null-safe) → GENERIC si inconnu. */
        public static ImagePurpose parse(String raw) {
            if (raw == null || raw.isBlank()) {
                return GENERIC;
            }
            try {
                return valueOf(raw.toUpperCase());
            } catch (IllegalArgumentException e) {
                return GENERIC;
            }
        }
    }
}
