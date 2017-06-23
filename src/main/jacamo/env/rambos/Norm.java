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

public class Norm extends AbstractNorm {

  /**
   * Norm constructor.
   * 
   * In order to successfully create a norm, none of the builder's attributes should be
   * {@code null}.
   * 
   * @param builder builder from which data should be obtained
   */
  private Norm(NormBuilder builder) {
    if ((builder.id != null) && (builder.condition != null) && (builder.issuer != null)
        && (builder.content != null)) {
      id = builder.id;
      disabled = builder.disabled;
      condition = builder.condition;
      issuer = builder.issuer;
      content = builder.content;
    } else {
      throw new RuntimeException(
          "The following properties should not be null: id, condition, issuer, content.");
    }
  }

  public static final class NormBuilder extends AbstractNormBuilder<NormBuilder> {

    @Override
    protected NormBuilder getThis() {
      return this;
    }

    @Override
    public Norm build() {
      return new Norm(getThis());
    }

  }

}
