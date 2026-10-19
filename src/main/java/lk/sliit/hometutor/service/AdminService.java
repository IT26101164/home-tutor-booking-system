package lk.sliit.hometutor.service;

import java.math.*;
import java.time.*;
import java.util.*;
import lk.sliit.hometutor.algorithm.*;
import lk.sliit.hometutor.model.*;
import lk.sliit.hometutor.repository.*;
import lk.sliit.hometutor.service.Views.*;

/** AdminService: owns its component rules; persistence is shared. */
public final class AdminService extends ServiceSupport {
  public AdminService(ApplicationStore store) {
    super(store);
  }

  public List<UserView> users(long uid) {
    synchronized (store) {
      role(uid, "ADMIN");
      return store.state.users.values().stream().map(this::view).toList();
    }
  }

  public Map<String, Long> stats(long uid) {
    synchronized (store) {
      role(uid, "ADMIN");
      return Map.of(
          "users",
          store.state.users.values().stream().filter(User::isActive).count(),
          "tutors",
          store.state.tutors.values().stream()
              .filter(t -> store.state.users.get(t.userId()).isActive())
              .count(),
          "bookings",
          (long) store.state.bookings.size(),
          "reviews",
          (long) store.state.reviews.size());
    }
  }

  /** Admin-only account creation: unlike public registration, admin roles are allowed here. */
  public UserView createUser(
      long uid, String name, String address, String pass, String assignedRole) {
    synchronized (store) {
      role(uid, "ADMIN");
      String n = text(name, "Name", 80), e = email(address), p = password(pass);
      if (assignedRole == null || !Set.of("STUDENT", "TUTOR", "ADMIN").contains(assignedRole))
        throw fail(400, "Invalid role.");
      if (store.state.users.values().stream().anyMatch(u -> u.getEmail().equals(e)))
        throw fail(409, "This email is already registered.");
      String hash = PasswordHasher.hash(p);
      return commit(
          next -> {
            User u = User.create(next.newId(), n, e, hash, true, assignedRole);
            next.users.put(u.getId(), u);
            return view(u);
          });
    }
  }

  public UserView updateUser(long uid, long target, String name, String address) {
    synchronized (store) {
      role(uid, "ADMIN");
      User u = store.state.users.get(target);
      if (u == null) throw fail(404, "Account not found.");
      String n = text(name, "Name", 80), e = email(address);
      if (store.state.users.values().stream()
          .anyMatch(x -> x.getId() != target && x.getEmail().equals(e)))
        throw fail(409, "This email is already registered.");
      return commit(
          next -> {
            User edited = User.create(target, n, e, u.getPasswordHash(), u.isActive(), u.getRole());
            next.users.put(target, edited);
            return view(edited);
          });
    }
  }

  public void deactivate(long uid, long target) {
    synchronized (store) {
      role(uid, "ADMIN");
      if (uid == target) throw fail(400, "You cannot deactivate your own admin account.");
      User u = store.state.users.get(target);
      if (u == null) throw fail(404, "Account not found.");
      boolean activeBooking =
          store.state.bookings.values().stream()
              .anyMatch(
                  b ->
                      b.holdsSlot()
                          && (b.studentId() == target
                              || store.state.tutors.get(b.tutorId()).userId() == target));
      if (activeBooking)
        throw fail(409, "Resolve active bookings before deactivating this account.");
      commit(
          next -> {
            next.users.put(
                target,
                User.create(
                    target, u.getName(), u.getEmail(), u.getPasswordHash(), false, u.getRole()));
            return null;
          });
    }
  }

  public UserView reactivate(long uid, long target) {
    synchronized (store) {
      role(uid, "ADMIN");
      User u = store.state.users.get(target);
      if (u == null) throw fail(404, "Account not found.");
      return commit(
          next -> {
            User restored =
                User.create(
                    target, u.getName(), u.getEmail(), u.getPasswordHash(), true, u.getRole());
            next.users.put(target, restored);
            return view(restored);
          });
    }
  }
}
