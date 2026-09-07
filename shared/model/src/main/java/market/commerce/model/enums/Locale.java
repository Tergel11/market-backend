package market.commerce.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * @author Tergel
 */
@Getter
@RequiredArgsConstructor
public enum Locale {

    MN("mn"),
    EN("en");

    @JsonValue
    private final String value;

    public static Locale findByValue(String value) {
        return Arrays.stream(values())
                .filter(locale -> locale.value.equalsIgnoreCase(value))
                .findFirst()
                .orElse(null);
    }
}
