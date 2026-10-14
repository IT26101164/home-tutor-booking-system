package lk.sliit.hometutor.web;

import static lk.sliit.hometutor.web.SessionUser.uid;

import jakarta.servlet.http.*;
import java.util.*;
import lk.sliit.hometutor.service.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BookingController {
  private final BookingService service;

  public BookingController(BookingService service) {
    this.service = service;
  }

  public record BookingInput(long slotId) {}

  public record StatusInput(String status) {}

  @GetMapping("/bookings")
  public Object bookings(HttpServletRequest r) {
    return service.bookings(uid(r));
  }

  @PostMapping("/bookings")
  public Object book(@RequestBody BookingInput i, HttpServletRequest r) {
    return service.createBooking(uid(r), i.slotId());
  }

  @PutMapping("/bookings/{id}/status")
  public Object status(@PathVariable long id, @RequestBody StatusInput i, HttpServletRequest r) {
    return service.changeStatus(uid(r), id, i.status());
  }

  @PutMapping("/bookings/{id}/reschedule")
  public Object reschedule(
      @PathVariable long id, @RequestBody BookingInput i, HttpServletRequest r) {
    return service.reschedule(uid(r), id, i.slotId());
  }

  @DeleteMapping("/bookings/{id}")
  public void deleteBooking(@PathVariable long id, HttpServletRequest r) {
    service.deleteBooking(uid(r), id);
  }
}

