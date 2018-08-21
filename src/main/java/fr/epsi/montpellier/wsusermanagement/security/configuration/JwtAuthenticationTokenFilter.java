package fr.epsi.montpellier.wsusermanagement.security.configuration;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import fr.epsi.montpellier.wsusermanagement.security.service.JwtTokenService;


@Component
public class JwtAuthenticationTokenFilter extends GenericFilterBean {

    private static final String BEARER = "Bearer";

    @Autowired
    private JwtTokenService jwtTokenService;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {

        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        // Assume we have only one Authorization header value
        final Optional<String> token = Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION));

        if(token.isPresent() && token.get().startsWith(BEARER)) {

            String bearerToken = token.get().substring(BEARER.length()+1);
            String message = null;

            try {
                final Jws<Claims> claims = jwtTokenService.validateToken(bearerToken);

                Object objRole = claims.getBody().get("roles", String.class);
                List<GrantedAuthority> authorityList = AuthorityUtils.commaSeparatedStringToAuthorityList(objRole.toString());
                Authentication authentication = new UsernamePasswordAuthenticationToken(claims.getBody().getSubject(),
                        "PROTECTED", authorityList);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            catch (SignatureException ex) {
                message = "Invalid JWT signature";
            }
            catch (MalformedJwtException ex) {
                message = "Invalid JWT token";
            }
            catch (ExpiredJwtException ex) {
                message = "Expired JWT token";
            }
            catch (UnsupportedJwtException ex) {
                message = "Unsupported JWT token";
            }
            catch (IllegalArgumentException ex) {
                message = "JWT claims string is empty.";
            }
            catch (JwtException exception) {
                message = "JwtException raised.";
            }
            if (message != null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
                return;
            }

        }

        chain.doFilter(servletRequest, servletResponse);
        SecurityContextHolder.getContext().setAuthentication(null); // Clean authentication after process

    }

}