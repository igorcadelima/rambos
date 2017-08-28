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
package rambos.institution;

import static jason.asSyntax.ASSyntax.createAtom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import rambos.norm.Norms;
import rambos.sanction.Sanctions;

/**
 * Static utility methods pertaining to {@link RegulativeSpec} instances.
 * 
 * @author igorcadelima
 *
 */
public final class RegulativeSpecs {
  private static final String SCHEMA_PATH = "/xsd/regulative-spec.xsd";
  private static final String NORMS_TAG = "norms";
  private static final String SANCTIONS_TAG = "sanctions";
  private static final String LINKED_SANCTIONS_TAG = "linked-sanctions";

  private RegulativeSpecs() {}

  /** Return a regulative specification instance based on the {@code regulativeSpec} file. */
  public static RegulativeSpec fromFile(String file) {
    try {
      Document specDoc = NormSpecUtil.parseDocument(file, SCHEMA_PATH);
      RegulativeSpec regulativeSpec = new BasicRegulativeSpec();
      extractNorms(specDoc, regulativeSpec);
      extractSanctions(specDoc, regulativeSpec);
      extractLinks(specDoc, regulativeSpec);
      return regulativeSpec;
    } catch (ParserConfigurationException | SAXException | IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /** Extract norms from {@code specDoc} and add them to {@code regulativeSpec}. */
  private static void extractNorms(Document specDoc, RegulativeSpec regulativeSpec) {
    Node normsRootEl = specDoc.getElementsByTagName(NORMS_TAG)
                              .item(0);
    List<Element> norms = getChildElements(normsRootEl);
    norms.forEach(normEl -> regulativeSpec.addNorm(Norms.of(normEl)));
  }

  /** Extract sanctions from {@code specDoc} and add them to {@code regulativeSpec}. */
  private static void extractSanctions(Document specDoc, RegulativeSpec regulativeSpec) {
    Node sanctionsRootEl = specDoc.getElementsByTagName(SANCTIONS_TAG)
                                  .item(0);
    List<Element> sanctions = getChildElements(sanctionsRootEl);
    sanctions.forEach(sanctionEl -> regulativeSpec.addSanction(Sanctions.parse(sanctionEl)));
  }

  /** Extract links from {@code specDoc} and add them to {@code regulativeSpec}. */
  private static void extractLinks(Document specDoc, RegulativeSpec regulativeSpec) {
    NodeList linkedSanctionsNodes = specDoc.getElementsByTagName(LINKED_SANCTIONS_TAG);
    for (int i = 0; i < linkedSanctionsNodes.getLength(); i++) {
      Node linkedSanctionsNode = linkedSanctionsNodes.item(i);
      List<Element> sanctionIdEls = getChildElements(linkedSanctionsNode);
      sanctionIdEls.forEach(sanctionIdEl -> {
        String sanctionId = sanctionIdEl.getTextContent();
        String normId = ((Element) linkedSanctionsNode.getParentNode()).getAttribute("id");
        regulativeSpec.addLink(createAtom(normId), createAtom(sanctionId));
      });
    }
  }

  /**
   * Extract and return {@code parent} node's child elements.
   * 
   * @param parent parent node
   * @return list of child elements
   */
  private static List<Element> getChildElements(Node parent) {
    List<Element> nodes = new ArrayList<>();
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
