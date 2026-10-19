package com.cleverpos.auth;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component
public class JwtService {
  private final SecretKey key;
  public JwtService(@Value("${app.jwt-secret}") String s){ key = Keys.hmacShaKeyFor(s.getBytes(StandardCharsets.UTF_8)); }
  public String issue(AppUser u){
    return Jwts.builder().subject(u.getUsername()).claim("tenant", u.getTenantId()).claim("role", u.getRole())
      .expiration(new Date(System.currentTimeMillis() + 12 * 3600_000L)).signWith(key).compact(); }
  public Claims parse(String t){ return Jwts.parser().verifyWith(key).build().parseSignedClaims(t).getPayload(); }
}
