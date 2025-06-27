package com.org.service;


import com.org.dto.LoginRequest;
import com.org.dto.SignupRequest;
import com.org.jwt.JwtUtil;
import com.org.model.User;
import com.org.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Lazy
    @Autowired
    private AuthenticationManager authenticationManager;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getEmail()) // or user.getUsername() if you prefer
                .password(user.getPassword())
                .roles(user.getRole()) // example: ADMIN, USER
                .build();
    }


    public void registerUser(SignupRequest request) {
        System.out.println(request.toString());
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Phone number already exists");
        }

        if (userRepository.existsByCnic(request.getCnic())) {
            throw new RuntimeException("CNIC already exists");
        }

        if (request.getDob() != null && request.getDob().isAfter(LocalDate.now().minusYears(18))) {
            throw new RuntimeException("User must be 18 years or older");
        }

        // 🖼️ Default profile image URL
        String defaultImageUrl = "https://www.shutterstock.com/image-vector/blank-avatar-photo-place-holder-600nw-1095249842.jpg";

        com.org.model.User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole() != null ? request.getRole() : "USER")
                .cnic(request.getCnic())
                .dob(request.getDob())
                .createdAt(request.getCreatedAt())
                .address(request.getAddress())
                .profilePictureUrl(defaultImageUrl)
                .build();

        userRepository.save(user);
    }


    public ResponseEntity<?> loginUser(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email"));

        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole());



        return ResponseEntity.ok()
                .body(Map.of(
                        "accessToken", accessToken,
                        "role", user.getRole(),
                        "email", user.getEmail(),
                        "username", user.getUsername(),
                        "id", user.getId().toString()

                ));
    }

    public ResponseEntity<?> refreshAccessToken(String refreshToken) {
        try {
            String email = jwtUtil.extractEmail(refreshToken);

            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(401).body("Invalid refresh token");
            }

            if (!jwtUtil.isTokenValid(refreshToken)) {
                return ResponseEntity.status(401).body("Refresh token expired");
            }

            User user = userOpt.get();
            String newAccessToken = jwtUtil.generateToken(user.getEmail(), user.getRole());

            return ResponseEntity.ok(Map.of(
                    "accessToken", newAccessToken,
                    "role", user.getRole(),
                    "email", user.getEmail(),
                    "username", user.getUsername(),
                    "id", user.getId().toString()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Token invalid or expired");
        }
    }





    public Optional<com.org.model.User> getUserById(ObjectId id) {
        return userRepository.findById(id);
    }

    public Optional<com.org.model.User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}

