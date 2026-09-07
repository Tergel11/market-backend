package market.commerce.exceptionhandler;

import com.mongodb.MongoException;
import jakarta.servlet.ServletException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import market.commerce.dto.ErrorResponse;
import market.commerce.exception.ErrorException;
import market.commerce.exception.MessageException;
import market.commerce.exception.NotFoundException;
import market.commerce.exception.OutOfStockException;
import market.commerce.exception.PaymentException;
import market.commerce.exception.PermissionException;
import market.commerce.util.LocalizationUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Turns every exception into the same {@link ErrorResponse} shape so the
 * storefront and admin clients only have to parse one error format.
 *
 * @author Tergel
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final LocalizationUtil localizationUtil;

    @ExceptionHandler({AccessDeniedException.class, PermissionException.class})
    protected ResponseEntity<ErrorResponse> handleAccessDenied(Exception ex, WebRequest request) {
        log.error("AccessDeniedException : {}", ex.getMessage());
        return build(message(ex, "error.permission"), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(NotFoundException.class)
    protected ResponseEntity<ErrorResponse> handleNotFound(Exception ex, WebRequest request) {
        log.error("NotFoundException : {}", ex.getMessage());
        return build(message(ex, "data.not-found"), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(OutOfStockException.class)
    protected ResponseEntity<ErrorResponse> handleOutOfStock(OutOfStockException ex, WebRequest request) {
        log.error("OutOfStockException : variantId={}, available={}", ex.getVariantId(), ex.getAvailable());
        return build(message(ex, "error.out-of-stock"), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(PaymentException.class)
    protected ResponseEntity<ErrorResponse> handlePayment(PaymentException ex, WebRequest request) {
        log.error("PaymentException : code={}, message={}", ex.getProviderCode(), ex.getMessage());
        return build(message(ex, "error.payment"), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler({ErrorException.class, MessageException.class})
    protected ResponseEntity<ErrorResponse> handleBusinessError(Exception ex, WebRequest request) {
        log.error("BusinessException : {}", ex.getMessage());
        return build(message(ex, "error.bad-request"), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));

        log.error("ValidationException : {}", fieldErrors);

        ErrorResponse response = base(
                localizationUtil.invalidRequest(), HttpStatus.BAD_REQUEST, request);
        response.setFieldErrors(fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ErrorResponse> handleUnreadableBody(Exception ex, WebRequest request) {
        log.error("HttpMessageNotReadableException : {}", ex.getMessage());
        return build(localizationUtil.invalidRequest(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(ServletException.class)
    protected ResponseEntity<ErrorResponse> handleServlet(Exception ex, WebRequest request) {
        log.error("ServletException : {}", ex.getMessage());
        return build(ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(MongoException.class)
    protected ResponseEntity<ErrorResponse> handleMongo(Exception ex, WebRequest request) {
        log.error("MongoException : ", ex);
        return build(localizationUtil.buildMessage("error.database"),
                HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, WebRequest request) {
        log.error("Unhandled exception : ", ex);
        return build(localizationUtil.errorServer(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    private String message(Exception ex, String fallbackCode) {
        return ObjectUtils.isEmpty(ex.getMessage())
                ? localizationUtil.buildMessage(fallbackCode)
                : ex.getMessage();
    }

    private ResponseEntity<ErrorResponse> build(String message, HttpStatus status, WebRequest request) {
        return ResponseEntity.status(status).body(base(message, status, request));
    }

    private ErrorResponse base(String message, HttpStatus status, WebRequest request) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
    }
}
