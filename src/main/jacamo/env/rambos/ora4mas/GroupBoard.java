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

import java.util.logging.Logger;

import jason.util.Config;
import npl.parser.ParseException;
import ora4mas.nopl.JasonTermWrapper;
import moise.common.MoiseException;
import ora4mas.nopl.WebInterface;
import ora4mas.nopl.oe.Group;
import rambos.os.OS;

public class GroupBoard extends ora4mas.nopl.GroupBoard {
  protected Logger logger = Logger.getLogger(GroupBoard.class.getName());

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
}
