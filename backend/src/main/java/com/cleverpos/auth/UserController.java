package com.cleverpos.auth;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/users")
public class UserController {
  private final AppUserRepository repo; private final BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
  public UserController(AppUserRepository r){ repo = r; }
  public record NewUser(@NotBlank String username, String name, @Size(min=6) String password, @Pattern(regexp="ADMIN|CASHIER", message="must be ADMIN or CASHIER") String role){}
  private Map<String,Object> view(AppUser u){
    return Map.of("id", u.getId(), "username", u.getUsername(), "name", u.getName() == null ? "" : u.getName(), "role", u.getRole()); }
  @GetMapping public List<Map<String,Object>> list(@RequestAttribute("tenantId") String t){
    return repo.findByTenantIdOrderByIdDesc(t).stream().map(this::view).toList(); }
  @PostMapping public Map<String,Object> create(@RequestAttribute("tenantId") String t, @Valid @RequestBody NewUser n){
    if (repo.findByUsername(n.username()).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
    AppUser u = new AppUser(); u.setTenantId(t); u.setUsername(n.username()); u.setName(n.name());
    u.setPasswordHash(enc.encode(n.password())); u.setRole(n.role()); return view(repo.save(u)); }
  @DeleteMapping("/{id}") public void delete(@RequestAttribute("tenantId") String t, @RequestAttribute("user") String me, @PathVariable Long id){
    AppUser u = repo.findById(id).filter(x -> t.equals(x.getTenantId())).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    if (u.getUsername().equals(me)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot delete yourself");
    repo.delete(u); }
}
