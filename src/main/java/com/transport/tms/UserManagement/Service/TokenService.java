package com.transport.tms.UserManagement.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.List;

@Component
public class TokenService {

    @Value("${transport.secret.key}")
    private String secretKey;

    public String generateAccessToken(List<String> permissions, String userName) {
        String defaultSite = "";

        return Jwts
                .builder()
                .setId("TemaJWT")
                .setSubject("tema")
                .claim("authorities",
                        permissions)
                .claim("username", userName)
                .claim("site", defaultSite)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 100000000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public Claims decodeAccessToken(String accessToken) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(accessToken)
                .getBody();
    }

    private SecretKey getSigningKey() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] keyBytes = digest.digest(secretKey.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize JWT signing key", e);
        }
    }
}
