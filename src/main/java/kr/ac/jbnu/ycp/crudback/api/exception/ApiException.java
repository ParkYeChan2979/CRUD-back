package kr.ac.jbnu.ycp.crudback.api.exception;

import org.springframework.http.HttpStatus;

/**
 * 원하는 HTTP 상태코드와 메시지를 담아 던지는 예외
 * GlobalExceptionHandler 가 받아서 표준 응답 포맷으로 변환한다.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }
}
