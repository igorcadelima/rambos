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
import npl.TimeTerm;
import rambos.Obligation.ObligationBuilder;

/**
 * @author igorcadelima
 *
 */
public abstract class RegulationContent implements IRegulationContent {
  protected Atom target;
  protected LogicalFormula maintenanceCondition;
  protected Literal aim;
  protected TimeTerm deadline;

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
    deadline = (TimeTerm) literal.getTerm(3);
  }

  protected RegulationContent(RegulationContentBuilder<?> builder) {
    target = builder.target;
    maintenanceCondition = builder.maintenanceCondition;
    aim = builder.aim;
    deadline = builder.deadline;
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
  public TimeTerm getDeadline() {
    return deadline;
  }

  @Override
  public String toString() {
    Literal literal = ASSyntax.createLiteral(getFunctor().lowercase());
    literal.addTerm(target);
    literal.addTerm(maintenanceCondition);
    literal.addTerm(aim);
    literal.addTerm(deadline);
    return literal.toString();
  }

  protected static abstract class RegulationContentBuilder<T extends RegulationContentBuilder<T>> {
    private Atom target;
    private LogicalFormula maintenanceCondition;
    private Literal aim;
    private TimeTerm deadline;

    /**
     * Set attributes based on the terms of the given {@code literal}, regardless of the functor.
     * 
     * @param literal literal from which terms should be obtained
     * @return builder with updated state
     * @throws IllegalArgumentException if number of term is different from 1
     */
    public T setFrom(Literal literal) {
      int nTerms = literal.getTerms().size();

      if (nTerms == 4) {
        target = (Atom) literal.getTerm(0);
        maintenanceCondition = (LogicalFormula) literal.getTerm(1);
        aim = (Literal) literal.getTerm(2);
        deadline = (TimeTerm) literal.getTerm(3);
        return getThis();
      }
      throw new IllegalArgumentException("Invalid literal");
    }

    /**
     * Get current instance of the class and return it.
     * 
     * @return current instance of the class
     * @see ObligationBuilder#getThis()
     */
    protected abstract T getThis();

    public T setTarget(Atom target) {
      this.target = target;
      return getThis();
    }

    public T setMaintenanceCondition(LogicalFormula condition) {
      this.maintenanceCondition = condition;
      return getThis();
    }

    public T setAim(Literal aim) {
      this.aim = aim;
      return getThis();
    }

    public T setDeadline(TimeTerm deadline) {
      this.deadline = deadline;
      return getThis();
    }

    public abstract RegulationContent build();
  }
}
