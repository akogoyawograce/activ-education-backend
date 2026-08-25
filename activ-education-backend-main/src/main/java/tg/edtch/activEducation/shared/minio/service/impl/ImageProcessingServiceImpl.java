package tg.edtch.activEducation.shared.minio.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tg.edtch.activEducation.shared.minio.config.MinioProperties;
import tg.edtch.activEducation.shared.minio.service.ImageProcessingService;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * Redimensionne et compresse les images à l'upload :
 * <ul>
 *   <li>BANNER  : crop central + downscale vers 1280x400 (JPEG)</li>
 *   <li>LOGO    : contain vers 512x512 max, PNG si transparence sinon JPEG</li>
 *   <li>GENERIC : downscale proportionnel si dépasse 2560px, compression</li>
 * </ul>
 * Formats non supportés (webp, heic, svg, gif animé…) → {@code null} :
 * le fichier original est conservé tel quel.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImageProcessingServiceImpl implements ImageProcessingService {

    private static final long MAX_PROCESSED_BYTES = 30L * 1024 * 1024;

    private final MinioProperties minioProperties;

    @Override
    public ImageProcessResult process(MultipartFile file, ImagePurpose purpose) {
        if (!minioProperties.getImageProcessing().isEnabled()) {
            return null;
        }
        if (file == null || file.isEmpty()) {
            return null;
        }
        // Ne pas toucher aux gros fichiers (éviter OOM) : on les garde tels quels.
        if (file.getSize() > MAX_PROCESSED_BYTES) {
            log.debug("Image trop volumineuse ({} Mo), upload original", file.getSize() / 1024 / 1024);
            return null;
        }
        String contentType = file.getContentType();
        if (contentType == null) {
            return null;
        }
        // Certains clients envoient "image/jpg" au lieu de "image/jpeg"
        if (contentType.equalsIgnoreCase("image/jpg")) {
            contentType = "image/jpeg";
        }
        // GIF : ImageIO ne lit que la première frame → on préserve l'animation.
        if (contentType.equalsIgnoreCase("image/gif")) {
            return null;
        }
        boolean supported = false;
        for (String t : minioProperties.getImageProcessing().getSupportedTypes()) {
            if (contentType.equalsIgnoreCase(t)) {
                supported = true;
                break;
            }
        }
        if (!supported) {
            log.debug("Format image non traité : {}", contentType);
            return null;
        }

        try (InputStream in = file.getInputStream()) {
            BufferedImage source = ImageIO.read(in);
            if (source == null) {
                log.warn("Image illisible : {}", file.getOriginalFilename());
                return null;
            }
            BufferedImage target = resize(source, purpose);
            boolean hasAlpha = target.getColorModel().hasAlpha()
                    && target.getTransparency() != Transparency.OPAQUE;

            String outType = hasAlpha ? "png" : "jpg";
            byte[] bytes = encode(target, outType);
            if (bytes == null) {
                return null;
            }
            String outContentType = hasAlpha ? "image/png" : "image/jpeg";
            log.info("Image traitée : {} → {} ({}x{}, {} ko, {})",
                    file.getOriginalFilename(), purpose, target.getWidth(),
                    target.getHeight(), bytes.length / 1024, outType);
            return new ImageProcessResult(bytes, outContentType);
        } catch (IOException e) {
            log.warn("Erreur de traitement d'image (upload original) : {}", e.getMessage());
            return null;
        }
    }

    private BufferedImage resize(BufferedImage source, ImagePurpose purpose) {
        int w = source.getWidth();
        int h = source.getHeight();
        if (purpose == null) {
            purpose = ImagePurpose.GENERIC;
        }

        switch (purpose) {
            case BANNER -> {
                int maxW = minioProperties.getImageProcessing().getMaxBannerWidth();
                int maxH = minioProperties.getImageProcessing().getMaxBannerHeight();
                if (w <= maxW && h <= maxH) {
                    return source; // déjà plus petit que la cible : on garde
                }
                // Cover : downscale au ratio qui couvre la cible, puis crop central.
                double ratio = Math.max((double) maxW / w, (double) maxH / h);
                int sw = (int) Math.round(w * ratio);
                int sh = (int) Math.round(h * ratio);
                BufferedImage scaled = scale(source, sw, sh);
                int x = Math.max(0, (sw - maxW) / 2);
                int y = Math.max(0, (sh - maxH) / 2);
                return scaled.getSubimage(x, y, Math.min(maxW, sw), Math.min(maxH, sh));
            }
            case LOGO -> {
                int maxSize = minioProperties.getImageProcessing().getMaxLogoSize();
                if (w <= maxSize && h <= maxSize) {
                    return source; // logo déjà petit : taille native préservée
                }
                double ratio = Math.min((double) maxSize / w, (double) maxSize / h);
                int nw = Math.max(1, (int) Math.round(w * ratio));
                int nh = Math.max(1, (int) Math.round(h * ratio));
                return scale(source, nw, nh);
            }
            default -> {
                int maxDim = minioProperties.getImageProcessing().getMaxDimension();
                int largest = Math.max(w, h);
                if (largest <= maxDim) {
                    return source;
                }
                double ratio = (double) maxDim / largest;
                int nw = Math.max(1, (int) Math.round(w * ratio));
                int nh = Math.max(1, (int) Math.round(h * ratio));
                return scale(source, nw, nh);
            }
        }
    }

    private BufferedImage scale(BufferedImage source, int width, int height) {
        boolean hasAlpha = source.getColorModel().hasAlpha()
                && source.getTransparency() != Transparency.OPAQUE;
        int type = hasAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        BufferedImage scaled = new BufferedImage(width, height, type);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(source, 0, 0, width, height, null);
        g.dispose();
        return scaled;
    }

    private byte[] encode(BufferedImage image, String format) {
        try {
            if ("png".equals(format)) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                if (ImageIO.write(image, "png", out)) {
                    return out.toByteArray();
                }
                return null;
            }
            // JPEG avec qualité configurable
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            if (!writers.hasNext()) {
                return null;
            }
            ImageWriter writer = writers.next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(minioProperties.getImageProcessing().getJpgQuality());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(out)) {
                writer.setOutput(ios);
                writer.write(null, new IIOImage(image, null, null), param);
            } finally {
                writer.dispose();
            }
            return out.toByteArray();
        } catch (IOException e) {
            log.warn("Échec d'encodage d'image : {}", e.getMessage());
            return null;
        }
    }
}
