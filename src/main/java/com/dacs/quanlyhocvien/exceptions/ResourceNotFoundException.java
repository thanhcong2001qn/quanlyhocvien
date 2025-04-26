package com.dacs.quanlyhocvien.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception khi không tìm thấy tài nguyên yêu cầu
 * @author thanhcong2001qncode
 * @since 2025-04-23
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s không được tìm thấy với %s: '%s'", resourceName, fieldName, fieldValue));
    }
}