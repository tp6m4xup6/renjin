package org.renjin.primitives.subset;

import com.google.common.collect.UnmodifiableIterator;
import org.renjin.sexp.*;

import java.util.Iterator;

/**
 * Selection of the entire source vector
 * through the use of x[]
 */
public class CompleteSelection extends Selection {

  private SEXP source;
  private AtomicVector sourceDim;
  
  public CompleteSelection(SEXP source) {
    super(source);
    this.source = source;
    this.sourceDim = (AtomicVector) source.getAttributes().getDim();
  }
  
  @Override
  public Iterator<Integer> iterator() {
    return new UnmodifiableIterator<Integer>() {
      private int i=0;
      
      @Override
      public boolean hasNext() {
        return i < source.length();
      }

      @Override
      public Integer next() {
        return i++;
      }
    };
  }

  @Override
  public int getSourceDimensions() {
    if(sourceDim.length() == 0) {
      return 1;
    } else {
      return sourceDim.length();
    }
  }

  @Override
  public int getElementCount() {
    return source.length();
  }

  private int getDimensionLength(int d) {
    if(sourceDim.length() == 0 && d == 0) {
      return source.length();
    } else {
      return sourceDim.getElementAsInt(d);
    }
  }

  @Override
  protected AtomicVector getNames(int dimensionIndex) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterable<Integer> getSelectionAlongDimension(int dimensionIndex) {
    final int length = getDimensionLength(dimensionIndex);
    return new Iterable<Integer>() {
      
      @Override
      public Iterator<Integer> iterator() {
        return new UnmodifiableIterator<Integer>() {
          private int i = 0;
          @Override
          public boolean hasNext() {
            return i < length;
          }

          @Override
          public Integer next() {
            return i++;
          }
        };
      }
    };
  }

  @Override
  public int getSelectedDimensionCount() {
    return sourceDim.length();
  }

  @Override
  public boolean isSingleElementSelectedFromDimension(int i) {
    return sourceDim.getElementAsInt(i) == 1;
  }

  @Override
  public Vector select(Vector sourceVector) {
    return (Vector) source.setAttributes(AttributeMap.EMPTY);
  }
}
