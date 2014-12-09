/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
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

package org.renjin.primitives.subset;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.renjin.eval.EvalException;
import org.renjin.sexp.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SubscriptOperation {

  private Vector source;

  private boolean drop = true;

  private List<SEXP> subscripts;

  private Selection selection;
  
  public SubscriptOperation() {
  }

  public SubscriptOperation setSource(SEXP source, ListVector arguments, int skipBeginning, int skipEnd) {
    if(source instanceof PairList.Node) {
      this.source = ((PairList.Node) source).toVector();
    } else if(source instanceof Vector) {
      this.source = (Vector) source;
    } else {
      throw new EvalException("Invalid source: " + source);
    }
    subscripts = Lists.newArrayList();
    for(int i=skipBeginning; i+skipEnd<arguments.length();++i) {
      subscripts.add(arguments.getElementAsSEXP(i));
    }
    
    // how the subscripts are interpreted depends both on how many
    // and what kind of subscripts are provided, and the dimension of the
    // source vector
   
  
    if(subscripts.isEmpty()) {  
      selection = new CompleteSelection(source);
    
    } else if(subscripts.size() == 1) {
      
      SEXP subscript = subscripts.get(0);
      
      // if the single argument is a matrix or greater, then
      // we treat it as a matrix of coordinates
      if(CoordinateMatrixSelection.isCoordinateMatrix(source, subscript)) {
        
        selection = new CoordinateMatrixSelection(source, subscript);
      
      } else {
      
        // otherwise we treat the source
        // as a vector, regardless of whether it has dimensions or not
        
        selection = new VectorIndexSelection(source, subscript);
      }
      
    } else {
      
      // otherwise we have multiple subscripts, and we treat each subscript
      // as applying as whole to its dimensions (including whole rows or columns
      // in the case of matrices)
      
      selection = new DimensionSelection(source, subscripts);
      
    }
      
    return this;
  }
  
  public SubscriptOperation setSource(SEXP source, ListVector arguments) {
    return setSource(source, arguments, 0, 0);
  }

  public SubscriptOperation setDrop(boolean drop) {
    this.drop = drop;
    return this;
  }

  public boolean isSourceOneDimensionalArray() {
    return source.getAttributes().getDim().length() == 1;
  }

  public SEXP extractSingle() {

    // this seems like an abritrary limitation,
    // that is x[[TRUE]] happily takes the first item but
    // x[[1:2]] will throw an error, may be we can
    // just drop the distinction across the board?
    if(selection instanceof VectorIndexSelection ||
       selection instanceof CoordinateMatrixSelection) {
      if(selection.getElementCount() > 1) {
        throw new EvalException("attempt to select more than one element");
      }
    }

    if(selection.getElementCount() < 1) {
      throw new EvalException("attempt to select less than one element");
    }

    int index = selection.iterator().next();
    if(index < 0 || index >= source.length()) {
      throw new EvalException("subscript out of bounds");
    }
    return source.getElementAsSEXP(index);

  }

  public Vector extract() {

    if(source == Null.INSTANCE) {
      return Null.INSTANCE;

    } else {
      return (Vector)selection.select(source).setAttributes(extractAttributes());
    }
  }


  private AttributeMap extractAttributes() {
    if(source.getAttributes().has(Symbols.DIM)) {
      return extractArrayAttributes();

    } else {
      StringVector sourceNames = source.getAttributes().getNames();
      if(sourceNames != null) {
        return new AttributeMap.Builder()
            .set(Symbols.NAMES, selection.select(sourceNames))
            .build();
      }
    }

    return AttributeMap.EMPTY;
  }


  private AttributeMap extractArrayAttributes() {

    AttributeMap.Builder attributes = new AttributeMap.Builder();

    // Get the number of dimensions of the selection
    // This will only be > 1 for matrix subscripts like x[1,2,]
    int selectedDimCount = selection.getSelectedDimensionCount();

    // Build the list of dimension lengths/counts
    // and corresponding dim names
    IntArrayVector.Builder selectedDim = new IntArrayVector.Builder();
    List<Vector> selectedDimNames = new ArrayList<Vector>();

    boolean hasDimNames = false;

    for (int i = 0; i < selectedDimCount; ++i) {

      // if the drop flag is set (the default)
      // we ignore dimensions of length 1
      if(drop && selection.isSingleElementSelectedFromDimension(i)) {
        continue;
      }

      // calculate the size of this dimension (number of rows/columns/etc)
      selectedDim.add(Iterables.size(selection.getSelectionAlongDimension(i)));

      // retrieve dimension names for this row/column/dim
      Vector selectedNames = selection.getDimensionNames(i);
      selectedDimNames.add(selectedNames);
      if(selectedNames != Null.INSTANCE) {
        hasDimNames = true;
      }
    }

    // Determine whether the selection should have a DIM attribute
    boolean hasDims = true;
    if(dropDimensions(selectedDim, drop)) {
      // No dim attribute, but use the first dimname as the resulting names
      if(hasDimNames) {
        attributes.setNames((StringVector) selectedDimNames.get(0));
      }
    } else {
      attributes.setDim(selectedDim.build());
      if(hasDimNames) {
        attributes.set(Symbols.DIMNAMES, new ListVector(selectedDimNames));
      }
    }
    return attributes.build();
  }

  /**
   * Returns {@code true} if the {@code dim} attribute should be dropped from the result.
   *
   */
  private boolean dropDimensions(IntArrayVector.Builder selectedDim, boolean drop) {
    int dimCount = selectedDim.length();

    if(dimCount == 0) {
      return true;
    }

    if(drop && dimCount == 1) {
      // If the drop flag is set, and we are selecting only a single dimension, then drop
      // the dim attribute AS LONG AS the source was NOT a 1-dimensional array
      if(!isSourceOneDimensionalArray()) {
        return true;
      }
    }

    return false;
  }

  private boolean sourceIsSingleDimensionArray() {
    return source.getAttribute(Symbols.DIM).length() == 1;
  }


  public Vector replace(SEXP elements) {

    // [[<- and [<- seem to have a special meaning when
    // the replacement value is NULL and the vector is a list
    if(source instanceof ListVector && elements == Null.INSTANCE) {
      return remove();

    } else if(subscripts.size() == 1 && subscripts.get(0) instanceof StringVector) {
      return replaceByName(elements);
    }
    
    if(!selection.isEmpty() && elements.length() == 0) {
      throw new EvalException("replacement has zero length");
    }

    Vector.Builder result = createReplacementBuilder(elements);
    
    int replacement = 0;
    for(int index : selection) {
      assert index < source.length() || selection.getSourceDimensions() == 1;
      if(!IntVector.isNA(index)) {
        result.setFrom(index, elements, replacement++);
        if(replacement >= elements.length()) {
          replacement = 0;
        }
      }
    }
    return result.build();
  }

  private Vector replaceByName(SEXP elements) {
    StringVector namesToReplace = (StringVector) subscripts.get(0);
    Vector.Builder result = createReplacementBuilder(elements);
    StringArrayVector.Builder names = source.getNames() == Null.INSTANCE ? StringVector.newBuilder() :
        (StringArrayVector.Builder) source.getNames().newCopyBuilder();

    int replacementIndex = 0;

    for(String nameToReplace : namesToReplace) {
      int index = source.getIndexByName(nameToReplace);
      if(index == -1) {
        index = result.length();
        names.set(index, nameToReplace);
      }

      result.setFrom(index, elements, replacementIndex++);

      if(replacementIndex >= elements.length()) {
        replacementIndex = 0;
      }
    }

    result.setAttribute(Symbols.NAMES, names.build());
    return result.build();
  }

  public Vector remove() {
    Set<Integer> indicesToRemove = Sets.newHashSet();

    for(int index : selection) {
      if(!IntVector.isNA(index)) {
        indicesToRemove.add(index);
      }
    }

    Vector.Builder result = source.newBuilderWithInitialSize(0);
    result.copyAttributesFrom(source);
    for(int i=0;i!=source.length();++i) {
      if(!indicesToRemove.contains(i)) {
        result.addFrom(source, i);
      }
    }
    return result.build();
  }


  private Vector.Builder createReplacementBuilder(SEXP elements) {
    Vector.Builder result;

    Vector.Type replacementType;
    if(elements instanceof AtomicVector) {
      replacementType = ((AtomicVector) elements).getVectorType();
    } else {
      replacementType = ListVector.VECTOR_TYPE;
    }

    if(source.getVectorType().isWiderThanOrEqualTo(replacementType)) {
      result = source.newCopyBuilder();
    } else {
      result = replacementType.newBuilderWithInitialSize(source.length());
      result.copyAttributesFrom(source);
      for(int i=0;i!= source.length();++i) {
        result.setFrom(i, source, i);
      }
    }
    return result;
  }
}
