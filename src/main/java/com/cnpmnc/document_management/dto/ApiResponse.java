package com.cnpmnc.document_management.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T>{
    private final int code;
    private final String message;
    private final T data;

    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 1. Get and update 200 OK (Có data)
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(HttpStatus.OK.value(), "Success", data);
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(HttpStatus.OK.value(), message, null);
    }

    public static <T> ApiResponse<T> success(String message,T data) {
        return new ApiResponse<>(HttpStatus.OK.value(), message, data);
    }

    // 2. Created 201 (Dùng cho POST)
    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(HttpStatus.CREATED.value(), "Created successfully", data);
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(HttpStatus.CREATED.value(), message, data);
    }

    // 3. Delete 200 OK (Không có data trả về)
    public static <T> ApiResponse<T> deleteSuccess() {
        return new ApiResponse<>(HttpStatus.OK.value(), "Deleted Successfully", null);
    }

    public static <T> ApiResponse<T> deleteSuccess(String message) {
        return new ApiResponse<>(HttpStatus.OK.value(), message, null);
    }

    // 4. Custom Error (Dùng cho Exception Handler)
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
