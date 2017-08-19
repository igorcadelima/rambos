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
package rambos.common.link;

import java.util.HashSet;
import java.util.Set;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;

/**
 * @author igorcadelima
 *
 */
final class BasicLink implements Link {
  static final String FUNCTOR = "link";

  private Atom norm;
  private Set<Atom> sanctions = new HashSet<Atom>();

  BasicLink(Atom norm) {
    this.norm = norm;
  }

  @Override
  public Atom getNorm() {
    return norm;
  }

  @Override
  public Set<Atom> getSanctions() {
    return sanctions;
  }

  @Override
  public boolean addSanction(Atom sanction) {
    return sanctions.add(sanction);
  }

  @Override
  public boolean removeSanction(Atom sanction) {
    return sanctions.remove(sanction);
  }

  @Override
  public String getFunctor() {
    return FUNCTOR;
  }

  @Override
  public Literal toLiteral() {
    Literal l = ASSyntax.createLiteral(FUNCTOR);
    sanctions.forEach(sanction -> l.addTerm(sanction));
    return l;
  }

  @Override
  public String toString() {
    return toLiteral().toString();
  }
}
