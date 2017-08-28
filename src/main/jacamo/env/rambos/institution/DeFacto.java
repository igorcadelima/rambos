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

import java.util.HashMap;
import java.util.Map;

import cartago.Artifact;
import cartago.LINK;
import cartago.OPERATION;
import cartago.ObsProperty;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import rambos.common.Enums;
import rambos.common.id.Id;
import rambos.common.id.Uuid;
import rambos.registry.SanctionDecisions;
import rambos.registry.SanctionDecision;
import rambos.registry.SanctionDecision.Efficacy;

/**
 * @author igorcadelima
 *
 */
public final class DeFacto extends Artifact {
  private Map<Id, SanctionDecision> sanctionDecisions = new HashMap<>();

  private Uuid decisionIdFrom(Object obj) {
    if (obj instanceof Uuid)
      return (Uuid) obj;
    else if (obj instanceof String)
      return Uuid.parse((String) obj);
    else
      failed("Expected " + String.class.getCanonicalName() + " or "
          + SanctionDecision.class.getCanonicalName() + " but got " + obj.getClass()
                                                                         .getCanonicalName());
    assert false; // Execution should never reach this point!
    return null;
  }

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
  public void addDecision(Object decision) {
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
    sanctionDecisions.put(decision.getId(), decision);
    defineObsProperty(decision.getFunctor(), (Object[]) decision.toLiteral()
                                                                .getTermsArray());
  }

  /**
   * Update the efficacy value of an existing sanction decision.
   * 
   * Note that {@code decisionId} should be an instance of {@link Uuid} or {@link String} and
   * {@code efficacy} should be an instance of {@link Efficacy} or {@link String}.
   * 
   * @param decisionId id of the sanction decision to be updated
   * @param efficacy new efficacy value
   */
  @LINK
  @OPERATION
  public void updateEfficacy(Object decisionId, Object efficacy) {
    Efficacy efficacyObj = Enums.lookup(Efficacy.class, efficacy.toString());
    if (efficacyObj == null) {
      failed(efficacy + " is not a valid efficacy value");
    }

    SanctionDecision decision = sanctionDecisions.get(decisionIdFrom(decisionId));
    ObsProperty prop =
        getObsPropertyByTemplate(decision.getFunctor(), (Object[]) decision.toLiteral()
                                                                           .getTermsArray());
    decision.setEfficacy(efficacyObj);
    prop.updateValue(7, ASSyntax.createAtom(efficacy.toString()));
  }

  /**
   * Set whether a sanction decision has been applied or not.
   * 
   * Note that {@code decisionId} should be an instance of {@link Uuid} or {@link String}.
   * 
   * @param decisionId id of the sanction decision to be updated
   * @param applied {@code true} if applied, else {@code false}
   */
  @LINK
  @OPERATION
  public void setApplied(Object decisionId, boolean applied) {
    SanctionDecision decision = sanctionDecisions.get(decisionIdFrom(decisionId));
    ObsProperty prop =
        getObsPropertyByTemplate(decision.getFunctor(), (Object[]) decision.toLiteral()
                                                                           .getTermsArray());
    decision.setApplied(applied);
    prop.updateValue(8, applied ? Atom.LTrue : Atom.LFalse);
  }

  /**
   * Remove sanction decision with the given {@code id}.
   * 
   * Only instances of {@link Uuid} or {@link String} should be passed as argument. If
   * {@code decisionId} is an instance of {@link String}, then it will be parsed using
   * {@link Uuid#parse(String)} before being removed.
   * 
   * @param id id of the decision to be removed
   */
  @LINK
  @OPERATION
  public void removeDecision(Object id) {
    Id idObj = decisionIdFrom(id);
    SanctionDecision decision = sanctionDecisions.remove(idObj);
    removeObsPropertyByTemplate(decision.getFunctor(), (Object[]) decision.toLiteral()
                                                                          .getTermsArray());
  }
}
