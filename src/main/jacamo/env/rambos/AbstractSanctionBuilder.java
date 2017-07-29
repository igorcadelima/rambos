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

import jason.asSyntax.Atom;
import jason.asSyntax.LogicalFormula;

/**
 * @author igorcadelima
 *
 */
public abstract class AbstractSanctionBuilder<T extends AbstractSanctionBuilder<T>>
    implements ISanctionBuilder<T> {
  protected Atom id;
  protected Status status = Status.ENABLED;
  protected LogicalFormula condition;
  protected SanctionCategory category;
  protected IContent content;

  /**
   * Get current instance of the class and return it.
   * 
   * @return current instance of the class
   * @see SanctionBuilder#getThis()
   */
  protected abstract T getThis();

  @Override
  public T setId(Atom id) {
    this.id = id;
    return getThis();
  }

  @Override
  public T setStatus(Status status) {
    this.status = status;
    return getThis();
  }

  @Override
  public T setCondition(LogicalFormula condition) {
    this.condition = condition;
    return getThis();
  }

  @Override
  public T setCategory(SanctionCategory category) {
    this.category = category;
    return getThis();
  }

  @Override
  public T setContent(IContent content) {
    this.content = content;
    return getThis();
  }
}
