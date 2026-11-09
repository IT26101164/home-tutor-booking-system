package lk.sliit.hometutor.service;

import java.security.*;
import java.util.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordHasher {
  private static final int ITERATIONS = 210000;

  public static String hash(String password) {
    byte[] salt = new byte[16];
    new SecureRandom().nextBytes(salt);
    return ITERATIONS
        + ":"
        + Base64.getEncoder().encodeToString(salt)
        + ":"
        + Base64.getEncoder().encodeToString(derive(password, salt, ITERATIONS));
  }

  public static boolean matches(String password, String encoded) {
    try {
      String[] a = encoded.split(":");
      return MessageDigest.isEqual(
          Base64.getDecoder().decode(a[2]),
          derive(password, Base64.getDecoder().decode(a[1]), Integer.parseInt(a[0])));
    } catch (RuntimeException e) {
      return false;
    }
  }

  private static byte[] derive(String p, byte[] salt, int iterations) {
    PBEKeySpec spec = new PBEKeySpec(p.toCharArray(), salt, iterations, 256);
    try {
      return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(e);
    } finally {
      spec.clearPassword();
    }
  }
}
