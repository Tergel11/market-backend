package market.commerce;

import org.springframework.util.ObjectUtils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * @author Tergel
 */
public class LocalStringUtil {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    private LocalStringUtil() {
    }

    public static boolean isEmpty(String value) {
        return ObjectUtils.isEmpty(value) || value.trim().isEmpty();
    }

    /**
     * Product / category url slug: "Эрэгтэй цамц 2024" -> "eregtei-tsamts-2024"
     */
    public static String toSlug(String input) {
        if (isEmpty(input))
            return null;

        String noWhitespace = WHITESPACE.matcher(input.trim()).replaceAll("-");
        String normalized = Normalizer.normalize(noWhitespace, Normalizer.Form.NFD);
        return NON_LATIN.matcher(normalized).replaceAll("").toLowerCase(Locale.ENGLISH);
    }

    public static String maskPhone(String phone) {
        if (isEmpty(phone) || phone.length() < 4)
            return phone;
        return "****" + phone.substring(phone.length() - 4);
    }

    public static String maskEmail(String email) {
        if (isEmpty(email) || !email.contains("@"))
            return email;
        int at = email.indexOf('@');
        String name = email.substring(0, at);
        String visible = name.length() <= 2 ? name : name.substring(0, 2);
        return visible + "***" + email.substring(at);
    }
}
