package lk.sliit.hometutor.model;

public final class AdminUser extends User {
    public AdminUser(long id, String name, String email, String hash, boolean active) {
        super(id, name, email, hash, active);
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }
}
