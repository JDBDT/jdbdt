package org.jdbdt;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MiscTest {

  static final byte[] ZBA = { };
  static final String ZSTR = "";
  
  @Test
  public void testToHexString1() {
    long value = 0xa0_0a_1c_c1_19_91_f9_9fL;
    byte[] data = ByteBuffer.allocate(8).putLong(value).array();
    String hs = Misc.toHexString(data);
    assertEquals(Long.toHexString(value), hs);
  }
  
  @Test
  public void testToHexString2() {
    assertEquals(ZSTR, Misc.toHexString(ZBA));
  }
  
  @Test
  public void testFromHexString1() {
    long value = 0xa0_0a_1c_c1_19_91_f9_9fL;
    byte[] expected = ByteBuffer.allocate(8).putLong(value).array();
    byte[] actual = Misc.fromHexString(Long.toHexString(value));
    assertArrayEquals(expected, actual);
  }
  
  @Test
  public void testFromHexString2() {
    long value = 0xa0_0a_1c_c1_19_91_f9_9fL;
    byte[] expected = ByteBuffer.allocate(8).putLong(value).array();
    byte[] actual = Misc.fromHexString(Long.toHexString(value).toUpperCase());
    assertArrayEquals(expected, actual);
  }
  
  @Test
  public void testFromHexString3() {
    assertArrayEquals(ZBA, Misc.fromHexString(ZSTR));
  }
  
  @Test(expected=InvalidOperationException.class)
  public void testFromHexString4() {
    Misc.fromHexString("a1f");
  }
  
  @Test(expected=InvalidOperationException.class)
  public void testFromHexString5() {
    Misc.fromHexString("a1fg");
  }
  
  @Test(expected=InvalidOperationException.class)
  public void testFromHexString6() {
    Misc.fromHexString("a1f/");
  }
  
  @Test
  public void testSHA1EmptyInput() {
    byte[] expected = Misc.fromHexString("da39a3ee5e6b4b0d3255bfef95601890afd80709");
    byte[] actual = Misc.sha1(new ByteArrayInputStream(new byte[0]));
    assertArrayEquals(expected, actual);
  }
  
  @Test (expected=InternalAPIError.class)
  public void testSHA1WithFileReadError() throws IOException {
    InputStream in = mock(InputStream.class);
    when(in.read(any())).thenThrow(new IOException());
    Misc.sha1(in);
  }
}
