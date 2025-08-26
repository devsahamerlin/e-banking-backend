package com.merlin.digitalbanking.ebankingbackend.web;

import com.merlin.digitalbanking.ebankingbackend.dto.CreateUserDTO;
import com.merlin.digitalbanking.ebankingbackend.dto.LoginRequest;
import com.merlin.digitalbanking.ebankingbackend.dto.UserDTO;
import com.merlin.digitalbanking.ebankingbackend.enums.UserRole;
import com.merlin.digitalbanking.ebankingbackend.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class SecurityRestController {
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final UserService userService;

    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_USER') or hasAuthority('SCOPE_ROLE_ADMIN') or hasAuthority('SCOPE_ROLE_MANAGER')")
    public ResponseEntity<Map<String, Object>> getProfile(Authentication authentication) {
        String username = authentication.getName();

        UserDTO user = userService.findByUsername(username);

        Map<String, Object> profile = new HashMap<>();
        profile.put("username", user.username());
        profile.put("email", user.email());
        profile.put("firstName", user.firstName());
        profile.put("lastName", user.lastName());
        profile.put("role", user.role());
        profile.put("isActive", user.isActive());
        profile.put("lastLogin", user.lastLogin());
        profile.put("authorities", authentication.getAuthorities());

        return ResponseEntity.ok(profile);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
        try {
            log.info("Login attempt for username: {}", loginRequest.username());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.username(),
                            loginRequest.password()
                    )
            );

            UserDTO user = userService.findByUsername(loginRequest.username());

            Instant instant = Instant.now();

            String scope = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(" "));

            JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                    .issuedAt(instant)
                    .expiresAt(instant.plus(10, ChronoUnit.MINUTES))
                    .subject(loginRequest.username())
                    .claim("scope", scope)
                    .claim("userId", user.id())
                    .claim("email", user.email())
                    .claim("role", user.role().name())
                    .build();

            JwtEncoderParameters jwtEncoderParameters = JwtEncoderParameters
                    .from(JwsHeader.with(MacAlgorithm.HS512).build(), jwtClaimsSet);

            String jwt = jwtEncoder.encode(jwtEncoderParameters).getTokenValue();

            Map<String, Object> response = new HashMap<>();
            response.put("access_token", jwt);
            response.put("token_type", "Bearer");
            response.put("expires_in", 24 * 60 * 60); // 24 hours in seconds
            response.put("user", Map.of(
                    "id", user.id(),
                    "username", user.username(),
                    "email", user.email(),
                    "firstName", user.firstName(),
                    "lastName", user.lastName(),
                    "role", user.role()
            ));

            log.info("Login successful for user: {}", loginRequest.username());
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials for username: {}", loginRequest.username());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        } catch (DisabledException e) {
            log.warn("Account disabled for username: {}", loginRequest.username());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Account is disabled"));
        } catch (Exception e) {
            log.error("Login error for username: {}", loginRequest.username(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Authentication failed"));
        }
    }

    @PostMapping("/refresh")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_USER') or hasAuthority('SCOPE_ROLE_ADMIN') or hasAuthority('SCOPE_ROLE_MANAGER')")
    public ResponseEntity<Map<String, Object>> refreshToken(Authentication authentication) {
        String username = authentication.getName();

        UserDTO user = userService.findByUsername(username);

        if (!user.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Account is disabled"));
        }

        Instant instant = Instant.now();
        String scope = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                .issuedAt(instant)
                .expiresAt(instant.plus(10, ChronoUnit.MINUTES))
                .subject(username)
                .claim("scope", scope)
                .claim("userId", user.id())
                .claim("email", user.email())
                .claim("role", user.role().name())
                .build();

        JwtEncoderParameters jwtEncoderParameters = JwtEncoderParameters
                .from(JwsHeader.with(MacAlgorithm.HS512).build(), jwtClaimsSet);

        String jwt = jwtEncoder.encode(jwtEncoderParameters).getTokenValue();

        Map<String, Object> response = new HashMap<>();
        response.put("access_token", jwt);
        response.put("token_type", "Bearer");
        response.put("expires_in", 24 * 60 * 60);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody CreateUserDTO createUserDTO) {
        try {
            CreateUserDTO userRegistration = new CreateUserDTO(
                    createUserDTO.username(),
                    createUserDTO.password(),
                    createUserDTO.email(),
                    createUserDTO.firstName(),
                    createUserDTO.lastName(),
                    UserRole.USER
            );

            UserDTO user = userService.createUser(userRegistration);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("user", Map.of(
                    "id", user.id(),
                    "username", user.username(),
                    "email", user.email(),
                    "firstName", user.firstName(),
                    "lastName", user.lastName(),
                    "role", user.role()
            ));

            log.info("User registered successfully: {}", user.username());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            log.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

//    @GetMapping("/profile")
//    public Authentication authentication(Authentication authentication) {
//        return authentication;
//    }

//    @PostMapping("/login")
//    public Map<String, String> login(String username, String password) {
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(username, password));
//        Instant instant = Instant.now();
//
//        String scope = authentication.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority)
////                .map(authority -> authority.startsWith("ROLE_") ?
////                        authority.substring(5) : authority) // Remove ROLE_ prefix
//                .collect(Collectors.joining(" "));
//
//        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
//                .issuedAt(instant)
//                .expiresAt(instant.plus(10, ChronoUnit.MINUTES))
//                .subject(username)
//                .claim("scope", scope)
//                .build();
//
//        JwtEncoderParameters jwtEncoderParameters = JwtEncoderParameters
//                .from(JwsHeader.with(MacAlgorithm.HS512).build(), jwtClaimsSet);
//
//        String jwt = jwtEncoder.encode(jwtEncoderParameters).getTokenValue();
//        return Map.of("access_token", jwt);
//    }
}
