package io.hhplus.tdd;

import io.hhplus.tdd.exception.IllegalPointException;
import io.hhplus.tdd.exception.InsufficientPointException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
class ApiControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = IllegalPointException.class)
    public ResponseEntity<ErrorResponse> handleIllegalPointException(IllegalPointException exception) {
        return buildErrorResponseEntity(exception, HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(value = InsufficientPointException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientPointException(InsufficientPointException exception) {
        return buildErrorResponseEntity(exception, HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity.status(500).body(new ErrorResponse("500", "에러가 발생 했습니다."));
    }

    private static ResponseEntity<ErrorResponse> buildErrorResponseEntity(Exception exception, int status) {
        return ResponseEntity.status(status).body(
                ErrorResponse.of()
                        .code(String.valueOf(status))
                        .message(exception.getMessage())
                        .build()
        );
    }
}
