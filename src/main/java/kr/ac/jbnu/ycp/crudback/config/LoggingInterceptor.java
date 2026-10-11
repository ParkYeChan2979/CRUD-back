package kr.ac.jbnu.ycp.crudback.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 미들웨어 1: 요청/응답 로그 출력
 * 요청 시작 시 메소드·URI 를, 완료 시 상태코드와 처리 시간(ms)을 출력한다.
 */
@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME, System.currentTimeMillis());

        String query = request.getQueryString();
        log.info("[REQUEST]  {} {}{}", request.getMethod(), request.getRequestURI(),
                query != null ? "?" + query : "");
        return true; // false 로 하면 컨트롤러로 안 넘어감
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        long elapsed = System.currentTimeMillis() - (long) request.getAttribute(START_TIME);
        log.info("[RESPONSE] {} {} -> status={} ({}ms)", request.getMethod(), request.getRequestURI(),
                response.getStatus(), elapsed);
    }
}
