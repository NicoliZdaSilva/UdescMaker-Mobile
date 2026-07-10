package com.udescmaker.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final UdescMakerProperties properties;

    public WebConfig(UdescMakerProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        var cors = properties.getCors();
        if (cors.isAllowCredentials() && cors.getAllowedOrigins().contains("*")) {
            throw new IllegalStateException("CORS não permite origem '*' quando credenciais estão habilitadas");
        }
        registry.addMapping("/api/**")
                .allowedOrigins(cors.getAllowedOrigins().toArray(String[]::new))
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("Content-Type", "Accept", "Authorization")
                .allowCredentials(cors.isAllowCredentials())
                .maxAge(3600);
    }
}
