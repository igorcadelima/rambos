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
import jason.asSyntax.NumberTerm;

/**
 * @author igorcadelima
 *
 */
public abstract class RegulationContent implements IRegulationContent {
  protected Atom target;
  protected LogicalFormula maintenanceCondition;
  protected Literal aim;
  protected NumberTerm deadline;

  /**
   * @param literal literal content
   * @throws IllegalArgumentException if arity of the literal is not 4
   */
  protected RegulationContent(Literal literal) throws IllegalArgumentException {
    if (literal.getArity() != 4) {
      throw new IllegalArgumentException("The arity of the literal should be 4");
    }
    target = (Atom) literal.getTerm(0);
    maintenanceCondition = (LogicalFormula) literal.getTerm(1);
    aim = (Literal) literal.getTerm(2);
    deadline = (NumberTerm) literal.getTerm(3);
  }

  @Override
  public Atom getTarget() {
    return target;
  }

  @Override
  public LogicalFormula getMaintenanceCondition() {
    return maintenanceCondition;
  }

  @Override
  public Literal getAim() {
    return aim;
  }

  @Override
  public NumberTerm getDeadline() {
    return deadline;
  }

  @Override
  public Literal toLiteral() {
    Literal l = ASSyntax.createLiteral(getFunctor().lowercase());
    l.addTerm(target);
    l.addTerm(maintenanceCondition);
    l.addTerm(aim);
    l.addTerm(deadline);
    return l;
  }

  @Override
  public String toString() {
    return toLiteral().toString();
  }
}
