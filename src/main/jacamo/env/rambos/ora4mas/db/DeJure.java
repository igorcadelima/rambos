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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cartago.Artifact;
import cartago.ArtifactConfig;
import cartago.ArtifactId;
import cartago.LINK;
import cartago.OPERATION;
import cartago.OpFeedbackParam;
import cartago.OperationException;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.parser.ParseException;
import rambos.INorm;
import rambos.ISanction;
import rambos.Norms;

/**
 * @author igorcadelima
 *
 */
public class DeJure extends Artifact {
  // normId -> norm
  private Map<Atom, INorm> norms = new ConcurrentHashMap<Atom, INorm>();
  // sanctionId -> sanction
  private Map<Atom, ISanction> sanctions = new ConcurrentHashMap<Atom, ISanction>();
  // normId -> [sanctionId0, sanctionId1, ..., sanctionIdn]
  private Map<Atom, Set<Atom>> links = new ConcurrentHashMap<Atom, Set<Atom>>();

  /**
   * Initialise a {@link DeJure} repository based on data from the {@link DeJureBuilder} passed as
   * argument.
   * 
   * @param builder builder from which data should be obtained
   * @throws ParseException
   */
  @SuppressWarnings("unused")
  private void init(DeJureBuilder builder) throws ParseException {
    builder.norms.forEach((name, norm) -> addNorm(norm));
    builder.sanctions.forEach((name, sanction) -> addSanction(sanction));
    builder.links.forEach((norm, sanctions) -> {
      sanctions.forEach(sanction -> addLink(norm.toString(), sanction.toString()));
    });
  }

  /**
   * Add new norm into norms set.
   * 
   * Only instances of {@link INorm} or {@link String} should be passed as argument. For both cases,
   * the norm will be added using {@link #addNorms(INorm...)}. If {@code n} is an instance of
   * {@link String}, then it will be parsed using {@link Norms#parse(String)} before being added to
   * the norms set.
   * 
   * @param norm norm to be added
   */
  @LINK
  @OPERATION
  public <T> void addNorm(T norm) {
    if (norm instanceof INorm)
      addNorms((INorm) norm);
    else if (norm instanceof String)
      addNorms(Norms.parse((String) norm));
    else
      failed("Expected " + String.class.getCanonicalName() + " or " + INorm.class.getCanonicalName()
          + " but got " + norm.getClass()
                              .getCanonicalName());
  }

  /**
   * Add new norms into norms set.
   * 
   * For each added norm, an empty set is created and linked to it in the links set. Additionally,
   * observable properties are created for the norm and the link created.
   * 
   * Only norms with different ids from the ones in the set can be added. If an already existing
   * norm is tried to be added, such addition is just ignored.
   * 
   * @param norms norms to be added
   */
  @LINK
  @OPERATION
  public void addNorms(INorm... norms) {
    // TODO: check whether operator agent is a legislator
    for (INorm norm : norms) {
      if (this.norms.containsKey(norm.getId()))
        continue;

      try {
        Literal literalNorm = ASSyntax.parseLiteral(norm.toString());
        this.norms.put(norm.getId(), norm);
        defineObsProperty("norm", literalNorm.getTerms()
                                             .toArray());
      } catch (ParseException e) {
        continue;
      }

      // Create empty set and link it to norm
      links.put(norm.getId(), new HashSet<Atom>());
      defineObsProperty("link", norm.getId(), new String[0]);
    }
  }

  /**
   * Remove norm from norms set.
   * 
   * @param norm
   * @return true if norm is removed successfully
   */
  @LINK
  @OPERATION
  public synchronized boolean removeNorm(INorm norm) {
    // TODO: check whether operator agent is a legislator
    if (norms.remove(norm.getId()) == norm) {
      links.remove(norm.getId());
      return true;
    }
    return false;
  }

  /**
   * Add new sanction into sanctions set.
   * 
   * Only new sanctions can be added into the set. If this happens successfully, a observable
   * property is created to inform such addition.
   * 
   * If an already existing sanction is tried to be added, such action is just ignored and nothing
   * happens.
   * 
   * @param sanction sanction to be added
   */
  @LINK
  @OPERATION
  public void addSanction(ISanction sanction) {
    // TODO: check whether operator agent is a legislator
    if (sanctions.containsKey(sanction.getId()))
      return;

    try {
      Literal literalSanction = ASSyntax.parseLiteral(sanction.toString());
      sanctions.put(sanction.getId(), sanction);
      defineObsProperty("sanction", literalSanction.getTerms()
                                                   .toArray());
    } catch (ParseException e) {
      return;
    }
  }

  /**
   * Remove sanction from sanctions set.
   * 
   * @param sanction
   * @return true if sanction is removed successfully
   */
  @LINK
  @OPERATION
  public synchronized boolean removeSanction(ISanction sanction) {
    // TODO: check whether operator agent is a legislator
    if (sanctions.remove(sanction.getId()) == sanction) {
      return true;
    }
    return false;
  }

  /**
   * Add link between norm and sanction to links set.
   * 
   * The observable property containing the link is also updated to reflect the changes in the links
   * set. However, if there is already a link between the norm and the sanction, no changes at all
   * are performed.
   * 
   * @param normId norm id
   * @param sanctionId sanction id
   * @throws NullPointerException if any of the arguments are {@code null}
   */
  @LINK
  @OPERATION
  public void addLink(String normId, String sanctionId) {
    // TODO: check whether operator agent is a legislator
    Atom normIdAtom = ASSyntax.createAtom(normId);
    Atom sanctionIdAtom = ASSyntax.createAtom(sanctionId);

    if (!norms.containsKey(normIdAtom)) {
      failed("No norm found with id " + normId);
    } else if (!sanctions.containsKey(sanctionIdAtom)) {
      failed("No sanction found with id " + sanctionId);
    }

    Set<Atom> linkedSanctions = links.get(normIdAtom);
    if (linkedSanctions.add(sanctionIdAtom)) {
      updateObsProperty("link", normIdAtom, linkedSanctions.toArray());
    }
  }

  /**
   * Remove link from links set.
   * 
   * @param normId the norm id
   * @param sanctionId the sanction id
   * @return true if link is destroyed successfully
   */
  @LINK
  @OPERATION
  public synchronized boolean removeLink(String normId, String sanctionId) {
    // TODO: check whether operator agent is a legislator
    INorm norm = norms.get(ASSyntax.createAtom(normId));
    ISanction sanction = sanctions.get(ASSyntax.createAtom(sanctionId));
    return removeLink(norm, sanction);
  }

  /**
   * Remove link from links set.
   * 
   * @param norm the norm
   * @param sanction the sanction
   * @return true if link is destroyed successfully
   */
  @LINK
  @OPERATION
  public synchronized boolean removeLink(INorm norm, ISanction sanction) {
    // TODO: check whether operator agent is a legislator
    if (norm == null || sanction == null)
      return false;

    Set<Atom> linkedSanctions = links.get(norm.getId());
    if (linkedSanctions.remove(sanction.getId())) {
      links.put(norm.getId(), linkedSanctions);
      return true;
    }

    return false;
  }

  /**
   * Return norms set through {@link out}.
   * 
   * @return out output parameter to return norms set
   */
  @LINK
  @OPERATION
  public void getNorms(OpFeedbackParam<Set<INorm>> out) {
    Set<INorm> set = new HashSet<INorm>(norms.values());
    out.set(set);
  }

  /**
   * Get sanction set as a {@code {@link Map}<{@link String}, {@link ISanction}>}, whose keys are
   * sanctions ids.
   * 
   * @return copy of the sanctions
   */
  @LINK
  @OPERATION
  public Map<Atom, ISanction> getSanctions() {
    return new ConcurrentHashMap<Atom, ISanction>(sanctions);
  }

  /**
   * Get links set as a {@code {@link Map}<{@link String}, {@link Set}<{@link String}>>}, whose keys
   * are norms ids.
   * 
   * @return copy of the links
   */
  @LINK
  @OPERATION
  public Map<Atom, Set<Atom>> getLinks() {
    return new ConcurrentHashMap<Atom, Set<Atom>>(links);
  }

  public static class DeJureBuilder extends AbstractDeJureBuilder {
    @Override
    @LINK
    public void build(String name, OpFeedbackParam<ArtifactId> out) throws OperationException {
      out.set(makeArtifact(name, DeJure.class.getName(), new ArtifactConfig(this)));
    }
  }
}
