package lk.sliit.hometutor.service;

import java.math.*;
import java.time.*;
import java.util.*;
import lk.sliit.hometutor.algorithm.*;
import lk.sliit.hometutor.model.*;
import lk.sliit.hometutor.repository.*;
import lk.sliit.hometutor.service.Views.*;

/** ReviewService: owns its component rules; persistence is shared. */
public final class ReviewService extends ServiceSupport {
  public ReviewService(ApplicationStore store) {
    super(store);
  }

  public List<ReviewView> reviews(long tid) {
    synchronized (store) {
      tutor(tid);
      return store.state.reviews.values().stream()
          .filter(r -> r.tutorId() == tid)
          .map(
              r ->
                  new ReviewView(
                      r.id(),
                      r.studentId(),
                      store.state.users.get(r.studentId()).getName(),
                      tid,
                      r.bookingId(),
                      r.rating(),
                      r.comment()))
          .toList();
    }
  }

  public Review saveReview(long uid, Long id, long bid, int rating, String comment) {
    synchronized (store) {
      role(uid, "STUDENT");
      Booking b = booking(bid);
      if (b.studentId() != uid || b.status() != Booking.Status.COMPLETED)
        throw fail(403, "Reviews require your own completed booking.");
      if (rating < 1 || rating > 5) throw fail(400, "Rating must be 1–5.");
      String c = text(comment, "Review", 1000);
      if (id != null) {
        Review old = store.state.reviews.get(id);
        if (old == null) throw fail(404, "Review not found.");
        if (old.studentId() != uid || old.bookingId() != bid) throw fail(403, "Not your review.");
      }
      if (store.state.reviews.values().stream()
          .anyMatch(r -> r.bookingId() == bid && (id == null || r.id() != id)))
        throw fail(409, "You have already reviewed this booking.");
      return commit(
          s -> {
            long key = id == null ? s.newId() : id;
            Review v = new Review(key, uid, b.tutorId(), bid, rating, c);
            s.reviews.put(key, v);
            return v;
          });
    }
  }

  public void deleteReview(long uid, long id) {
    synchronized (store) {
      User u = actor(uid);
      Review r = store.state.reviews.get(id);
      if (r == null) throw fail(404, "Review not found.");
      if (r.studentId() != uid && !u.getRole().equals("ADMIN")) throw fail(403, "Not your review.");
      commit(
          s -> {
            s.reviews.remove(id);
            return null;
          });
    }
  }
}
