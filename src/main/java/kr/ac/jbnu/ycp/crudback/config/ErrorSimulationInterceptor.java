package kr.ac.jbnu.ycp.crudback.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.ac.jbnu.ycp.crudback.api.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 미들웨어 2: 5xx 응답 테스트용 오류 시뮬레이션
 * 요청 헤더 X-Simulate-Error 값에 따라 서버 오류를 강제로 발생시킨다.
 *  - 500: 예상하지 못한 서버 오류 (RuntimeException)
 *  - 503: 서비스 점검 중
 */
@Component
public class ErrorSimulationInterceptor implements HandlerInterceptor {

    private static final String HEADER = "X-Simulate-Error";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String simulate = request.getHeader(HEADER);
        if ("500".equals(simulate)) {
            throw new IllegalStateException("X-Simulate-Error 헤더로 발생시킨 서버 오류");
        }
        if ("503".equals(simulate)) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "현재 서비스 점검 중입니다. 잠시 후 다시 시도해주세요.");
        }
        return true;
    }
}
