package org.jdbdt;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Utility class grouping miscellaneous functionality.
 * 
 * @since 0.4
 *
 */
final class Misc {

  /** Private constructor to prevent instantiation. */
  private Misc() { } 

  @SuppressWarnings("javadoc")
  private final static char[] HEX_CHARS = "0123456789abcdef".toCharArray();



  /**
   * Convert byte array to a "hexa"-string.
   * @param data Byte array to convert.
   * @return "Hexa-string" representation.
   */
  static String toHexString(byte[] data) {
    char[] chArray = new char[data.length * 2];
    int pos = 0;
    for (byte b : data) {
      chArray[pos++] = HEX_CHARS[(b >> 4) & 0x0f];
      chArray[pos++] = HEX_CHARS[b & 0x0f];
    }
    return new String(chArray);
  }

  /**
   * Convert a "hexa"-string to a byte array.
   * @param str Input string.
   * @return Corresponding array of bytes.
   */
  static byte[] fromHexString(String str) {
    if (str.length() % 2 != 0) {
      throw new InvalidOperationException("Hex-string has odd length!");
    }
    byte[] data = new byte[str.length() / 2];
    int spos = 0;
    for (int pos = 0; pos < data.length; pos++) {
      int d1 = Character.digit(str.charAt(spos++), 16);
      int d2 = Character.digit(str.charAt(spos++), 16);
      if (d1 < 0 || d2 < 0) {
        throw new InvalidOperationException("Mal-formed hex-string!");
      }
      data[pos] = (byte) ((d1 << 4) | d2);
    }
    return data;
  }

  /** Thread-local handle for checksum handle. */
  private static final ThreadLocal<MessageDigest> SHA1_DIGEST_TL = new ThreadLocal<>();

  /** SHA-1 digest constant. */
  private static final String SHA1_DIGEST = "SHA-1";

  /**
   * Compute SHA-1 hash value for a given input stream.
   * @param in Input stream
   * @return SHA-1 hash value (array of 20 bytes).
   */
  static byte[] sha1(InputStream in) {
    try {
      MessageDigest md = SHA1_DIGEST_TL.get();
      if (md == null) {
        md = MessageDigest.getInstance(SHA1_DIGEST);        
        SHA1_DIGEST_TL.set(md);
      }
      md.reset();
      byte[] buffer = new byte[4096];
      int n;
      while ( (n = in.read(buffer)) > 0) {
        md.update(buffer, 0, n);
      }
      return md.digest();
    }
    catch(NoSuchAlgorithmException | IOException e) {
      throw new InternalAPIError(e);
    }
  }
}
