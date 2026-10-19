package lk.sliit.hometutor.model;

import java.math.BigDecimal;

/** One primary subject per tutor keeps the sorting requirement unambiguous. */
public record Tutor(
    long id,
    long userId,
    String subject,
    String qualification,
    String location,
    String bio,
    BigDecimal hourlyFee,
    int experienceYears) {}
