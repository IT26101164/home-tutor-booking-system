[README.md](https://github.com/user-attachments/files/34605625/README.md)
# HomeTutor — Six-member full-stack project

A SE1020 learning project with **Java 17, Spring Boot 3.5.0, HTML/CSS/JavaScript, an embedded H2 relational database, a custom tutor BST, and a custom Merge Sort**. A readable UTF-8 text snapshot is also exported as a coursework file-handling backup. No Node build or external frontend CDN is needed.

## Start with your member guide

Read **team/START-HERE-TA.md** first. This edition splits the application into six independently owned frontend/controller/service components while keeping one working Maven project.

| Member | Module | Frontend | Controller / Service |
|---|---|---|---|
| 1 | Accounts | accounts.js | AccountController / AccountService |
| 2 | Tutors + algorithms | tutors.js | TutorController / TutorProfileService |
| 3 | Availability | availability.js | AvailabilityController / AvailabilityService |
| 4 | Bookings | bookings.js | BookingController / BookingService |
| 5 | Reviews | reviews.js | ReviewController / ReviewService |
| 6 | Admin accounts | admin.js | AdminController / AdminService |

Each `team/member-XX-...` folder includes exact source paths, a component test and contribution guidance. Everyone clones the SAME repository and opens the WHOLE root folder. See team/GIT-WORKFLOW.md for branches, real commits, pull requests and review.

## Start in IntelliJ IDEA (Windows / macOS / Linux)

1. Extract the ZIP. Open the **home-tutor-team** folder (the folder containing `pom.xml`) in IntelliJ.
2. Select a **JDK 17 or newer** as the project SDK. A JRE alone cannot compile the source.
3. Allow Maven to download the dependencies. Use IntelliJ's bundled Maven if Maven is not installed separately.
4. Open `src/main/java/lk/sliit/hometutor/HomeTutorApplication.java` and run its `main` method.
5. Visit **http://localhost:8080** in your browser. Keep the Java application running.
6. Stop and start it again: previously saved changes remain in the local H2 database (`data/hometutor-db.mv.db`). A readable backup snapshot is exported to `data/state.txt`.

Alternatively, with Java 17+ and Maven 3.6.3+ installed:

```sh
mvn clean test package
java -jar target/home-tutor-1.0.0.jar
```

For development, run the main class from IntelliJ so your current source is used. The delivery also contains `run.bat` and `run.sh`. They use Maven when installed; otherwise open the project in IntelliJ and run the main application class. IntelliJ remains the recommended way to inspect and learn the source. After source edits, build with `mvn clean package`; run the resulting `target/home-tutor-1.0.0.jar`. The root JAR is a delivery snapshot and does not rebuild itself.

## Demonstration accounts

| Role | Email | Password |
|---|---|---|
| Student / Parent | student@hometutor.test | Demo123! |
| Tutor | tutor@hometutor.test | Demo123! |
| Administrator | admin@hometutor.test | Demo123! |
| Additional tutors | tutor2@hometutor.test through tutor6@hometutor.test | Demo123! |

All names, profiles and reviews are fictional demonstration content. Fresh installs create six tutors, future lesson slots and completed sample sessions automatically. Demo initialization runs only when the account store is empty. Dates are generated relative to first start, in **Asia/Colombo**.

If the installation is older and slots have expired, log in as a tutor and add new slots. To reset demo data, stop the server and move `data` to a backup folder before restarting. This resets all local accounts and bookings. Never do it over data you need.

## Implemented screens

Home, tutor search, tutor detail / available slots / reviews, registration, login, bookings, tutor profile management, availability management, account settings, admin dashboard and user management. Forms adapt to mobile screens and show backend error messages.

## Main features

- Student/parent and tutor self-registration; admin is seeded, never self-selected during registration.
- Session login/logout, PBKDF2 salted password hashes, CSRF protection, role and ownership checks.
- Admin-only account creation, update, deactivation and restoration; self-admin deactivation is blocked.
- Account view/update/deactivation. Deactivation is the account Delete operation; history is preserved.
- Tutor profile Create/Read/Update/Delete. A profile with booking history must instead be deactivated at account level.
- Tutor ID BST lookup; subject/location filtering via tree traversal; custom stable Merge Sort by primary subject (case-insensitive) and ID tie-break.
- Time-slot Create/Read/Update/Delete, overlap checks and past-time validation.
- Booking Create/Read, reschedule, accept/reject/cancel/complete, and deletion of rejected/cancelled records.
- Pending and confirmed bookings reserve the slot. A student cannot hold overlapping lessons.
- Completed sessions allow one review per booking; owner can edit/delete, admin can moderate by deleting.
- File saves replace one UTF-8 snapshot atomically; failed writes do not change the live state.

## Design decisions

**Storage:** the primary data store is an embedded H2 relational database. `DatabaseStateRepository` maps the shared `StateRepository` contract to six relational tables: `app_meta`, `app_users`, `tutor_profiles`, `lesson_slots`, `lesson_bookings`, and `tutor_reviews`. A Spring transaction replaces the snapshot consistently so all six modules share one commit boundary. H2 stores data persistently in `data/hometutor-db.mv.db`. After a successful database commit, `FileStateRepository` exports the same state to `data/state.txt` as a readable UTF-8 coursework backup. The text file is not the primary database.

**BST:** `tutorId` is the key. Startup rebuilds the tree around medians; subsequent add/update/delete operations call the custom tree operations after a successful file save. It is not an AVL/red-black tree. Search costs O(h), average O(log n) under suitable shape, worst O(n). Traversal and subject/location filtering cost O(n). Full snapshot writes also cost O(total stored records), so a whole persistent update is not O(log n).

**Merge Sort:** sorting means primary subject name A–Z, with ID as the tie-break. It does not claim to rank teaching quality. Each tutor has one primary subject. If your lecturer intends proficiency-based sorting or multiple subjects, revise the model and comparator accordingly.

**Money and time:** BigDecimal stores LKR fees. Booking price is snapshotted from hourly fee × slot duration / 60. Displayed lesson times are Sri Lanka local time. No payment gateway or email service is integrated.

**Cancellation/deletion:** only future active bookings can be rescheduled. Cancellation after start requires admin. Completed bookings stay as history. Slots with any booking history are protected from edits and deletion. Tutor accounts with active bookings cannot be deactivated.

## Project structure

```text
src/main/java/lk/sliit/hometutor/
  HomeTutorApplication.java
  model/        User hierarchy, Tutor, Slot, Booking, Review, State
  algorithm/    TutorBST, TutorMergeSort
  repository/   StateRepository, DatabaseStateRepository, FileStateRepository (backup export)
  service/      Six module services, ApplicationStore, ServiceSupport, Views, facade
  web/          Six module controllers, AppConfig, SessionUser, ApiErrors
src/main/resources/static/    index.html, styles.css, js/ (six modules + shared/router)
src/test/java/                core checks and HTTP integration tests
scripts/                     framework-free Java checks
sample-data/                 example file-format snapshot
docs/                        report, class/workflow diagrams, viva guide, test results
team/                        six member guides, ownership map, IntelliJ/Git workflow
```

## Configuration

| Setting | Default | Purpose |
|---|---|---|
| `PORT` | 8080 | Web server port |
| `TUTOR_DATA_DIR` | `./data` | Writable directory for text backup `state.txt` |
| `TUTOR_DB_DIR` | `./data` | Directory for persistent H2 database files |
| `TUTOR_DEMO` | true | Seed a fresh empty install with demonstration content |

Only run one application process against a data directory. A shared store lock protects simultaneous requests across all six module services inside that process; this is a single-server coursework design, not a distributed platform. H2 is embedded and file-persistent; it is not configured as a network database. Use an HTTPS reverse proxy and secure cookies before an internet deployment. Demo passwords must not be reused for real users. Login rate limiting and password recovery are outside this coursework build.

With `TUTOR_DEMO=false` on an empty directory, students/tutors can register, but no admin is auto-created. Use demo mode for the supplied viva scenario.

## Verification

```sh
mvn test
# Optional: algorithm / service / storage checks with just a JDK
sh scripts/test-core.sh
```

See `docs/TEST-RESULTS.md` for the baseline checks documented in the uploaded project. The H2 repository conversion in this edition still needs a fresh `mvn clean test` run on your computer. Do not claim a check passed unless that file records it.

## Your submission work

Read and understand your assigned component. Replace report placeholders with actual team names, student IDs, contributions and screenshots. Put the project in your team's GitHub repository and make genuine commits as you work. This package does not invent individual contributions or a commit history. Confirm any updated lecturer guidance before final submission, especially the Spring Boot interpretation of the supplied description.

Official technical reference: https://docs.spring.io/spring-boot/3.5/system-requirements.html
