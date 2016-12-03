package org.jdbdt;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;

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
   * @param hexStr Input string.
   * @return Corresponding array of bytes.
   */
  static byte[] fromHexString(String hexStr) {
    if (hexStr.length() % 2 != 0) {
      throw new InvalidOperationException("Hex-string has odd length!");
    }
    byte[] data = new byte[hexStr.length() / 2];
    int spos = 0;
    for (int pos = 0; pos < data.length; pos++) {
      char c1 = hexStr.charAt(spos++);
      char c2 = hexStr.charAt(spos++);
      data[pos] = (byte) ((hexDigit(c1) << 4) | hexDigit(c2));
    }
    return data;
  }

  @SuppressWarnings("javadoc")
  private static int hexDigit(char c) {
    int d = Character.isDigit(c) ? c - '0' : 10 + Character.toLowerCase(c) - 'a';
    if (d < 0 || d > 15) {
      throw new InvalidOperationException("Invalid character: '" + c + "'");
    }
    return d;
  }

  /** Thread-local handle for checksum handle. */
  private static final ThreadLocal<MessageDigest> CHECKSUM_TL = new ThreadLocal<>();

  /** Checksum (digest algorithm to use) */
  private static final String CHECKSUM_ALGORITHM = "MD5";

  /**
   * Compute digest checksum for a given input stream.
   * @param in Input stream
   * @return Digest checksum as an array of bytes.
   */
  static byte[] checksum(InputStream in) {
    try {
      MessageDigest md = CHECKSUM_TL.get();
      if (md == null) {
        md = MessageDigest.getInstance(CHECKSUM_ALGORITHM);        
        CHECKSUM_TL.set(md);
      }
      md.reset();
      byte[] buffer = new byte[4096];
      int n;
      while ( (n = in.read(buffer, 0, buffer.length)) > 0) {
        md.update(buffer, 0, n);
      }
      return md.digest();
    }
    catch(NoSuchAlgorithmException | IOException e) {
      throw new InternalAPIError(e);
    }
  }

  /**
   * Get digest checksum for a BLOB.
   * @param blob The BLOB.
   * @return  Digest checksum as an array of bytes.
   */
  static byte[] checksum(Blob blob) {
    try {
      return checksum(blob.getBinaryStream());
    } catch (SQLException e) {
      throw new DBExecutionException(e);
    }
  }

  /**
   * Get digest checksum for a CLOB value.
   * @param clob The CLOB value.
   * @return  Digest checksum as an array of bytes.
   */
  static byte[] checksum(Clob clob) {
    try {
      return checksum(clob.getAsciiStream());
    } catch (SQLException e) {
      throw new DBExecutionException(e);
    }
  }

}
