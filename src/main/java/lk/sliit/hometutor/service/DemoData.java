package lk.sliit.hometutor.service;

import java.math.*;
import java.time.*;
import java.util.*;
import lk.sliit.hometutor.algorithm.*;
import lk.sliit.hometutor.model.*;
import lk.sliit.hometutor.repository.*;
import lk.sliit.hometutor.service.Views.*;

/** DemoData: owns its component rules; persistence is shared. */
public final class DemoData extends ServiceSupport {
    public DemoData(ApplicationStore store) {
        super(store);
    }

    public void seedDemo() {
        synchronized (store) {
            if (!store.state.users.isEmpty()) return;
            String hash = PasswordHasher.hash("Demo123!");
            commit(
                    s -> {
                        long admin = s.newId();
                        s.users.put(
                                admin,
                                User.create(admin, "Administrator", "admin@hometutor.test", hash, true, "ADMIN"));
                        long student = s.newId();
                        s.users.put(
                                student,
                                User.create(
                                        student, "Usman (Demo)", "student@hometutor.test", hash, true, "STUDENT"));
                        String[][] data = {
                                {
                                        "Ayesha Farook",
                                        "Mathematics",
                                        "BSc Mathematics · University of Colombo",
                                        "Colombo",
                                        "Patient, step-by-step O/L and A/L mathematics lessons.",
                                        "1800",
                                        "6"
                                },
                                {
                                        "Nimal Perera",
                                        "Physics",
                                        "BSc Physics · University of Peradeniya",
                                        "Kandy",
                                        "Make difficult physics ideas clear with real-world examples.",
                                        "2200",
                                        "8"
                                },
                                {
                                        "Sarah Fernando",
                                        "English",
                                        "BA English · University of Kelaniya",
                                        "Colombo",
                                        "Build confidence in spoken English and school examinations.",
                                        "1500",
                                        "4"
                                },
                                {
                                        "Ravi Kumar",
                                        "Chemistry",
                                        "BSc Chemistry · University of Jaffna",
                                        "Jaffna",
                                        "Practical explanations, revision plans and past-paper practice.",
                                        "2000",
                                        "5"
                                },
                                {
                                        "Fathima Zain",
                                        "ICT",
                                        "BSc Software Engineering",
                                        "Gampaha",
                                        "Learn programming fundamentals and ICT through hands-on activities.",
                                        "1700",
                                        "3"
                                },
                                {
                                        "Dinesh Silva",
                                        "Mathematics",
                                        "BEd Mathematics",
                                        "Kandy",
                                        "Friendly individual support for grades 6–11.",
                                        "1400",
                                        "7"
                                }
                        };
                        for (int i = 0; i < data.length; i++) {
                            String[] d = data[i];
                            long uid = s.newId(), tid = s.newId();
                            s.users.put(
                                    uid,
                                    User.create(
                                            uid,
                                            d[0],
                                            i == 0 ? "tutor@hometutor.test" : "tutor" + (i + 1) + "@hometutor.test",
                                            hash,
                                            true,
                                            "TUTOR"));
                            s.tutors.put(
                                    tid,
                                    new Tutor(
                                            tid,
                                            uid,
                                            d[1],
                                            d[2],
                                            d[3],
                                            d[4],
                                            new BigDecimal(d[5]),
                                            Integer.parseInt(d[6])));
                            for (int day = 1; day <= 5; day++) {
                                long sid = s.newId();
                                LocalDateTime start = now().toLocalDate().plusDays(day).atTime(16 + i % 3, 0);
                                s.slots.put(sid, new Slot(sid, tid, start, start.plusHours(1)));
                            }
                            long past = s.newId();
                            LocalDateTime old =
                                    now().minusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0);
                            s.slots.put(past, new Slot(past, tid, old, old.plusHours(1)));
                            long bid = s.newId();
                            s.bookings.put(
                                    bid,
                                    new Booking(
                                            bid, student, tid, past, Booking.Status.COMPLETED, new BigDecimal(d[5])));
                            long rid = s.newId();
                            s.reviews.put(
                                    rid,
                                    new Review(
                                            rid,
                                            student,
                                            tid,
                                            bid,
                                            5,
                                            "Clear explanations and a helpful lesson. Demonstration review."));
                        }
                        return null;
                    });
        }
    }
}
