package org.jdbdt;

import static org.junit.Assert.*;

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
}
