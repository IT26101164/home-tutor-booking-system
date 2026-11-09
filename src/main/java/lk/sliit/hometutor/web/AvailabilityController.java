package lk.sliit.hometutor.web;

import static lk.sliit.hometutor.web.SessionUser.uid;

import jakarta.servlet.http.*;
import java.time.LocalDateTime;
import java.util.*;
import lk.sliit.hometutor.service.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AvailabilityController {
  private final AvailabilityService service;

  public AvailabilityController(AvailabilityService service) {
    this.service = service;
  }

  public record SlotInput(LocalDateTime start, LocalDateTime end) {}

  @GetMapping("/tutors/{id}/slots")
  public Object slots(
      @PathVariable long id, @RequestParam(defaultValue = "true") boolean available) {
    return service.slots(id, available);
  }

  @PostMapping("/tutors/{id}/slots")
  public Object createSlot(@PathVariable long id, @RequestBody SlotInput i, HttpServletRequest r) {
    return service.saveSlot(uid(r), id, null, i.start(), i.end());
  }

  @PutMapping("/tutors/{tid}/slots/{sid}")
  public Object editSlot(
      @PathVariable long tid,
      @PathVariable long sid,
      @RequestBody SlotInput i,
      HttpServletRequest r) {
    return service.saveSlot(uid(r), tid, sid, i.start(), i.end());
  }

  @DeleteMapping("/slots/{id}")
  public void deleteSlot(@PathVariable long id, HttpServletRequest r) {
    service.deleteSlot(uid(r), id);
  }
}
