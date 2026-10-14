package lk.sliit.hometutor.model;

import java.math.BigDecimal;

public record Booking(
    long id, long studentId, long tutorId, long slotId, Status status, BigDecimal totalFee) {
  public enum Status {
    PENDING,
    CONFIRMED,
    REJECTED,
    CANCELLED,
    COMPLETED
  }

  public Booking withStatus(Status value) {
    return new Booking(id, studentId, tutorId, slotId, value, totalFee);
  }

  public boolean holdsSlot() {
    return status == Status.PENDING || status == Status.CONFIRMED;
  }
}
