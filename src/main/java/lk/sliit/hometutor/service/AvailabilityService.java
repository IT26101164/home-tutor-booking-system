package lk.sliit.hometutor.service;

import java.math.*;
import java.time.*;
import java.util.*;
import lk.sliit.hometutor.algorithm.*;
import lk.sliit.hometutor.model.*;
import lk.sliit.hometutor.repository.*;
import lk.sliit.hometutor.service.Views.*;

/** AvailabilityService: owns its component rules; persistence is shared. */
public final class AvailabilityService extends ServiceSupport {
  public AvailabilityService(ApplicationStore store) {
    super(store);
  }

  public List<SlotView> slots(long tid, boolean onlyAvailable) {
    synchronized (store) {
      tutor(tid);
      return store.state.slots.values().stream()
          .filter(s -> s.tutorId() == tid)
          .sorted(Comparator.comparing(Slot::start))
          .map(
              s ->
                  new SlotView(
                      s.id(),
                      s.tutorId(),
                      s.start(),
                      s.end(),
                      s.start().isAfter(now()) && !held(s.id(), -1)))
          .filter(s -> !onlyAvailable || s.available())
          .toList();
    }
  }

  private void validateSlot(long tid, long ignore, LocalDateTime start, LocalDateTime end) {
    if (start == null
        || end == null
        || !start.isAfter(now())
        || !end.isAfter(start)
        || Duration.between(start, end).toMinutes() < 15
        || Duration.between(start, end).compareTo(Duration.ofHours(8)) > 0)
      throw fail(400, "Choose a future slot lasting 15 minutes to 8 hours.");
    if (store.state.slots.values().stream()
        .anyMatch(
            s ->
                s.id() != ignore
                    && s.tutorId() == tid
                    && start.isBefore(s.end())
                    && end.isAfter(s.start())))
      throw fail(409, "This slot overlaps an existing tutor slot.");
  }

  public SlotView saveSlot(long uid, long tid, Long id, LocalDateTime start, LocalDateTime end) {
    synchronized (store) {
      ownTutor(uid, tid);
      if (id != null) {
        Slot old = slot(id);
        if (old.tutorId() != tid) throw fail(403, "Slot belongs to another tutor.");
        if (store.state.bookings.values().stream().anyMatch(b -> b.slotId() == id))
          throw fail(409, "A slot with booking history cannot be edited. Add a new slot.");
      }
      validateSlot(tid, id == null ? -1 : id, start, end);
      return commit(
          s -> {
            long sid = id == null ? s.newId() : id;
            Slot v = new Slot(sid, tid, start, end);
            s.slots.put(sid, v);
            return new SlotView(sid, tid, start, end, true);
          });
    }
  }

  public void deleteSlot(long uid, long id) {
    synchronized (store) {
      Slot x = slot(id);
      ownTutor(uid, x.tutorId());
      if (store.state.bookings.values().stream().anyMatch(b -> b.slotId() == id))
        throw fail(409, "A slot with booking history cannot be deleted.");
      commit(
          s -> {
            s.slots.remove(id);
            return null;
          });
    }
  }
}
