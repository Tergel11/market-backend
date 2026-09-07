package market.commerce.social;

/**
 * Verifies a sign-in token issued by an identity provider.
 *
 * @author Tergel
 */
public interface SocialTokenVerifier {

    /**
     * @param idToken the provider's ID token, straight from the client
     * @return the verified identity
     * @throws market.commerce.exception.MessageException if the token is
     *         invalid, expired, or signed by someone else
     */
    SocialUserInfo verify(String idToken);
}
