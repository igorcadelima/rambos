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

import rambos.IFact.Efficacy;
import rambos.util.Builder;

/**
 * @author igorcadelima
 *
 */
public interface IFactBuilder<T extends IFactBuilder<T>> extends Builder<IFact> {
  T setTime(long t);

  /**
   * Set time using to the given value, rounded to the closest possible representation.
   * 
   * This method rounds the argument using {@link Math#round(double)} and passes it to
   * {@link #setTime(long)}.
   * 
   * @param t time
   * @return builder instance
   */
  default T setTime(double t) {
    return setTime(Math.round(t));
  }

  T setSanctioner(String sId);

  T setSanctionee(String sId);

  T setNorm(String nId);

  T setSanction(String sId);

  T setEfficacy(Efficacy e);
}
