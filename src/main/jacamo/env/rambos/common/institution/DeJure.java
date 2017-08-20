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
package rambos.common.institution;

import static jason.asSyntax.ASSyntax.createAtom;

import java.io.IOException;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import cartago.Artifact;
import cartago.LINK;
import cartago.OPERATION;
import cartago.ObsProperty;
import cartago.OpFeedbackParam;
import jason.asSyntax.Atom;
import rambos.common.link.Links;
import rambos.common.norm.Norm;
import rambos.common.norm.Norms;
import rambos.common.sanction.Sanction;
import rambos.common.util.Literable;

/**
 * This artefact stores a legislation, which is composed of norms, sanctions, and norm-sanction
 * links. It provides operations to consult and make changes to the legislation, as well as
 * observable properties which reflect the current state of the legislation.
 * 
 * @author igorcadelima
 *
 */
public final class DeJure extends Artifact {
  Legislation legislation;

  /**
   * Initialise {@link DeJure} repository based on data from the given legislative specification.
   * 
   * @param legislativeSpec path to file with the legislative specification
   * @throws IOException
   * @throws SAXException
   * @throws ParserConfigurationException
   */
  public void init(String legislativeSpec)
      throws ParserConfigurationException, SAXException, IOException {
    Document spec = NormSpecUtil.parseDocument(legislativeSpec);
    legislation = Legislations.of(spec);
    legislation.getNorms()
               .forEach(norm -> defineObsProperty(norm));
    legislation.getSanctions()
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
   * Add new norm to legislation and define observable property for it.
   * 
   * Only instances of {@link Norm} or {@link String} should be passed as argument. If {@code norm}
   * is an instance of {@link String}, then it will be parsed using {@link Norms#parse(String)}
   * before being added to the legislation.
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
   * Add new norm to legislation and define observable property for it.
   * 
   * Note that only norms with different ids from the ones present in the legislation can be added,
   * even if disabled. If an already existing norm is tried to be added, such addition attempt is
   * just ignored.
   * 
   * @param norm norm to be added
   */
  private void addNorm(Norm norm) {
    if (legislation.addNorm(norm)) {
      defineObsProperty(norm);
      defineObsProperty(Links.of(norm.getId()));
    }
  }

  /**
   * Remove norm from legislation and observable property with it.
   * 
   * @param norm norm to be removed
   */
  @LINK
  @OPERATION
  public void removeNorm(Norm norm) {
    // TODO: check whether operator agent is a legislator
    if (legislation.removeNorm(norm.getId()) != null) {
      removeObsProperty(norm);
    }
  }

  /**
   * Add new sanction to legislation and define observable property for it.
   * 
   * Note that only sanction with different ids from the ones present in the legislation can be
   * added, even if disabled. If an already existing sanction is tried to be added, such addition
   * attempt is just ignored.
   * 
   * @param sanction sanction to be added
   */
  @LINK
  @OPERATION
  public void addSanction(Sanction sanction) {
    // TODO: check whether operator agent is a legislator
    if (legislation.addSanction(sanction)) {
      defineObsProperty(sanction);
    }
  }

  /**
   * Remove sanction from legislation and observable property with it.
   * 
   * @param sanction
   * @return {@code true} if sanction was removed successfully
   */
  @LINK
  @OPERATION
  public void removeSanction(Sanction sanction) {
    // TODO: check whether operator agent is a legislator
    if (legislation.removeSanction(sanction.getId()) != null) {
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

    if (legislation.addLink(normIdAtom, sanctionIdAtom)) {
      Norm norm = legislation.getNorm(normIdAtom);
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
    Norm norm = legislation.getNorm(normIdAtom);

    if (legislation.unlink(normIdAtom, createAtom(sanctionId))) {
      updateObsProperty(norm, normIdAtom);
    }
  }

  /** Return norms through {@link out}. */
  @LINK
  @OPERATION
  public void getNorms(OpFeedbackParam<Set<Norm>> out) {
    out.set(legislation.getNorms());
  }

  /** Return sanctions through {@link out}. */
  @LINK
  @OPERATION
  public void getSanctions(OpFeedbackParam<Set<Sanction>> out) {
    out.set(legislation.getSanctions());
  }
}
