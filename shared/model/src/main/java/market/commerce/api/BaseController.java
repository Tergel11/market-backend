package market.commerce.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import market.commerce.exception.MessageException;
import market.commerce.exception.NotFoundException;
import market.commerce.util.LocalizationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * @author Tergel
 */
@Slf4j
public class BaseController {

    @Autowired
    protected LocalizationUtil localizationUtil;

    @Autowired
    protected ObjectMapper objectMapper;

    public ResponseEntity<?> badRequest() {
        throw new MessageException(localizationUtil.badRequest());
    }

    public ResponseEntity<?> badRequestMessage(String message) {
        throw new MessageException(message);
    }

    public ResponseEntity<?> badRequestLocale(String locale) {
        throw new MessageException(localizationUtil.buildMessage(locale));
    }

    public ResponseEntity<?> errorInvalidRequest() {
        throw new MessageException(localizationUtil.invalidRequest());
    }

    public ResponseEntity<?> errorNotFound() {
        throw new NotFoundException(localizationUtil.notFound());
    }

    public ResponseEntity<?> errorDataExists() {
        throw new MessageException(localizationUtil.dataExists());
    }

    public ResponseEntity<?> errorPermission() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(localizationUtil.buildMessage("error.permission"));
    }

    public ResponseEntity<?> serverError() {
        return serverError(localizationUtil.errorServer());
    }

    public ResponseEntity<?> serverError(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
    }
}
