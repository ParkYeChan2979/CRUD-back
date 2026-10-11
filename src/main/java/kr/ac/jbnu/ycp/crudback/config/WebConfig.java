package kr.ac.jbnu.ycp.crudback.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoggingInterceptor loggingInterceptor;
    private final ErrorSimulationInterceptor errorSimulationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 등록 순서대로 실행된다: 로그 → 오류 시뮬레이션
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/api/**");
        registry.addInterceptor(errorSimulationInterceptor)
                .addPathPatterns("/api/**");
    }
}
