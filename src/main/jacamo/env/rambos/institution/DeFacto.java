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
package rambos.institution;

import java.util.HashSet;
import java.util.Set;

import cartago.Artifact;
import cartago.LINK;
import cartago.OPERATION;
import cartago.ObsProperty;
import jason.asSyntax.ASSyntax;
import rambos.common.Enums;
import rambos.registry.SanctionDecisions;
import rambos.registry.SanctionDecision;
import rambos.registry.SanctionDecision.Efficacy;

/**
 * @author igorcadelima
 *
 */
public final class DeFacto extends Artifact {
  private Set<SanctionDecision> sanctionDecisions = new HashSet<>();

  /**
   * Add new decision into the sanction decisions set.
   * 
   * Only instances of {@link SanctionDecision} or {@link String} should be passed as argument. For
   * both cases, the registry will be added using {@link #addDecision(SanctionDecision)}. If
   * {@code decision} is an instance of {@link String}, then it will be parsed using
   * {@link SanctionDecisions#parse(String)} before being added to the sanction decisions set.
   * 
   * @param decision sanction decision to be added
   */
  @LINK
  @OPERATION
  public <T> void addDecision(T decision) {
    if (decision instanceof SanctionDecision)
      addDecision((SanctionDecision) decision);
    else if (decision instanceof String)
      addDecision(SanctionDecisions.parse((String) decision));
    else
      failed("Expected " + String.class.getCanonicalName() + " or "
          + SanctionDecision.class.getCanonicalName() + " but got " + decision.getClass()
                                                                              .getCanonicalName());
  }

  /** Add new {@code decision} into the sanction decisions set. */
  private void addDecision(SanctionDecision decision) {
    sanctionDecisions.add(decision);
    defineObsProperty(decision.getFunctor(), (Object[]) decision.toLiteral()
                                                                .getTermsArray());
  }

  /**
   * Update the efficacy value of an existing sanction decision.
   * 
   * @param decision string with literal format of the sanction decision to be updated
   * @param efficacy new efficacy value
   */
  @LINK
  @OPERATION
  public void updateEfficacy(String decision, String efficacy) {
    Efficacy efficacyObj = Enums.lookup(Efficacy.class, efficacy);
    if (efficacyObj == null) {
      failed(efficacy + " is not a valid efficacy value");
    }

    SanctionDecision decisionObj = SanctionDecisions.parse(decision);
    ObsProperty prop =
        getObsPropertyByTemplate(decisionObj.getFunctor(), (Object[]) decisionObj.toLiteral()
                                                                                 .getTermsArray());
    sanctionDecisions.remove(decisionObj);
    decisionObj.setEfficacy(efficacyObj);
    sanctionDecisions.add(decisionObj);
    prop.updateValue(6, ASSyntax.createAtom(efficacy));
  }

  /** Remove a decision from the sanction decisions set. */
  @LINK
  @OPERATION
  public void removeDecision(SanctionDecision decision) {
    sanctionDecisions.remove(decision);
    removeObsPropertyByTemplate(decision.getFunctor(), (Object[]) decision.toLiteral()
                                                                          .getTermsArray());
  }
}
