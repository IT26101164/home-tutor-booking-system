package lk.sliit.hometutor;

import static org.junit.jupiter.api.Assertions.*;

import lk.sliit.hometutor.service.*:
import org.junit.jupiter.api.Test;

class AdminModuleTest {
  @Test
  void componentCrud() throws Exception {
    ModuleFixture f = new ModuleFixture();
    var svc = f.app.admin();
    assertThrows(
        AppException.class,
        () -> svc.createUser(f.student, "Blocked", "blocked@test.com", "Password1", "ADMIN"));
    var u = svc.createUser(f.admin, "Admin two", "admin2@test.com", "Password1", "ADMIN");
    assertEquals("ADMIN", u.role());
    assertEquals("Edited admin", svc.updateUser(f.admin, u.id(), "Edited admin", u.email()).name());
    svc.deactivate(f.admin, u.id());
    assertFalse(
        svc.users(f.admin).stream()
            .filter(x -> x.id() == u.id())
            .findFirst()
            .orElseThrow()
            .active());
    assertThrows(AppException.class, () -> f.app.accounts().login(u.email(), "Password1"));
    assertTrue(svc.reactivate(f.admin, u.id()).active());
    assertThrows(AppException.class, () -> svc.deactivate(f.admin, f.admin));
  }
}
