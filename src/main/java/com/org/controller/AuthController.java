package com.org.controller;


import com.org.jwt.JwtUtil;
import com.org.model.User;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import com.org.dto.LoginRequest;
import com.org.dto.SignupRequest;
import com.org.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody SignupRequest signupRequest, HttpServletResponse response) {
        try {
            // ✅ Create the user
            userService.registerUser(signupRequest);

            // ✅ Load the user again
            User user = userService.getUserByEmail(signupRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found after signup"));

            // ✅ Generate tokens
            String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole());
            String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

            // ✅ Create refresh token cookie
            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(true) // ✅ true in production
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60)
                    .sameSite("Lax")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            // ✅ Return access token & user info
            return ResponseEntity.ok(Map.of(
                    "accessToken", accessToken,
                    "role", user.getRole(),
                    "email", user.getEmail(),
                    "username", user.getUsername(),
                    "id", user.getId().toString(),
                    "statusCode", 200
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error registering user: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            String refreshToken = jwtUtil.generateRefreshToken(loginRequest.getEmail());

            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(true) // set to true in production with HTTPS
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60)
                    .sameSite("Lax")
                    .build();


            return ResponseEntity.ok()
                    .header("Set-Cookie", cookie.toString())
                    .body(userService.loginUser(loginRequest));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Login failed: " + e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null) {
            return ResponseEntity.status(401).body("Refresh token missing");
        }

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true) // set to true in production with HTTPS
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body(userService.refreshAccessToken(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // 🍪 Create an expired cookie to clear refresh token
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true) // should be true in production
                .path("/") // must match original path
                .maxAge(0) // deletes the cookie
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header("Set-Cookie", deleteCookie.toString())
                .body("Logged out successfully");
    }


}
