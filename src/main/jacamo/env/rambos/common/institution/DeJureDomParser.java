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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cartago.ArtifactId;
import cartago.LINK;
import cartago.OPERATION;
import cartago.OpFeedbackParam;
import cartago.OperationException;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import rambos.common.norm.Norm;
import rambos.common.norm.Norms;
import rambos.common.sanction.Sanction;
import rambos.common.sanction.Sanctions;

/**
 * @author igorcadelima
 *
 */
public final class DeJureDomParser extends DeJureParser<Document> {
  protected static final String NORMS_TAG = "norms";
  protected static final String SANCTIONS_TAG = "sanctions";
  protected static final String LINKS_TAG = "links";

  // normId -> norm
  private Map<Atom, Norm> norms;
  // sanctionId -> sanction
  private Map<Atom, Sanction> sanctions;
  // normId -> [sanctionId0, sanctionId1, ..., sanctionIdn]
  private Map<Atom, Set<Atom>> links;

  @LINK
  @OPERATION
  @Override
  void parse(Document ns, String deJureName, OpFeedbackParam<ArtifactId> deJureOut) {
    norms = new ConcurrentHashMap<Atom, Norm>();
    sanctions = new ConcurrentHashMap<Atom, Sanction>();
    links = new ConcurrentHashMap<Atom, Set<Atom>>();

    try {
      NormSpecUtil.validate(ns, NormSpecUtil.NS_SCHEMA_PATH);
      execLinkedOp(builderId, "norms", extractNorms(ns));
      execLinkedOp(builderId, "sanctions", extractSanctions(ns));
      execLinkedOp(builderId, "links", extractLinks(ns));
      execLinkedOp(builderId, "build", deJureName, deJureOut);
    } catch (SAXException | IOException | OperationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  protected Map<Atom, Norm> extractNorms(Document ns) {
    Node normsRootEl = ns.getElementsByTagName(NORMS_TAG)
                         .item(0);
    List<Element> norms = getChildElements(normsRootEl);

    for (Element normEl : norms) {
      Norm norm = Norms.parse(normEl);
      addNorm(norm);
    }
    return this.norms;
  }

  @Override
  protected Map<Atom, Sanction> extractSanctions(Document ns) {
    Node sanctionsRootEl = ns.getElementsByTagName(SANCTIONS_TAG)
                             .item(0);
    List<Element> sanctions = getChildElements(sanctionsRootEl);

    for (Element sanctionEl : sanctions)
      addSanction(Sanctions.parse(sanctionEl));

    return this.sanctions;
  }

  @Override
  protected Map<Atom, Set<Atom>> extractLinks(Document ns) {
    Node linksNode = ns.getElementsByTagName(LINKS_TAG)
                       .item(0);
    List<Element> links = getChildElements(linksNode);
    links.forEach(l -> {
      String normId = l.getElementsByTagName("norm-id")
                       .item(0)
                       .getTextContent();
      Node sanctionIdsNode = l.getElementsByTagName("sanction-ids")
                              .item(0);
      List<Element> sanctionIds = getChildElements(sanctionIdsNode);
      sanctionIds.forEach(s -> addLink(normId, s.getTextContent()));
    });
    return this.links;
  }


  /**
   * Extract and return {@code parent} node's child elements.
   * 
   * @param parent parent node
   * @return list of child elements
   */
  private List<Element> getChildElements(Node parent) {
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

  /**
   * Add norm to norms set.
   * 
   * @param n norm to be added
   */
  private void addNorm(Norm n) {
    if (norms.put(n.getId(), n) == n) {
      links.put(n.getId(), new HashSet<Atom>());
    }
  }

  /**
   * Add sanction to sanctions set.
   * 
   * @param s sanction to be added
   */
  private void addSanction(Sanction s) {
    sanctions.put(s.getId(), s);
  }

  /**
   * Add link to links set.
   * 
   * @param n the norm
   * @param s the sanction
   */
  private void addLink(Norm n, Sanction s) {
    Set<Atom> linkedSanctions = Optional.ofNullable(links.get(n.getId()))
                                        .orElse(new HashSet<Atom>());
    if (linkedSanctions.add(s.getId()))
      links.put(n.getId(), linkedSanctions);
  }

  /**
   * Add link to links set.
   * 
   * @param normId the norm id
   * @param sanctionId the sanction id
   */
  private void addLink(String normId, String sanctionId) {
    Norm n = norms.get(ASSyntax.createAtom(normId));
    Sanction s = sanctions.get(ASSyntax.createAtom(sanctionId));
    addLink(n, s);
  }
}