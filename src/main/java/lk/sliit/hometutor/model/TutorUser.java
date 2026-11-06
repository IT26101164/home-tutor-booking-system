package lk.sliit.hometutor.model;

public final class TutorUser extends User {
  public TutorUser(long id, String name, String email, String hash, boolean active) {
    super(id, name, email, hash, active);
  }

  @Override
  public String getRole() {
    return "TUTOR";
  }
}
