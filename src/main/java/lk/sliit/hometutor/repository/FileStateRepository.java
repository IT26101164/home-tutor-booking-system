<<<<<<< Updated upstream
package lk.sliit.hometutor.repository;

import java.io.*;
import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import lk.sliit.hometutor.model.*;

/** One UTF-8 TSV snapshot makes each business operation a single atomic file replacement. */
public final class FileStateRepository implements StateRepository {
  private final Path file;

  public FileStateRepository(Path directory) {
    file = directory.resolve("state.txt");
  }

  public Path file() {
    return file;
  }

  private static String esc(Object value) {
    return String.valueOf(value)
        .replace("\\", "\\\\")
        .replace("\t", "\\t")
        .replace("\n", "\\n")
        .replace("\r", "\\r");
  }

  private static String unesc(String s) {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '\\') {
        if (++i == s.length()) throw new IllegalArgumentException("Broken escape");
        char n = s.charAt(i);
        b.append(
            switch (n) {
              case 't' -> '\t';
              case 'n' -> '\n';
              case 'r' -> '\r';
              case '\\' -> '\\';
              default -> throw new IllegalArgumentException("Bad escape");
            });
      } else b.append(c);
    }
    return b.toString();
  }

  private static void row(StringBuilder b, Object... a) {
    for (int i = 0; i < a.length; i++) {
      if (i > 0) b.append('\t');
      b.append(esc(a[i]));
    }
    b.append('\n');
  }

  @Override
  public State load() {
    if (!Files.exists(file)) return new State();
    State s = new State();
    try {
      List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
      if (lines.isEmpty() || !lines.get(0).equals("HOME_TUTOR_V1"))
        throw new IllegalArgumentException("Invalid file header");
      for (String line : lines.subList(1, lines.size())) {
        if (line.isBlank()) continue;
        String[] a =
            Arrays.stream(line.split("\t", -1))
                .map(FileStateRepository::unesc)
                .toArray(String[]::new);
        switch (a[0]) {
          case "META" -> s.nextId = Long.parseLong(a[1]);
          case "USER" -> {
            long id = Long.parseLong(a[1]);
            s.users.put(id, User.create(id, a[2], a[3], a[4], Boolean.parseBoolean(a[5]), a[6]));
          }
          case "TUTOR" -> {
            long id = Long.parseLong(a[1]);
            s.tutors.put(
                id,
                new Tutor(
                    id,
                    Long.parseLong(a[2]),
                    a[3],
                    a[4],
                    a[5],
                    a[6],
                    new BigDecimal(a[7]),
                    Integer.parseInt(a[8])));
          }
          case "SLOT" -> {
            long id = Long.parseLong(a[1]);
            s.slots.put(
                id,
                new Slot(
                    id,
                    Long.parseLong(a[2]),
                    LocalDateTime.parse(a[3]),
                    LocalDateTime.parse(a[4])));
          }
          case "BOOKING" -> {
            long id = Long.parseLong(a[1]);
            s.bookings.put(
                id,
                new Booking(
                    id,
                    Long.parseLong(a[2]),
                    Long.parseLong(a[3]),
                    Long.parseLong(a[4]),
                    Booking.Status.valueOf(a[5]),
                    new BigDecimal(a[6])));
          }
          case "REVIEW" -> {
            long id = Long.parseLong(a[1]);
            s.reviews.put(
                id,
                new Review(
                    id,
                    Long.parseLong(a[2]),
                    Long.parseLong(a[3]),
                    Long.parseLong(a[4]),
                    Integer.parseInt(a[5]),
                    a[6]));
          }
          default -> throw new IllegalArgumentException("Unknown row type");
        }
      }
      return s;
    } catch (IOException | RuntimeException e) {
      throw new IllegalStateException(
          "Cannot read data file; restore a valid backup. Existing data was not overwritten.", e);
    }
  }

  @Override
  public void save(State s) {
    StringBuilder b = new StringBuilder("HOME_TUTOR_V1\n");
    row(b, "META", s.nextId);
    for (User u : s.users.values())
      row(
          b,
          "USER",
          u.getId(),
          u.getName(),
          u.getEmail(),
          u.getPasswordHash(),
          u.isActive(),
          u.getRole());
    for (Tutor t : s.tutors.values())
      row(
          b,
          "TUTOR",
          t.id(),
          t.userId(),
          t.subject(),
          t.qualification(),
          t.location(),
          t.bio(),
          t.hourlyFee(),
          t.experienceYears());
    for (Slot x : s.slots.values()) row(b, "SLOT", x.id(), x.tutorId(), x.start(), x.end());
    for (Booking x : s.bookings.values())
      row(b, "BOOKING", x.id(), x.studentId(), x.tutorId(), x.slotId(), x.status(), x.totalFee());
    for (Review x : s.reviews.values())
      row(b, "REVIEW", x.id(), x.studentId(), x.tutorId(), x.bookingId(), x.rating(), x.comment());
    Path tmp = null;
    try {
      Files.createDirectories(file.getParent());
      tmp = Files.createTempFile(file.getParent(), "state-", ".tmp");
      Files.writeString(tmp, b, StandardCharsets.UTF_8);
      try (FileChannel c = FileChannel.open(tmp, StandardOpenOption.WRITE)) {
        c.force(true);
      }
      Files.move(tmp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new IllegalStateException("Data could not be saved. No success was committed.", e);
    } finally {
      if (tmp != null)
        try {
          Files.deleteIfExists(tmp);
        } catch (IOException ignored) {
        }
    }
  }
}

=======
package lk.sliit.hometutor.repository;

import java.io.*;
import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import lk.sliit.hometutor.model.*;

/** One UTF-8 TSV snapshot makes each business operation a single atomic file replacement. */
public final class FileStateRepository implements StateRepository {
  private final Path file;

  public FileStateRepository(Path directory) {
    file = directory.resolve("state.txt");
  }

  public Path file() {
    return file;
  }

  private static String esc(Object value) {
    return String.valueOf(value)
        .replace("\\", "\\\\")
        .replace("\t", "\\t")
        .replace("\n", "\\n")
        .replace("\r", "\\r");
  }

  private static String unesc(String s) {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '\\') {
        if (++i == s.length()) throw new IllegalArgumentException("Broken escape");
        char n = s.charAt(i);
        b.append(
            switch (n) {
              case 't' -> '\t';
              case 'n' -> '\n';
              case 'r' -> '\r';
              case '\\' -> '\\';
              default -> throw new IllegalArgumentException("Bad escape");
            });
      } else b.append(c);
    }
    return b.toString();
  }

  private static void row(StringBuilder b, Object... a) {
    for (int i = 0; i < a.length; i++) {
      if (i > 0) b.append('\t');
      b.append(esc(a[i]));
    }
    b.append('\n');
  }

  @Override
  public State load() {
    if (!Files.exists(file)) return new State();
    State s = new State();
    try {
      List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
      if (lines.isEmpty() || !lines.get(0).equals("HOME_TUTOR_V1"))
        throw new IllegalArgumentException("Invalid file header");
      for (String line : lines.subList(1, lines.size())) {
        if (line.isBlank()) continue;
        String[] a =
            Arrays.stream(line.split("\t", -1))
                .map(FileStateRepository::unesc)
                .toArray(String[]::new);
        switch (a[0]) {
          case "META" -> s.nextId = Long.parseLong(a[1]);
          case "USER" -> {
            long id = Long.parseLong(a[1]);
            s.users.put(id, User.create(id, a[2], a[3], a[4], Boolean.parseBoolean(a[5]), a[6]));
          }
          case "TUTOR" -> {
            long id = Long.parseLong(a[1]);
            s.tutors.put(
                id,
                new Tutor(
                    id,
                    Long.parseLong(a[2]),
                    a[3],
                    a[4],
                    a[5],
                    a[6],
                    new BigDecimal(a[7]),
                    Integer.parseInt(a[8])));
          }
          case "SLOT" -> {
            long id = Long.parseLong(a[1]);
            s.slots.put(
                id,
                new Slot(
                    id,
                    Long.parseLong(a[2]),
                    LocalDateTime.parse(a[3]),
                    LocalDateTime.parse(a[4])));
          }
          case "BOOKING" -> {
            long id = Long.parseLong(a[1]);
            s.bookings.put(
                id,
                new Booking(
                    id,
                    Long.parseLong(a[2]),
                    Long.parseLong(a[3]),
                    Long.parseLong(a[4]),
                    Booking.Status.valueOf(a[5]),
                    new BigDecimal(a[6])));
          }
          case "REVIEW" -> {
            long id = Long.parseLong(a[1]);
            s.reviews.put(
                id,
                new Review(
                    id,
                    Long.parseLong(a[2]),
                    Long.parseLong(a[3]),
                    Long.parseLong(a[4]),
                    Integer.parseInt(a[5]),
                    a[6]));
          }
          default -> throw new IllegalArgumentException("Unknown row type");
        }
      }
      return s;
    } catch (IOException | RuntimeException e) {
      throw new IllegalStateException(
          "Cannot read data file; restore a valid backup. Existing data was not overwritten.", e);
    }
  }

  @Override
  public void save(State s) {
    StringBuilder b = new StringBuilder("HOME_TUTOR_V1\n");
    row(b, "META", s.nextId);
    for (User u : s.users.values())
      row(
          b,
          "USER",
          u.getId(),
          u.getName(),
          u.getEmail(),
          u.getPasswordHash(),
          u.isActive(),
          u.getRole());
    for (Tutor t : s.tutors.values())
      row(
          b,
          "TUTOR",
          t.id(),
          t.userId(),
          t.subject(),
          t.qualification(),
          t.location(),
          t.bio(),
          t.hourlyFee(),
          t.experienceYears());
    for (Slot x : s.slots.values()) row(b, "SLOT", x.id(), x.tutorId(), x.start(), x.end());
    for (Booking x : s.bookings.values())
      row(b, "BOOKING", x.id(), x.studentId(), x.tutorId(), x.slotId(), x.status(), x.totalFee());
    for (Review x : s.reviews.values())
      row(b, "REVIEW", x.id(), x.studentId(), x.tutorId(), x.bookingId(), x.rating(), x.comment());
    Path tmp = null;
    try {
      Files.createDirectories(file.getParent());
      tmp = Files.createTempFile(file.getParent(), "state-", ".tmp");
      Files.writeString(tmp, b, StandardCharsets.UTF_8);
      try (FileChannel c = FileChannel.open(tmp, StandardOpenOption.WRITE)) {
        c.force(true);
      }
      Files.move(tmp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new IllegalStateException("Data could not be saved. No success was committed.", e);
    } finally {
      if (tmp != null)
        try {
          Files.deleteIfExists(tmp);
        } catch (IOException ignored) {
        }
    }
  }
}

>>>>>>> Stashed changes
