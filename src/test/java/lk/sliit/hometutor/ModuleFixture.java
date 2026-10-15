package lk.sliit.hometutor;

import java.nio.file.*;
import java.time.*;
import lk.sliit.hometutor.repository.*;
import lk.sliit.hometutor.service.*;

final class ModuleFixture {
  final TutorService app;
  final long student, tutor, admin, profile;

  ModuleFixture() throws Exception {
    app =
        new TutorService(
            new FileStateRepository(Files.createTempDirectory("module-test-")),
            Clock.fixed(Instant.parse("2026-10-06T00:00:00Z"), ZoneId.of("Asia/Colombo")));
    app.seedDemo();
    student = app.login("student@hometutor.test", "Demo123!").id();
    tutor = app.login("tutor@hometutor.test", "Demo123!").id();
    admin = app.login("admin@hometutor.test", "Demo123!").id();
    profile = app.myTutor(tutor).id();
  }
}
