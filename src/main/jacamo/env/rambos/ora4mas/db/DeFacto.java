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
package rambos.ora4mas.db;

import java.util.HashSet;
import java.util.Set;

import cartago.Artifact;
import cartago.LINK;
import cartago.OPERATION;
import cartago.ObsProperty;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.parser.ParseException;
import rambos.Facts;
import rambos.IFact;
import rambos.IFact.Efficacy;
import rambos.util.Enums;

/**
 * @author igorcadelima
 *
 */
public class DeFacto extends Artifact {
  private Set<IFact> facts = new HashSet<IFact>();

  /**
   * Add new fact into the facts set.
   * 
   * Only instances of {@link IFact} or {@link String} should be passed as argument. For both cases,
   * the fact will be added using {@link #addFact(IFact)}. If {@code f} is an instance of
   * {@link String}, then it will be parsed using {@link Facts#parse(String)} before being added to
   * the facts set.
   * 
   * @param fact fact to be added
   */
  @LINK
  @OPERATION
  public <T> void addFact(T fact) {
    if (fact instanceof IFact)
      addFact((IFact) fact);
    else if (fact instanceof String)
      addFact(Facts.parse((String) fact));
    else
      failed("Expected " + String.class.getCanonicalName() + " or " + IFact.class.getCanonicalName()
          + " but got " + fact.getClass()
                              .getCanonicalName());
  }

  /**
   * Add new fact into the facts set.
   * 
   * @param fact
   */
  private void addFact(IFact fact) {
    try {
      Literal literalFact = ASSyntax.parseLiteral(fact.toString());
      facts.add(fact);
      defineObsProperty(Facts.FUNCTOR, (Object[]) literalFact.getTermsArray());
    } catch (ParseException e) {
      e.printStackTrace();
    }
  }

  /**
   * Update the efficacy value of an existing fact.
   * 
   * @param fact string with literal format of the fact to be updated
   * @param efficacy new efficacy value
   */
  @LINK
  @OPERATION
  public void updateFactEfficacy(String fact, String efficacy) {
    Efficacy efficacyObj = Enums.lookup(Efficacy.class, efficacy);
    if (efficacyObj == null) {
      failed(efficacy + " is not a valid efficacy value");
    }

    IFact factObj = Facts.parse(fact);
    ObsProperty prop = getObsPropertyByTemplate(Facts.FUNCTOR, (Object[]) factObj.toLiteral()
                                                                                 .getTermsArray());
    facts.remove(factObj);
    factObj.setEfficacy(efficacyObj);
    facts.add(factObj);
    prop.updateValue(6, ASSyntax.createAtom(efficacy));
  }

  /**
   * Remove a fact from the facts set.
   * 
   * @param fact
   */
  @LINK
  @OPERATION
  public void removeFact(IFact fact) {
    try {
      Literal literalFact = ASSyntax.parseLiteral(fact.toString());
      facts.remove(fact);
      removeObsPropertyByTemplate(Facts.FUNCTOR, literalFact.getTerms()
                                                            .toArray());
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
