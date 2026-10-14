package lk.sliit.hometutor.web;

import static lk.sliit.hometutor.web.SessionUser.uid;

import jakarta.servlet.http.*;
import java.math.BigDecimal;
import java.util.*;
import lk.sliit.hometutor.service.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TutorController {
  private final TutorProfileService service;

  public TutorController(TutorProfileService service) {
    this.service = service;
  }

  public record TutorInput(
      String subject,
      String qualification,
      String location,
      String bio,
      BigDecimal hourlyFee,
      int experienceYears) {}

  @GetMapping("/tutors")
  public Object tutors(
      @RequestParam(defaultValue = "") String subject,
      @RequestParam(defaultValue = "") String location,
      @RequestParam(required = false) Long id) {
    return service.search(subject, location, id);
  }

  @GetMapping("/tutors/mine")
  public Object mine(HttpServletRequest r) {
    return service.myTutor(uid(r));
  }

  @GetMapping("/tutors/{id}")
  public Object tutor(@PathVariable long id) {
    return service.detail(id);
  }

  @PostMapping("/tutors")
  public Object createTutor(@RequestBody TutorInput i, HttpServletRequest r) {
    return service.saveTutor(
        uid(r),
        null,
        i.subject(),
        i.qualification(),
        i.location(),
        i.bio(),
        i.hourlyFee(),
        i.experienceYears());
  }

  @PutMapping("/tutors/{id}")
  public Object editTutor(@PathVariable long id, @RequestBody TutorInput i, HttpServletRequest r) {
    return service.saveTutor(
        uid(r),
        id,
        i.subject(),
        i.qualification(),
        i.location(),
        i.bio(),
        i.hourlyFee(),
        i.experienceYears());
  }

  @DeleteMapping("/tutors/{id}")
  public void deleteTutor(@PathVariable long id, HttpServletRequest r) {
    service.deleteTutor(uid(r), id);
  }
}
