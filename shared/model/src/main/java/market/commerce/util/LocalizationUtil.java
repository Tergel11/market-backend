package market.commerce.util;

import lombok.RequiredArgsConstructor;
import market.commerce.model.MultiLanguage;
import market.commerce.model.enums.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tergel
 */
@Component
@RequiredArgsConstructor
public class LocalizationUtil {

    private final MessageSource messageSource;

    public String buildMessage(final String templateName) {
        return buildMessage(templateName, null);
    }

    public String buildMessage(final String templateName, final String[] additionalParam) {
        return messageSource.getMessage(templateName, additionalParam, LocaleContextHolder.getLocale());
    }

    public String dataExists() {
        return buildMessage("data.exists");
    }

    public String notFound() {
        return buildMessage("data.not-found");
    }

    public String invalidRequest() {
        return buildMessage("error.invalid-request");
    }

    public String badRequest() {
        return buildMessage("error.bad-request");
    }

    public String errorServer() {
        return buildMessage("error.app");
    }

    public static String getLocale() {
        String locale = LocaleContextHolder.getLocale().getLanguage();
        return ObjectUtils.isEmpty(locale) ? "mn" : locale;
    }

    public static boolean isMn() {
        return getLocale().equals("mn");
    }

    public static Locale currentLocale() {
        return isMn() ? Locale.MN : Locale.EN;
    }

    /** Flattens a localized field down to the caller's language. */
    public static String translate(MultiLanguage multiLanguage) {
        return multiLanguage == null ? null : multiLanguage.get(currentLocale());
    }

    public static Map<String, String> localeConvertToMap(Map<Locale, String> data) {
        if (ObjectUtils.isEmpty(data))
            return null;

        Map<String, String> response = new HashMap<>();
        for (Locale locale : data.keySet())
            response.put(locale.getValue(), data.get(locale));

        return response;
    }

    public static Map<Locale, String> stringConvertToMap(Map<String, String> data) {
        if (ObjectUtils.isEmpty(data))
            return null;

        Map<Locale, String> response = new HashMap<>();
        for (String locale : data.keySet()) {
            Locale found = Locale.findByValue(locale);
            if (found != null)
                response.put(found, data.get(locale));
        }

        return response;
    }
}
