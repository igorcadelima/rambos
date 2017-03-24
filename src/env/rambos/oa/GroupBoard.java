package rambos.oa;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cartago.ArtifactId;
import cartago.CartagoException;
import cartago.LINK;
import cartago.OPERATION;
import cartago.OperationException;
import jason.NoValueException;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import jason.util.Config;
import moise.common.MoiseException;
import moise.oe.GroupInstance;
import moise.oe.RolePlayer;
import moise.os.Cardinality;
import moise.tools.os2dot;
import moise.xml.DOMUtils;
import npl.NormativeFailureException;
import npl.parser.ParseException;
import ora4mas.nopl.GUIInterface;
import ora4mas.nopl.JasonTermWrapper;
import ora4mas.nopl.Operation;
import ora4mas.nopl.WebInterface;
import ora4mas.nopl.oe.Group;
import ora4mas.nopl.oe.Player;
import ora4mas.nopl.tools.os2nopl;
import rambos.oa.util.DJUtil;
import rambos.os.OS;

public class GroupBoard extends ora4mas.nopl.GroupBoard {
	protected moise.os.ss.Group spec;
	protected Set<ArtifactId> schemes = new HashSet<ArtifactId>();
	protected Set<ArtifactId> listeners = new HashSet<ArtifactId>();
	protected ArtifactId parentGroup = null;

	// schemes to be responsible to when well formed
	protected List<String> futureSchemes = new LinkedList<String>();

//	public static final String obsPropSpec = "specification";
//	public static final String obsPropPlay = Group.playPI.getFunctor();
//	public static final String obsPropSchemes = "schemes";
//	public static final String obsPropSubgroups = "subgroups";
//	public static final String obsPropParentGroup = "parentGroup";
//	public static final String obsWellFormed = "formationStatus";

	protected Logger logger = Logger.getLogger(GroupBoard.class.getName());

	protected Group getGrpState() {
		return (Group) orgState;
	}

	/**
	 * Initialises the group board
	 * 
	 * @param osFile
	 *            the organisation specification file (path and file name)
	 * @param groupType
	 *            the type of the group (as defined in the OS)
	 * @throws ParseException
	 *             if the OS file is not correct
	 * @throws MoiseException
	 *             if grType was not specified
	 * @throws OperationException
	 *             if parentGroupId doesn't exit
	 */
	public void init(final String osFile, final String groupType)
			throws ParseException, MoiseException, OperationException {
		final String groupName = getId().getName();
		orgState = new Group(groupName);

		String mechanismOSFile = "/org/org.xml";
    	InputStream mechanismOSResource = getClass().getResourceAsStream(mechanismOSFile);
    	OS os = OS.create(mechanismOSResource);
    	os.extend(osFile);

		spec = os.getSS().getRootGrSpec().findSubGroup(groupType);

		if (spec == null)
			throw new MoiseException("Group " + groupType + " does not exist!");

		oeId = getCreatorId().getWorkspaceId().getName();

		// observable properties
		defineObsProperty(obsPropSchemes, getGrpState().getResponsibleForAsProlog());
		defineObsProperty(obsWellFormed, new JasonTermWrapper("nok"));
		defineObsProperty(obsPropSpec, new JasonTermWrapper(spec.getAsProlog()));
		defineObsProperty(obsPropSubgroups, getGrpState().getSubgroupsAsProlog());
		defineObsProperty(obsPropParentGroup, new JasonTermWrapper(getGrpState().getParentGroup()));

		// load normative program
		initNormativeEngine(os, "group(" + groupType + ")");
		installNormativeSignaler(); // TODO replace this with the new one

		// install monitor of agents quitting the system
		initWspRuleEngine();

		if (!"false".equals(Config.get().getProperty(Config.START_WEB_OI))) {
			WebInterface w = WebInterface.get();
			try {
				w.registerOEBrowserView(oeId, "/group/", groupName, this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// TODO: not working! UpdateGuiThread should be reimplemented
//	@Override
//	@OPERATION
//	public void debug(String kind) throws Exception {
//		final String grId = getId().getName();
//		if (kind.equals("inspector_gui(on)")) {
//			gui = GUIInterface.add(grId, ":: Group Board " + grId + " (" + spec.getId() + ") ::", nengine, true);
//
//			updateGUIThread = new UpdateGuiThread();
//			updateGUIThread.start();
//
//			updateGuiOE();
//
//			gui.setNormativeProgram(getNPLSrc());
//			gui.setSpecification(specToStr(spec.getSS().getOS(),
//					DOMUtils.getTransformerFactory().newTransformer(DOMUtils.getXSL("ss"))));
//		}
//		if (kind.equals("inspector_gui(off)")) {
//			System.out.println("not implemented yet, ask the developers to do so.");
//		}
//	}

	/**
	 * The agent executing this operation tries to destroy the instance of the
	 * group
	 * 
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
	 * The agent executing this operation tries to connect the group to a
	 * parentGroup
	 * 
	 * @param parentGroupId
	 *            the group Id to connect to
	 */
	@Override
	@OPERATION
	public void setParentGroup(String parentGroupId) throws OperationException {
		parentGroup = lookupArtifact(parentGroupId);
		getGrpState().setParentGroup(parentGroupId);
		execLinkedOp(parentGroup, "addSubgroup", getGrpState().getId(), spec.getId(), parentGroupId);
		execLinkedOp(parentGroup, "updateSubgroupPlayers", orgState.getId(), orgState.getPlayers());
		execLinkedOp(parentGroup, "updateSubgroupFormationStatus", getGrpState().getId(), isWellFormed());
		execLinkedOp(parentGroup, "updateSubgroupPlayers", orgState.getId(), orgState.getPlayers());
		getObsProperty(obsPropParentGroup).updateValue(new JasonTermWrapper(getGrpState().getParentGroup()));
		updateGuiOE();
	}

	/**
	 * The agent executing this operation tries to adopt a role in the group
	 * 
	 * @param role
	 *            the role being adopted
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
					execLinkedOp(parentGroup, "updateSubgroupPlayers", orgState.getId(), orgState.getPlayers());
					if (status != oldStatus) {
						logger.fine(orgState.getId() + ": informing parent group that now my formation is " + status);
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
						// logger.info("Since the group "+orgState.getId()+" is
						// now well formed, adding scheme "+sch);
						addScheme(sch);
					}
				}
			}
		}, "Error adopting role " + role);
	}

	/**
	 * The agent executing this operation tries to give up a role in the group
	 * 
	 * @param role
	 *            the role being removed/leaved
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
					execLinkedOp(parentGroup, "updateSubgroupPlayers", orgState.getId(), orgState.getPlayers());
					execLinkedOp(parentGroup, "updateSubgroupFormationStatus", orgState.getId(), status);
				}
			}
		}, "Error leaving role " + role);
	}

	private boolean leaveRoleWithoutVerify(String ag, String role, boolean oldStatus)
			throws CartagoException, OperationException {
		boolean status = isWellFormed();
		removeObsPropertyByTemplate(obsPropPlay, new JasonTermWrapper(ag), new JasonTermWrapper(role),
				new JasonTermWrapper(this.getId().getName()));
		if (status != oldStatus)
			getObsProperty(obsWellFormed).updateValue(new JasonTermWrapper(status ? "ok" : "nok"));
		updateGuiOE();
		return status;
	}

	/**
	 * The agent executing this operation tries to add a scheme under the
	 * responsibility of a group
	 * 
	 * @param schName
	 *            the scheme Id being added
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
	 * @param schName
	 *            the scheme Id being added
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
	 * The agent executing this operation tries to remove a scheme that is under
	 * the responsibility of a group
	 * 
	 * @param schId
	 *            the scheme Id being removed
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

	// renamed linked operations
	private void notifyObservers() throws CartagoException {
		for (ArtifactId a : schemes) {
			execLinkedOp(a, "rambos_updateRolePlayers", orgState.getId(), orgState.getPlayers());
		}
		for (ArtifactId a : listeners) {
			execLinkedOp(a, "rambos_updateRolePlayers", orgState.getId(), orgState.getPlayers());
		}
	}

//	@LINK
//	protected void updateSubgroupPlayers(final String grId, final Collection<Player> rp) {
//		ora4masOperationTemplate(new Operation() {
//			public void exec() throws NormativeFailureException, Exception {
//				boolean oldStatus = isWellFormed();
//
//				Group g = getGrpState().getSubgroup(grId);
//				g.clearPlayers();
//				for (Player p : rp)
//					g.addPlayer(p.getAg(), p.getTarget());
//
//				nengine.verifyNorms();
//
//				boolean status = isWellFormed();
//				if (status != oldStatus)
//					getObsProperty(obsWellFormed).updateValue(new JasonTermWrapper(status ? "ok" : "nok"));
//
//				if (parentGroup != null) {
//					// New formation status
//					execLinkedOp(parentGroup, "updateSubgroupFormationStatus", orgState.getId(), status);
//					execLinkedOp(parentGroup, "updateSubgroupPlayers", grId, rp);
//				}
//			}
//		}, null);
//	}

//	@LINK
//	protected void updateSubgroupFormationStatus(final String grId, final boolean isWellFormed) {
//		ora4masOperationTemplate(new Operation() {
//			public void exec() throws NormativeFailureException, Exception {
//				boolean oldStatus = isWellFormed();
//
//				logger.fine("updating status of " + grId + " to " + isWellFormed);
//				getGrpState().setSubgroupWellformed(grId, isWellFormed);
//
//				nengine.verifyNorms();
//
//				boolean status = isWellFormed();
//				if (status != oldStatus) {
//					logger.fine("now I, " + orgState.getId() + ", am " + status);
//					getObsProperty(obsWellFormed).updateValue(new JasonTermWrapper(status ? "ok" : "nok"));
//				}
//				if (parentGroup != null) {
//					execLinkedOp(parentGroup, "updateSubgroupFormationStatus", grId, isWellFormed);
//					execLinkedOp(parentGroup, "updateSubgroupFormationStatus", orgState.getId(), status);
//				}
//			}
//		}, null);
//	}

//	@LINK
//	protected void addSubgroup(final String grId, final String grType, final String parentGr) {
//		ora4masOperationTemplate(new Operation() {
//			public void exec() throws NormativeFailureException, Exception {
//				boolean oldStatus = isWellFormed();
//
//				getGrpState().addSubgroup(grId, grType, parentGr);
//
//				nengine.verifyNorms();
//
//				boolean status = isWellFormed();
//				if (status != oldStatus)
//					getObsProperty(obsWellFormed).updateValue(new JasonTermWrapper(status ? "ok" : "nok"));
//				getObsProperty(obsPropSubgroups).updateValue(getGrpState().getSubgroupsAsProlog());
//
//				if (parentGroup != null) {
//					execLinkedOp(parentGroup, "addSubgroup", grId, grType, parentGr);
//					execLinkedOp(parentGroup, "updateSubgroupFormationStatus", orgState.getId(), status);
//				}
//			}
//		}, null);
//	}

//	@LINK
//	protected void removeSubgroup(final String grId) {
//		ora4masOperationTemplate(new Operation() {
//			public void exec() throws NormativeFailureException, Exception {
//				boolean oldStatus = isWellFormed();
//
//				getGrpState().removeSubgroup(grId);
//
//				nengine.verifyNorms();
//
//				boolean status = isWellFormed();
//				if (status != oldStatus)
//					getObsProperty(obsWellFormed).updateValue(new JasonTermWrapper(status ? "ok" : "nok"));
//				getObsProperty(obsPropSubgroups).updateValue(getGrpState().getSubgroupsAsProlog());
//
//				if (parentGroup != null) {
//					execLinkedOp(parentGroup, "removeSubgroup", grId);
//					// Update formation status
//					execLinkedOp(parentGroup, "updateSubgroupFormationStatus", orgState.getId(), status);
//				}
//			}
//		}, null);
//	}

//	/**
//	 * Commands that the owner of the group can perform.
//	 * 
//	 * @param cmd,
//	 *            possible values (as strings): adoptRole(<agent>,<role>)
//	 *            setCardinality(<element type>,<element id>,<new min>,<new
//	 *            max>) [element type= role/subgroup]
//	 * 
//	 * @throws CartagoException
//	 * @throws jason.asSyntax.parser.ParseException
//	 * @throws NoValueException
//	 * @throws MoiseException
//	 */
//	@OPERATION
//	@LINK
//	public void admCommand(String cmd) throws CartagoException, jason.asSyntax.parser.ParseException, NoValueException,
//			MoiseException, ParseException {
//		// this operation is available only for the owner of the artifact
//		if (getCurrentOpAgentId() != null && (!getOpUserName().equals(ownerAgent))
//				&& !getOpUserName().equals("workspace-manager")) {
//			failed("Error: agent '" + getOpUserName() + "' is not allowed to run " + cmd, "reason",
//					new JasonTermWrapper("not_allowed_to_start(admCommand)"));
//		} else {
//			Literal lCmd = ASSyntax.parseLiteral(cmd);
//			if (lCmd.getFunctor().equals("adoptRole")) {
//				adoptRole(fixAgName(lCmd.getTerm(0).toString()), lCmd.getTerm(1).toString());
//			} else if (lCmd.getFunctor().equals("leaveRole")) {
//				System.out.println("adm leave role not implemented yet! come back soon");
//			} else if (lCmd.getFunctor().equals("setCardinality")) {
//				setCardinality(lCmd.getTerm(0).toString(), lCmd.getTerm(1).toString(),
//						(int) ((NumberTerm) lCmd.getTerm(2)).solve(), (int) ((NumberTerm) lCmd.getTerm(3)).solve());
//			}
//		}
//	}

	@Override
	public void setCardinality(String element, String id, int min, int max) throws MoiseException, ParseException {
		if (element.equals("role")) {
			spec.setRoleCardinality(id, new Cardinality(min, max));

			getObsProperty(obsWellFormed).updateValue(new JasonTermWrapper(isWellFormed() ? "ok" : "nok"));

			postReorgUpdates(spec.getSS().getOS(), "group(" + spec.getId() + ")", "ss");
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

//	protected String getStyleSheetName() {
//		return "noplGroupInstance";
//	}

//	public boolean isWellFormed() {
//		Term aGr = ASSyntax.createAtom(this.getId().getName());
//		return nengine.holds(ASSyntax.createLiteral("well_formed", aGr));
//	}

	@Override
	public Element getAsDOM(Document document) {
		return getGrAsDOM(getGrpState(), spec.getId(), isWellFormed(), ownerAgent, getGrpState(), document);
	}

//	public static Element getGrAsDOM(Group gr, String spec, boolean isWellFormed, String owner, Group root,
//			Document document) {
//		Element grEle = (Element) document.createElement(GroupInstance.getXMLTag());
//		grEle.setAttribute("id", gr.getId());
//		grEle.setAttribute("specification", spec);
//
//		// status
//		Element wfEle = (Element) document.createElement("well-formed");
//		if (isWellFormed) {
//			wfEle.appendChild(document.createTextNode("ok"));
//		} else {
//			wfEle.appendChild(document.createTextNode("not ok"));
//		}
//		grEle.appendChild(wfEle);
//
//		// players
//		if (!gr.getPlayers().isEmpty()) {
//			Element plEle = (Element) document.createElement("players");
//			for (Player p : gr.getPlayers()) {
//				Element rpEle = (Element) document.createElement(RolePlayer.getXMLTag());
//				rpEle.setAttribute("role", p.getTarget());
//				rpEle.setAttribute("agent", p.getAg());
//				plEle.appendChild(rpEle);
//			}
//			grEle.appendChild(plEle);
//		}
//
//		// schemes
//		if (!gr.getSchemesResponsibleFor().isEmpty()) {
//			Element rfEle = (Element) document.createElement("responsible-for");
//			for (String sch : gr.getSchemesResponsibleFor()) {
//				Element schEle = (Element) document.createElement("scheme");
//				schEle.setAttribute("id", sch);
//				rfEle.appendChild(schEle);
//			}
//			grEle.appendChild(rfEle);
//		}
//
//		// subgroups
//		boolean has = false;
//		Element sgEle = (Element) document.createElement("subgroups");
//		for (Group gi : root.getSubgroups()) {
//			if (gi.getParentGroup().equals(gr.getId())) {
//				has = true;
//				sgEle.appendChild(
//						getGrAsDOM(gi, gi.getGrType(), root.isSubgroupWellformed(gi.getId()), null, root, document));
//			}
//		}
//		if (has)
//			grEle.appendChild(sgEle);
//
//		// parent group
//		grEle.setAttribute("parent-group", gr.getParentGroup());
//
//		if (owner != null)
//			grEle.setAttribute("owner", owner);
//
//		return grEle;
//	}

//	public String getAsDot() {
//		os2dot t = new os2dot();
//		t.showFS = false;
//		t.showNS = false;
//		t.showLinks = true;
//
//		try {
//
//			StringWriter so = new StringWriter();
//			so.append("digraph " + getGrpState().getId() + " {\n");
//			so.append("    rankdir=BT;\n");
//			so.append("    compound=true;\n\n");
//
//			so.append(t.transformRolesDef(spec.getSS()));
//			// so.append( t.transform(spec.getSS().getRootGrSpec(),
//			// getGrpState()) );
//			so.append(t.transform(spec, getGrpState()));
//
//			so.append("}\n");
//			return so.toString();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return null;
//	}
}
