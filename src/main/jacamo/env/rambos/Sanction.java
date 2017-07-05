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
public class Sanction extends AbstractSanction {

  /**
   * @param id
   * @param state
   * @param condition
   * @param category
   */
  public Sanction(String id, State state, LogicalFormula condition, SanctionCategory category,
      IContent content) {
    this.id = id;
    this.state = state;
    this.condition = condition;
    this.category = category;
    this.content = content;
  }

  /**
   * Sanction constructor.
   * 
   * In order to successfully create a sanction, none of the builder's attributes should be
   * {@code null}.
   * 
   * @param builder
   */
  private Sanction(SanctionBuilder builder) {
    if ((builder.id != null) && (builder.condition != null) && (builder.category != null)
        && (builder.content != null)) {
      this.id = builder.id;
      this.state = builder.state;
      this.condition = builder.condition;
      this.category = builder.category;
      this.content = builder.content;
    } else {
      throw new RuntimeException(
          "The following properties should not be null: id, condition, category, content");
    }
  }

  public static final class SanctionBuilder extends AbstractSanctionBuilder<SanctionBuilder> {

    @Override
    protected SanctionBuilder getThis() {
      return this;
    }

    @Override
    public Sanction build() {
      return new Sanction(getThis());
    }
  }
}
