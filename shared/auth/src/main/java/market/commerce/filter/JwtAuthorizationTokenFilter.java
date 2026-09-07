package market.commerce.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import market.commerce.dto.AuthUserPrincipal;
import market.commerce.model.enums.ApplicationRole;
import market.commerce.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Reads the JWT from the configured header and puts an
 * {@link AuthUserPrincipal} into the security context.
 *
 * @author Tergel
 */
@Slf4j
public class JwtAuthorizationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Value("${jwt.token.header}")
    private String tokenHeader;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (!ObjectUtils.isEmpty(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                if (jwtTokenUtil.isValid(token)) {
                    List<ApplicationRole> roles = jwtTokenUtil.getRoles(token).stream()
                            .map(ApplicationRole::valueOf)
                            .toList();

                    AuthUserPrincipal principal = AuthUserPrincipal.builder()
                            .id(jwtTokenUtil.getSubject(token))
                            .roles(roles)
                            .merchantId(jwtTokenUtil.getMerchantId(token))
                            .build();

                    var authentication = new UsernamePasswordAuthenticationToken(principal, null, roles);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception ex) {
                log.warn("Could not authorize token : {}", ex.getMessage());
            }
        }

        chain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(tokenHeader);
        if (ObjectUtils.isEmpty(header))
            header = request.getHeader("Authorization");

        if (ObjectUtils.isEmpty(header))
            return null;

        return header.startsWith("Bearer ") ? header.substring(7) : header;
    }
}
