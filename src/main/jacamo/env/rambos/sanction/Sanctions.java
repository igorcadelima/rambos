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

import jason.asSyntax.ASSyntax;
import jason.asSyntax.parser.ParseException;
import rambos.common.Enums;
import rambos.common.Status;
import rambos.sanction.BasicSanction.Builder;

/**
 * Static utility methods pertaining to {@link Sanction} instances.
 * 
 * @author igorcadelima
 *
 */
public final class Sanctions {
  private Sanctions() {}

  /**
   * Return a copy of the given sanction.
   * 
   * @param sanction sanction to be copied
   * @return copy of {@code sanction}
   */
  public static Sanction newInstance(Sanction sanction) {
    return new BasicSanction.Builder().id(sanction.getId())
                                      .status(sanction.getStatus())
                                      .condition(sanction.getCondition())
                                      .category(sanction.getCategory())
                                      .content(sanction.getContent())
                                      .build();
  }

  /**
   * Return a new {@link Sanction} instance initialised to the value represented by the specified
   * {@code Element}.
   * 
   * @param el element to be parsed
   * @return {@link Sanction} instance represented by {@code el}
   * @throws NullPointerException if element is {@code null}
   */
  public static Sanction parse(Element el) {
    Builder builder = new Builder();
    builder.id(ASSyntax.createAtom(el.getAttribute("id")))
           .status(Enums.lookup(Status.class, el.getAttribute("status"), Status.ENABLED));

    NodeList props = el.getChildNodes();
    for (int i = 0; i < props.getLength(); i++) {
      Node prop = props.item(i);

      try {
        switch (prop.getNodeName()) {
          case "condition":
            builder.condition(ASSyntax.parseFormula(prop.getTextContent()));
            break;
          case "category":
            builder.category(SanctionCategories.of((Element) prop));
            break;
          case "content":
            builder.content(ASSyntax.parseFormula(prop.getTextContent()));
          default:
            // Ignore
        }
      } catch (ParseException e) {
        e.printStackTrace();
      }
    }
    return builder.build();
  }
}
