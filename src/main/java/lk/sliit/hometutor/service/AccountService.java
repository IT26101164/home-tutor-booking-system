package lk.sliit.hometutor.service;

import java.math.*;
import java.time.*;
import java.util.*;
import lk.sliit.hometutor.algorithm.*;
import lk.sliit.hometutor.model.*;
import lk.sliit.hometutor.repository.*;
import lk.sliit.hometutor.service.Views.*;

/** AccountService: owns its component rules; persistence is shared. */
public final class AccountService extends ServiceSupport {
  public AccountService(ApplicationStore store) {
    super(store);
  }

  public UserView me(long id) {
    synchronized (store) {
      return view(actor(id));
    }
  }

  public UserView register(String name, String email, String pass, String role) {
    synchronized (store) {
      String n = text(name, "Name", 80), e = email(email), p = password(pass);
      if (role == null || !Set.of("STUDENT", "TUTOR").contains(role))
        throw fail(400, "Choose Student or Tutor.");
      if (store.state.users.values().stream().anyMatch(u -> u.getEmail().equals(e)))
        throw fail(409, "This email is already registered.");
      String hash = PasswordHasher.hash(p);
      return commit(
          s -> {
            User u = User.create(s.newId(), n, e, hash, true, role);
            s.users.put(u.getId(), u);
            return view(u);
          });
    }
  }

  public UserView login(String email, String pass) {
    synchronized (store) {
      String e = email(email);
      if (pass == null || pass.length() > 128) throw fail(401, "Email or password is incorrect.");
      User u =
          store.state.users.values().stream()
              .filter(x -> x.getEmail().equals(e) && x.isActive())
              .findFirst()
              .orElse(null);
      if (u == null || !PasswordHasher.matches(pass, u.getPasswordHash()))
        throw fail(401, "Email or password is incorrect.");
      return view(u);
    }
  }

  public UserView updateAccount(long uid, String name, String email, String newPassword) {
    synchronized (store) {
      User u = actor(uid);
      String n = text(name, "Name", 80), e = email(email);
      if (store.state.users.values().stream()
          .anyMatch(x -> x.getId() != uid && x.getEmail().equals(e)))
        throw fail(409, "This email is already registered.");
      String hash =
          newPassword == null || newPassword.isBlank()
              ? u.getPasswordHash()
              : PasswordHasher.hash(password(newPassword));
      return commit(
          s -> {
            User v = User.create(uid, n, e, hash, true, u.getRole());
            s.users.put(uid, v);
            return view(v);
          });
    }
  }

  public void deleteAccount(long actorId, long targetId) {
    synchronized (store) {
      User a = actor(actorId);
      if (actorId != targetId && !a.getRole().equals("ADMIN")) throw fail(403, "Not permitted.");
      User u = store.state.users.get(targetId);
      if (u == null) throw fail(404, "Account not found.");
      if (u.getRole().equals("ADMIN"))
        throw fail(400, "Admin accounts cannot be deactivated here.");
      boolean activeBooking =
          store.state.bookings.values().stream()
              .anyMatch(
                  b ->
                      b.holdsSlot()
                          && (b.studentId() == targetId
                              || store.state.tutors.get(b.tutorId()).userId() == targetId));
      if (activeBooking)
        throw fail(409, "Cancel or complete active bookings before deactivating this account.");
      commit(
          s -> {
            s.users.put(
                targetId,
                User.create(
                    u.getId(), u.getName(), u.getEmail(), u.getPasswordHash(), false, u.getRole()));
            return null;
          });
    }
  }
}
