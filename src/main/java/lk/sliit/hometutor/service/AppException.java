<<<<<<< Updated upstream
package lk.sliit.hometutor.service;

public final class AppException extends RuntimeException {
  private final int status;

  public AppException(int status, String message) {
    super(message);
    this.status = status;
  }

  public int status() {
    return status;
  }
}
=======
package lk.sliit.hometutor.service;

public final class AppException extends RuntimeException {
  private final int status;

  public AppException(int status, String message) {
    super(message);
    this.status = status;
  }

  public int status() {
    return status;
  }
}
>>>>>>> Stashed changes
