package lk.sliit.hometutor;

import static org.junit.jupiter.api.Assertions.*;

import lk.sliit.hometutor.service.*;
import org.junit.jupiter.api.Test;

class AvailabilityModuleTest {
  @Test
  void componentCrud() throws Exception {
    ModuleFixture f = new ModuleFixture();
    var svc = f.app.availability();
    var start = f.app.now().plusDays(20);
    var slot = svc.saveSlot(f.tutor, f.profile, null, start, start.plusHours(1));
    assertTrue(svc.slots(f.profile, true).stream().anyMatch(x -> x.id() == slot.id()));
    assertThrows(
        AppException.class,
        () -> svc.saveSlot(f.tutor, f.profile, null, start.plusMinutes(20), start.plusHours(2)));
    svc.saveSlot(f.tutor, f.profile, slot.id(), start.plusDays(1), start.plusDays(1).plusHours(1));
    svc.deleteSlot(f.tutor, slot.id());
    assertTrue(svc.slots(f.profile, false).stream().noneMatch(x -> x.id() == slot.id()));
  }
}
