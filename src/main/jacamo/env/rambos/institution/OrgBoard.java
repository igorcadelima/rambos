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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import cartago.*;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import moise.os.ns.NS;

public class OrgBoard extends ora4mas.nopl.OrgBoard {
  private Map<String, ArtifactId> aids = new HashMap<String, ArtifactId>();
  private ArtifactId deJure;
  private Document ns;
  private OrgSpec os;

  /**
   * Initialise {@link OrgBoard} separating the normative from the organisational specification.
   * 
   * @param osFile path to organisation specification
   */
  public void init(final String osFile) {
    try {
      Document doc = NormSpecUtil.parseDocument(osFile);
      Node nsNode = doc.getElementsByTagName(NS.getXMLTag())
                       .item(0);
      doc.getDocumentElement()
         .removeChild(nsNode);

      ns = NormSpecUtil.nodeToDocument(nsNode);
      createOS(doc);
    } catch (ParserConfigurationException | SAXException | IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Create {@link OrgSpec} instance which will be shared with every {@link GroupBoard},
   * {@link SchemeBoard}, and {@link NormativeBoard} that belongs with this {@link OrgBoard}
   * instance.
   * 
   * @param osDoc organisational specification
   */
  private void createOS(Document osDoc) {
    String mechanismOSFile = "/org/org.xml";
    InputStream mechanismOSResource = getClass().getResourceAsStream(mechanismOSFile);
    os = OrgSpec.create(mechanismOSResource);
    os.extend(osDoc);
  }

  /**
   * Create {@link DaFacto} repository named "<i>[org_name]</i>.de_facto", where <i>[org_name]</i>
   * is the name of the organisation.
   * 
   * @param aid output parameter which returns the {@link ArtifactId} of the newly created
   *        {@link DeFacto}
   * @throws OperationException
   */
  @OPERATION
  public void createDeFacto(OpFeedbackParam<ArtifactId> aid) throws OperationException {
    String name = getId().getName() + ".de_facto";
    if (hasObsProperty(name)) {
      failed("There cannot be more than one De Facto in an organisation");
    }

    aid.set(makeArtifact(name, DeFacto.class.getName(), ArtifactConfig.DEFAULT_CONFIG));
    defineObsProperty("de_facto", ASSyntax.createAtom(name), aid);
  }

  /**
   * Create {@link DeJure} repository named "<i>[org_name]</i>.de_jure", where <i>[org_name]</i> is
   * the name of the organisation.
   * 
   * @param aid output parameter which returns the {@link ArtifactId} of {@link DeJure}
   * @throws OperationException
   */
  @OPERATION
  public void createDeJure(OpFeedbackParam<ArtifactId> aid) throws OperationException {
    if (deJure != null) {
      failed("There cannot be more than one De Jure in an organisation");
    }

    String name = getId().getName() + ".de_jure";
    ArtifactId djb = makeArtifact("djb", DeJure.Builder.class.getName(), new ArtifactConfig());
    ArtifactId djp = makeArtifact("djp", DeJureDOMParser.class.getName(), new ArtifactConfig(djb));
    execLinkedOp(djp, "parse", ns, name, aid);
    deJure = aid.get();
    defineObsProperty("de_jure", ASSyntax.createAtom(name), aid);

    dispose(djb);
    dispose(djp);
  }

  @Override
  @OPERATION
  public void createGroup(String id, String type, OpFeedbackParam<ArtifactId> gaid)
      throws OperationException {
    ArtifactId aid = makeArtifact(id, GroupBoard.class.getName(), new ArtifactConfig(os, type));
    aids.put(id, aid);
    defineObsProperty("group", new Atom(id), new Atom(type), aid);
    gaid.set(aid);
  }

  @Override
  @OPERATION
  public void removeGroup(String id) {
    removeArtefact(id, "group");
  }

  @Override
  @OPERATION
  public void createScheme(String id, String type, OpFeedbackParam<ArtifactId> said)
      throws OperationException {
    String name = getId().getName();
    ArtifactId aid =
        makeArtifact(id, SchemeBoard.class.getName(), new ArtifactConfig(os, name, type));
    aids.put(id, aid);
    defineObsProperty("scheme", new Atom(id), new Atom(type), aid);
    said.set(aid);
  }

  @Override
  @OPERATION
  public void removeScheme(String id) {
    removeArtefact(id, "scheme");
  }

  /**
   * Remove artefact.
   * 
   * @param id id of the artefact to be removed
   * @param kind kind of the artefact, which should be either {@code "group"} or {@code "scheme"}
   */
  private void removeArtefact(String id, String kind) {
    ArtifactId aid = aids.remove(id);
    if (aid == null) {
      failed("No " + kind + " board for " + id);
    }
    removeObsPropertyByTemplate(kind, ASSyntax.createAtom(id), null, null);

    try {
      execLinkedOp(aid, "destroy");
    } catch (OperationException e) {
      e.printStackTrace();
    }
  }

  /**
   * Get the id of the organisation's {@link DeJure} and return it through an output parameter.
   * 
   * @param deJureId output parameter which returns {@link DeJure}'s id
   */
  @LINK
  void getDeJureId(OpFeedbackParam<ArtifactId> deJureId) {
    deJureId.set(deJure);
  }
}
