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

import cartago.Artifact;
import cartago.ArtifactId;
import cartago.LINK;
import cartago.OpFeedbackParam;
import cartago.OperationException;
import rambos.INorm;
import rambos.Sanction;

/**
 * @author igorcadelima
 *
 */
public abstract class DeJureParser<T> extends Artifact {
  protected ArtifactId builderId;

  /**
   * Initialise parser by passing the {@link ArtifactId} of the builder class which will be used to
   * build {@link DeJure}.
   * 
   * @param builderId {@link ArtifactId} of the builder class
   */
  public void init(ArtifactId builderId) {
    this.builderId = builderId;
  }

  /**
   * Parse the normative specification creating, create a {@link DeJure} artefact and return its
   * {@link ArtifactId} using the {@code deJureOut} output parameter.
   * 
   * @param ns normative specification
   * @param deJureName name of the {@link DeJure} repository
   * @param deJureOut output parameter which will hold the {@link ArtifactId} of the {@link DeJure}
   *        repository
   */
  public abstract void parse(T ns, String deJureName, OpFeedbackParam<ArtifactId> deJureOut);

  /**
   * Extract and return norms map.
   * 
   * @param ns normative specification
   * @return {@link Map} containing norm id as key and {@link INorm} as value
   */
  protected abstract Map<String, INorm> extractNorms(T ns);

  /**
   * Extract and return sanctions map.
   * 
   * @param ns normative specification
   * @return {@link Map} containing sanction id as key and {@link Sanction} as value
   */
  protected abstract Map<String, Sanction> extractSanctions(T ns);

  /**
   * Extract and return links map.
   * 
   * @param ns normative specification
   * @return {@link Map} containing norm id as key and sanction id as value
   */
  protected abstract Map<String, Set<String>> extractLinks(T ns);

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
