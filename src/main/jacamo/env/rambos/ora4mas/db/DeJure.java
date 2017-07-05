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
import jason.asSyntax.Literal;
import jason.asSyntax.parser.ParseException;
import rambos.INorm;
import rambos.Sanction;

/**
 * @author igorcadelima
 *
 */
public class DeJure extends Artifact {
  // normId -> norm
  private Map<String, INorm> norms = new ConcurrentHashMap<String, INorm>();
  // sanctionId -> sanction
  private Map<String, Sanction> sanctions;
  // normId -> [sanctionId0, sanctionId1, ..., sanctionIdn]
  private Map<String, Set<String>> links = new ConcurrentHashMap<String, Set<String>>();

  /**
   * Initialise a {@link DeJure} repository based on data from the {@link DeJureBuilder} passed as
   * argument.
   * 
   * @param builder builder from which data should be obtained
   * @throws ParseException
   */
  @SuppressWarnings("unused")
  private void init(DeJureBuilder builder) throws ParseException {
    addNorms(builder.norms.values()
                          .toArray(new INorm[0]));
    sanctions = builder.sanctions;
    builder.links.forEach((k, v) -> {
      v.forEach(s -> addLink(k, s));
    });
  }

  /**
   * Add new norm into norms set.
   * 
   * This method just calls {@link #addNorms(INorm...)} passing {@code n} as argument.
   * 
   * @param n norm to be added
   * @throws ParseException
   */
  @LINK
  @OPERATION
  public void addNorm(INorm n) {
    addNorms(n);
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
   * @param ns norms to be added
   */
  @LINK
  @OPERATION
  public void addNorms(INorm... ns) {
    // TODO: check whether operator agent is a legislator
    for (INorm n : ns) {
      if (norms.containsKey(n.getId()))
        continue;

      try {
        Literal literalNorm = ASSyntax.parseLiteral(n.toString());
        norms.put(n.getId(), n);
        defineObsProperty("norm", literalNorm.getTerms()
                                             .toArray());
      } catch (ParseException e) {
        continue;
      }

      // Create empty set and link it to norm
      links.put(n.getId(), new HashSet<String>());
      defineObsProperty("link", ASSyntax.createAtom(n.getId()), new String[0]);
    }
  }

  /**
   * Remove norm from norms set.
   * 
   * @param n
   * @return true if norm is removed successfully
   */
  @LINK
  @OPERATION
  public synchronized boolean removeNorm(INorm n) {
    // TODO: check whether operator agent is a legislator
    if (norms.remove(n.getId()) == n) {
      links.remove(n.getId());
      return true;
    }
    return false;
  }

  /**
   * Add sanction to sanctions set.
   * 
   * @param s sanction to be added
   * @return true if sanction is added successfully
   */
  @LINK
  @OPERATION
  public synchronized boolean addSanction(Sanction s) {
    // TODO: check whether operator agent is a legislator
    if (sanctions.put(s.getId(), s) == s) {
      return true;
    }
    return false;
  }

  /**
   * Remove sanction from sanctions set.
   * 
   * @param s
   * @return true if sanction is removed successfully
   */
  @LINK
  @OPERATION
  public synchronized boolean removeSanction(Sanction s) {
    // TODO: check whether operator agent is a legislator
    if (sanctions.remove(s.getId()) == s) {
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
    INorm n = norms.get(normId);
    Sanction s = sanctions.get(sanctionId);

    Set<String> linkedSanctions = links.get(normId);
    if (linkedSanctions.add(s.getId())) {
      links.put(normId, linkedSanctions);
      updateObsProperty("link", ASSyntax.createAtom(n.getId()), linkedSanctions.toArray());
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
    INorm n = norms.get(normId);
    Sanction s = sanctions.get(sanctionId);
    return removeLink(n, s);
  }

  /**
   * Remove link from links set.
   * 
   * @param n the norm
   * @param s the sanction
   * @return true if link is destroyed successfully
   */
  @LINK
  @OPERATION
  public synchronized boolean removeLink(INorm n, Sanction s) {
    // TODO: check whether operator agent is a legislator
    if (n == null || s == null)
      return false;

    Set<String> linkedSanctions = links.get(n.getId());
    if (linkedSanctions.remove(s.getId())) {
      links.put(n.getId(), linkedSanctions);
      return true;
    }

    return false;
  }

  /**
   * Return norms set through {@link out}.
   * 
   * @return norms
   */
  @LINK
  @OPERATION
  public void getNorms(OpFeedbackParam<Set<INorm>> out) {
    Set<INorm> set = new HashSet<INorm>(norms.values());
    out.set(set);
  }

  /**
   * Get sanction set as a {@code {@link Map}<{@link String}, {@link Sanction}>}, whose keys are
   * sanctions ids.
   * 
   * @return copy of the sanctions
   */
  @LINK
  @OPERATION
  public Map<String, Sanction> getSanctions() {
    return new ConcurrentHashMap<String, Sanction>(sanctions);
  }

  /**
   * Get links set as a {@code {@link Map}<{@link String}, {@link Set}<{@link String}>>}, whose keys
   * are norms ids.
   * 
   * @return copy of the links
   */
  @LINK
  @OPERATION
  public Map<String, Set<String>> getLinks() {
    return new ConcurrentHashMap<String, Set<String>>(links);
  }

  public static class DeJureBuilder extends AbstractDeJureBuilder {
    @Override
    @LINK
    public void build(String name, OpFeedbackParam<ArtifactId> out) throws OperationException {
      out.set(makeArtifact(name, DeJure.class.getName(), new ArtifactConfig(this)));
    }

    /**
     * Destroy this artefact by calling {@link Artifact#dispose(ArtifactId)}.
     * 
     * @throws OperationException
     */
    @LINK
    public void destroy() throws OperationException {
      try {
        dispose(getId());
      } catch (Exception e) {
        /*
         * CArtAgO's bug. An issue has been created in
         * https://github.com/CArtAgO-lang/cartago/issues/2
         */
      }
    }
  }
}
