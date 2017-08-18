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
package rambos.core.institution;

import jason.util.Config;
import npl.parser.ParseException;
import ora4mas.nopl.JasonTermWrapper;
import moise.common.MoiseException;
import ora4mas.nopl.WebInterface;
import ora4mas.nopl.oe.Group;

/**
 * <b>Do not</b> rely on visibility modifiers which are present in this class. They are subject to
 * change without further notice due to bugs regarding visibility modifiers in CArtAgO. If you have
 * any question regarding this issue, please contact the maintainers of this tool.
 * 
 * @author igorcadelima
 *
 */
public final class GroupBoard extends ora4mas.nopl.GroupBoard {
  /**
   * Initialises the group board.
   * 
   * @param os organisational specification
   * @param type the group type as defined in the OS
   * @throws MoiseException if group type is not specified in OS
   * @throws ParseException if group scope cannot be found in OS
   */
  void init(OrgSpec os, final String type) throws MoiseException, ParseException {
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
      WebInterface webGui = WebInterface.get();
      try {
        webGui.registerOEBrowserView(oeId, "/group/", groupName, this);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Define observable properties.
   */
  private void defineObsProperties() {
    defineObsProperty(obsPropSchemes, getGrpState().getResponsibleForAsProlog());
    defineObsProperty(obsWellFormed, new JasonTermWrapper("nok"));
    defineObsProperty(obsPropSpec, new JasonTermWrapper(spec.getAsProlog()));
    defineObsProperty(obsPropSubgroups, getGrpState().getSubgroupsAsProlog());
    defineObsProperty(obsPropParentGroup, new JasonTermWrapper(getGrpState().getParentGroup()));
  }
}
