/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997-2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.renjin.sexp;

import com.google.common.base.Joiner;

/**
 * A vector of {@link FunctionCall}s
 *
 */
public class ExpressionVector extends ListVector {
  public static final String TYPE_NAME = "expression";

  public static final Vector.Type VECTOR_TYPE = new VectorType();


  public ExpressionVector(SEXP[] functionCalls, AttributeMap attributes) {
    super(functionCalls, attributes);
  }

  public ExpressionVector(SEXP... functionCalls) {
    super(functionCalls);
  }

  public ExpressionVector(Iterable<SEXP> expressions, AttributeMap attributes) {
    super(expressions, attributes);
  }

  public ExpressionVector(Iterable<SEXP> expressions){
    super(expressions);
  }

  @Override
  public Builder newBuilderWithInitialSize(int initialSize) {
    return new Builder();
  }

  @Override
  public Builder newCopyBuilder() {
    return new Builder(this);
  }

  @Override
  public Type getVectorType() {
    return VECTOR_TYPE;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("expression(");
    Joiner.on(", ").appendTo(sb, this);
    return sb.append(")").toString();
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  public static class VectorType extends Vector.Type {

    public VectorType() {
      super(Order.EXPRESSION);
    }

    @Override
    public Vector.Builder newBuilder() {
      return new Builder();
    }

    @Override
    public Vector.Builder newBuilderWithInitialSize(int initialSize) {
      return new Builder(initialSize);
    }

    @Override
    public Vector.Builder newBuilderWithInitialCapacity(int initialCapacity) {
      return new Builder(0, initialCapacity);
    }

    @Override
    public String getName() {
      return TYPE_NAME;
    }

    @Override
    public Vector getElementAsVector(Vector vector, int index) {
      return new ExpressionVector(vector.getElementAsSEXP(index));
    }

    @Override
    public int compareElements(Vector vector1, int index1, Vector vector2, int index2) {
      return 0;
    }

    @Override
    public boolean elementsEqual(Vector vector1, int index1, Vector vector2, int index2) {
      return false;
    }
  }


  public static class Builder extends ListVector.Builder {
    
    public Builder() {
      super();
    }

    public Builder(int initialLength) {
      super(initialLength);
    }

    public Builder(int initialSize, int initialCapacity) {
      super(initialSize, initialCapacity);
    }

    public Builder(ListVector toClone) {
      super(toClone);
    }

    @Override
    public ExpressionVector build() {
      return new ExpressionVector(getValues(), buildAttributes());
    }
  }
}
