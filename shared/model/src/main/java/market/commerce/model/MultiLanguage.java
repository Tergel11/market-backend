package market.commerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import market.commerce.model.enums.Locale;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

/**
 * Localized text embedded in catalog documents ({name: {mn: "...", en: "..."}}).
 *
 * @author Tergel
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultiLanguage implements Serializable {

    private Map<Locale, String> values;

    public static MultiLanguage of(String mn, String en) {
        Map<Locale, String> values = new EnumMap<>(Locale.class);
        if (mn != null) values.put(Locale.MN, mn);
        if (en != null) values.put(Locale.EN, en);
        return MultiLanguage.builder().values(values).build();
    }

    public String get(Locale locale) {
        if (values == null || values.isEmpty())
            return null;
        return values.getOrDefault(locale, values.get(Locale.MN));
    }
}
