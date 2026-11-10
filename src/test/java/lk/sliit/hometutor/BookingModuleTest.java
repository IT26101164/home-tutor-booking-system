
package lk.sliit.hometutor;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import lk.sliit.hometutor.service.AppException;
import org.junit.jupiter.api.Test;

class BookingModuleTest {

    @Test
    void testBookingCrud() throws Exception {

        ModuleFixture f = new ModuleFixture();

        var bookingService = f.app.bookingsModule();
        var availabilityService = f.app.availability();

        // Create two future slots for testing.
        LocalDateTime start = f.app.now().plusDays(30);

        var slot1 = availabilityService.saveSlot(
                f.tutor,
                f.profile,
                null,
                start,
                start.plusHours(1)
        );

        var slot2 = availabilityService.saveSlot(
                f.tutor,
                f.profile,
                null,
                start.plusDays(1),
                start.plusDays(1).plusHours(1)
        );

        // CREATE
        var booking = bookingService.createBooking(
                f.student, slot1.id()
        );

        assertEquals("PENDING", booking.status());

        // READ
        assertTrue(
                bookingService.bookings(f.student)
                        .stream()
                        .anyMatch(b -> b.id() == booking.id())
        );

        // UPDATE - Reschedule
        var updated = bookingService.reschedule(
                f.student, booking.id(), slot2.id()
        );

        assertEquals(slot2.id(), updated.slotId());

        // UPDATE - Confirm
        var confirmed = bookingService.changeStatus(
                f.tutor, booking.id(), "CONFIRMED"
        );

        assertEquals("CONFIRMED", confirmed.status());

        // UPDATE - Cancel
        var cancelled = bookingService.changeStatus(
                f.student, booking.id(), "CANCELLED"
        );

        assertEquals("CANCELLED", cancelled.status());

        // DELETE
        bookingService.deleteBooking(
                f.student, booking.id()
        );

        assertFalse(
                bookingService.bookings(f.student)
                        .stream()
                        .anyMatch(b -> b.id() == booking.id())
        );

        // VALIDATION - Cannot book an invalid slot.
        assertThrows(
                AppException.class,
                () -> bookingService.createBooking(
                        f.student, -999L
                )
        );
    }

    @Test
    void testBookingFilePersistence() throws Exception {

        // Create a temporary folder for test data.
        java.nio.file.Path folder =
                java.nio.file.Files.createTempDirectory("booking-persistence-");

        // Create the first application instance.
        var app1 = new lk.sliit.hometutor.service.TutorService(
                new lk.sliit.hometutor.repository.FileStateRepository(folder),
                java.time.Clock.systemDefaultZone()
        );

        // Add demo data.
        app1.seedDemo();

        long student = app1.accounts()
                .login("student@hometutor.test", "Demo123!")
                .id();

        long tutor = app1.accounts()
                .login("tutor@hometutor.test", "Demo123!")
                .id();

        long profile = app1.profiles()
                .myTutor(tutor)
                .id();

        // Create a new future slot.
        var start = app1.now().plusDays(40);

        var slot = app1.availability().saveSlot(
                tutor,
                profile,
                null,
                start,
                start.plusHours(1)
        );

        // CREATE BOOKING
        var booking = app1.bookingsModule()
                .createBooking(student, slot.id());

        long bookingId = booking.id();

        // Simulate application restart using a new service instance.
        var app2 = new lk.sliit.hometutor.service.TutorService(
                new lk.sliit.hometutor.repository.FileStateRepository(folder),
                java.time.Clock.systemDefaultZone()
        );

        // READ BOOKING AFTER RESTART
        boolean bookingExists = app2.bookingsModule()
                .bookings(student)
                .stream()
                .anyMatch(b -> b.id() == bookingId);

        assertTrue(
                bookingExists,
                "Booking should remain saved after application restart"
        );
    }

}
