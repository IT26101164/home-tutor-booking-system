
package lk.sliit.hometutor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZoneId;

import lk.sliit.hometutor.repository.FileStateRepository;
import lk.sliit.hometutor.service.TutorService;

class ModuleFixture {

    final TutorService app;
    final long admin;
    final long student;
    final long tutor;
    final long profile;

    ModuleFixture() throws Exception {

        Path tempDir = Files.createTempDirectory("hometutor-test-");

        app = new TutorService(
                new FileStateRepository(tempDir),
                Clock.system(ZoneId.of("Asia/Colombo"))
        );

        app.seedDemo();

        admin = app.login(
                "admin@hometutor.test", "Demo123!"
        ).id();

        student = app.login(
                "student@hometutor.test", "Demo123!"
        ).id();

        tutor = app.login(
                "tutor@hometutor.test", "Demo123!"
        ).id();

        profile = app.profiles()
                .myTutor(tutor)
                .id();
    }
}
