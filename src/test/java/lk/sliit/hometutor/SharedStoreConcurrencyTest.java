package lk.sliit.hometutor;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.*;
import java.time.*;
import java.util.concurrent.*;
import lk.sliit.hometutor.repository.*;
import lk.sliit.hometutor.service.*;
import org.junit.jupiter.api.Test;

class SharedStoreConcurrencyTest {
  @Test
  void separateServiceInstancesShareOneBookingLock() throws Exception {
    ApplicationStore store =
        new ApplicationStore(
            new FileStateRepository(Files.createTempDirectory("shared-lock-")),
            Clock.fixed(Instant.parse("2026-10-06T00:00:00Z"), ZoneId.of("Asia/Colombo")));
    new DemoData(store).seedDemo();
    AccountService accounts = new AccountService(store);
    long first = accounts.login("student@hometutor.test", "Demo123!").id();
    long second = accounts.register("Second", "second@lock.test", "Password1", "STUDENT").id();
    long tid = new TutorProfileService(store).search("Mathematics", "Colombo", null).get(0).id();
    long sid = new AvailabilityService(store).slots(tid, true).get(0).id();
    BookingService a = new BookingService(store), b = new BookingService(store);
    ExecutorService pool = Executors.newFixedThreadPool(2);
    CountDownLatch gate = new CountDownLatch(1);
    try {
      Callable<Boolean> one =
          () -> {
            gate.await();
            try {
              a.createBooking(first, sid);
              return true;
            } catch (AppException e) {
              assertEquals(409, e.status());
              return false;
            }
          };
      Callable<Boolean> two =
          () -> {
            gate.await();
            try {
              b.createBooking(second, sid);
              return true;
            } catch (AppException e) {
              assertEquals(409, e.status());
              return false;
            }
          };
      var x = pool.submit(one);
      var y = pool.submit(two);
      gate.countDown();
      assertNotEquals(x.get(5, TimeUnit.SECONDS), y.get(5, TimeUnit.SECONDS));
    } finally {
      pool.shutdownNow();
    }
  }
}
