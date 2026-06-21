package tg.edtch.activEducation.shared.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration globale de Spring MVC.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.stream()
                .filter(c -> c instanceof MappingJackson2HttpMessageConverter)
                .map(c -> (MappingJackson2HttpMessageConverter) c)
                .forEach(converter -> {
                    List<MediaType> mediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());
                    // Autorise Spring à utiliser Jackson pour désérialiser du contenu envoyé par
                    // défaut
                    // sous "application/octet-stream" dans les requêtes multipart/form-data.
                    if (!mediaTypes.contains(MediaType.APPLICATION_OCTET_STREAM)) {
                        mediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
                    }
                    converter.setSupportedMediaTypes(mediaTypes);
                });
    }
}
