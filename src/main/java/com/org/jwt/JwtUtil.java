package com.org.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    // ✅ 256-bit secret key
    private final SecretKey key = Keys.hmacShaKeyFor(
            "mysecretkeymysecretkeymysecretkey!".getBytes(StandardCharsets.UTF_8)
    );

    // ✅ Generate Access Token (includes "type": "access")
    public String generateToken(String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("type", "access"); // 👈 Important to add type

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15)) // 15 minutes
                .signWith(key)
                .compact();
    }

    // ✅ Generate Refresh Token
    public String generateRefreshToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7)) // 7 days
                .signWith(key)
                .compact();
    }

    // ✅ Extract email
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    // ✅ Extract role
    public String extractRole(String token) {
        return (String) getClaims(token).get("role");
    }

    // ✅ Extract type (access or refresh)
    public String extractTokenType(String token) {
        return (String) getClaims(token).get("type");
    }

    // ✅ Check if token is valid and not expired
    public boolean isTokenValid(String token) {
        try {
            getClaims(token); // Will throw exception if invalid
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    // ✅ Validate token for email + not expired
    public boolean validateToken(String token, String email) {
        return extractEmail(token).equals(email) && !isTokenExpired(token);
    }

    // ✅ Check token expiry
    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    // ✅ Get claims safely
    private Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
    }
}
