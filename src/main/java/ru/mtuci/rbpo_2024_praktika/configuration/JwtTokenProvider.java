package ru.mtuci.rbpo_2024_praktika.configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private static final String AUTHORITIES_KEY = "roles";

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String createToken(String username, Set<GrantedAuthority> authorities) {
        // Преобразуем GrantedAuthority в String, чтобы сохранить их в токене
        String authoritiesString = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setSubject(username)  // Имя пользователя
                .claim("roles", authoritiesString)  // Добавляем роли (или права) пользователя
                .setIssuedAt(new Date())  // Время создания токена
                .setExpiration(new Date(System.currentTimeMillis() + expiration))  // Время истечения токена
                .signWith(SignatureAlgorithm.HS512, secret)  // Подписываем токен секретным ключом
                .compact();  // Формируем итоговый токен
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // Log or handle the error (e.g., TokenExpiredException, MalformedJwtException, etc.)
            return false;
        }
    }

    public String getUsername(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    public Set<GrantedAuthority> getAuthorities(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();

        // Получаем строку с ролями из токена
        String rolesString = claims.get("roles", String.class);

        // Преобразуем строку в Set<GrantedAuthority>
        return Arrays.stream(rolesString.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
}

