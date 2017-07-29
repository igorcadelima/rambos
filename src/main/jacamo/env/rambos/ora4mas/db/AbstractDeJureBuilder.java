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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cartago.Artifact;
import cartago.LINK;
import cartago.OPERATION;
import jason.asSyntax.Atom;
import rambos.INorm;
import rambos.ISanction;

/**
 * @author igorcadelima
 *
 */
public abstract class AbstractDeJureBuilder extends Artifact implements IDeJureBuilder {

  // normId -> norm
  protected Map<Atom, INorm> norms = new ConcurrentHashMap<Atom, INorm>();
  // sanctionId -> sanction
  protected Map<Atom, ISanction> sanctions = new ConcurrentHashMap<Atom, ISanction>();
  // normId -> [sanctionId0, sanctionId1, ..., sanctionIdn]
  protected Map<Atom, Set<Atom>> links = new ConcurrentHashMap<Atom, Set<Atom>>();

  /**
   * Default artefact initialiser.
   */
  public void init() {}

  @LINK
  @OPERATION
  @Override
  public IDeJureBuilder setNorms(Map<Atom, INorm> norms) {
    this.norms = norms;
    return this;
  }

  @LINK
  @OPERATION
  @Override
  public IDeJureBuilder setSanctions(Map<Atom, ISanction> sanctions) {
    this.sanctions = sanctions;
    return this;
  }

  @LINK
  @OPERATION
  @Override
  public IDeJureBuilder setLinks(Map<Atom, Set<Atom>> links) {
    this.links = links;
    return this;
  }
}
