package lk.sliit.hometutor.service;

import java.math.*;
import java.time.*;
import java.util.*;
import lk.sliit.hometutor.algorithm.*;
import lk.sliit.hometutor.model.*;
import lk.sliit.hometutor.repository.*;
import lk.sliit.hometutor.service.Views.*;

/** BookingService: owns its component rules; persistence is shared. */
public final class BookingService extends ServiceSupport {
  public BookingService(ApplicationStore store) {
    super(store);
  }

  private void canBook(long uid, Slot slot, long exclude) {
    tutor(slot.tutorId());
    if (!slot.start().isAfter(now())) throw fail(400, "This time slot has already started.");
    if (held(slot.id(), exclude)) throw fail(409, "This time slot has already been reserved.");
    if (store.state.bookings.values().stream()
        .filter(b -> b.id() != exclude && b.studentId() == uid && b.holdsSlot())
        .map(b -> store.state.slots.get(b.slotId()))
        .anyMatch(s -> slot.start().isBefore(s.end()) && slot.end().isAfter(s.start())))
      throw fail(409, "You already have an overlapping booking.");
  }

  private BigDecimal total(Slot s) {
    return tutor(s.tutorId())
        .hourlyFee()
        .multiply(BigDecimal.valueOf(Duration.between(s.start(), s.end()).toMinutes()))
        .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
  }

  public BookingView createBooking(long uid, long slotId) {
    synchronized (store) {
      role(uid, "STUDENT");
      Slot sl = slot(slotId);
      canBook(uid, sl, -1);
      BigDecimal fee = total(sl);
      long id =
          commit(
              s -> {
                long key = s.newId();
                s.bookings.put(
                    key, new Booking(key, uid, sl.tutorId(), slotId, Booking.Status.PENDING, fee));
                return key;
              });
      return view(booking(id));
    }
  }

  public List<BookingView> bookings(long uid) {
    synchronized (store) {
      User u = actor(uid);
      return store.state.bookings.values().stream()
          .filter(
              b ->
                  u.getRole().equals("ADMIN")
                      || b.studentId() == uid
                      || store.state.tutors.get(b.tutorId()).userId() == uid)
          .sorted(Comparator.comparingLong(Booking::id).reversed())
          .map(this::view)
          .toList();
    }
  }

  public BookingView changeStatus(long uid, long id, String target) {
    throw new AppException(501, "This operation is added on Day 3 of the learning build.");
  }

  public BookingView reschedule(long uid, long id, long slotId) {
    throw new AppException(501, "This operation is added on Day 3 of the learning build.");
  }

  public void deleteBooking(long uid, long id) {
    throw new AppException(501, "This operation is added on Day 4 of the learning build.");
  }
}
