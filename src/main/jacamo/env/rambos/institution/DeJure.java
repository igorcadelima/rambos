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

import static jason.asSyntax.ASSyntax.createAtom;

import java.util.Set;

import cartago.Artifact;
import cartago.LINK;
import cartago.OPERATION;
import cartago.ObsProperty;
import cartago.OpFeedbackParam;
import jason.asSyntax.Atom;
import rambos.common.Literable;
import rambos.norm.Norm;
import rambos.norm.Norms;
import rambos.sanction.Sanction;

/**
 * This artefact stores a regulative specification, which is composed of norms, sanctions, and
 * norm-sanction links. It provides operations to consult and make changes, as well as observable
 * properties which reflect the current state of the regulative specification.
 * 
 * @author igorcadelima
 *
 */
public final class DeJure extends Artifact {
  RegulativeSpec regulativeSpec;

  /**
   * Initialise {@link DeJure} repository based on data from the given regulative specification.
   * 
   * @param regulativeSpec path to file with the regulative specification
   */
  public void init(String regulativeSpec) {
    this.regulativeSpec = RegulativeSpecs.fromFile(regulativeSpec);
    this.regulativeSpec.getNorms()
                       .forEach(norm -> defineObsProperty(norm));
    this.regulativeSpec.getSanctions()
                       .forEach(sanction -> defineObsProperty(sanction));
  }

  /** Handy method to define observable properties of a {@code literable}. */
  private void defineObsProperty(Literable literable) {
    defineObsProperty(literable.getFunctor(), literable.toLiteral()
                                                       .getTerms()
                                                       .toArray());
  }

  /** Handy method to update observable properties of a {@code literable}. */
  private void updateObsProperty(Literable literable, Atom id) {
    ObsProperty property = getObsPropertyByTemplate(literable.getFunctor(), id, null);
    property.updateValue(literable.toLiteral()
                                  .getTerms()
                                  .toArray());
  }

  /** Handy method to remove observable properties of a {@code literable}. */
  private void removeObsProperty(Literable literable) {
    removeObsPropertyByTemplate(literable.getFunctor(), literable.toLiteral()
                                                                 .getTerms()
                                                                 .toArray());
  }

  /**
   * Add new norm to regulative specification and define observable property for it.
   * 
   * Only instances of {@link Norm} or {@link String} should be passed as argument. If {@code norm}
   * is an instance of {@link String}, then it will be parsed using {@link Norms#parse(String)}
   * before being added to the regulative specification.
   * 
   * @param norm norm to be added
   */
  @LINK
  @OPERATION
  public <T> void addNorm(T norm) {
    if (norm instanceof Norm)
      addNorm((Norm) norm);
    else if (norm instanceof String)
      addNorm(Norms.parse((String) norm));
    else
      failed("Expected " + String.class.getCanonicalName() + " or " + Norm.class.getCanonicalName()
          + " but got " + norm.getClass()
                              .getCanonicalName());
  }

  /**
   * Add new norm to regulative specification and define observable property for it.
   * 
   * Note that only norms with different ids from the ones present in the specification can be
   * added, even if disabled. If an already existing norm is tried to be added, such addition
   * attempt is just ignored.
   * 
   * @param norm norm to be added
   */
  private void addNorm(Norm norm) {
    if (regulativeSpec.addNorm(norm)) {
      defineObsProperty(norm);
    }
  }

  /**
   * Remove norm from regulative specification and observable property with it.
   * 
   * @param norm norm to be removed
   */
  @LINK
  @OPERATION
  public void removeNorm(Norm norm) {
    // TODO: check whether operator agent is a legislator
    if (regulativeSpec.removeNorm(norm.getId()) != null) {
      removeObsProperty(norm);
    }
  }

  /**
   * Add new sanction to regulative specification and define observable property for it.
   * 
   * Note that only sanction with different ids from the ones present in the specification can be
   * added, even if disabled. If an already existing sanction is tried to be added, such addition
   * attempt is just ignored.
   * 
   * @param sanction sanction to be added
   */
  @LINK
  @OPERATION
  public void addSanction(Sanction sanction) {
    // TODO: check whether operator agent is a legislator
    if (regulativeSpec.addSanction(sanction)) {
      defineObsProperty(sanction);
    }
  }

  /**
   * Remove sanction from regulative specification and observable property with it.
   * 
   * @param sanction sanction to be removed
   * @return {@code true} if sanction was removed successfully
   */
  @LINK
  @OPERATION
  public void removeSanction(Sanction sanction) {
    // TODO: check whether operator agent is a legislator
    if (regulativeSpec.removeSanction(sanction.getId()) != null) {
      removeObsProperty(sanction);
    }
  }

  /**
   * Link existing norm and sanction with given ids.
   * 
   * The observable property of the norm is also updated upon successful linking.
   * 
   * @param normId id of the norm to be linked
   * @param sanctionId id of the sanction to be linked
   * @throws NullPointerException if any of the arguments are {@code null}
   */
  @LINK
  @OPERATION
  public void link(String normId, String sanctionId) {
    // TODO: check whether operator agent is a legislator
    Atom normIdAtom = createAtom(normId);
    Atom sanctionIdAtom = createAtom(sanctionId);

    if (regulativeSpec.addLink(normIdAtom, sanctionIdAtom)) {
      Norm norm = regulativeSpec.getNorm(normIdAtom);
      updateObsProperty(norm, normIdAtom);
    }
  }

  /**
   * Unlink existing norm and sanction with given ids.
   * 
   * The observable property of the norm is also updated upon successful unlinking.
   * 
   * @param normId id of the norm
   * @param sanctionId id of the sanction
   */
  @LINK
  @OPERATION
  public void unlink(String normId, String sanctionId) {
    // TODO: check whether operator agent is a legislator
    Atom normIdAtom = createAtom(normId);
    Norm norm = regulativeSpec.getNorm(normIdAtom);

    if (regulativeSpec.unlink(normIdAtom, createAtom(sanctionId))) {
      updateObsProperty(norm, normIdAtom);
    }
  }

  /** Return norms through {@link out}. */
  @LINK
  @OPERATION
  public void getNorms(OpFeedbackParam<Set<Norm>> out) {
    out.set(regulativeSpec.getNorms());
  }

  /** Return sanctions through {@link out}. */
  @LINK
  @OPERATION
  public void getSanctions(OpFeedbackParam<Set<Sanction>> out) {
    out.set(regulativeSpec.getSanctions());
  }
}
