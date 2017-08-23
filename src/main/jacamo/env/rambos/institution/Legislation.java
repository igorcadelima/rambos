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

import java.util.Set;

import jason.asSyntax.Atom;
import rambos.norm.Norm;
import rambos.sanction.Sanction;

/**
 * An aggregation of norms, sanctions, and links among them.
 * 
 * @author igorcadelima
 *
 */
public interface Legislation {
  /** Return norm with given {@code id}, or {@code null} if non-existing. */
  Norm getNorm(Atom id);

  /** Return sanction with given {@code id}, or {@code null} if non-existing */
  Sanction getSanction(Atom id);

  /** Return norms. */
  Set<Norm> getNorms();

  /** Return sanctions. */
  Set<Sanction> getSanctions();

  /**
   * Add copy of {@code norm} if it is not already in the legislation.
   * 
   * @param norm norm to be added
   * @return {@code true} if the norm was successfully added
   */
  boolean addNorm(Norm norm);

  /**
   * Add copy of {@code sanction} if it is not already in the legislation.
   * 
   * @param sanction sanction to be added
   * @return {@code true} if the sanction was successfully added
   */
  boolean addSanction(Sanction sanction);

  /**
   * Link existing norm and sanction with given ids.
   * 
   * @param normId id of the norm to be linked
   * @param sanctionId id of the sanction to be linked
   * @return {@code true} if the norm and sanction with the given ids were in the legislation and
   *         there was no link between them
   */
  boolean addLink(Atom normId, Atom sanctionId);

  /**
   * Enable norm with given id.
   * 
   * @param normId id of the norm
   * @return {@code true} if a disabled norm with the given id could be found and enabled
   */
  boolean enableNorm(Atom normId);

  /**
   * Enable sanction with given id.
   * 
   * @param sanctionId id of the sanction
   * @return {@code true} if a disabled sanction with the given id could be found and enabled
   */
  boolean enableSanction(Atom sanctionId);

  /**
   * Disable norm with given id.
   * 
   * @param normId id of the norm
   * @return {@codetrue} if an enabled norm with the given id could be found and disabled
   */
  boolean disableNorm(Atom normId);

  /**
   * Disable sanction with given id.
   * 
   * @param sanctionId id of the sanction
   * @return {@code true} if an enabled sanction with the given id could be found and disabled
   */
  boolean disableSanction(Atom sanctionId);

  /**
   * Remove norm with given id.
   * 
   * @param normId id of the norm
   * @return the previous sanction with whose id is {@code sanctionId}, or {@code null} if there was
   *         no sanction for {@code sanctionId}
   */
  Norm removeNorm(Atom normId);

  /**
   * Remove sanction with given id.
   * 
   * @param sanctionId id of the sanction
   * @return the previous sanction with whose id is {@code sanctionId}, or {@code null} if there was
   *         no sanction for {@code sanctionId}
   */
  Sanction removeSanction(Atom sanctionId);

  /**
   * Unlink existing norm and sanction with given ids.
   * 
   * @param normId id of the norm
   * @param sanctionId id of the sanction
   * @return {@code true} if there was a link between the norm and sanction with the given ids
   */
  boolean unlink(Atom normId, Atom sanctionId);
}
