package org.jdbdt;

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
  private final static char[] HEX_CHARS 
     = "0123456789abcdef".toCharArray();

  /**
   * Convert byte array to a "hexa"-string.
   * @param data Byte array to convert.
   * @return "Hexa-string" representation.
   */
  static String toHexString(byte[] data) {
    char[] chArray = new char[data.length * 2];
    int pos = 0;
    for (byte b : data) {
      chArray[pos++] = HEX_CHARS[(b & 0xf0) >> 4];
      chArray[pos++] = HEX_CHARS[(b & 0x0f)];
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
      throw new IllegalArgumentException("Hex-string has odd length!");
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
}
