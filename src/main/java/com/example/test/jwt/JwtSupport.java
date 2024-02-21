package com.example.test.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtSupport {
//    private final byte[] keyBytes = "1234567890".getBytes();
//    private final SecretKey key = Keys.hmacShaKeyFor(keyBytes);
    private final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final JwtParser parser = Jwts.parserBuilder().setSigningKey(key).build();

    public BearerToken generate(String username) {
        String compactToken = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(15, ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();

        return new BearerToken(compactToken);
    }

    public String getId(BearerToken token) {
        return parser.parseClaimsJws(token.getValue()).getBody().getSubject();
    }

    public boolean isValid(BearerToken token, UserDetails user) {
        Claims claims = parser.parseClaimsJws(token.getValue()).getBody();
        boolean unexpired = claims.getExpiration().after(Date.from(Instant.now()));

        return unexpired && claims.getSubject().equals(user != null ? user.getUsername() : null);
    }
}
