package com.arsw.ids_ia.security.oauth2;

import java.util.Base64;

import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.SerializationUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HttpCookieOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Cookie cookie = getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        if (cookie == null) {
            return null;
        }
        byte[] bytes = Base64.getUrlDecoder().decode(cookie.getValue());
        return (OAuth2AuthorizationRequest) SerializationUtils.deserialize(bytes);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            removeAuthorizationRequestCookies(request, response);
            return;
        }
        byte[] bytes = SerializationUtils.serialize(authorizationRequest);
        String value = Base64.getUrlEncoder().encodeToString(bytes);
        addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, value, COOKIE_EXPIRE_SECONDS);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        OAuth2AuthorizationRequest req = loadAuthorizationRequest(request);
        removeAuthorizationRequestCookies(request, response);
        return req;
    }

    public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
    }

    private static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    private static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie cookie = getCookie(request, name);
        if (cookie != null) {
            cookie.setValue("");
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
    }

    private static Cookie getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(name)) {
                return cookie;
            }
        }
        return null;
    }
}
