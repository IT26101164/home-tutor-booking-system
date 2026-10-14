package lk.sliit.hometutor.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class Views {
  private Views() {}

  public record UserView(long id, String name, String email, String role, boolean active) {}

  public record TutorView(
      long id,
      long userId,
      String name,
      String subject,
      String qualification,
      String location,
      String bio,
      BigDecimal hourlyFee,
      int experienceYears,
      double rating,
      long reviewCount) {}

  public record SlotView(
      long id, long tutorId, LocalDateTime start, LocalDateTime end, boolean available) {}

  public record BookingView(
      long id,
      long studentId,
      String studentName,
      long tutorId,
      String tutorName,
      long slotId,
      LocalDateTime start,
      LocalDateTime end,
      String status,
      BigDecimal totalFee) {}

  public record ReviewView(
      long id,
      long studentId,
      String studentName,
      long tutorId,
      long bookingId,
      int rating,
      String comment) {}
}
