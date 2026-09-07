package market.commerce;

import java.security.SecureRandom;

/**
 * @author Tergel
 */
public class PasswordUtil {

    private static final String DIGITS = "0123456789";
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String generateOtp(int length) {
        return generate(length, DIGITS);
    }

    public static String generateCode(int length) {
        return generate(length, ALPHANUMERIC);
    }

    private static String generate(int length, String alphabet) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            builder.append(alphabet.charAt(RANDOM.nextInt(alphabet.length())));
        return builder.toString();
    }
}
