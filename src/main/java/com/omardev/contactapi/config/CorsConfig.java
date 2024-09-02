package com.omardev.contactapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    private static final List<String> ALLOWED_HEADERS = List.of(
            "Origin",
            "Content-Type",
            "Accept",
            "Authorization",
            "X-Requested-With",
            "Access-Control-Allow-Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
    );

    private static final List<String> EXPOSED_HEADERS = ALLOWED_HEADERS;

    private static final List<String> ALLOWED_METHODS = List.of(
            "GET",
            "POST",
            "PUT",
            "PATCH",
            "DELETE",
            "OPTIONS"
    );

    @Bean
    public CorsFilter corsFilter() {
        var corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedOrigins(List.of("http://localhost:5173"));
        corsConfiguration.setAllowedHeaders(ALLOWED_HEADERS);
        corsConfiguration.setExposedHeaders(EXPOSED_HEADERS);
        corsConfiguration.setAllowedMethods(ALLOWED_METHODS);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsFilter(source);
    }
}
