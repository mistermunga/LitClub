package com.litclub.Backend.security.jwt;

import com.litclub.Backend.construct.user.UserRecord;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Value;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private SecretKey signingKey;

    @PostConstruct
    public void init(){

        if (secretKey == null || secretKey.isEmpty()){
            throw new IllegalStateException("Secret Key is Empty");
        }

        try {
            byte[] keyBytes = Decoders.BASE64.decode(secretKey.trim());
            signingKey = Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            signingKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        }

        if (signingKey.getEncoded().length < 32){
            throw new IllegalStateException("Secret Key length is less than 32");
        }

        System.out.println(
                "Initialised JWT settings." +
                        "\nKey length (bytes): " + signingKey.getEncoded().length +
                        "\nExpiration time: " + (jwtExpiration / 3600000) + " hrs"
        );

    }

    public String generateToken(UserRecord ur) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", ur.userID());
        claims.put("clubs", ur.clubs());

        return buildToken(claims, ur.username());
    }

    // ===== Utility =====
    public String stripBearer(String token){
        if (token == null){return "";}
        return token.startsWith("Bearer ") ? token.substring(7) : token;
    }

    public String extractUsername(String token){
        if (token == null){return "";}
        return parseAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token){
        try{
            return extractExpiration(token).after(new Date());
        } catch(Exception e){
            return false;
        }
    }

    // ===== Internals =====
    private String buildToken(Map<String, Object> claims, String subject) {

        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expirationAt = Date.from(now.plusMillis(jwtExpiration));

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(issuedAt)
                .expiration(expirationAt)
                .signWith(signingKey)
                .compact();
    }

    private Claims parseAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(stripBearer(token))
                .getPayload();
    }

    private Date extractExpiration(@Nonnull String token) {
        return parseAllClaims(token).getExpiration();
    }
}
