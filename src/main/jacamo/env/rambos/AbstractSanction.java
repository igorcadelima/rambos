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
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.LogicalFormula;

/**
 * @author igorcadelima
 *
 */
public class AbstractSanction implements ISanction {
  protected Atom id;
  protected Status status = Status.ENABLED;
  protected LogicalFormula condition;
  protected SanctionCategory category;
  protected IContent content;

  @Override
  public Atom getId() {
    return id;
  }

  @Override
  public Status getStatus() {
    return status;
  }

  @Override
  public LogicalFormula getCondition() {
    return condition;
  }

  @Override
  public SanctionCategory getCategory() {
    return category;
  }

  @Override
  public IContent getContent() {
    return content;
  }

  @Override
  public Literal toLiteral() {
    Literal l = ASSyntax.createLiteral(Sanctions.FUNCTOR);
    l.addTerm(id);
    l.addTerm(ASSyntax.createAtom(status.lowercase()));
    l.addTerm(condition);
    l.addTerm(category.toLiteral());
    l.addTerm(content.toLiteral());
    return l;
  }

  @Override
  public String toString() {
    return toLiteral().toString();
  }
}
