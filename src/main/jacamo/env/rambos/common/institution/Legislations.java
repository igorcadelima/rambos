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
package rambos.common.institution;

import static jason.asSyntax.ASSyntax.createAtom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import rambos.common.norm.Norms;
import rambos.common.sanction.Sanctions;

/**
 * Static utility methods pertaining to {@link Legislation} instances.
 * 
 * @author igorcadelima
 *
 */
public final class Legislations {
  private static final String SCHEMA_PATH = "/xsd/legislation.xsd";
  private static final String NORMS_TAG = "norms";
  private static final String SANCTIONS_TAG = "sanctions";
  private static final String LINKS_TAG = "links";
  private static final String NORM_ID_TAG = "norm-id";
  private static final String SANCTION_IDS_TAG = "sanction-ids";

  private Legislations() {}

  /** Return a legislation instance based on {@code spec}. */
  public static Legislation of(Document spec) {
    try {
      NormSpecUtil.validate(spec, SCHEMA_PATH);
      Legislation legislation = new BasicLegislation();
      extractNorms(spec, legislation);
      extractSanctions(spec, legislation);
      extractLinks(spec, legislation);
      return legislation;
    } catch (SAXException | IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /** Extract norms from {@code spec} and add them to {@code legislation}. */
  private static void extractNorms(Document spec, Legislation legislation) {
    Node normsRootEl = spec.getElementsByTagName(NORMS_TAG)
                           .item(0);
    List<Element> norms = getChildElements(normsRootEl);
    norms.forEach(normEl -> legislation.addNorm(Norms.of(normEl)));
  }

  /** Extract sanctions from {@code spec} and add them to {@code legislation}. */
  private static void extractSanctions(Document spec, Legislation legislation) {
    Node sanctionsRootEl = spec.getElementsByTagName(SANCTIONS_TAG)
                               .item(0);
    List<Element> sanctions = getChildElements(sanctionsRootEl);
    sanctions.forEach(sanctionEl -> legislation.addSanction(Sanctions.parse(sanctionEl)));
  }

  /** Extract links from {@code spec} and add them to {@code legislation}. */
  private static void extractLinks(Document spec, Legislation legislation) {
    Node linksNode = spec.getElementsByTagName(LINKS_TAG)
                         .item(0);
    List<Element> links = getChildElements(linksNode);
    links.forEach(l -> {
      String normId = l.getElementsByTagName(NORM_ID_TAG)
                       .item(0)
                       .getTextContent();
      Node sanctionIdsNode = l.getElementsByTagName(SANCTION_IDS_TAG)
                              .item(0);
      List<Element> sanctionIds = getChildElements(sanctionIdsNode);
      sanctionIds.forEach(sanctionEl -> legislation.addLink(createAtom(normId),
          createAtom(sanctionEl.getTextContent())));
    });
  }

  /**
   * Extract and return {@code parent} node's child elements.
   * 
   * @param parent parent node
   * @return list of child elements
   */
  private static List<Element> getChildElements(Node parent) {
    List<Element> nodes = new ArrayList<Element>();
    NodeList childNodes = parent.getChildNodes();

    for (int i = 0; i < childNodes.getLength(); i++) {
      Node normNode = childNodes.item(i);

      if (normNode.getNodeType() == Node.ELEMENT_NODE) {
        Element normEl = (Element) normNode;
        nodes.add(normEl);
      }
    }
    return nodes;
  }
}
