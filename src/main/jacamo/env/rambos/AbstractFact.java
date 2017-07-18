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
  protected Boolean efficacy;

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
  public Boolean getEfficacy() {
    return efficacy;
  }

  @Override
  public void setEfficacy(Boolean efficacy) {
    this.efficacy = efficacy;
  }

  @Override
  public String toString() {
    Literal l = ASSyntax.createLiteral("fact");
    l.addTerm(ASSyntax.createNumber(time));
    l.addTerm(ASSyntax.createAtom(sanctioner));
    l.addTerm(ASSyntax.createAtom(sanctionee));
    l.addTerm(ASSyntax.createAtom(norm));
    l.addTerm(ASSyntax.createAtom(sanction));
    l.addTerm(ASSyntax.createAtom(efficacy == null ? "nd" : efficacy.toString()));
    return l.toString();
  }
}
