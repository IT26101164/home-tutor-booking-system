package lk.sliit.hometutor.service;

import java.math.*;
import java.time.*;
import java.util.*;
import lk.sliit.hometutor.algorithm.*;
import lk.sliit.hometutor.model.*;
import lk.sliit.hometutor.repository.*;
import lk.sliit.hometutor.service.Views.*;

/** TutorProfileService: owns its component rules; persistence is shared. */
public final class TutorProfileService extends ServiceSupport {
  public TutorProfileService(ApplicationStore store) {
    super(store);
  }

  public List<TutorView> search(String subject, String location, Long id) {
    synchronized (store) {
      String sub = subject == null ? "" : subject.trim().toLowerCase(Locale.ROOT),
          loc = location == null ? "" : location.trim().toLowerCase(Locale.ROOT);
      List<Tutor> candidates =
          id == null
              ? store.tutors.inOrder()
              : Optional.ofNullable(store.tutors.search(id)).map(List::of).orElse(List.of());
      List<Tutor> filtered =
          candidates.stream()
              .filter(
                  t ->
                      store.state.users.get(t.userId()).isActive()
                          && t.subject().toLowerCase(Locale.ROOT).contains(sub)
                          && t.location().toLowerCase(Locale.ROOT).contains(loc))
              .toList();
      return TutorMergeSort.sort(filtered).stream().map(this::view).toList();
    }
  }

  public TutorView detail(long id) {
    synchronized (store) {
      return view(tutor(id));
    }
  }

  public TutorView myTutor(long uid) {
    synchronized (store) {
      role(uid, "TUTOR");
      return store.state.tutors.values().stream()
          .filter(t -> t.userId() == uid)
          .findFirst()
          .map(this::view)
          .orElse(null);
    }
  }

  public TutorView saveTutor(
      long uid,
      Long id,
      String subject,
      String qualification,
      String location,
      String bio,
      BigDecimal fee,
      int experience) {
    if (id != null) throw new AppException(501, "Editing is added on Day 3 of the learning build.");
    synchronized (store) {
      role(uid, "TUTOR", "ADMIN");
      long owner = uid;
      if (id != null) owner = ownTutor(uid, id).userId();
      else {
        role(uid, "TUTOR");
        if (store.state.tutors.values().stream().anyMatch(t -> t.userId() == uid))
          throw fail(409, "You already have a tutor profile.");
      }
      String sub = text(subject, "Subject", 80),
          qual = text(qualification, "Qualification", 180),
          loc = text(location, "Location", 100),
          bi = text(bio, "Bio", 1200);
      if (fee == null || fee.signum() <= 0 || fee.compareTo(new BigDecimal("100000")) > 0)
        throw fail(400, "Hourly fee must be between 0.01 and 100000 LKR.");
      if (experience < 0 || experience > 60) throw fail(400, "Experience must be 0–60 years.");
      long userId = owner;
      BigDecimal price = fee.setScale(2, RoundingMode.HALF_UP);
      long tid =
          commit(
              s -> {
                long key = id == null ? s.newId() : id;
                Tutor t = new Tutor(key, userId, sub, qual, loc, bi, price, experience);
                s.tutors.put(key, t);
                return key;
              });
      return view(tutor(tid));
    }
  }

  public void deleteTutor(long uid, long id) {
    throw new AppException(501, "This operation is added on Day 4 of the learning build.");
  }
}
