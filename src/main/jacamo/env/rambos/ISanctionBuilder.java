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
package rambos;

import jason.asSyntax.LogicalFormula;
import rambos.util.Builder;

/**
 * @author igorcadelima
 *
 */
public interface ISanctionBuilder<T extends ISanctionBuilder<T>> extends Builder<ISanction> {
  /**
   * Set id for the sanction.
   * 
   * @param id sanction's id
   * @return builder builder instance
   */
  T setId(String id);

  /**
   * Set status of the sanction.
   * 
   * @param status sanction's status
   * @return builder builder instance
   */
  T setStatus(Status status);

  /**
   * Set condition for the sanction.
   * 
   * @param condition sanction's activation condition
   * @return builder builder instance
   */
  T setCondition(LogicalFormula condition);

  /**
   * Set category for the sanction.
   * 
   * @param category sanction category
   * @return builder builder instance
   */
  T setCategory(SanctionCategory category);

  /**
   * Set content for the norm.
   * 
   * @param content norm's content
   * @return builder builder instance
   */
  T setContent(IContent content);

  /**
   * Build sanction based on the data provided using the setter methods.
   * 
   * @return new sanction
   */
  ISanction build();
}
