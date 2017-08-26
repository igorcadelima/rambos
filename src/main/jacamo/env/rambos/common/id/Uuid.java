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
package rambos.common.id;

import java.util.UUID;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;

/**
 * Represent an universally unique identifier.
 * 
 * @author igorcadelima
 *
 */
public final class Uuid implements Id {
  private final UUID id = UUID.randomUUID();

  private Uuid() {}

  /** Return a new instance of {@code Uuid}. */
  public static Uuid newInstance() {
    return new Uuid();
  }

  public String toString() {
    return new StringBuilder(40).append(getFunctor())
                                .append('(')
                                .append(id)
                                .append(')')
                                .toString();
  }

  @Override
  public String getFunctor() {
    return "id";
  }

  @Override
  public Literal toLiteral() {
    Literal l = ASSyntax.createLiteral(getFunctor());
    l.addTerm(ASSyntax.createAtom(id.toString()));
    return l;
  }
}
