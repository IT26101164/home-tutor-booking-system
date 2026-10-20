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
    throw new AppException(501, "This operation is added on Day 3 of the learning build.");
  }

  public void deleteAccount(long actorId, long targetId) {
    throw new AppException(501, "This operation is added on Day 4 of the learning build.");
  }
}
