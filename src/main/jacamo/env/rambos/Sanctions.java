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

import jason.asSyntax.ASSyntax;
import jason.asSyntax.parser.ParseException;
import rambos.Sanction.SanctionBuilder;
import rambos.util.Enums;

/**
 * Static utility methods pertaining to {@link ISanction} instances.
 * 
 * @author igorcadelima
 *
 */
public final class Sanctions {
  public static final String FUNCTOR = "sanction";

  private Sanctions() {}

  /**
   * Return a new {@link ISanction} instance initialised to the value represented by the specified
   * {@code Element}.
   * 
   * @param el element to be parsed
   * @return {@link ISanction} instance represented by {@code el}
   * @throws NullPointerException if element is {@code null}
   */
  public static ISanction parse(Element el) {
    SanctionBuilder builder = new SanctionBuilder();
    builder.setId(el.getAttribute("id"))
           .setState(Enums.lookup(State.class, el.getAttribute("state"), State.ENABLED));

    NodeList properties = el.getChildNodes();
    for (int i = 0; i < properties.getLength(); i++) {
      Node p = properties.item(i);

      switch (p.getNodeName()) {
        case "condition":
          try {
            builder.setCondition(ASSyntax.parseFormula(p.getTextContent()));
          } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
          break;
        case "category":
          builder.setCategory(SanctionCategories.parse((Element) p));
          break;
        case "content":
          builder.setContent(new ContentStringParser().parse(p.getTextContent()));
      }
    }
    return builder.build();
  }
}
