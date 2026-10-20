package com.cleverpos.auth;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
@Component
public class AuthInterceptor implements HandlerInterceptor {
  private final JwtService jwt;
  public AuthInterceptor(JwtService jwt){ this.jwt = jwt; }
  private boolean deny(HttpServletResponse r, int code, String msg) throws Exception {
    r.setStatus(code); r.setContentType("application/json"); r.getWriter().write("{\"message\":\"" + msg + "\"}"); return false; }
  public boolean preHandle(HttpServletRequest q, HttpServletResponse r, Object h) throws Exception {
    String m = q.getMethod(), p = q.getRequestURI();
    if (m.equals("OPTIONS") || p.startsWith("/api/auth/")) return true;
    String a = q.getHeader("Authorization");
    if (a == null || !a.startsWith("Bearer ")) return deny(r, 401, "Login required");
    Claims c;
    try { c = jwt.parse(a.substring(7)); } catch (Exception e) { return deny(r, 401, "Session expired, sign in again"); }
    String role = c.get("role", String.class);
    if (role == null) return deny(r, 401, "Session expired, sign in again");
    q.setAttribute("tenantId", c.get("tenant", String.class)); q.setAttribute("role", role); q.setAttribute("user", c.getSubject());
    boolean write = !m.equals("GET");
    boolean cashierOk = !p.startsWith("/api/users") && !p.startsWith("/api/reports") && !p.startsWith("/api/ai") && !m.equals("DELETE") && !p.endsWith("/return")
      && (!write || (m.equals("POST") && (p.startsWith("/api/bills") || p.startsWith("/api/payments") || p.startsWith("/api/khata") || p.startsWith("/api/stock"))));
    boolean ok = role.equals("OWNER") || (role.equals("ADMIN") && !p.startsWith("/api/users")) || (role.equals("CASHIER") && cashierOk);
    return ok || deny(r, 403, "Not allowed for your role");
  }
}
