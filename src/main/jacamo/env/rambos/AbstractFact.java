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

/**
 * @author igorcadelima
 *
 */
public abstract class AbstractFact implements IFact {
  protected long time;
  protected String sanctioner;
  protected String sanctionee;
  protected String norm;
  protected String sanction;
  protected Efficacy efficacy = Efficacy.INDETERMINATE;

  @Override
  public long getTime() {
    return time;
  }

  @Override
  public String getSanctioner() {
    return sanctioner;
  }

  @Override
  public String getSanctionee() {
    return sanctionee;
  }

  @Override
  public String getNorm() {
    return norm;
  }

  @Override
  public String getSanction() {
    return sanction;
  }

  @Override
  public Efficacy getEfficacy() {
    return efficacy;
  }

  @Override
  public void setEfficacy(Efficacy efficacy) {
    this.efficacy = efficacy;
  }

  @Override
  public Literal toLiteral() {
    Literal l = ASSyntax.createLiteral(Facts.FUNCTOR);
    l.addTerm(ASSyntax.createNumber(time));
    l.addTerm(ASSyntax.createAtom(sanctioner));
    l.addTerm(ASSyntax.createAtom(sanctionee));
    l.addTerm(ASSyntax.createAtom(norm));
    l.addTerm(ASSyntax.createAtom(sanction));
    l.addTerm(ASSyntax.createAtom(efficacy.lowercase()));
    return l;
  }

  @Override
  public String toString() {
    return toLiteral().toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((efficacy == null) ? 0 : efficacy.hashCode());
    result = prime * result + ((norm == null) ? 0 : norm.hashCode());
    result = prime * result + ((sanction == null) ? 0 : sanction.hashCode());
    result = prime * result + ((sanctionee == null) ? 0 : sanctionee.hashCode());
    result = prime * result + ((sanctioner == null) ? 0 : sanctioner.hashCode());
    result = prime * result + (int) (time ^ (time >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AbstractFact other = (AbstractFact) obj;
    if (efficacy != other.efficacy)
      return false;
    if (norm == null) {
      if (other.norm != null)
        return false;
    } else if (!norm.equals(other.norm))
      return false;
    if (sanction == null) {
      if (other.sanction != null)
        return false;
    } else if (!sanction.equals(other.sanction))
      return false;
    if (sanctionee == null) {
      if (other.sanctionee != null)
        return false;
    } else if (!sanctionee.equals(other.sanctionee))
      return false;
    if (sanctioner == null) {
      if (other.sanctioner != null)
        return false;
    } else if (!sanctioner.equals(other.sanctioner))
      return false;
    if (time != other.time)
      return false;
    return true;
  }
}
