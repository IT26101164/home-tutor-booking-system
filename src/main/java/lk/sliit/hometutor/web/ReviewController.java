package lk.sliit.hometutor.web;

import static lk.sliit.hometutor.web.SessionUser.uid;

import jakarta.servlet.http.*;
import java.util.*;
import lk.sliit.hometutor.service.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ReviewController {
  private final ReviewService service;

  public ReviewController(ReviewService service) {
    this.service = service;
  }

  public record ReviewInput(long bookingId, int rating, String comment) {}

  @GetMapping("/tutors/{id}/reviews")
  public Object reviews(@PathVariable long id) {
    return service.reviews(id);
  }

  @PostMapping("/reviews")
  public Object review(@RequestBody ReviewInput i, HttpServletRequest r) {
    return service.saveReview(uid(r), null, i.bookingId(), i.rating(), i.comment());
  }

  @PutMapping("/reviews/{id}")
  public Object editReview(
      @PathVariable long id, @RequestBody ReviewInput i, HttpServletRequest r) {
    return service.saveReview(uid(r), id, i.bookingId(), i.rating(), i.comment());
  }

  @DeleteMapping("/reviews/{id}")
  public void deleteReview(@PathVariable long id, HttpServletRequest r) {
    service.deleteReview(uid(r), id);
  }
}
