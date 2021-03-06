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
package rambos.sanction;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import rambos.common.Enums;
import rambos.sanction.BasicSanctionCategory.Builder;

/**
 * Static utility methods pertaining to {@link SanctionCategory} instances.
 * 
 * @author igorcadelima
 *
 */
final class SanctionCategories {
  private SanctionCategories() {}

  /**
   * Return a new {@link BasicSanctionCategory} object initialised to the value represented by the
   * specified {@code Element}.
   * 
   * @param el element to be parsed
   * @return {@link BasicSanctionCategory} object represented by {@code el}
   * @throws NullPointerException if element is {@code null}
   */
  public static BasicSanctionCategory of(Element el) {
    NodeList dimensions = el.getChildNodes();
    Builder builder = new Builder();

    for (int i = 0; i < dimensions.getLength(); i++) {
      Node dimensionNode = dimensions.item(i);
      String dimension = dimensionNode.getTextContent();

      switch (dimensionNode.getNodeName()) {
        case "purpose":
          builder.purpose(Enums.lookup(SanctionPurpose.class, dimension));
          break;
        case "issuer":
          builder.issuer(Enums.lookup(SanctionIssuer.class, dimension));
          break;
        case "locus":
          builder.locus(Enums.lookup(SanctionLocus.class, dimension));
          break;
        case "mode":
          builder.mode(Enums.lookup(SanctionMode.class, dimension));
          break;
        case "polarity":
          builder.polarity(Enums.lookup(SanctionPolarity.class, dimension));
          break;
        case "discernability":
          builder.discernability(Enums.lookup(SanctionDiscernability.class, dimension));
          break;
      }
    }
    return builder.build();
  }
}
