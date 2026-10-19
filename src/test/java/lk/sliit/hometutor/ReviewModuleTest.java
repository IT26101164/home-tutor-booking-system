package lk.sliit.hometutor;

import static org.junit.jupiter.api.Assertions.*;

import lk.sliit.hometutor.service.*;
import org.junit.jupiter.api.Test;

class ReviewModuleTest {
  @Test
  void componentCrud() throws Exception {
    ModuleFixture f = new ModuleFixture();
    var svc = f.app.reviewsModule();
    var completed =
        f.app.bookings(f.student).stream()
            .filter(x -> x.tutorId() == f.profile && x.status().equals("COMPLETED"))
            .findFirst()
            .orElseThrow();
    svc.deleteReview(f.student, svc.reviews(f.profile).get(0).id());
    var review = svc.saveReview(f.student, null, completed.id(), 4, "Helpful lesson");
    assertEquals(4, review.rating());
    assertEquals(
        5, svc.saveReview(f.student, review.id(), completed.id(), 5, "Updated review").rating());
    svc.deleteReview(f.student, review.id());
    assertTrue(svc.reviews(f.profile).isEmpty());
  }
}
