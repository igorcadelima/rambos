/*******************************************************************************
 * MIT License
 *
 * Copyright (c) Igor Conrado Alves de Lima <igorcadelima@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package rambos.core;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import rambos.core.BasicFact.Builder;
import rambos.core.Fact.Efficacy;
import rambos.core.Fact.Motive;
import rambos.core.util.Enums;

/**
 * Static utility methods pertaining to {@link Fact} instances.
 * 
 * @author igorcadelima
 *
 */
public final class Facts {
  private Facts() {}

  /**
   * Return a new fact initialised to the value represented by the specified {@code String}.
   * 
   * @param fact string to be parsed
   * @return fact represented by the string argument
   * @throws IllegalArgumentException if string does not contain a parsable fact
   * @throws NullPointerException if string is {@code null}
   */
  public static Fact parse(String fact) {
    try {
      Literal l = ASSyntax.parseLiteral(fact);

      if (!l.getFunctor()
            .equals(BasicFact.FUNCTOR)) {
        throw new IllegalArgumentException();
      }

      return new Builder().time(((NumberTerm) l.getTerm(0)).solve())
                          .sanctioner(l.getTerm(1)
                                       .toString())
                          .sanctionee(l.getTerm(2)
                                       .toString())
                          .norm(l.getTerm(3)
                                 .toString())
                          .sanction(l.getTerm(4)
                                     .toString())
                          .motive(Enums.lookup(Motive.class, l.getTerm(5)
                                                              .toString()))
                          .efficacy(Enums.lookup(Efficacy.class, l.getTerm(6)
                                                                  .toString()))
                          .build();

    } catch (Exception e) {
      throw new IllegalArgumentException("String does not contain a parsable fact");
    }
  }
}
