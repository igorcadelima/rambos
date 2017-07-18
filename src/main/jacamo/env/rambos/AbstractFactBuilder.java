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

/**
 * @author igorcadelima
 *
 */
public abstract class AbstractFactBuilder<T extends AbstractFactBuilder<T>>
    implements IFactBuilder<T> {
  protected long time;
  protected String sanctionerId;
  protected String sanctioneeId;
  protected String norm;
  protected String sanction;
  protected Boolean efficacy;

  /**
   * Get current instance of the class and return it.
   * 
   * @return current instance of the class
   * @see FactBuilder#getThis()
   */
  protected abstract T getThis();

  @Override
  public T setTime(long t) {
    time = t;
    return getThis();
  }

  @Override
  public T setSanctioner(String sId) {
    sanctionerId = sId;
    return getThis();
  }

  @Override
  public T setSanctionee(String sId) {
    sanctioneeId = sId;
    return getThis();
  }

  @Override
  public T setNorm(String nId) {
    norm = nId;
    return getThis();
  }

  @Override
  public T setSanction(String sId) {
    sanction = sId;
    return getThis();
  }

  @Override
  public T setEfficacy(Boolean e) {
    efficacy = e;
    return getThis();
  }
}