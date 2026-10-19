package lk.sliit.hometutor;

import static org.junit.jupiter.api.Assertions.*;

import lk.sliit.hometutor.service.*;
import org.junit.jupiter.api.Test;

class TutorModuleTest {
  @Test
  void componentCrud() throws Exception {
    ModuleFixture f = new ModuleFixture();
    var uid =
        f.app.accounts().register("Tutor demo", "module2@test.com", "Password1", "TUTOR").id();
    var svc = f.app.profiles();
    var t =
        svc.saveTutor(
            uid,
            null,
            "Zoology",
            "BSc",
            "Galle",
            "Patient teaching",
            new java.math.BigDecimal("1200"),
            3);
    assertEquals(t.id(), svc.search("Zoology", "Galle", t.id()).get(0).id());
    svc.saveTutor(
        uid,
        t.id(),
        "Biology",
        "BSc",
        "Galle",
        "Clear lessons",
        new java.math.BigDecimal("1500"),
        4);
    assertEquals("Biology", svc.detail(t.id()).subject());
    svc.deleteTutor(uid, t.id());
    assertTrue(svc.search("", "", t.id()).isEmpty());
  }
}
