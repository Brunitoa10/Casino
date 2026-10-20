package com.aurum.casino.shared.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.aurum.casino.auth.AuthInterceptor;
import com.aurum.casino.auth.CurrentPlayerArgumentResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AurumProperties properties;
    private final AuthInterceptor authInterceptor;
    private final CurrentPlayerArgumentResolver currentPlayerResolver;

    public WebConfig(AurumProperties properties,
                     AuthInterceptor authInterceptor,
                     CurrentPlayerArgumentResolver currentPlayerResolver) {
        this.properties = properties;
        this.authInterceptor = authInterceptor;
        this.currentPlayerResolver = currentPlayerResolver;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(properties.cors().allowedOrigins().toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/register", "/api/health");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentPlayerResolver);
    }
}
