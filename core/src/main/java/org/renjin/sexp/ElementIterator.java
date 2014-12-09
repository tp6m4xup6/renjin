package org.renjin.sexp;

/**
 * An iterator over an {@code AtomicVector}'s elements.
 *
 */
public interface ElementIterator {

  boolean hasNext();

  /**
   *
   * @return the next element as an {@code int}
   */
  int nextInt();

  /**
   *
   * @return the next element as a {@code double}
   */
  double nextDouble();

  /**
   *
   * @return the next element as a {@code String}
   */
  String nextString();

  /**
   *
   * @return the next element as a logical, encoded as an {@code int}, where
   * {@code TRUE} = 1, {@code FALSE} = 0, and {@code NA} = {@link IntVector#NA}
   */
  int nextLogical();

}
