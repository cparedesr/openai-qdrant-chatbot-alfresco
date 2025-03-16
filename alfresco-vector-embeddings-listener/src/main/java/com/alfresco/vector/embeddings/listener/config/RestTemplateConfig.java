package com.alfresco.vector.embeddings.listener.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración que expone RestTemplate como bean
 * para usarlo en inyecciones (@Autowired, constructor, etc.).
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}