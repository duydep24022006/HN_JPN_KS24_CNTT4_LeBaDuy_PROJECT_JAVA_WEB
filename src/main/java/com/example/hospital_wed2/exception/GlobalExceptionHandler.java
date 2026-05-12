package com.example.hospital_wed2.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(ResourceNotFoundException e, Model model) {
        log.warn("Resource not found: {}", e.getMessage());
        model.addAttribute("errorCode", "404");
        model.addAttribute("errorMessage", e.getMessage());
        return "error/generic";
    }

    @ExceptionHandler(UnauthorizedException.class)
    public String handleUnauthorized(UnauthorizedException e, Model model) {
        log.warn("Unauthorized access: {}", e.getMessage());
        model.addAttribute("errorCode", "403");
        model.addAttribute("errorMessage", e.getMessage());
        return "error/generic";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleBadRequest(IllegalArgumentException e, Model model) {
        model.addAttribute("errorCode", "400");
        model.addAttribute("errorMessage", e.getMessage());
        return "error/generic";
    }

    @ExceptionHandler(IllegalStateException.class)
    public String handleConflict(IllegalStateException e, Model model) {
        model.addAttribute("errorCode", "409");
        model.addAttribute("errorMessage", e.getMessage());
        return "error/generic";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String handleForbidden(Model model) {
        model.addAttribute("errorCode", "403");
        model.addAttribute("errorMessage", "Bạn không có quyền truy cập trang này.");
        return "error/generic";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNotFound(Model model) {
        model.addAttribute("errorCode", "404");
        model.addAttribute("errorMessage", "Trang không tồn tại.");
        return "error/generic";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception e, Model model) {
        log.error("Unhandled exception occurred", e);
        model.addAttribute("errorCode", "500");
        model.addAttribute("errorMessage", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.");
        return "error/generic";
    }
}