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

import static jason.asSyntax.ASSyntax.parseLiteral;

import java.util.Iterator;

import cartago.ArtifactObsProperty;
import jason.RevisionFailedException;
import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.parser.ParseException;

/**
 * @author igorcadelima
 *
 */
public abstract class AbstractNorm implements INorm {
  protected String id;
  protected State state = State.ENABLED;
  protected LogicalFormula condition;
  protected String issuer;
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
  public String getIssuer() {
    return issuer;
  }

  @Override
  public IContent getContent() {
    return content;
  }

  @Override
  public boolean match(ArtifactObsProperty event) {
    try {
      // Parse event data to Literal
      Literal propLiteral = parseLiteral(event.toString());

      // Add parsed Literal to BB to check norm condition against it
      Agent ag = new Agent();
      ag.addBel(propLiteral);

      // Get norm condition to see whether the event is ruled by it
      LogicalFormula condition = getCondition();
      Iterator<Unifier> i = condition.logicalConsequence(ag, new Unifier());

      return i.hasNext();
    } catch (ParseException | RevisionFailedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public String toString() {
    Literal l = ASSyntax.createLiteral("norm");
    l.addTerm(ASSyntax.createAtom(id));
    l.addTerm(ASSyntax.createAtom(state.toString()));
    l.addTerm(condition);
    l.addTerm(ASSyntax.createAtom(issuer));
    l.addTerm(ASSyntax.createLiteral(content.toString()));
    return l.toString();
  }

  protected static abstract class AbstractNormBuilder<T extends AbstractNormBuilder<T>>
      implements INormBuilder<T> {
    protected String id;
    protected State state = State.ENABLED;
    protected LogicalFormula condition;
    protected String issuer;
    protected IContent content;

    /**
     * Get current instance of the class and return it.
     * 
     * @return current instance of the class
     * @see NormBuilder#getThis()
     */
    protected abstract T getThis();

    @Override
    public T setId(String id) {
      this.id = id;
      return getThis();
    }

    @Override
    public T setState(State state) {
      this.state = state;
      return getThis();
    }

    @Override
    public T setCondition(LogicalFormula condition) {
      this.condition = condition;
      return getThis();
    }

    @Override
    public T setIssuer(String issuer) {
      this.issuer = issuer;
      return getThis();
    }

    @Override
    public T setContent(IContent content) {
      this.content = content;
      return getThis();
    }
  }
}
