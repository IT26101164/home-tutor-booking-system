package lk.sliit.hometutor.model;

/** Abstract parent: subclasses provide their role using runtime polymorphism. */
public abstract class User {
  private final long id;
  private final String name, email, passwordHash;
  private final boolean active;

  protected User(long id, String name, String email, String passwordHash, boolean active) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.passwordHash = passwordHash;
    this.active = active;
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public boolean isActive() {
    return active;
  }

  public abstract String getRole();

  public static User create(
      long id, String name, String email, String hash, boolean active, String role) {
    return switch (role) {
      case "STUDENT" -> new Student(id, name, email, hash, active);
      case "TUTOR" -> new TutorUser(id, name, email, hash, active);
      case "ADMIN" -> new AdminUser(id, name, email, hash, active);
      default -> throw new IllegalArgumentException("Invalid role");
    };
  }
}
