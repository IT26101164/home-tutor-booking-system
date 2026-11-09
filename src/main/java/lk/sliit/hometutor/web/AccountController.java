package lk.sliit.hometutor.web;

import static lk.sliit.hometutor.web.SessionUser.uid;

import jakarta.servlet.http.*;
import java.util.*;
import lk.sliit.hometutor.service.*;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AccountController {
  private final AccountService service;

  public AccountController(AccountService service) {
    this.service = service;
  }

  public record Login(String email, String password) {}

  public record Registration(String name, String email, String password, String role) {}

  public record AccountInput(String name, String email, String password) {}

  @GetMapping("/session")
  public Map<String, Object> session(HttpServletRequest r, CsrfToken csrf) {
    Map<String, Object> m = new HashMap<>();
    m.put("csrfToken", csrf.getToken());
    m.put("csrfHeader", csrf.getHeaderName());
    try {
      m.put("user", service.me(uid(r)));
    } catch (AppException e) {
      m.put("user", null);
    }
    return m;
  }

  @PostMapping("/auth/register")
  public Object register(@RequestBody Registration in, HttpServletRequest r) {
    var u = service.register(in.name(), in.email(), in.password(), in.role());
    r.getSession();
    r.changeSessionId();
    r.getSession().setAttribute("uid", u.id());
    return u;
  }

  @PostMapping("/auth/login")
  public Object login(@RequestBody Login in, HttpServletRequest r) {
    var u = service.login(in.email(), in.password());
    r.getSession();
    r.changeSessionId();
    r.getSession().setAttribute("uid", u.id());
    return u;
  }

  @PostMapping("/auth/logout")
  public Map<String, String> logout(HttpServletRequest r) {
    HttpSession s = r.getSession(false);
    if (s != null) s.invalidate();
    return Map.of("message", "Signed out");
  }

  @PutMapping("/account")
  public Object account(@RequestBody AccountInput in, HttpServletRequest r) {
    return service.updateAccount(uid(r), in.name(), in.email(), in.password());
  }

  @DeleteMapping("/account")
  public void deleteAccount(HttpServletRequest r) {
    long id = uid(r);
    service.deleteAccount(id, id);
    r.getSession().invalidate();
  }
}
