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
        throw new AppException(501, "This operation is added on Day 3 of the learning build.");
    }

    public void deactivate(long uid, long target) {
        throw new AppException(501, "This operation is added on Day 4 of the learning build.");
    }

    public UserView reactivate(long uid, long target) {
        throw new AppException(501, "This operation is added on Day 3 of the learning build.");
    }
}
