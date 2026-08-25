package tg.edtch.activEducation.shared.minio.service.impl;

/**
 * Résultat du traitement d'une image.
 *
 * @param bytes       les octets de l'image optimisée
 * @param contentType le nouveau content-type (peut différer de l'original)
 */
public record ImageProcessResult(byte[] bytes, String contentType) {
}
