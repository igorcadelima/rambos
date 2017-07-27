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

import java.util.Collection;

import cartago.ArtifactConfig;
import cartago.ArtifactId;
import cartago.LINK;
import cartago.OperationException;
import jason.util.Config;
import moise.common.MoiseException;
import npl.NormativeFailureException;
import npl.parser.ParseException;
import ora4mas.nopl.JasonTermWrapper;
import ora4mas.nopl.Operation;
import ora4mas.nopl.WebInterface;
import ora4mas.nopl.oe.Group;
import ora4mas.nopl.oe.Player;
import ora4mas.nopl.oe.Scheme;
import ora4mas.nopl.tools.os2nopl;
import rambos.os.OS;

public class SchemeBoard extends ora4mas.nopl.SchemeBoard {
  protected String orgName;
  protected String djSpecFileUri;

  /**
   * Initialise the scheme board.
   * 
   * @param os organisational specification
   * @param orgName name of the organisation
   * @param schType type of the scheme (as defined in the OS)
   * @throws ParseException if the OS file is not correct
   * @throws MoiseException if schType was not specified
   */
  public void init(OS os, String orgName, String schType) throws ParseException, MoiseException {
    spec = os.getFS()
             .findScheme(schType);

    if (spec == null)
      throw new MoiseException("scheme " + schType + " does not exist!");

    this.orgName = orgName;
    orgState = new Scheme(spec, getId().getName());
    oeId = getCreatorId().getWorkspaceId()
                         .getName();

    setupNormativeModule();
    setupObservableProperties();
    setupBrowserView();
  }

  /**
   * Setup normative module.
   * 
   * @throws MoiseException
   * @throws ParseException
   */
  private void setupNormativeModule() throws MoiseException, ParseException {
    moise.os.OS os = spec.getFS()
                         .getOS();
    String type = "scheme(" + spec.getId() + ")";
    initNormativeEngine(os, type);
    installNormativeSignaler();
    initWspRuleEngine();
  }

  /**
   * Setup observable properties.
   */
  private void setupObservableProperties() {
    updateGoalStateObsProp();
    defineObsProperty(obsPropGroups, getSchState().getResponsibleGroupsAsProlog());
    defineObsProperty(obsPropSpec, new JasonTermWrapper(spec.getAsProlog()));
  }

  /**
   * Setup browser view.
   */
  private void setupBrowserView() {
    if (Boolean.valueOf(Config.get()
                              .getProperty(Config.START_WEB_OI))) {
      WebInterface w = WebInterface.get();
      w.registerOEBrowserView(oeId, "/scheme/", orgState.getId(), this);
    }
  }

  @LINK
  @Override
  protected void updateRolePlayers(final String grId, final Collection<Player> rp) {
    ora4masOperationTemplate(new Operation() {
      public void exec() throws OperationException, NormativeFailureException {
        Group g = new Group(grId);
        rp.forEach(p -> g.addPlayer(p.getAg(), p.getTarget()));

        boolean newResponsible = !getSchState().getGroupsResponsibleFor()
                                               .contains(g);
        if (newResponsible) {
          createLink(g);
          createNormativeBoardFor(grId);
        }
      }
    }, null);
  }

  /**
   * Create link with a given group.
   * 
   * @param g group to be linked with
   * @throws NormativeFailureException
   */
  private void createLink(Group g) throws NormativeFailureException {
    g.addResponsibleForScheme(orgState.getId());
    getSchState().addGroupResponsibleFor(g);
    nengine.verifyNorms();
    getObsProperty(obsPropGroups).updateValue(getSchState().getResponsibleGroupsAsProlog());
  }

  /**
   * Create normative board for the responsibility link from a group to a scheme.
   * <p>
   * This method names the artefact as {@code group_name.scheme_name}, loads norms from the scheme
   * into the it, and subscribes it to receive updates about dynamic facts from the scheme board.
   * </p>
   * 
   * @param groupName name of the group responsible for the scheme
   * @throws OperationException
   */
  private void createNormativeBoardFor(String groupName) throws OperationException {
    String name = groupName + "." + orgState.getId();
    String template = NormativeBoard.class.getName();
    ArtifactId aid = makeArtifact(name, template, new ArtifactConfig(orgName));
    execLinkedOp(aid, "load", os2nopl.transform(spec, false));
    execInternalOp("subscribeDFP", aid);
  }
}
