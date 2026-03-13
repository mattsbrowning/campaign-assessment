package com.example.campaignassessment.config;

import com.example.campaignassessment.interceptor.ApiKeyInterceptor;
import com.example.campaignassessment.interceptor.RateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ApiKeyInterceptor apiKeyInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;

    public WebMvcConfig(ApiKeyInterceptor apiKeyInterceptor,
                        RateLimitInterceptor rateLimitInterceptor) {
        this.apiKeyInterceptor = apiKeyInterceptor;
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    // Swagger UI and OpenAPI spec endpoints are public — no auth or rate limiting.
    private static final String[] PUBLIC_PATHS = {
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/v3/api-docs.yaml"
    };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Auth is checked first; rate limiting only applies to authenticated requests.
        registry.addInterceptor(apiKeyInterceptor).excludePathPatterns(PUBLIC_PATHS);
        registry.addInterceptor(rateLimitInterceptor).excludePathPatterns(PUBLIC_PATHS);
    }
}
