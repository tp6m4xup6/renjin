package org.renjin.vector;

import org.renjin.sexp.ElementIterator;
import org.renjin.sexp.Vector;

/**
 * Simplest form o
 */
public interface ElementCollection {

  Vector.Type getType();

  ElementIterator elementIterator();

}
