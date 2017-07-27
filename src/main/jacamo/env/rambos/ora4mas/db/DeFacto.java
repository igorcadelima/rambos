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
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.parser.ParseException;
import rambos.Facts;
import rambos.IFact;

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
   * @param f fact to be added
   */
  @LINK
  @OPERATION
  public <T> void addFact(T f) {
    if (f instanceof IFact)
      addFact((IFact) f);
    else if (f instanceof String)
      addFact(Facts.parse((String) f));
    else
      failed("Expected " + String.class.getCanonicalName() + " or " + IFact.class.getCanonicalName()
          + " but got " + f.getClass()
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
      defineObsProperty("fact", literalFact.getTerms()
                                           .toArray());
    } catch (ParseException e) {
      e.printStackTrace();
    }
  }

  /**
   * Update an existing fact from the facts set by replacing it with a new one.
   * 
   * @param oldFact
   * @param newFact
   */
  @LINK
  @OPERATION
  public void updateFact(IFact oldFact, IFact newFact) {
    removeFact(oldFact);
    addFact(newFact);
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
      removeObsPropertyByTemplate("fact", literalFact.getTerms()
                                                     .toArray());
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
