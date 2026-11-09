package lk.sliit.hometutor.model;

public final class Student extends User {
  public Student(long id, String name, String email, String hash, boolean active) {
    super(id, name, email, hash, active);
  }

  @Override
  public String getRole() {
    return "STUDENT";
  }
}
