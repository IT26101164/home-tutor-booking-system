package lk.sliit.hometutor;

import java.math.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import lk.sliit.hometutor.algorithm.*;
import lk.sliit.hometutor.model.*;
import lk.sliit.hometutor.repository.*;
import lk.sliit.hometutor.service.*;

/** Runs with plain JDK as well as through JUnit. No framework mocks. */
public final class CoreChecks {
  static int checks;

  static void ok(boolean value, String message) {
    checks++;
    if (!value) throw new AssertionError(message);
  }

  static void rejects(int status, Runnable action) {
    try {
      action.run();
      throw new AssertionError("Expected rejection " + status);
    } catch (AppException e) {
      ok(
          e.status() == status,
          "Status " + e.status() + " expected " + status + ": " + e.getMessage());
    }
  }

  static Tutor t(long id, String subject) {
    return new Tutor(id, id, subject, "BSc", "Colombo", "Bio", new BigDecimal("1000"), 3);
  }

  public static void main(String[] args) throws Exception {
    run();
  }

  public static void run() throws Exception {
    checks = 0;
    TutorBST bst = new TutorBST();
    for (long id : new long[] {50, 30, 70, 20, 40, 60, 80}) bst.insert(t(id, "Math"));
    ok(bst.search(60).id() == 60, "BST search");
    ok(bst.search(99) == null, "BST missing");
    ok(bst.delete(50), "BST delete root two children");
    ok(
        bst.inOrder().stream()
            .map(Tutor::id)
            .toList()
            .equals(List.of(20L, 30L, 40L, 60L, 70L, 80L)),
        "BST ordered after root deletion");
    ok(bst.delete(20) && bst.delete(30) && !bst.delete(999), "BST leaf, one child, missing");
    bst.insert(t(40, "Physics"));
    ok(bst.search(40).subject().equals("Physics"), "BST replaces duplicate key");
    List<Tutor> input =
        List.of(t(3, "Physics"), t(2, "mathematics"), t(4, "Chemistry"), t(1, "Mathematics"));
    var sorted = TutorMergeSort.sort(input);
    ok(
        sorted.stream().map(Tutor::id).toList().equals(List.of(4L, 1L, 2L, 3L)),
        "Merge sort case-insensitive and tie ID");
    ok(input.get(0).id() == 3, "Merge sort preserves input");
    ok(TutorMergeSort.sort(List.of()).isEmpty(), "Empty sort");
    Path dir = Files.createTempDirectory("home-tutor-test-");
    Clock clock = Clock.fixed(Instant.parse("2026-10-06T00:00:00Z"), ZoneId.of("Asia/Colombo"));
    FileStateRepository repo = new FileStateRepository(dir);
    TutorService s = new TutorService(repo, clock);
    s.seedDemo();
    var student = s.login("student@hometutor.test", "Demo123!");
    var tutor = s.login("tutor@hometutor.test", "Demo123!");
    var admin = s.login("admin@hometutor.test", "Demo123!");
    ok(student.role().equals("STUDENT"), "Polymorphic student role");
    rejects(401, () -> s.login(student.email(), "wrong"));
    rejects(409, () -> s.register("Name", student.email(), "Password1", "STUDENT"));
    rejects(400, () -> s.register("Name", "other@test.com", "123", "TUTOR"));
    rejects(400, () -> s.register("Name", "other@test.com", "Password1", "ADMIN"));
    lk.sliit.hometutor.service.Views.UserView other =
        s.register("Second student", "other@test.com", "Password1", "STUDENT");
    var own = s.myTutor(tutor.id());
    ok(own != null, "Seed tutor");
    ok(s.search("math", "colombo", null).size() == 1, "Subject location filtering");
    ok(s.search("", "", own.id()).size() == 1, "ID lookup");
    var slots = s.slots(own.id(), true);
    long sid = slots.get(0).id();
    var b = s.createBooking(student.id(), sid);
    ok(b.status().equals("PENDING"), "Create pending");
    rejects(409, () -> s.createBooking(other.id(), sid));
    rejects(403, () -> s.changeStatus(other.id(), b.id(), "CONFIRMED"));
    rejects(403, () -> s.changeStatus(student.id(), b.id(), "CONFIRMED"));
    ok(
        s.changeStatus(tutor.id(), b.id(), "CONFIRMED").status().equals("CONFIRMED"),
        "Tutor accepts");
    rejects(409, () -> s.changeStatus(tutor.id(), b.id(), "COMPLETED"));
    rejects(403, () -> s.saveReview(student.id(), null, b.id(), 5, "Early review"));
    long nextSlot = slots.get(1).id();
    ok(
        s.reschedule(student.id(), b.id(), nextSlot).status().equals("PENDING"),
        "Reschedule resets confirmation");
    ok(s.slots(own.id(), true).stream().anyMatch(x -> x.id() == sid), "Old slot released");
    s.changeStatus(tutor.id(), b.id(), "REJECTED");
    s.deleteBooking(student.id(), b.id());
    ok(
        s.bookings(student.id()).stream().noneMatch(x -> x.id() == b.id()),
        "Delete rejected booking");
    LocalDateTime start = s.now().plusDays(10);
    var slot = s.saveSlot(tutor.id(), own.id(), null, start, start.plusHours(1));
    rejects(
        409,
        () -> s.saveSlot(tutor.id(), own.id(), null, start.plusMinutes(15), start.plusHours(2)));
    rejects(
        400,
        () ->
            s.saveSlot(
                tutor.id(),
                own.id(),
                null,
                start.plusDays(1),
                start.plusDays(1).plusHours(8).plusMinutes(1)));
    var edit =
        s.saveSlot(
            tutor.id(), own.id(), slot.id(), start.plusDays(1), start.plusDays(1).plusHours(2));
    ok(edit.start().equals(start.plusDays(1)), "Update slot");
    s.deleteSlot(tutor.id(), slot.id());
    ok(s.slots(own.id(), false).stream().noneMatch(x -> x.id() == slot.id()), "Delete slot");
    var completed =
        s.bookings(student.id()).stream()
            .filter(x -> x.tutorId() == own.id() && x.status().equals("COMPLETED"))
            .findFirst()
            .orElseThrow();
    var review = s.reviews(own.id()).get(0);
    s.deleteReview(student.id(), review.id());
    var newReview =
        s.saveReview(
            student.id(), null, completed.id(), 4, "Helpful\nTamil lesson\twith \\ examples.");
    s.saveReview(student.id(), newReview.id(), completed.id(), 5, "Updated review");
    rejects(403, () -> s.deleteReview(other.id(), newReview.id()));
    rejects(409, () -> s.saveReview(student.id(), null, completed.id(), 3, "Duplicate"));
    s.updateAccount(other.id(), "Name\tWith\nNewline\\", other.email(), "");
    TutorService reload = new TutorService(repo, clock);
    ok(reload.me(other.id()).name().equals("Name\tWith\nNewline\\"), "Escaped file round trip");
    ok(reload.reviews(own.id()).get(0).rating() == 5, "Review persisted");
    ok(!Files.readString(repo.file()).contains("Demo123!"), "No plaintext passwords");
    long raceSlot = slots.get(2).id();
    ExecutorService pool = Executors.newFixedThreadPool(2);
    CountDownLatch gate = new CountDownLatch(1);
    List<Future<Boolean>> futures = new ArrayList<>();
    for (long id : new long[] {student.id(), other.id()})
      futures.add(
          pool.submit(
              () -> {
                gate.await();
                try {
                  s.createBooking(id, raceSlot);
                  return true;
                } catch (AppException x) {
                  return false;
                }
              }));
    gate.countDown();
    int successes = 0;
    for (var f : futures) if (f.get()) successes++;
    pool.shutdown();
    ok(successes == 1, "Concurrent double booking rejected");
    var teach2 = s.register("New tutor", "newtutor@test.com", "Password1", "TUTOR");
    var profile =
        s.saveTutor(
            teach2.id(),
            null,
            "Biology",
            "BSc",
            "Galle",
            "Teaching biology",
            new BigDecimal("1200"),
            2);
    ok(s.search("Biology", "", null).size() == 1, "Tutor BST insertion");
    s.saveTutor(
        teach2.id(),
        profile.id(),
        "Science",
        "BSc",
        "Galle",
        "Updated bio",
        new BigDecimal("1300"),
        3);
    ok(s.detail(profile.id()).subject().equals("Science"), "Tutor update");
    rejects(403, () -> s.deleteTutor(tutor.id(), profile.id()));
    s.deleteTutor(teach2.id(), profile.id());
    ok(s.search("", "", profile.id()).isEmpty(), "Tutor BST deletion");
    s.deleteAccount(teach2.id(), teach2.id());
    rejects(401, () -> s.login("newtutor@test.com", "Password1"));
    State initial = repo.load();
    StateRepository broken =
        new StateRepository() {
          public State load() {
            return initial;
          }

          public void save(State state) {
            throw new IllegalStateException("Disk full");
          }
        };
    TutorService fail = new TutorService(broken, clock);
    String oldName = fail.me(student.id()).name();
    try {
      fail.updateAccount(student.id(), "Not persisted", student.email(), "");
      throw new AssertionError("Expected disk failure");
    } catch (IllegalStateException expected) {
    }
    ok(fail.me(student.id()).name().equals(oldName), "Failed disk save leaves memory unchanged");
    // Advance clock to exercise completion and review eligibility through real transitions.
    var futureBooking = s.createBooking(student.id(), slots.get(4).id());
    s.changeStatus(tutor.id(), futureBooking.id(), "CONFIRMED");
    TutorService later = new TutorService(repo, Clock.offset(clock, Duration.ofDays(7)));
    ok(
        later
            .changeStatus(tutor.id(), futureBooking.id(), "COMPLETED")
            .status()
            .equals("COMPLETED"),
        "Complete after session end");
    ok(
        later.saveReview(student.id(), null, futureBooking.id(), 5, "Great lesson").rating() == 5,
        "Review completed session");
    Files.writeString(dir.resolve("state.txt"), "corrupt");
    try {
      new FileStateRepository(dir).load();
      throw new AssertionError("Should reject corrupt data");
    } catch (IllegalStateException expected) {
      ok(true, "Corrupt file rejected without overwrite");
    }
    System.out.println(
        "PASS: "
            + checks
            + " core checks (BST, Merge Sort, CRUD, authorization, persistence, race protection,"
            + " rollback).");
  }
}
