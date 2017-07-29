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
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.parser.ParseException;
import rambos.Norm.NormBuilder;
import rambos.util.Enums;

/**
 * Static utility methods pertaining to {@link INorm} instances.
 * 
 * @author igorcadelima
 *
 */
public final class Norms {
  public static final String FUNCTOR = "norm";

  private Norms() {}

  /**
   * Return a new norm initialised to the value represented by the specified {@code String}.
   * 
   * @param in string to be parsed
   * @return norm represented by the string argument
   * @throws IllegalArgumentException if string does not contain a parsable norm
   * @throws NullPointerException if string is {@code null}
   */
  public static INorm parse(String in) {
    try {
      Literal l = ASSyntax.parseLiteral(in);
      Atom id = (Atom) l.getTerm(0);
      Status status = Enums.lookup(Status.class, l.getTerm(1)
                                                  .toString());
      LogicalFormula condition = (LogicalFormula) l.getTerm(2);
      Atom issuer = (Atom) l.getTerm(3);
      IContent content = new ContentStringParser().parse(l.getTerm(4)
                                                          .toString());
      return new Norm.NormBuilder().setId(id)
                                   .setStatus(status)
                                   .setCondition(condition)
                                   .setIssuer(issuer)
                                   .setContent(content)
                                   .build();
    } catch (ParseException e) {
      throw new IllegalArgumentException("String does not contain a parsable norm");
    }
  }

  public static INorm parse(Element el) {
    NormBuilder builder = new NormBuilder();
    builder.setId(ASSyntax.createAtom(el.getAttribute("id")))
           .setStatus(Enums.lookup(Status.class, el.getAttribute("state"), Status.ENABLED));

    NodeList properties = el.getChildNodes();
    for (int i = 0; i < properties.getLength(); i++) {
      Node prop = properties.item(i);
      String propContent = prop.getTextContent();

      switch (prop.getNodeName()) {
        case "condition":
          try {
            builder.setCondition(ASSyntax.parseFormula(propContent));
          } catch (ParseException e) {
            throw new IllegalArgumentException("Element does not contain a parsable norm");
          }
          break;
        case "issuer":
          builder.setIssuer(ASSyntax.createAtom(propContent));
          break;
        case "content":
          builder.setContent(new ContentStringParser().parse(propContent));
          break;
      }
    }
    return builder.build();
  }
}
