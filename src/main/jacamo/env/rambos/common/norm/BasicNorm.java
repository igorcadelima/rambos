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
package rambos.common.norm;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.LogicalFormula;

final class BasicNorm implements Norm {
  static final String FUNCTOR = "norm";

  private Atom id;
  private Status status = Status.ENABLED;
  private LogicalFormula condition;
  private Atom issuer;
  private NormContent content;

  /** Constructs an {@link AbstractNorm} with the properties specified in {@code builder}. */
  private BasicNorm(Builder builder) {
    if ((builder.id != null) && (builder.condition != null) && (builder.issuer != null)
        && (builder.content != null)) {
      id = builder.id;
      status = builder.status;
      condition = builder.condition;
      issuer = builder.issuer;
      content = builder.content;
    } else {
      throw new NullPointerException(
          "The following properties should not be null: id, condition, issuer, content.");
    }
  }

  static final class Builder {
    private Atom id;
    private Status status = Status.ENABLED;
    private LogicalFormula condition;
    private Atom issuer;
    private NormContent content;

    /**
     * Set id for the norm.
     * 
     * @param id norm's id
     * @return builder builder instance
     */
    Builder id(Atom id) {
      this.id = id;
      return this;
    }

    /**
     * Set status of the norm.
     * 
     * @param status norm's status
     * @return builder builder instance
     */
    Builder status(Status status) {
      this.status = status;
      return this;
    }

    /**
     * Set condition for the norm.
     * 
     * @param condition norm's activation condition
     * @return builder builder instance
     */
    Builder condition(LogicalFormula condition) {
      this.condition = condition;
      return this;
    }

    /**
     * Set issuer for the norm.
     * 
     * @param issuer norm's issuer
     * @return builder builder instance
     */
    Builder issuer(Atom issuer) {
      this.issuer = issuer;
      return this;
    }

    /**
     * Set content for the norm.
     * 
     * @param content norm's content
     * @return builder builder instance
     */
    Builder content(NormContent content) {
      this.content = content;
      return this;
    }

    /**
     * Return a new {@link Norm} with the specified implementation.
     * 
     * @return new {@code INorm}
     */
    BasicNorm build() {
      return new BasicNorm(this);
    }
  }

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
  public Atom getIssuer() {
    return issuer;
  }

  @Override
  public NormContent getContent() {
    return content;
  }

  @Override
  public String getFunctor() {
    return FUNCTOR;
  }

  @Override
  public Literal toLiteral() {
    Literal l = ASSyntax.createLiteral(getFunctor());
    l.addTerm(id);
    l.addTerm(ASSyntax.createAtom(status.lowercase()));
    l.addTerm(condition);
    l.addTerm(issuer);
    l.addTerm(content.toLiteral());
    return l;
  }

  @Override
  public String toString() {
    return toLiteral().toString();
  }
}
