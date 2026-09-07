package market.commerce.service.system;

import lombok.RequiredArgsConstructor;
import market.commerce.repository.SystemConfigRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @author Tergel
 */
@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;

    public String get(String code, String defaultValue) {
        return systemConfigRepository.findByCode(code)
                .map(config -> config.getValue())
                .orElse(defaultValue);
    }

    public BigDecimal getDecimal(String code, BigDecimal defaultValue) {
        String value = get(code, null);
        return value == null ? defaultValue : new BigDecimal(value);
    }

    public int getInt(String code, int defaultValue) {
        String value = get(code, null);
        return value == null ? defaultValue : Integer.parseInt(value);
    }
}
