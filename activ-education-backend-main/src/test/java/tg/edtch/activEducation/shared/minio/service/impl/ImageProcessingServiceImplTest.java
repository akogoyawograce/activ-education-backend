package tg.edtch.activEducation.shared.minio.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import tg.edtch.activEducation.shared.minio.config.MinioProperties;
import tg.edtch.activEducation.shared.minio.service.ImageProcessingService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ImageProcessingServiceImplTest {

    private ImageProcessingServiceImpl service;

    @BeforeEach
    void setUp() {
        MinioProperties props = new MinioProperties();
        service = new ImageProcessingServiceImpl(props);
    }

    private MockMultipartFile image(int width, int height, String type, boolean alpha) {
        BufferedImage img = new BufferedImage(width, height,
                alpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(img, type, out);
            return new MockMultipartFile("file", "test." + type, "image/" + type,
                    out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void banner_downsizesAndCropsTo1280x400() throws Exception {
        MockMultipartFile file = image(3000, 2000, "jpg", false);
        ImageProcessResult result = service.process(file, ImageProcessingService.ImagePurpose.BANNER);

        assertNotNull(result);
        assertEquals("image/jpeg", result.contentType());
        BufferedImage out = ImageIO.read(new ByteArrayInputStream(result.bytes()));
        assertEquals(1280, out.getWidth());
        assertEquals(400, out.getHeight());
    }

    @Test
    void banner_smallerThanTarget_keepsNativeSize() throws Exception {
        MockMultipartFile file = image(800, 300, "jpg", false);
        ImageProcessResult result = service.process(file, ImageProcessingService.ImagePurpose.BANNER);

        assertNotNull(result);
        BufferedImage out = ImageIO.read(new ByteArrayInputStream(result.bytes()));
        assertEquals(800, out.getWidth());
        assertEquals(300, out.getHeight());
    }

    @Test
    void logo_downsizesTo512MaxPreservingRatio() throws Exception {
        MockMultipartFile file = image(2048, 1024, "jpg", false);
        ImageProcessResult result = service.process(file, ImageProcessingService.ImagePurpose.LOGO);

        assertNotNull(result);
        BufferedImage out = ImageIO.read(new ByteArrayInputStream(result.bytes()));
        assertTrue(out.getWidth() <= 512);
        assertTrue(out.getHeight() <= 512);
        assertEquals(512, out.getWidth());
        assertEquals(256, out.getHeight());
    }

    @Test
    void logo_withAlpha_staysPng() throws Exception {
        MockMultipartFile file = image(200, 200, "png", true);
        ImageProcessResult result = service.process(file, ImageProcessingService.ImagePurpose.LOGO);

        assertNotNull(result);
        assertEquals("image/png", result.contentType());
    }

    @Test
    void generic_compressesLargeImage() throws Exception {
        MockMultipartFile file = image(4000, 3000, "jpg", false);
        ImageProcessResult result = service.process(file, ImageProcessingService.ImagePurpose.GENERIC);

        assertNotNull(result);
        BufferedImage out = ImageIO.read(new ByteArrayInputStream(result.bytes()));
        assertTrue(out.getWidth() <= 2560);
    }

    @Test
    void gif_isLeftUntouched() throws IOException {
        MockMultipartFile file = image(100, 100, "png", false);
        // GIF non supporté par ImageIO pour l'écriture en test → on simule le content-type
        MockMultipartFile gif = new MockMultipartFile("file", "anim.gif", "image/gif", file.getBytes());
        ImageProcessResult result = service.process(gif, ImageProcessingService.ImagePurpose.GENERIC);
        assertNull(result);
    }

    @Test
    void unsupportedFormat_returnsNull() {
        MockMultipartFile webp = new MockMultipartFile("file", "img.webp", "image/webp",
                new byte[] { 1, 2, 3 });
        ImageProcessResult result = service.process(webp, ImageProcessingService.ImagePurpose.GENERIC);
        assertNull(result);
    }

    @Test
    void disabled_returnsNull() {
        MinioProperties props = new MinioProperties();
        props.getImageProcessing().setEnabled(false);
        ImageProcessingServiceImpl disabled = new ImageProcessingServiceImpl(props);
        MockMultipartFile file = image(3000, 2000, "jpg", false);
        assertNull(disabled.process(file, ImageProcessingService.ImagePurpose.BANNER));
    }
}
