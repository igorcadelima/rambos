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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cartago.Artifact;
import cartago.ArtifactId;
import cartago.LINK;
import cartago.OpFeedbackParam;
import cartago.OperationException;
import jason.asSyntax.Atom;
import rambos.common.norm.Norm;
import rambos.common.sanction.Sanction;

/**
 * @author igorcadelima
 *
 */
abstract class AbstractDeJureBuilder extends Artifact {
  // normId -> norm
  protected Map<Atom, Norm> norms = new ConcurrentHashMap<Atom, Norm>();
  // sanctionId -> sanction
  protected Map<Atom, Sanction> sanctions = new ConcurrentHashMap<Atom, Sanction>();
  // normId -> [sanctionId0, sanctionId1, ..., sanctionIdn]
  protected Map<Atom, Set<Atom>> links = new ConcurrentHashMap<Atom, Set<Atom>>();

  /**
   * Default artefact initialiser.
   */
  void init() {}

  /**
   * Set norms.
   * 
   * @param norms mapping of norms ids to norms themselves
   * @return builder after setting norms
   */
  @LINK
  AbstractDeJureBuilder norms(Map<Atom, Norm> norms) {
    this.norms = norms;
    return this;
  }

  /**
   * Set sanctions.
   * 
   * @param sanctions mapping of sanctions ids to sanctions themselves
   * @return builder after setting sanctions
   */
  @LINK
  AbstractDeJureBuilder sanctions(Map<Atom, Sanction> sanctions) {
    this.sanctions = sanctions;
    return this;
  }

  /**
   * Set links between norms and sanctions.
   * 
   * @param links mapping of norms ids to a set of sanctions ids
   * @return builder after setting links
   */
  @LINK
  AbstractDeJureBuilder links(Map<Atom, Set<Atom>> links) {
    this.links = links;
    return this;
  }

  /**
   * @param name name of the repository artefact
   * @param out output parameter
   * @throws OperationException
   */
  abstract void build(String name, OpFeedbackParam<ArtifactId> out) throws OperationException;
}
