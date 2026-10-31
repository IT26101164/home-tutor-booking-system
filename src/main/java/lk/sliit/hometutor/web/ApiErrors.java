package lk.sliit.hometutor.web;

import java.util.Map;
import lk.sliit.hometutor.service.AppException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiErrors {
    @ExceptionHandler(AppException.class)
    ResponseEntity<?> business(AppException e) {
        return ResponseEntity.status(e.status()).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    ResponseEntity<?> invalid(Exception e) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", "Invalid input. Check dates, numbers and required fields."));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<?> unexpected(Exception e) {
        org.slf4j.LoggerFactory.getLogger(ApiErrors.class).error("Request failed", e);
        return ResponseEntity.internalServerError()
                .body(
                        Map.of(
                                "message",
                                "The operation could not be completed. Please check the server log and retry."));
    }
}
