package com.arsw.ids_ia.security.oauth2;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.arsw.ids_ia.security.jwt.JwtTokenProvider;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationInMs;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String username;
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else if (principal instanceof User) {
            username = ((User) principal).getUsername();
        } else {
            username = authentication.getName();
        }

        String accessToken = tokenProvider.generateToken(username);
        String refreshToken = tokenProvider.generateRefreshToken(username);

        String redirectUrl = frontendUrl + "/oauth2/callback#accessToken="
                + URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
                + "&refreshToken=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8)
                + "&tokenType=Bearer"
                + "&expiresIn=" + jwtExpirationInMs;

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
