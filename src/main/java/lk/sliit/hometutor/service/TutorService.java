<<<<<<< Updated upstream
package lk.sliit.hometutor.service;

import java.math.*;
import java.time.*;
import java.util.*;
import lk.sliit.hometutor.algorithm.*;
import lk.sliit.hometutor.model.*;
import lk.sliit.hometutor.repository.*;
import lk.sliit.hometutor.service.Views.*;

/** Composition root and test facade. Business rules live in the six module services. */
public final class TutorService {
  private final ApplicationStore store;
  private final AccountService accounts;

  public AccountService accounts() {
    return accounts;
  }

  private final TutorProfileService profiles;

  public TutorProfileService profiles() {
    return profiles;
  }

  private final AvailabilityService availability;

  public AvailabilityService availability() {
    return availability;
  }

  private final BookingService bookingsModule;

  public BookingService bookingsModule() {
    return bookingsModule;
  }

  private final ReviewService reviewsModule;

  public ReviewService reviewsModule() {
    return reviewsModule;
  }

  private final AdminService admin;

  public AdminService admin() {
    return admin;
  }

  private final DemoData demo;

  public DemoData demo() {
    return demo;
  }

  public TutorService(StateRepository repository, Clock clock) {
    store = new ApplicationStore(repository, clock);
    accounts = new AccountService(store);
    profiles = new TutorProfileService(store);
    availability = new AvailabilityService(store);
    bookingsModule = new BookingService(store);
    reviewsModule = new ReviewService(store);
    admin = new AdminService(store);
    demo = new DemoData(store);
  }

  public LocalDateTime now() {
    return store.now();
  }

  public UserView me(long id) {
    return accounts.me(id);
  }

  public UserView register(String name, String email, String pass, String role) {
    return accounts.register(name, email, pass, role);
  }

  public UserView login(String email, String pass) {
    return accounts.login(email, pass);
  }

  public UserView updateAccount(long uid, String name, String email, String newPassword) {
    return accounts.updateAccount(uid, name, email, newPassword);
  }

  public void deleteAccount(long actorId, long targetId) {
    accounts.deleteAccount(actorId, targetId);
  }

  public List<TutorView> search(String subject, String location, Long id) {
    return profiles.search(subject, location, id);
  }

  public TutorView detail(long id) {
    return profiles.detail(id);
  }

  public TutorView myTutor(long uid) {
    return profiles.myTutor(uid);
  }

  public TutorView saveTutor(
      long uid,
      Long id,
      String subject,
      String qualification,
      String location,
      String bio,
      BigDecimal fee,
      int experience) {
    return profiles.saveTutor(uid, id, subject, qualification, location, bio, fee, experience);
  }

  public void deleteTutor(long uid, long id) {
    profiles.deleteTutor(uid, id);
  }

  public List<SlotView> slots(long tid, boolean onlyAvailable) {
    return availability.slots(tid, onlyAvailable);
  }

  public SlotView saveSlot(long uid, long tid, Long id, LocalDateTime start, LocalDateTime end) {
    return availability.saveSlot(uid, tid, id, start, end);
  }

  public void deleteSlot(long uid, long id) {
    availability.deleteSlot(uid, id);
  }

  public BookingView createBooking(long uid, long slotId) {
    return bookingsModule.createBooking(uid, slotId);
  }

  public List<BookingView> bookings(long uid) {
    return bookingsModule.bookings(uid);
  }

  public BookingView changeStatus(long uid, long id, String target) {
    return bookingsModule.changeStatus(uid, id, target);
  }

  public BookingView reschedule(long uid, long id, long slotId) {
    return bookingsModule.reschedule(uid, id, slotId);
  }

  public void deleteBooking(long uid, long id) {
    bookingsModule.deleteBooking(uid, id);
  }

  public List<ReviewView> reviews(long tid) {
    return reviewsModule.reviews(tid);
  }

  public Review saveReview(long uid, Long id, long bid, int rating, String comment) {
    return reviewsModule.saveReview(uid, id, bid, rating, comment);
  }

  public void deleteReview(long uid, long id) {
    reviewsModule.deleteReview(uid, id);
  }

  public List<UserView> users(long uid) {
    return admin.users(uid);
  }

  public Map<String, Long> stats(long uid) {
    return admin.stats(uid);
  }

  public void seedDemo() {
    demo.seedDemo();
  }
}
=======
package lk.sliit.hometutor.service;

import java.math.*;
import java.time.*;
import java.util.*;
import lk.sliit.hometutor.algorithm.*;
import lk.sliit.hometutor.model.*;
import lk.sliit.hometutor.repository.*;
import lk.sliit.hometutor.service.Views.*;

/** Composition root and test facade. Business rules live in the six module services. */
public final class TutorService {
  private final ApplicationStore store;
  private final AccountService accounts;

  public AccountService accounts() {
    return accounts;
  }

  private final TutorProfileService profiles;

  public TutorProfileService profiles() {
    return profiles;
  }

  private final AvailabilityService availability;

  public AvailabilityService availability() {
    return availability;
  }

  private final BookingService bookingsModule;

  public BookingService bookingsModule() {
    return bookingsModule;
  }

  private final ReviewService reviewsModule;

  public ReviewService reviewsModule() {
    return reviewsModule;
  }

  private final AdminService admin;

  public AdminService admin() {
    return admin;
  }

  private final DemoData demo;

  public DemoData demo() {
    return demo;
  }

  public TutorService(StateRepository repository, Clock clock) {
    store = new ApplicationStore(repository, clock);
    accounts = new AccountService(store);
    profiles = new TutorProfileService(store);
    availability = new AvailabilityService(store);
    bookingsModule = new BookingService(store);
    reviewsModule = new ReviewService(store);
    admin = new AdminService(store);
    demo = new DemoData(store);
  }

  public LocalDateTime now() {
    return store.now();
  }

  public UserView me(long id) {
    return accounts.me(id);
  }

  public UserView register(String name, String email, String pass, String role) {
    return accounts.register(name, email, pass, role);
  }

  public UserView login(String email, String pass) {
    return accounts.login(email, pass);
  }

  public UserView updateAccount(long uid, String name, String email, String newPassword) {
    return accounts.updateAccount(uid, name, email, newPassword);
  }

  public void deleteAccount(long actorId, long targetId) {
    accounts.deleteAccount(actorId, targetId);
  }

  public List<TutorView> search(String subject, String location, Long id) {
    return profiles.search(subject, location, id);
  }

  public TutorView detail(long id) {
    return profiles.detail(id);
  }

  public TutorView myTutor(long uid) {
    return profiles.myTutor(uid);
  }

  public TutorView saveTutor(
      long uid,
      Long id,
      String subject,
      String qualification,
      String location,
      String bio,
      BigDecimal fee,
      int experience) {
    return profiles.saveTutor(uid, id, subject, qualification, location, bio, fee, experience);
  }

  public void deleteTutor(long uid, long id) {
    profiles.deleteTutor(uid, id);
  }

  public List<SlotView> slots(long tid, boolean onlyAvailable) {
    return availability.slots(tid, onlyAvailable);
  }

  public SlotView saveSlot(long uid, long tid, Long id, LocalDateTime start, LocalDateTime end) {
    return availability.saveSlot(uid, tid, id, start, end);
  }

  public void deleteSlot(long uid, long id) {
    availability.deleteSlot(uid, id);
  }

  public BookingView createBooking(long uid, long slotId) {
    return bookingsModule.createBooking(uid, slotId);
  }

  public List<BookingView> bookings(long uid) {
    return bookingsModule.bookings(uid);
  }

  public BookingView changeStatus(long uid, long id, String target) {
    return bookingsModule.changeStatus(uid, id, target);
  }

  public BookingView reschedule(long uid, long id, long slotId) {
    return bookingsModule.reschedule(uid, id, slotId);
  }

  public void deleteBooking(long uid, long id) {
    bookingsModule.deleteBooking(uid, id);
  }

  public List<ReviewView> reviews(long tid) {
    return reviewsModule.reviews(tid);
  }

  public Review saveReview(long uid, Long id, long bid, int rating, String comment) {
    return reviewsModule.saveReview(uid, id, bid, rating, comment);
  }

  public void deleteReview(long uid, long id) {
    reviewsModule.deleteReview(uid, id);
  }

  public List<UserView> users(long uid) {
    return admin.users(uid);
  }

  public Map<String, Long> stats(long uid) {
    return admin.stats(uid);
  }

  public void seedDemo() {
    demo.seedDemo();
  }
}
>>>>>>> Stashed changes
