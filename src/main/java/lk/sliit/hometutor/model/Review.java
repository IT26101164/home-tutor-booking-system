package lk.sliit.hometutor.model;

public record Review(
    long id, long studentId, long tutorId, long bookingId, int rating, String comment) {}
