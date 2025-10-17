package com.arsw.ids_ia.security.oauth2;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.arsw.ids_ia.entity.Role;
import com.arsw.ids_ia.entity.User;
import com.arsw.ids_ia.repository.RoleRepository;
import com.arsw.ids_ia.repository.UserRepository;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(oAuth2User);
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
        }
    }

    private OAuth2User processOAuth2User(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        Boolean emailVerified = attributes.get("email_verified") instanceof Boolean
                ? (Boolean) attributes.get("email_verified")
                : Boolean.valueOf(String.valueOf(attributes.get("email_verified")));

        if (email == null || !Boolean.TRUE.equals(emailVerified)) {
            throw new IllegalArgumentException("Email is missing or not verified by Google");
        }

        Optional<User> existingByEmail = userRepository.findByEmail(email);
        if (existingByEmail.isPresent()) {
            // Permitir solo si fue creado por Google previamente (usamos password sentinel)
            User existing = existingByEmail.get();
            if ("{noop}google-oauth2".equals(existing.getPassword())) {
                return new DefaultOAuth2User(
                        Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                        attributes,
                        "email"
                );
            }
            // Rechazar si el usuario existe y no fue creado por Google
            throw new IllegalArgumentException("A user with this email already exists");
        }

        // Crear usuario automáticamente si no existe
        User user = new User();
        user.setEmail(email);
        user.setUsername(email); // username interno = email
        user.setPassword("{noop}google-oauth2"); // placeholder, no se usa para login con Google
        user.setIsActive(true);

        Role defaultRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("Default role ROLE_USER not found"));
        user.getRoles().add(defaultRole);
        userRepository.save(user);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "email"
        );
    }
}
