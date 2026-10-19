package lk.sliit.hometutor;

import static org.junit.jupiter.api.Assertions.*;

import lk.sliit.hometutor.service.*;
import org.junit.jupiter.api.Test;

class AccountModuleTest {
  @Test
  void componentCrud() throws Exception {
    ModuleFixture f = new ModuleFixture();
    var svc = f.app.accounts();
    var u = svc.register("Member one", "member1@test.com", "Password1", "STUDENT");
    assertEquals("STUDENT", svc.login(u.email(), "Password1").role());
    assertEquals("Updated", svc.updateAccount(u.id(), "Updated", u.email(), "").name());
    svc.deleteAccount(u.id(), u.id());
    assertThrows(AppException.class, () -> svc.login(u.email(), "Password1"));
  }
}
