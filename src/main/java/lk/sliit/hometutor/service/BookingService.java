
package lk.sliit.hometutor.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import lk.sliit.hometutor.model.Booking;
import lk.sliit.hometutor.model.Slot;
import lk.sliit.hometutor.model.User;
import lk.sliit.hometutor.service.Views.BookingView;

/**
 * Booking Management:
 * Create, Read, Update, Reschedule and Delete bookings.
 * Data persistence is managed by the shared ApplicationStore.
 */
public final class BookingService extends ServiceSupport {

    public BookingService(ApplicationStore store) {
        super(store);
    }

    // Check whether a student can book a particular slot.
    private void canBook(long uid, Slot slot, long exclude) {
        tutor(slot.tutorId());

        if (!slot.start().isAfter(now())) {
            throw fail(400, "This time slot has already started.");
        }

        if (held(slot.id(), exclude)) {
            throw fail(409, "This time slot has already been reserved.");
        }

        boolean overlapping = store.state.bookings.values()
                .stream()
                .filter(b ->
                        b.id() != exclude
                                && b.studentId() == uid
                                && b.holdsSlot())
                .map(b -> store.state.slots.get(b.slotId()))
                .anyMatch(s ->
                        slot.start().isBefore(s.end())
                                && slot.end().isAfter(s.start()));

        if (overlapping) {
            throw fail(409, "You already have an overlapping booking.");
        }
    }

    // Calculate booking fee based on tutor rate and duration.
    private BigDecimal total(Slot s) {
        return tutor(s.tutorId())
                .hourlyFee()
                .multiply(BigDecimal.valueOf(
                        Duration.between(
                                s.start(), s.end()
                        ).toMinutes()))
                .divide(
                        BigDecimal.valueOf(60),
                        2,
                        RoundingMode.HALF_UP
                );
    }

    // CREATE - Add a new booking.
    public BookingView createBooking(long uid, long slotId) {
        synchronized (store) {
            role(uid, "STUDENT");

            Slot sl = slot(slotId);
            canBook(uid, sl, -1);

            BigDecimal fee = total(sl);

            long id = commit(s -> {
                long key = s.newId();

                s.bookings.put(
                        key,
                        new Booking(
                                key,
                                uid,
                                sl.tutorId(),
                                slotId,
                                Booking.Status.PENDING,
                                fee
                        )
                );

                return key;
            });

            return view(booking(id));
        }
    }

    // READ - Retrieve bookings visible to the current user.
    public List<BookingView> bookings(long uid) {
        synchronized (store) {
            User u = actor(uid);

            return store.state.bookings.values()
                    .stream()
                    .filter(b ->
                            u.getRole().equals("ADMIN")
                                    || b.studentId() == uid
                                    || store.state.tutors
                                    .get(b.tutorId())
                                    .userId() == uid)
                    .sorted(
                            Comparator.comparingLong(
                                    Booking::id
                            ).reversed()
                    )
                    .map(this::view)
                    .toList();
        }
    }

    // UPDATE - Confirm, reject, cancel or complete a booking.
    public BookingView changeStatus(
            long uid, long id, String target) {

        synchronized (store) {
            User u = actor(uid);
            Booking b = booking(id);

            boolean admin = u.getRole().equals("ADMIN");
            boolean owner = b.studentId() == uid;
            boolean tutorOwner =
                    tutor(b.tutorId()).userId() == uid;

            Booking.Status next;

            try {
                next = Booking.Status.valueOf(
                        target.trim().toUpperCase(Locale.ROOT)
                );
            } catch (IllegalArgumentException
                     | NullPointerException e) {
                throw fail(400, "Invalid booking status.");
            }

            switch (next) {

                case CONFIRMED, REJECTED -> {
                    if (!admin && !tutorOwner) {
                        throw fail(
                                403,
                                "Only the tutor can accept or reject."
                        );
                    }

                    if (b.status() != Booking.Status.PENDING) {
                        throw fail(
                                409,
                                "Only pending bookings can be accepted or rejected."
                        );
                    }

                    if (next == Booking.Status.CONFIRMED
                            && !slot(b.slotId())
                            .start().isAfter(now())) {
                        throw fail(
                                409,
                                "Cannot accept a booking that has already started."
                        );
                    }
                }

                case CANCELLED -> {
                    if (!admin && !owner && !tutorOwner) {
                        throw fail(403, "Not your booking.");
                    }

                    if (!b.holdsSlot()) {
                        throw fail(
                                409,
                                "Only pending or confirmed bookings can be cancelled."
                        );
                    }

                    if (!admin && !slot(b.slotId())
                            .start().isAfter(now())) {
                        throw fail(
                                409,
                                "Contact the administrator to cancel a session that has already started."
                        );
                    }
                }

                case COMPLETED -> {
                    if (!admin && !tutorOwner) {
                        throw fail(
                                403,
                                "Only the tutor can complete the session."
                        );
                    }

                    if (b.status() != Booking.Status.CONFIRMED
                            || slot(b.slotId()).end().isAfter(now())) {
                        throw fail(
                                409,
                                "Complete a confirmed session only after its end time."
                        );
                    }
                }

                default -> throw fail(
                        400,
                        "This transition is not allowed."
                );
            }

            commit(s -> {
                s.bookings.put(id, b.withStatus(next));
                return null;
            });

            return view(booking(id));
        }
    }

    // UPDATE - Move an active booking to another slot.
    public BookingView reschedule(
            long uid, long id, long slotId) {

        synchronized (store) {
            role(uid, "STUDENT");

            Booking b = booking(id);

            if (b.studentId() != uid) {
                throw fail(403, "Not your booking.");
            }

            if (!b.holdsSlot()
                    || !slot(b.slotId()).start().isAfter(now())) {
                throw fail(
                        409,
                        "Only future active bookings can be rescheduled."
                );
            }

            Slot sl = slot(slotId);

            if (sl.tutorId() != b.tutorId()) {
                throw fail(
                        400,
                        "Select another slot for the same tutor."
                );
            }

            if (sl.id() == b.slotId()) {
                throw fail(
                        400,
                        "Select a different slot."
                );
            }

            canBook(uid, sl, id);

            BigDecimal fee = total(sl);

            commit(s -> {
                s.bookings.put(
                        id,
                        new Booking(
                                id,
                                uid,
                                b.tutorId(),
                                slotId,
                                Booking.Status.PENDING,
                                fee
                        )
                );

                return null;
            });

            return view(booking(id));
        }
    }

    // DELETE - Remove a cancelled or rejected booking.
    public void deleteBooking(long uid, long id) {
        synchronized (store) {
            User u = actor(uid);
            Booking b = booking(id);

            if (!u.getRole().equals("ADMIN")
                    && b.studentId() != uid) {
                throw fail(403, "Not your booking.");
            }

            if (b.status() != Booking.Status.CANCELLED
                    && b.status() != Booking.Status.REJECTED) {
                throw fail(
                        409,
                        "Only cancelled or rejected bookings may be deleted."
                );
            }

            // Preserve booking history referenced by reviews.
            boolean hasReview = store.state.reviews.values()
                    .stream()
                    .anyMatch(r -> r.bookingId() == id);

            if (hasReview) {
                throw fail(
                        409,
                        "Cannot delete a booking with review history."
                );
            }

            commit(s -> {
                s.bookings.remove(id);
                return null;
            });
        }
    }
}
