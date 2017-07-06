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
import jason.asSyntax.LogicalFormula;

/**
 * @author igorcadelima
 *
 */
public class AbstractSanction implements ISanction {
  protected String id;
  protected State state = State.ENABLED;
  protected LogicalFormula condition;
  protected SanctionCategory category;
  protected IContent content;

  @Override
  public String getId() {
    return id;
  }

  @Override
  public State getState() {
    return state;
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
  public String toString() {
    Literal l = ASSyntax.createLiteral("sanction");
    l.addTerm(ASSyntax.createAtom(id));
    l.addTerm(ASSyntax.createAtom(state.toString()
                                       .toLowerCase()));
    l.addTerm(condition);
    l.addTerm(ASSyntax.createAtom(category.toString()));
    l.addTerm(ASSyntax.createLiteral(content.toString()));
    return l.toString();
  }
}
