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
package rambos.core.registry;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import rambos.core.registry.BasicRegistry.Builder;
import rambos.core.registry.Registry.Cause;
import rambos.core.registry.Registry.Efficacy;
import rambos.core.util.Enums;

/**
 * Static utility methods pertaining to {@link Registry} instances.
 * 
 * @author igorcadelima
 *
 */
public final class Registries {
  private Registries() {}

  /**
   * Return a new sanction registry initialised to the value represented by the specified
   * {@code String}.
   * 
   * @param registry string to be parsed
   * @return registry represented by the string argument
   * @throws IllegalArgumentException if string does not contain a parsable sanction registry
   * @throws NullPointerException if string is {@code null}
   */
  public static Registry parse(String registry) {
    try {
      Literal l = ASSyntax.parseLiteral(registry);

      if (!l.getFunctor()
            .equals(BasicRegistry.FUNCTOR)) {
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
                          .cause(Enums.lookup(Cause.class, l.getTerm(5)
                                                            .toString()))
                          .efficacy(Enums.lookup(Efficacy.class, l.getTerm(6)
                                                                  .toString()))
                          .build();

    } catch (Exception e) {
      throw new IllegalArgumentException("String does not contain a parsable registry");
    }
  }
}
