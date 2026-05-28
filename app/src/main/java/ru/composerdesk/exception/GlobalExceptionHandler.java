package ru.composerdesk.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public Object handleApiException(ApiException ex, HttpServletRequest request) {
        log.error("API exception: {}", ex.getMessage(), ex);
        if (isAjax(request)) {
            return buildJsonResponse(ex.getStatus(), ex.getMessage());
        }
        return buildErrorPage(ex.getStatus().value(), ex.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public Object handleNoHandlerFound(NoHandlerFoundException ex, HttpServletRequest request) {
        log.warn("Page not found: {}", ex.getRequestURL());
        if (isAjax(request)) {
            return buildJsonResponse(HttpStatus.NOT_FOUND, "Страница не найдена");
        }
        return buildErrorPage(404, "Страница не найдена");
    }

    @ExceptionHandler(Exception.class)
    public Object handleUnknownException(Exception ex, HttpServletRequest request) {

        if (ex instanceof NoHandlerFoundException) {
            return buildErrorPage(404, "Страница не найдена");
        }

        log.error("Unexpected error: {}", ex.getMessage(), ex);

        if (isAjax(request)) {
            return buildJsonResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Внутренняя ошибка сервера"
            );
        }

        return buildErrorPage(500, "Внутренняя ошибка сервера");
    }

    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleAsyncRequestNotUsable(AsyncRequestNotUsableException ex) {
    }

    private boolean isAjax(HttpServletRequest request) {
        String xRequestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        return "XMLHttpRequest".equals(xRequestedWith) ||
                (accept != null && accept.contains("application/json"));
    }

    private ResponseEntity<Map<String, Object>> buildJsonResponse(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }

    private ModelAndView buildErrorPage(int status, String message) {
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("status", status);
        mav.addObject("message", message != null ? message : "Что-то пошло не так");
        mav.setStatus(HttpStatus.valueOf(status));
        return mav;
    }
}