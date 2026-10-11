package kr.ac.jbnu.ycp.crudback.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * 모든 API 가 공통으로 사용하는 표준 응답 포맷
 * 성공: { "status": "success", "data": {...}, "message": "..." }
 * 실패: { "status": "error", "message": "...", "data": {...} }
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // null 인 필드는 응답에서 제외
@JsonPropertyOrder({"status", "data", "message"}) // 응답 필드 순서 고정
public class ApiResponse<T> {
    private final String status;
    private final T data;
    private final String message;

    private ApiResponse(String status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", data, null);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>("success", data, message);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("error", null, message);
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>("error", data, message);
    }

    // getter
    public String getStatus() { return status; }
    public T getData() { return data; }
    public String getMessage() { return message; }
}
