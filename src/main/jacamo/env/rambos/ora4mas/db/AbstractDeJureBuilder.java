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
import rambos.Norm;
import rambos.Sanction;

/**
 * @author igorcadelima
 *
 */
public abstract class AbstractDeJureBuilder extends Artifact implements IDeJureBuilder {
  // normId -> norm
  protected Map<String, Norm> norms = new ConcurrentHashMap<String, Norm>();
  // sanctionId -> sanction
  protected Map<String, Sanction> sanctions = new ConcurrentHashMap<String, Sanction>();
  // normId -> [sanctionId0, sanctionId1, ..., sanctionIdn]
  protected Map<String, Set<String>> links = new ConcurrentHashMap<String, Set<String>>();

  /**
   * Default artefact initialiser.
   */
  public void init() {}

  @LINK
  @OPERATION
  @Override
  public IDeJureBuilder setNorms(Map<String, Norm> norms) {
    this.norms = norms;
    return this;
  }

  @LINK
  @OPERATION
  @Override
  public IDeJureBuilder setSanctions(Map<String, Sanction> sanctions) {
    this.sanctions = sanctions;
    return this;
  }

  @LINK
  @OPERATION
  @Override
  public IDeJureBuilder setLinks(Map<String, Set<String>> links) {
    this.links = links;
    return this;
  }
}
