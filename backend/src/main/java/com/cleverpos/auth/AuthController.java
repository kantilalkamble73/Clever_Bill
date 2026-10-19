package com.cleverpos.auth;
import com.cleverpos.shop.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/auth")
public class AuthController {
  private final AppUserRepository repo; private final ShopProfileRepository shops; private final JwtService jwt;
  private final BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
  public AuthController(AppUserRepository r, ShopProfileRepository s, JwtService j){ repo = r; shops = s; jwt = j; }
  public record Reg(@NotBlank String shopName, @NotBlank String username, @Size(min=6) String password, String name){}
  public record Login(String username, String password){}
  private Map<String,Object> session(AppUser u){
    Map<String,Object> m = new LinkedHashMap<>();
    m.put("token", jwt.issue(u)); m.put("role", u.getRole()); m.put("username", u.getUsername());
    m.put("name", u.getName() == null || u.getName().isBlank() ? u.getUsername() : u.getName()); return m; }
  @PostMapping("/register")
  public Map<String,Object> register(@Valid @RequestBody Reg r){
    if (repo.findByUsername(r.username()).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
    String t = UUID.randomUUID().toString();
    AppUser u = new AppUser(); u.setTenantId(t); u.setUsername(r.username()); u.setName(r.name());
    u.setPasswordHash(enc.encode(r.password())); u.setRole("OWNER"); repo.save(u);
    ShopProfile s = new ShopProfile(); s.setTenantId(t); s.setName(r.shopName()); shops.save(s);
    return session(u);
  }
  @PostMapping("/login")
  public Map<String,Object> login(@RequestBody Login l){
    AppUser u = repo.findByUsername(String.valueOf(l.username()))
      .filter(x -> l.password() != null && enc.matches(l.password(), x.getPasswordHash()))
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong username or password"));
    return session(u);
  }
}
