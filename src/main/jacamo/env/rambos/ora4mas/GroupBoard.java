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
package rambos.ora4mas;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cartago.ArtifactId;
import cartago.OperationException;
import cartago.CartagoException;
import cartago.LINK;
import cartago.OPERATION;
import jason.util.Config;
import moise.os.Cardinality;
import npl.NormativeFailureException;
import npl.parser.ParseException;
import ora4mas.nopl.JasonTermWrapper;
import moise.common.MoiseException;
import ora4mas.nopl.Operation;
import ora4mas.nopl.WebInterface;
import ora4mas.nopl.oe.Group;
import ora4mas.nopl.oe.Player;
import ora4mas.nopl.tools.os2nopl;
import rambos.os.OS;

public class GroupBoard extends ora4mas.nopl.GroupBoard {
  protected moise.os.ss.Group spec;
  protected Set<ArtifactId> schemes = new HashSet<ArtifactId>();
  protected Set<ArtifactId> listeners = new HashSet<ArtifactId>();
  protected ArtifactId parentGroup = null;

  /**
   * Schemes to be responsible for when well formed.
   */
  protected List<String> futureSchemes = new LinkedList<String>();

  protected Logger logger = Logger.getLogger(GroupBoard.class.getName());

  protected Group getGrpState() {
    return (Group) orgState;
  }

  /**
   * Initialises the group board.
   * 
   * @param os organisational specification
   * @param type the group type as defined in the OS
   * @throws MoiseException if group type is not specified in OS
   * @throws ParseException if group scope cannot be found in OS
   */
  public void init(OS os, final String type) throws MoiseException, ParseException {
    spec = os.getSS()
             .getRootGrSpec()
             .findSubGroup(type);

    if (spec == null)
      throw new MoiseException("Group " + type + " does not exist!");

    final String groupName = getId().getName();
    orgState = new Group(groupName);
    oeId = getCreatorId().getWorkspaceId()
                         .getName();
    defineObsProperties();

    // load normative program
    initNormativeEngine(os, "group(" + type + ")");
    installNormativeSignaler();

    // install monitor of agents quitting the system
    initWspRuleEngine();

    if (!"false".equals(Config.get()
                              .getProperty(Config.START_WEB_OI))) {
      WebInterface w = WebInterface.get();
      try {
        w.registerOEBrowserView(oeId, "/group/", groupName, this);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Define observable properties.
   */
  protected void defineObsProperties() {
    defineObsProperty(obsPropSchemes, getGrpState().getResponsibleForAsProlog());
    defineObsProperty(obsWellFormed, new JasonTermWrapper("nok"));
    defineObsProperty(obsPropSpec, new JasonTermWrapper(spec.getAsProlog()));
    defineObsProperty(obsPropSubgroups, getGrpState().getSubgroupsAsProlog());
    defineObsProperty(obsPropParentGroup, new JasonTermWrapper(getGrpState().getParentGroup()));
  }

  /**
   * The agent executing this operation tries to destroy the instance of the group
   */
  @Override
  @OPERATION
  public void destroy() {
    if (parentGroup != null) {
      try {
        execLinkedOp(parentGroup, "removeSubgroup", getGrpState().getId());
      } catch (OperationException e) {
        e.printStackTrace();
        return; // do not call super destroy
      }
    }

    super.destroy();
  }

  @Override
  public void agKilled(String agName) {
    // logger.info("****** "+agName+" has quit!");
    boolean oldStatus = isWellFormed();
    for (Player p : orgState.getPlayers()) {
      if (orgState.removePlayer(agName, p.getTarget())) {
        try {
          logger.info(agName + " has quit, role " + p.getTarget() + " removed by the platform!");
          leaveRoleWithoutVerify(agName, p.getTarget(), oldStatus);
        } catch (CartagoException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * The agent executing this operation tries to connect the group to a parentGroup
   * 
   * @param parentGroupId the group Id to connect to
   */
  @Override
  @OPERATION
  public void setParentGroup(String parentGroupId) throws OperationException {
    parentGroup = lookupArtifact(parentGroupId);
    getGrpState().setParentGroup(parentGroupId);
    execLinkedOp(parentGroup, "addSubgroup", getGrpState().getId(), spec.getId(), parentGroupId);
    execLinkedOp(parentGroup, "updateSubgroupPlayers", orgState.getId(), orgState.getPlayers());
    execLinkedOp(parentGroup, "updateSubgroupFormationStatus", getGrpState().getId(),
        isWellFormed());
    execLinkedOp(parentGroup, "updateSubgroupPlayers", orgState.getId(), orgState.getPlayers());
    getObsProperty(obsPropParentGroup).updateValue(
        new JasonTermWrapper(getGrpState().getParentGroup()));
    updateGuiOE();
  }

  /**
   * The agent executing this operation tries to adopt a role in the group
   * 
   * @param role the role being adopted
   */
  @Override
  @OPERATION
  public void adoptRole(String role) {
    adoptRole(getOpUserName(), role);
  }

  private void adoptRole(final String ag, final String role) {
    ora4masOperationTemplate(new Operation() {
      public void exec() throws NormativeFailureException, Exception {
        boolean oldStatus = isWellFormed();
        orgState.addPlayer(ag, role);

        nengine.verifyNorms();

        boolean status = isWellFormed();
        if (parentGroup != null) {
          execLinkedOp(parentGroup, "updateSubgroupPlayers", orgState.getId(),
              orgState.getPlayers());
          if (status != oldStatus) {
            logger.fine(
                orgState.getId() + ": informing parent group that now my formation is " + status);
            execLinkedOp(parentGroup, "updateSubgroupFormationStatus", orgState.getId(), status);
          }
        }
        notifyObservers();

        defineObsProperty(obsPropPlay, new JasonTermWrapper(ag), new JasonTermWrapper(role),
            new JasonTermWrapper(getId().getName()));
        if (status != oldStatus) {
          getObsProperty(obsWellFormed).updateValue(new JasonTermWrapper(status ? "ok" : "nok"));

          while (!futureSchemes.isEmpty()) {
            String sch = futureSchemes.remove(0);
            // logger.info("Since the group "+orgState.getId()+" is now well formed, adding scheme
            // "+sch);
            addScheme(sch);
          }
        }
      }
    }, "Error adopting role " + role);
  }

  /**
   * The agent executing this operation tries to give up a role in the group
   * 
   * @param role the role being removed/leaved
   */
  @Override
  @OPERATION
  public void leaveRole(final String role) {
    ora4masOperationTemplate(new Operation() {
      public void exec() throws NormativeFailureException, Exception {
        boolean oldStatus = isWellFormed();
        orgState.removePlayer(getOpUserName(), role);
        nengine.verifyNorms();
        boolean status = leaveRoleWithoutVerify(getOpUserName(), role, oldStatus);
        notifyObservers();
        if (parentGroup != null) {
          execLinkedOp(parentGroup, "updateSubgroupPlayers", orgState.getId(),
              orgState.getPlayers());
          execLinkedOp(parentGroup, "updateSubgroupFormationStatus", orgState.getId(), status);
        }
      }
    }, "Error leaving role " + role);
  }

  private boolean leaveRoleWithoutVerify(String ag, String role, boolean oldStatus)
      throws CartagoException, OperationException {
    boolean status = isWellFormed();
    removeObsPropertyByTemplate(obsPropPlay, new JasonTermWrapper(ag), new JasonTermWrapper(role),
        new JasonTermWrapper(this.getId()
                                 .getName()));
    if (status != oldStatus)
      getObsProperty(obsWellFormed).updateValue(new JasonTermWrapper(status ? "ok" : "nok"));
    updateGuiOE();
    return status;
  }

  /**
   * The agent executing this operation tries to add a scheme under the responsibility of a group
   * 
   * @param schName the scheme Id being added
   */
  @Override
  @OPERATION
  public void addScheme(final String schName) {
    ora4masOperationTemplate(new Operation() {
      public void exec() throws NormativeFailureException, Exception {
        ArtifactId schId = lookupArtifact(schName);
        getGrpState().addResponsibleForScheme(schName);
        nengine.verifyNorms();

        schemes.add(schId);

        notifyObservers();

        // update in subgroups
        for (Group sg : getGrpState().getSubgroups()) {
          ArtifactId sgid = lookupArtifact(sg.getId());
          execLinkedOp(sgid, "addScheme", schName);
        }
        getObsProperty(obsPropSchemes).updateValue(getGrpState().getResponsibleForAsProlog());
      }
    }, "Error adding scheme " + schName);
  }

  /**
   * The group will be responsible for the scheme when its formation is Ok
   * 
   * @param schName the scheme Id being added
   */
  @Override
  @OPERATION
  public void addSchemeWhenFormationOk(String schName) {
    if (!running)
      return;
    if (isWellFormed()) {
      addScheme(schName);
    } else {
      futureSchemes.add(schName);
    }
  }

  /**
   * The agent executing this operation tries to remove a scheme that is under the responsibility of
   * a group
   * 
   * @param schId the scheme Id being removed
   */
  @Override
  @OPERATION
  public void removeScheme(final String schId) {
    ora4masOperationTemplate(new Operation() {
      public void exec() throws NormativeFailureException, Exception {
        ArtifactId schAid = lookupArtifact(schId);
        getGrpState().removeResponsibleForScheme(schId);
        nengine.verifyNorms();
        execLinkedOp(schAid, "removeResponsibleGroup", orgState.getId());

        getObsProperty(obsPropSchemes).updateValue(getGrpState().getResponsibleForAsProlog());

        schemes.remove(schAid);
      }
    }, "Error removing scheme " + schId);
  }

  @Override
  @LINK
  public void addListener(String artId) {
    if (!running)
      return;
    try {
      listeners.add(lookupArtifact(artId));

      // update in subgroups
      for (Group sg : getGrpState().getSubgroups()) {
        ArtifactId sgid = lookupArtifact(sg.getId());
        execLinkedOp(sgid, "addListener", artId);
      }

    } catch (Exception e) {
      failed(e.toString());
    }
  }

  private void notifyObservers() throws CartagoException {
    for (ArtifactId a : schemes) {
      execLinkedOp(a, "updateRolePlayers", orgState.getId(), orgState.getPlayers());
    }
    for (ArtifactId a : listeners) {
      execLinkedOp(a, "updateRolePlayers", orgState.getId(), orgState.getPlayers());
    }
  }

  @Override
  public void setCardinality(String element, String id, int min, int max)
      throws MoiseException, ParseException {
    if (element.equals("role")) {
      spec.setRoleCardinality(id, new Cardinality(min, max));

      getObsProperty(obsWellFormed).updateValue(
          new JasonTermWrapper(isWellFormed() ? "ok" : "nok"));

      postReorgUpdates(spec.getSS()
                           .getOS(),
          "group(" + spec.getId() + ")", "ss");
    } else {
      System.out.println("setCardinality not implemented for " + element
          + ". Ask the developers to provide you this feature!");
    }
  }

  @Override
  public String getNPLSrc() {
    if (spec != null)
      return os2nopl.header(spec) + os2nopl.transform(spec);
    else
      return super.getNPLSrc();
  }

  @Override
  public Element getAsDOM(Document document) {
    return getGrAsDOM(getGrpState(), spec.getId(), isWellFormed(), ownerAgent, getGrpState(),
        document);
  }
}
