package com.arsw.ids_ia.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.arsw.ids_ia.entity.Role;
import com.arsw.ids_ia.entity.User;
import com.arsw.ids_ia.repository.RoleRepository;
import com.arsw.ids_ia.repository.UserRepository;
import com.arsw.ids_ia.security.jwt.JwtTokenProvider;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    public String authenticateUser(String usernameOrEmail, String password) {
        log.debug("Attempting authentication for {}", usernameOrEmail);
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(usernameOrEmail, password)
            );
            log.info("Authentication SUCCESS for {}", usernameOrEmail);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return tokenProvider.generateToken(authentication);
        } catch (Exception ex) {
            log.warn("Authentication FAILED for {}: {}", usernameOrEmail, ex.getClass().getSimpleName());
            throw ex;
        }
    }

    public String generateRefreshToken(String username) {
        return tokenProvider.generateRefreshToken(username);
    }

    public String refreshToken(String refreshToken) {
        if (tokenProvider.validateToken(refreshToken)) {
            String username = tokenProvider.getUsernameFromToken(refreshToken);
            return tokenProvider.generateToken(username);
        }
        throw new RuntimeException("Invalid refresh token");
    }

    public User registerUser(String username, String email, String password,
            String firstName, String lastName, Set<String> roleNames) {

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username is already taken!");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email is already in use!");
        }

        User user = new User(username, email, passwordEncoder.encode(password), firstName, lastName);

        Set<Role> roles = new HashSet<>();

        if (roleNames == null || roleNames.isEmpty()) {
            Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("User Role not set."));
            roles.add(userRole);
        } else {
            roleNames.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Admin Role not set."));
                        roles.add(adminRole);
                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(Role.RoleName.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Moderator Role not set."));
                        roles.add(modRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("User Role not set."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
