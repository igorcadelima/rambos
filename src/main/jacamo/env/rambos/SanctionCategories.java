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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import rambos.SanctionCategory.SanctionCategoryBuilder;

/**
 * Static utility methods pertaining to {@link SanctionCategory} instances.
 * 
 * @author igorcadelima
 *
 */
public final class SanctionCategories {
  private SanctionCategories() {}

  /**
   * Return a new {@link SanctionCategory} object initialised to the value represented by the
   * specified {@code Element}.
   * 
   * @param el element to be parsed
   * @return {@link SanctionCategory} object represented by {@code el}
   * @throws NullPointerException if element is {@code null}
   */
  public static SanctionCategory parse(Element el) {
    NodeList dimensions = el.getChildNodes();
    SanctionCategoryBuilder builder = new SanctionCategoryBuilder();

    for (int i = 0; i < dimensions.getLength(); i++) {
      Node dimension = dimensions.item(i);
      String dimensionContent = dimension.getTextContent()
                                         .toUpperCase();

      switch (dimension.getNodeName()) {
        case "purpose":
          builder.setPurpose(SanctionPurpose.valueOf(dimensionContent));
          break;
        case "issuer":
          builder.setIssuer(SanctionIssuer.valueOf(dimensionContent));
          break;
        case "locus":
          builder.setLocus(SanctionLocus.valueOf(dimensionContent));
          break;
        case "mode":
          builder.setMode(SanctionMode.valueOf(dimensionContent));
          break;
        case "polarity":
          builder.setPolarity(SanctionPolarity.valueOf(dimensionContent));
          break;
        case "discernability":
          builder.setDiscernability(SanctionDiscernability.valueOf(dimensionContent));
          break;
      }
    }
    return builder.build();
  }
}
