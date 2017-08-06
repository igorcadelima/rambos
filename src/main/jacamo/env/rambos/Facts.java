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
package rambos;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import rambos.Fact.FactBuilder;
import rambos.IFact.Efficacy;
import rambos.IFact.Motive;
import rambos.util.Enums;

/**
 * Static utility methods pertaining to {@link IFact} instances.
 * 
 * @author igorcadelima
 *
 */
public final class Facts {
  public static final String FUNCTOR = "fact";

  private Facts() {}

  /**
   * Return a new fact initialised to the value represented by the specified {@code String}.
   * 
   * @param in string to be parsed
   * @return fact represented by the string argument
   * @throws IllegalArgumentException if string does not contain a parsable fact
   * @throws NullPointerException if string is {@code null}
   */
  public static IFact parse(String in) {
    try {
      Literal l = ASSyntax.parseLiteral(in);

      if (!l.getFunctor()
            .equals(FUNCTOR)) {
        throw new IllegalArgumentException();
      }

      FactBuilder builder = new FactBuilder();
      return builder.setTime(((NumberTerm) l.getTerm(0)).solve())
                    .setSanctioner(l.getTerm(1)
                                    .toString())
                    .setSanctionee(l.getTerm(2)
                                    .toString())
                    .setNorm(l.getTerm(3)
                              .toString())
                    .setSanction(l.getTerm(4)
                                  .toString())
                    .setMotive(Enums.lookup(Motive.class, l.getTerm(5)
                                                           .toString()))
                    .setEfficacy(Enums.lookup(Efficacy.class, l.getTerm(6)
                                                               .toString()))
                    .build();

    } catch (Exception e) {
      throw new IllegalArgumentException("String does not contain a parsable fact");
    }
  }
}
