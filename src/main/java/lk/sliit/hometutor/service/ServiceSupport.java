package lk.sliit.hometutor.service;

import java.math.*;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import lk.sliit.hometutor.algorithm.*;
import lk.sliit.hometutor.model.*;
import lk.sliit.hometutor.repository.*;
import lk.sliit.hometutor.service.Views.*;

/** Internal shared validation and DTO mapping; module methods hold the store lock. */
abstract class ServiceSupport {
    protected final ApplicationStore store;

    protected ServiceSupport(ApplicationStore store) {
        this.store = store;
    }

    protected <T> T commit(Function<State, T> action) {
        return store.commit(action);
    }

    protected LocalDateTime now() {
        return store.now();
    }

    protected static AppException fail(int status, String text) {
        return new AppException(status, text);
    }

    protected static String text(String s, String field, int max) {
        if (s == null || s.isBlank() || s.trim().length() > max)
            throw fail(400, field + " is required (maximum " + max + " characters).");
        return s.trim();
    }

    protected static String email(String e) {
        e = text(e, "Email", 150).toLowerCase(Locale.ROOT);
        if (!e.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) throw fail(400, "Enter a valid email.");
        return e;
    }

    protected static String password(String p) {
        if (p == null || p.length() < 8 || p.length() > 128)
            throw fail(400, "Password must contain 8–128 characters.");
        return p;
    }

    protected User actor(long id) {
        User u = store.state.users.get(id);
        if (u == null || !u.isActive()) throw fail(401, "Please sign in.");
        return u;
    }

    protected User role(long id, String... roles) {
        User u = actor(id);
        if (!Arrays.asList(roles).contains(u.getRole()))
            throw fail(403, "You do not have permission for this action.");
        return u;
    }

    protected Tutor tutor(long id) {
        Tutor t = store.tutors.search(id);
        if (t == null || !store.state.users.get(t.userId()).isActive())
            throw fail(404, "Tutor not found.");
        return t;
    }

    protected Tutor ownTutor(long uid, long tid) {
        User u = role(uid, "TUTOR", "ADMIN");
        Tutor t = tutor(tid);
        if (!u.getRole().equals("ADMIN") && t.userId() != uid)
            throw fail(403, "This tutor profile belongs to another user.");
        return t;
    }

    protected Slot slot(long id) {
        Slot s = store.state.slots.get(id);
        if (s == null) throw fail(404, "Time slot not found.");
        return s;
    }

    protected Booking booking(long id) {
        Booking b = store.state.bookings.get(id);
        if (b == null) throw fail(404, "Booking not found.");
        return b;
    }

    protected boolean held(long sid, long exclude) {
        return store.state.bookings.values().stream()
                .anyMatch(b -> b.id() != exclude && b.slotId() == sid && b.holdsSlot());
    }

    protected UserView view(User u) {
        return new UserView(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.isActive());
    }

    protected TutorView view(Tutor t) {
        var reviews = store.state.reviews.values().stream().filter(r -> r.tutorId() == t.id()).toList();
        double avg = reviews.stream().mapToInt(Review::rating).average().orElse(0);
        return new TutorView(
                t.id(),
                t.userId(),
                store.state.users.get(t.userId()).getName(),
                t.subject(),
                t.qualification(),
                t.location(),
                t.bio(),
                t.hourlyFee(),
                t.experienceYears(),
                avg,
                reviews.size());
    }

    protected BookingView view(Booking b) {
        Slot s = slot(b.slotId());
        Tutor t = store.state.tutors.get(b.tutorId());
        return new BookingView(
                b.id(),
                b.studentId(),
                store.state.users.get(b.studentId()).getName(),
                b.tutorId(),
                store.state.users.get(t.userId()).getName(),
                s.id(),
                s.start(),
                s.end(),
                b.status().name(),
                b.totalFee());
    }
}
