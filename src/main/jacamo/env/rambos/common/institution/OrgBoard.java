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

import java.util.HashMap;
import java.util.Map;

import cartago.ArtifactConfig;
import cartago.ArtifactId;
import cartago.LINK;
import cartago.OPERATION;
import cartago.OpFeedbackParam;
import cartago.OperationException;
import moise.os.OS;

/**
 * This artefact should be used to create any other organisational artefact.
 * 
 * @author igorcadelima
 *
 */
public final class OrgBoard extends ora4mas.nopl.OrgBoard {
  private static final String MECHANISM_OS = "ClassResource:/org/org.xml";

  private Map<String, ArtifactId> aids = new HashMap<String, ArtifactId>();
  private ArtifactId deJure;
  private OS os;

  /**
   * Initialise {@link OrgBoard}.
   * 
   * @param osFile path to organisation specification
   */
  public void init(final String osFile) {
    os = OS.loadOSFromURI(MECHANISM_OS);
    OrgSpecs.extend(os, osFile);
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
    defineObsProperty("de_facto", createAtom(name), aid);
  }

  /**
   * Create {@link DeJure} repository named "<i>[org_name]</i>.de_jure", where <i>[org_name]</i> is
   * the name of the organisation.
   * 
   * @param legislativeSpec path to file with the legislative specification
   * @param aid output parameter which returns the {@link ArtifactId} of {@link DeJure}
   * @throws OperationException
   */
  @OPERATION
  public void createDeJure(String legislativeSpec, OpFeedbackParam<ArtifactId> aid)
      throws OperationException {
    if (deJure != null) {
      failed("There cannot be more than one De Jure in an organisation");
    }

    String name = getId().getName() + ".de_jure";
    deJure = makeArtifact(name, DeJure.class.getName(), new ArtifactConfig(legislativeSpec));
    aid.set(deJure);
    defineObsProperty("de_jure", createAtom(name), aid);
  }

  /**
   * Create {@link CapabilityBoard} named "<i>[org_name]</i>.capability_board", where
   * <i>[org_name]</i> is the name of the organisation.
   * 
   * @param aid output parameter which returns the {@link ArtifactId} of {@link CapabilityBoard}
   * @throws OperationException
   */
  @OPERATION
  public void createCapabilityBoard(OpFeedbackParam<ArtifactId> aid) throws OperationException {
    String name = getId().getName() + ".capability_board";

    if (aids.containsKey(name)) {
      failed("There cannot be more than one capability board in an organisation");
    }

    ArtifactId artId =
        makeArtifact(name, CapabilityBoard.class.getName(), ArtifactConfig.DEFAULT_CONFIG);
    aids.put(name, artId);
    defineObsProperty("capability_board", createAtom(name), artId);
    aid.set(artId);
  }

  @Override
  @OPERATION
  public void createGroup(String id, String type, OpFeedbackParam<ArtifactId> gaid)
      throws OperationException {
    ArtifactId aid = makeArtifact(id, GroupBoard.class.getName(), new ArtifactConfig(os, type));
    aids.put(id, aid);
    defineObsProperty("group", createAtom(id), createAtom(type), aid);
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
    defineObsProperty("scheme", createAtom(id), createAtom(type), aid);
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
    removeObsPropertyByTemplate(kind, createAtom(id), null, null);

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
