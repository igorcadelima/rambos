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

import jason.asSyntax.Literal;
import rambos.Obligation.ObligationBuilder;
import rambos.util.Enums;
import rambos.util.LowercaseEnum;

/**
 * Static utility methods pertaining to {@link IContent} instances.
 * 
 * @author igorcadelima
 *
 */
public final class Contents {
  private Contents() {}

  public enum Functor implements LowercaseEnum<Functor> {
    FAIL {
      @Override
      IContent newContent(Literal literal) {
        return new Failure(literal);
      }
    },

    OBLIGATION {
      @Override
      IContent newContent(Literal literal) {
        ObligationBuilder builder = new ObligationBuilder();
        return builder.setFrom(literal)
                      .build();
      }
    };

    /**
     * Factory method that returns a new {@link IContent} instance whose literal representation is
     * the given {@code literal}.
     * 
     * @param literal literal representation of the content
     * @return new content
     */
    abstract IContent newContent(Literal literal);
  }

  /**
   * Return a new {@link IContent} instance whose literal representation is the given
   * {@code literal}.
   * 
   * Unlike, {@link RegulationContentBuilder#setFrom(Literal)} and {@link Fails#of(Literal)}, this
   * method considers not only number of terms, but also the functor.
   * 
   * @param literal literal representation of the content
   * @return new content
   */
  public static IContent of(Literal literal) {
    return Enums.lookup(Functor.class, literal.getFunctor())
                .newContent(literal);
  }
}
