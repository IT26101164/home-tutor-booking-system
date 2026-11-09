package lk.sliit.hometutor.model;

import java.time.LocalDateTime;

public record Slot(long id, long tutorId, LocalDateTime start, LocalDateTime end) {}
