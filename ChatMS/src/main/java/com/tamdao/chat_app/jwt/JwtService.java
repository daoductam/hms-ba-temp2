package com.tamdao.chat_app.jwt;

import com.tamdao.chat_app.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    public long extractUserId(String jwtToken) {

        String userIdStr = extractClaim(jwtToken, claims -> claims.get("userId", String.class));

        return userIdStr != null ? Long.parseLong(userIdStr) : null;
    }

    private <T> T extractClaim(String jwtToken, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaim(jwtToken);
        return claimsResolvers.apply(claims);
    }

    public Claims extractAllClaim(String jwtToken) {

        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(jwtToken)
                .getPayload();
    }

    public SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateToken(User user) {

        return generateToken(new HashMap<>(), user);
    }

    public String generateToken(Map<String, Object> extraClaims, User user) {
        Map<String, Object> claims = new HashMap<>(extraClaims);
        claims.put("userId", user.getId());

        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+jwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }

    public boolean isTokenValid(String jwtToken, User user) {

        final Long userIdFromToken = extractUserId(jwtToken);

        final Long userId = user.getId();

        return (userIdFromToken != null && userIdFromToken.equals(userId) &&
                !isTokenExpired(jwtToken));
    }

    private boolean isTokenExpired(String jwtToken) {
        return extractExpiration(jwtToken).before(new Date());
    }

    private Date extractExpiration(String jwtToken) {
        return extractClaim(jwtToken, Claims::getExpiration);
    }
}
