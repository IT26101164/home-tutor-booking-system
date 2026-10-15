package lk.sliit.hometutor;

import static org.junit.jupiter.api.Assertions.*;

import lk.sliit.hometutor.service.*;
import org.junit.jupiter.api.Test;

class BookingModuleTest {
  @Test
  void componentCrud() throws Exception {
    ModuleFixture f = new ModuleFixture();
    var svc = f.app.bookingsModule();
    var slots = f.app.availability().slots(f.profile, true);
    var b = svc.createBooking(f.student, slots.get(0).id());
    assertEquals("PENDING", b.status());
    assertEquals("CONFIRMED", svc.changeStatus(f.tutor, b.id(), "CONFIRMED").status());
    assertEquals("PENDING", svc.reschedule(f.student, b.id(), slots.get(1).id()).status());
    svc.changeStatus(f.student, b.id(), "CANCELLED");
    svc.deleteBooking(f.student, b.id());
    assertTrue(svc.bookings(f.student).stream().noneMatch(x -> x.id() == b.id()));
  }
}
