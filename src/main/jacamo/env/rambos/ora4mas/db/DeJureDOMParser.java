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
package rambos.ora4mas.db;

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
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.parser.ParseException;
import rambos.ContentStringParser;
import rambos.IContent;
import rambos.INorm;
import rambos.Norms;
import rambos.Sanction;
import rambos.SanctionCategory;
import rambos.SanctionDiscernability;
import rambos.SanctionIssuer;
import rambos.SanctionLocus;
import rambos.SanctionMode;
import rambos.SanctionPolarity;
import rambos.SanctionPurpose;
import rambos.State;
import rambos.States;
import rambos.ora4mas.util.DJUtil;

/**
 * @author igorcadelima
 *
 */
public class DeJureDOMParser extends DeJureParser<Document> {
  protected static final String NORMS_TAG = "norms";
  protected static final String SANCTIONS_TAG = "sanctions";
  protected static final String LINKS_TAG = "links";

  // normId -> norm
  private Map<String, INorm> norms;
  // sanctionId -> sanction
  private Map<String, Sanction> sanctions;
  // normId -> [sanctionId0, sanctionId1, ..., sanctionIdn]
  private Map<String, Set<String>> links;

  @LINK
  @OPERATION
  @Override
  public void parse(Document ns, String deJureName, OpFeedbackParam<ArtifactId> deJureOut) {
    norms = new ConcurrentHashMap<String, INorm>();
    sanctions = new ConcurrentHashMap<String, Sanction>();
    links = new ConcurrentHashMap<String, Set<String>>();

    try {
      DJUtil.validate(ns, DJUtil.NS_SCHEMA_PATH);
      execLinkedOp(builderId, "setNorms", extractNorms(ns));
      execLinkedOp(builderId, "setSanctions", extractSanctions(ns));
      execLinkedOp(builderId, "setLinks", extractLinks(ns));
      execLinkedOp(builderId, "build", deJureName, deJureOut);
    } catch (SAXException | IOException | OperationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  protected Map<String, INorm> extractNorms(Document ns) {
    Node normsRootEl = ns.getElementsByTagName(NORMS_TAG)
                         .item(0);
    List<Element> norms = getChildElements(normsRootEl);

    for (Element normEl : norms) {
      INorm norm = Norms.parse(normEl);
      addNorm(norm);
    }
    return this.norms;
  }

  @Override
  protected Map<String, Sanction> extractSanctions(Document ns) {
    Node sanctionsRootEl = ns.getElementsByTagName(SANCTIONS_TAG)
                             .item(0);
    List<Element> sanctions = getChildElements(sanctionsRootEl);

    for (Element sanctionEl : sanctions) {
      String id = sanctionEl.getAttribute("id");
      State state = States.tryParse(sanctionEl.getAttribute("state"), State.ENABLED);
      LogicalFormula condition = null;
      SanctionCategory category = null;
      IContent content = null;

      List<Element> properties = getChildElements(sanctionEl);
      for (Element p : properties) {
        switch (p.getNodeName()) {
          case "condition":
            try {
              condition = ASSyntax.parseFormula(p.getTextContent());
            } catch (ParseException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
            break;
          case "category":
            category = parseSanctionCategory(p);
            break;
          case "content":
            content = new ContentStringParser().parse(p.getTextContent());
        }
      }
      addSanction(new Sanction(id, state, condition, category, content));
    }
    return this.sanctions;
  }

  /**
   * Extract the category dimensions from {@code category}, create a {@link SanctionCategory}, and
   * return it.
   * 
   * @param category
   * @return parsed {@link SanctionCategory} based on given node
   */
  private SanctionCategory parseSanctionCategory(Node category) {
    NodeList dimensions = category.getChildNodes();
    SanctionPurpose purpose = null;
    SanctionIssuer issuer = null;
    SanctionLocus locus = null;
    SanctionMode mode = null;
    SanctionPolarity polarity = null;
    SanctionDiscernability discernability = null;

    for (int i = 0; i < dimensions.getLength(); i++) {
      Node dimension = dimensions.item(i);
      String dimensionContent = dimension.getTextContent()
                                         .toUpperCase();

      switch (dimension.getNodeName()) {
        case "purpose":
          purpose = SanctionPurpose.valueOf(dimensionContent);
          break;
        case "issuer":
          issuer = SanctionIssuer.valueOf(dimensionContent);
          break;
        case "locus":
          locus = SanctionLocus.valueOf(dimensionContent);
          break;
        case "mode":
          mode = SanctionMode.valueOf(dimensionContent);
          break;
        case "polarity":
          polarity = SanctionPolarity.valueOf(dimensionContent);
          break;
        case "discernability":
          discernability = SanctionDiscernability.valueOf(dimensionContent);
          break;
      }
    }
    return new SanctionCategory(purpose, issuer, locus, mode, polarity, discernability);
  }

  @Override
  protected Map<String, Set<String>> extractLinks(Document ns) {
    Node linksRootEl = ns.getElementsByTagName(LINKS_TAG)
                         .item(0);
    NodeList linkNodes = linksRootEl.getChildNodes();
    for (int i = 0; i < linkNodes.getLength(); i++) {
      Node linkNode = linkNodes.item(i);

      if (linkNode.getNodeType() == Node.ELEMENT_NODE) {
        Node normIdNode = linkNode.getFirstChild();
        String normId = normIdNode.getTextContent();

        Node sanctionsNode = linkNode.getLastChild();
        NodeList sanctionIdsList = sanctionsNode.getChildNodes();
        for (int j = 0; j < sanctionIdsList.getLength(); j++) {
          Node sanctionIdNode = sanctionIdsList.item(j);

          if (sanctionIdNode.getNodeType() == Node.ELEMENT_NODE) {
            String sanctionId = sanctionIdNode.getTextContent();
            addLink(normId, sanctionId);
          }
        }
      }
    }
    return this.links;
  }

  /**
   * Extract and return {@code parant} node's child elements.
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
  private void addNorm(INorm n) {
    if (norms.put(n.getId(), n) == n) {
      links.put(n.getId(), new HashSet<String>());
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
  private void addLink(INorm n, Sanction s) {
    Set<String> linkedSanctions = Optional.ofNullable(links.get(n.getId()))
                                          .orElse(new HashSet<String>());
    if (linkedSanctions.add(s.getId())) {
      links.put(n.getId(), linkedSanctions);
    }
  }

  /**
   * Add link to links set.
   * 
   * @param normId the norm id
   * @param sanctionId the sanction id
   */
  private void addLink(String normId, String sanctionId) {
    INorm n = norms.get(normId);
    Sanction s = sanctions.get(sanctionId);
    addLink(n, s);
  }
}
