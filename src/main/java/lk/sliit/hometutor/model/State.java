package lk.sliit.hometutor.model;

import java.util.*;

/** Copy-on-write snapshot; saved before becoming the live in-memory state. */
public final class State {
  public long nextId = 1;
  public final Map<Long, User> users = new LinkedHashMap<>();
  public final Map<Long, Tutor> tutors = new LinkedHashMap<>();
  public final Map<Long, Slot> slots = new LinkedHashMap<>();
  public final Map<Long, Booking> bookings = new LinkedHashMap<>();
  public final Map<Long, Review> reviews = new LinkedHashMap<>();

  public long newId() {
    return nextId++;
  }

  public State copy() {
    State s = new State();
    s.nextId = nextId;
    s.users.putAll(users);
    s.tutors.putAll(tutors);
    s.slots.putAll(slots);
    s.bookings.putAll(bookings);
    s.reviews.putAll(reviews);
    return s;
  }
}
