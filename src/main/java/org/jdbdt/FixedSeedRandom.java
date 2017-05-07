package org.jdbdt;

import java.util.Random;

/**
 * A subclass of {@link java.util.Random} that inhibits re-seeding.
 * 
 * <p>
 * The only difference to {@link java.util.Random} is that a call to {@link #setSeed(long)} 
 * will result in an {@link InvalidOperationException} being thrown.
 * The seed can only be specified once at construction time.
 * </p>
 * 
 * @since 0.5
 */
final class FixedSeedRandom extends Random {
  
  /**
   * Flag indicating if seed was set.
   */
  private boolean seedIsSet;
  
  /**
   * Constructor.
   * @param seed Initial seed.
   */
  FixedSeedRandom(long seed) {
    super(seed);
  }
  
  /**
   * Always throws {@link InvalidOperationException} when called, 
   * overriding the behavior of {@link java.util.Random}.
   * 
   * @throws InvalidOperationException in all cases when called.
   */
  @Override
  public void setSeed(long seed) throws InvalidOperationException {
    if (!seedIsSet) {
      super.setSeed(seed); // called from the constructor of java.util.Random
      seedIsSet = true;
    } else {
      throw new InvalidOperationException("For repeatable tests do not reset the seed.");
    }
  }
  
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;
}
