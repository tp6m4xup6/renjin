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

package r.lang.primitive;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import r.lang.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class TypesTest extends EvalTestCase {

  @Test
  public void asCharacter() {
    assertThat( eval("as.character(1)"), equalTo( c("1") ));
    assertThat( eval("as.character(\"foobar\")"), equalTo( c("foobar") ));
    assertThat( eval("as.character(1L)"), equalTo( c("1") ));
    assertThat( eval("as.character(1.3333333333333333333333333333333333)"),
        equalTo(c("1.33333333333333")));
    assertThat( eval("as.character(TRUE)"), equalTo( c("TRUE") ));
    assertThat( eval("as.character(NA)"), equalTo( c( StringExp.NA )) );
  }

  @Test
  public void asDoubleFromDouble() {
    assertThat( eval("as.double(3.14)"), equalTo( c(3.14) ) );
    assertThat( eval("as.double(NA_real_)"), equalTo( c(DoubleExp.NA) ) );
  }

  @Test
  public void asDoubleFromInt() {
    assertThat( eval("as.double(3L)"), equalTo( c(3l) ));
  }

  @Test
  public void asDoubleFromLogical() {
    assertThat( eval("as.double(TRUE)"), equalTo( c(1d) ));
    assertThat( eval("as.double(FALSE)"), equalTo( c(0d) ));
  }

  @Test
  public void asDoubleFromString() {
    assertThat( eval("as.double(\"42\")"), equalTo( c(42d) ));
    assertThat( eval("as.double(\"not an integer\")"), equalTo( c(DoubleExp.NA) ));
  }

  @Test
  public void asIntFromDouble() {
    assertThat( eval("as.integer(3.1)"), equalTo( c_i( 3 )));
    assertThat( eval("as.integer(3.9)"), equalTo( c_i( 3 )));
    assertThat( eval("as.integer(NA_real_)"), equalTo( c_i( IntExp.NA )));
    assertThat( eval("as.integer(c(1, 9.32, 9.9, 5.0))"), equalTo( c_i(1, 9, 9, 5 )));
  }

  @Test
  public void environment() {
    assertThat( eval(".Internal(environment())"), CoreMatchers.is((SEXP)context.getGlobalEnvironment()));

  }

  @Test
  public void environmentOfRandomExp() {
    assertThat( eval(".Internal(environment(1))"), is((SEXP)NullExp.INSTANCE));
  }

  @Test
  public void environmentOfClosure() {
    eval("f <- function() { 1 } ");
    assertThat( eval(".Internal(environment( f ))"), is((SEXP)context.getGlobalEnvironment()));

  }

  @Test
  public void subset() {
    eval("x<-c(81,82,83,84,85,86) ");

    assertThat( eval( "x[4]" ), equalTo( c(84) ));
  }

  @Test
  public void subsetOfMultipleIndicies() {
    eval("  x<-c(91, 92, 93, 94, 95) ");

    assertThat( eval("x[1:3]"), equalTo((SEXP) new DoubleExp(91,92,93)));
  }
  @Test
  public void subsetOfPosAndZeroIndicies() {
    eval("  x<-c(91, 92, 93, 94, 95) ");

    assertThat( eval("x[c(1,0,1)]"), equalTo((SEXP) new DoubleExp(91, 91)));
  }

  @Test
  public void list() {
    assertThat( eval("list(\"a\")"), equalTo( list("a") ));
  }

  @Test
  public void listOfNulls() {
    assertThat( eval("list(NULL, NULL)"), equalTo( list(NULL, NULL) ));
  }

  @Test
  public void listOfNull() {
    assertThat( eval("list(NULL)"), equalTo( list(NULL) ));
  }


}