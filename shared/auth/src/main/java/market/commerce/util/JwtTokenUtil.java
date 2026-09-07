package market.commerce.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import market.commerce.model.enums.ApplicationRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Tergel
 */
@Slf4j
@Component
public class JwtTokenUtil {

    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_MERCHANT_ID = "merchantId";
    public static final String CLAIM_TYPE = "type";

    @Value("${jwt.token.secret}")
    private String secret;

    @Value("${jwt.token.expiration}")
    private long expirationSeconds;

    private Key signingKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String generateToken(String subject, List<ApplicationRole> roles, Map<String, Object> extraClaims) {
        long now = System.currentTimeMillis();
        var builder = Jwts.builder()
                .setSubject(subject)
                .claim(CLAIM_ROLES, roles == null ? List.of() : roles.stream().map(Enum::name).toList())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationSeconds * 1000));

        if (extraClaims != null)
            extraClaims.forEach(builder::claim);

        return builder.signWith(signingKey(), SignatureAlgorithm.HS512).compact();
    }

    public String getSubject(String token) {
        return getClaim(token, Claims::getSubject);
    }

    public Date getExpiration(String token) {
        return getClaim(token, Claims::getExpiration);
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        return getClaim(token, claims -> (List<String>) claims.get(CLAIM_ROLES));
    }

    public String getMerchantId(String token) {
        return getClaim(token, claims -> (String) claims.get(CLAIM_MERCHANT_ID));
    }

    public <T> T getClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(getAllClaims(token));
    }

    public boolean isValid(String token) {
        try {
            return !getExpiration(token).before(new Date());
        } catch (Exception ex) {
            log.warn("Invalid jwt token : {}", ex.getMessage());
            return false;
        }
    }

    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }
}
