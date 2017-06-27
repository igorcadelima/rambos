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

/**
 * @author igorcadelima
 *
 */
public interface INormBuilder<T extends INormBuilder<T>> extends Builder<INorm>{
  /**
   * Set id for the norm.
   * 
   * @param id norm's id
   * @return builder builder instance
   */
  T setId(String id);

  /**
   * Set state for the norm.
   * 
   * @param state norm's state
   * @return builder builder instance
   */
  T setState(State state);

  /**
   * Set condition for the norm.
   * 
   * @param condition norm's activation condition
   * @return builder builder instance
   */
  T setCondition(LogicalFormula condition);

  /**
   * Set issuer for the norm.
   * 
   * @param issuer norm's issuer
   * @return builder builder instance
   */
  T setIssuer(String issuer);

  /**
   * Set content for the norm.
   * 
   * @param content norm's content
   * @return builder builder instance
   */
  T setContent(IContent content);

  /**
   * Build norm based on the data provided using the setter methods.
   * 
   * @return new norm
   */
  INorm build();
}
