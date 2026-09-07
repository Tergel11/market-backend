package market.commerce.social;

import market.commerce.model.enums.SocialProvider;

/**
 * Provider-neutral result of verifying a sign-in token.
 *
 * <p>This is the seam: {@code SocialAuthService} only ever sees this record, so
 * swapping Firebase for direct Google/Apple JWKS verification later means
 * writing one new {@link SocialTokenVerifier} and changing nothing else.
 *
 * @param subject      stable user id (the Firebase uid)
 * @param provider     which method was used, null if unsupported
 * @param email        may be null for phone sign-in, or an Apple relay address
 * @param phoneNumber  E.164, set for phone sign-in
 *
 * @author Tergel
 */
public record SocialUserInfo(
        String subject,
        SocialProvider provider,
        String email,
        boolean emailVerified,
        String phoneNumber,
        String displayName,
        String pictureUrl) {
}
