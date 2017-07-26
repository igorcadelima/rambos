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
import cartago.CartagoException;
import cartago.LINK;
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
    this.orgName = orgName;

    final String schName = getId().getName();
    orgState = new Scheme(spec, schName);

    if (spec == null)
      throw new MoiseException("scheme " + schType + " does not exist!");

    oeId = getCreatorId().getWorkspaceId()
                         .getName();

    // load normative program
    initNormativeEngine(os, "scheme(" + schType + ")");
    installNormativeSignaler();
    initWspRuleEngine();

    // observable properties
    updateGoalStateObsProp();
    defineObsProperty(obsPropGroups, getSchState().getResponsibleGroupsAsProlog());
    defineObsProperty(obsPropSpec, new JasonTermWrapper(spec.getAsProlog()));

    if (!"false".equals(Config.get()
                              .getProperty(Config.START_WEB_OI))) {
      WebInterface w = WebInterface.get();
      try {
        w.registerOEBrowserView(oeId, "/scheme/", schName, this);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @LINK
  @Override
  protected void updateRolePlayers(final String grId, final Collection<Player> rp)
      throws NormativeFailureException, CartagoException {
    ora4masOperationTemplate(new Operation() {
      public void exec() throws NormativeFailureException, Exception {
        Group g = new Group(grId);
        for (Player p : rp)
          g.addPlayer(p.getAg(), p.getTarget());
        g.addResponsibleForScheme(orgState.getId());

        boolean newLink = !getSchState().getGroupsResponsibleFor()
                                        .contains(g);
        getSchState().addGroupResponsibleFor(g);

        nengine.verifyNorms();

        getObsProperty(obsPropGroups).updateValue(getSchState().getResponsibleGroupsAsProlog());
        if (newLink) {
          // First time the group is linked to this scheme, so create normative board
          String nbId = grId + "." + orgState.getId();
          ArtifactId aid =
              makeArtifact(nbId, NormativeBoard.class.getName(), new ArtifactConfig(orgName));
          execLinkedOp(aid, "load", os2nopl.transform(spec, false));
          execInternalOp("subscribeDFP", aid);
        }
      }
    }, null);
  }
}
