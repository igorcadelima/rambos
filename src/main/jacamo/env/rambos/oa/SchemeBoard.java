/*******************************************************************************
 * MIT License
 *
 * Copyright (c) Igor Conrado Alves de Lima <igorcadelima@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
package rambos.oa;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cartago.ArtifactConfig;
import cartago.ArtifactId;
import cartago.CartagoException;
import cartago.LINK;
import cartago.OPERATION;
import cartago.ObsProperty;
import cartago.OperationException;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.PredicateIndicator;
import jason.asSyntax.Term;
import jason.util.Config;
import moise.common.MoiseException;
import moise.oe.GoalInstance;
import moise.oe.MissionPlayer;
import moise.oe.SchemeInstance;
import moise.os.Cardinality;
import moise.os.fs.Goal;
import npl.NPLInterpreter;
import npl.NPLLiteral;
import npl.NormativeFailureException;
import npl.Scope;
import npl.parser.ParseException;
import ora4mas.nopl.GUIInterface;
import ora4mas.nopl.JasonTermWrapper;
import ora4mas.nopl.Operation;
import ora4mas.nopl.OrgArt;
import ora4mas.nopl.WebInterface;
import ora4mas.nopl.oe.Group;
import ora4mas.nopl.oe.Player;
import ora4mas.nopl.oe.Scheme;
import ora4mas.nopl.tools.os2nopl;
import rambos.oa.util.DJUtil;
import rambos.os.OS;

public class SchemeBoard extends ora4mas.nopl.SchemeBoard {
	protected moise.os.fs.Scheme spec;
    protected String djSpecFileUri;
//    public static final String obsPropSpec       = "specification";
//    public static final String obsPropGroups     = "groups";
//    public static final String obsPropCommitment = "commitment";
//    
//    public static final PredicateIndicator piGoalState = new PredicateIndicator("goalState", 5);

    protected Logger logger = Logger.getLogger(SchemeBoard.class.getName());

//    public Scheme getSchState() {
//        return (Scheme)orgState;
//    }
    
//    @Override
//    protected void initNormativeEngine(OS os, String type) throws MoiseException, ParseException {
//        nengine = new NPLInterpreter();
//        
////        NormativeSpec ns = new NormativeSpec(nsFileUri);
////        Scope root = ns.getRoot();
////        Scope scope = root.findScope(type);
////        
////        NormativeProgram p = new NormativeProgram();
////        new nplp(new StringReader(os2nopl.transform(os))).program(p, this);
////        Scope root = p.getRoot();
////        Scope scope = root.findScope(type);
////        if (scope == null)
////            throw new MoiseException("scope for "+type+" does not exist!");            
////        nengine.loadNP(scope);
//    }
    
    /**
     * Initialises the scheme artifact
     * 
     * @param osFile           the organisation specification file (path and file name)
     * @param schType          the type of the scheme (as defined in the OS)
     * @throws ParseException  if the OS file is not correct
     * @throws MoiseException  if schType was not specified
     */
    public void init(final String osFile, final String nsFileUri, final String schType) throws ParseException, MoiseException {
    	this.djSpecFileUri = nsFileUri;
    	
    	String mechanismOSFile = "/org/org.xml";
    	InputStream mechanismOSResource = getClass().getResourceAsStream(mechanismOSFile);
    	OS os = OS.create(mechanismOSResource);
    	os.extend(osFile);
    	
        spec = os.getFS().findScheme(schType);
        
        final String schName = getId().getName();
        orgState   = new Scheme(spec, schName);
        
        if (spec == null)
            throw new MoiseException("scheme "+schType+" does not exist!");
        
        oeId = getCreatorId().getWorkspaceId().getName();

        // load normative program
        initNormativeEngine(os, "scheme("+schType+")");
        installNormativeSignaler();
        initWspRuleEngine();

        // observable properties
        updateGoalStateObsProp();
        defineObsProperty(obsPropGroups,  getSchState().getResponsibleGroupsAsProlog());
        defineObsProperty(obsPropSpec, new JasonTermWrapper(spec.getAsProlog()));
        
        if (! "false".equals(Config.get().getProperty(Config.START_WEB_OI))) {
            WebInterface w = WebInterface.get();
            try {
                w.registerOEBrowserView(oeId, "/scheme/", schName, this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }                    
    }
 // TODO: not working! UpdateGuiThread should be reimplemented
//    @OPERATION public void debug(String kind) throws Exception {
//        final String schId = getId().getName();
//        if (kind.equals("inspector_gui(on)")) {
//            gui = GUIInterface.add(schId, "... Scheme Board "+schId+" ("+spec.getId()+") ...", nengine, true);
//            
//            updateGUIThread = new UpdateGuiThread();
//            updateGUIThread.start();
//         
//            updateGuiOE();
//            
//            gui.setNormativeProgram(getNPLSrc());
//            gui.setSpecification(specToStr(spec.getFS().getOS(), DOMUtils.getTransformerFactory().newTransformer(DOMUtils.getXSL("fsns"))));
//        }
//        if (kind.equals("inspector_gui(off)")) {
//            System.out.println("not implemented yet, ask the developers to do so.");
//        }    
//    }
    
    /**
     * The agent executing this operation tries to delete the scheme board artifact 
     */
//    @OPERATION public void destroy() {
//        try {
//            super.destroy();
//            
//            for (Group g: getSchState().getGroupsResponsibleFor()) {
//                ArtifactId aid;
//                try {
//                    aid = lookupArtifact(g.getId());
//                    if (aid != null)
//                        execLinkedOp(aid, "removeScheme", getId().getName());
//                } catch (OperationException e) {
//                    e.printStackTrace();
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    
    @Override
    public void agKilled(String agName) {
        //logger.info("****** "+agName+" has quit! Removing its missions.");
        for (Player p: orgState.getPlayers() ) {
            if (orgState.removePlayer(agName, p.getTarget())) {
                try {
                    logger.info(agName+" has quit, mission "+p.getTarget()+" removed by the platform!");
                    removeObsPropertyByTemplate(obsPropCommitment, 
                            new JasonTermWrapper(agName), 
                            new JasonTermWrapper(p.getTarget()), 
                            this.getId().getName());
                    //updateMonitorScheme();
                    updateGuiOE();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    
    /**
     * The agent executing this operation tries to commit to a mission in the scheme.
     * 
     * <p>Verifications:<ul> 
     *     <li>mission max cardinality</li>
     *     <li>mission permission (if the agent plays a role that permits it to commit to the mission)</li>
     * </ul>    
     * 
     * @param mission                     the mission being committed to
     * @throws NormativeFailureException  the failure produced if the adoption breaks some regimentation
     * @throws CartagoException           some cartago problem
     */
    @Override
    @OPERATION public void commitMission(String mission) throws CartagoException {
        commitMission(getOpUserName(), mission);
    }
    
    protected void commitMission(final String ag, final String mission) throws CartagoException {
        ora4masOperationTemplate(new Operation() {
            public void exec() throws NormativeFailureException, Exception {
                orgState.addPlayer(ag, mission);
                nengine.verifyNorms();
                
                defineObsProperty(obsPropCommitment, 
                        new JasonTermWrapper(ag), 
                        new JasonTermWrapper(mission), 
                        new JasonTermWrapper(getId().getName()));
                updateGoalStateObsProp();
                
                //updateMonitorScheme();
            }
        }, "Error committing to mission "+mission);
    }
    
    /**
     * The agent executing this operation tries to leave/remove its mission in the scheme
     * 
     * <p>Verifications:<ul> 
     *     <li>the agent must be committed to the mission</li>
     *     <li>the mission's goals have to be satisfied (otherwise the agent is obliged to commit again to the mission)</li>
     * </ul>
     * 
     * @param mission                     the mission being removed
     * @throws NormativeFailureException  the failure produced if the remove breaks some regimentation
     * @throws CartagoException           some cartago problem
     * @throws MoiseException             some moise inconsistency (the agent is not committed to the mission)
     */
    @Override
    @OPERATION public void leaveMission(final String mission) throws CartagoException, MoiseException {
        ora4masOperationTemplate(new Operation() {
            public void exec() throws NormativeFailureException, Exception {
                if (orgState.removePlayer(getOpUserName(), mission)) {
                    nengine.verifyNorms();
                    
                    removeObsPropertyByTemplate(obsPropCommitment, 
                            new JasonTermWrapper(getOpUserName()), 
                            new JasonTermWrapper(mission), 
                            new JasonTermWrapper(getId().getName()));
                    
                    //updateMonitorScheme();
                }                
            }
        },"Error leaving mission "+mission);
    }
    
    /** The agent executing this operation set the goal as performed by it.
     *  
     * <p>Verifications:<ul> 
     *     <li>the agent must be committed to the goal</li>
     *     <li>the goal has to be enabled</li>
     * </ul>
     */
//    @OPERATION public void goalAchieved(String goal) throws CartagoException {
//        goalDone(getOpUserName(), goal);
//    }
    
//    private void goalDone(final String agent, final String goal) throws CartagoException {
//        ora4masOperationTemplate(new Operation() {
//            public void exec() throws NormativeFailureException, Exception {
//                getSchState().addDoneGoal(agent, goal);
//                nengine.verifyNorms();
//                if (getSchState().computeSatisfiedGoals()) { // add satisfied goals
//                    //nengine.setDynamicFacts(orgState.transform());        
//                    nengine.verifyNorms();
//                }
//                //updateMonitorScheme();
//    
//                updateGoalStateObsProp();
//            }
//        },"Error achieving goal "+goal);
//    }
    
    /** The agent executing this operation sets a value for a goal argument.
     *  
     *  @param goal                     The goal to which the value should be added
     *  @param var                      name of the variable to which the value is modified
     *  @param value                    value set to the variable of the goal
     */
    @Override
    @OPERATION public void setArgumentValue(final String goal, final String var, final Object value) throws CartagoException {
        ora4masOperationTemplate(new Operation() {
            public void exec() throws NormativeFailureException, Exception {
                getSchState().setGoalArgValue(goal, var, value.toString());
                nengine.verifyNorms();
                //updateMonitorScheme();
    
                updateGoalStateObsProp();
                defineObsProperty("goalArgument", ASSyntax.createAtom(getId().getName()), 
                        new Atom(goal), 
                        ASSyntax.createString(var), 
                        value);
            }
        },"Error setting value of argument "+var+" of "+goal+" as "+value);
    }
    
    /** The agent executing this operation reset some goal.
     * It becomes not achieved, also goals that depends on it or sub-goals are set as unachieved    
     * @param goal                      The goal to be reset
     */
    @Override
    @OPERATION public void resetGoal(final String goal) throws CartagoException {
        ora4masOperationTemplate(new Operation() {
            public void exec() throws NormativeFailureException, Exception {
                if (getSchState().resetGoal(spec.getGoal(goal))) {
                    getSchState().computeSatisfiedGoals();
                }
                nengine.verifyNorms();
                //updateMonitorScheme();
    
                updateGoalStateObsProp();
            }
        }, "Error reseting goal "+goal);
    }

    /**
     * Commands that the owner of the scheme can perform.
     * 
     * @param cmd, possible values (as strings):
     *     commitMission(<agent>,<mission>),
     *     goalDone(<agent>,<goal>) -- for performance goals --,
     *     goalSatisfied(<goal>) -- for achievement goals --
     *     setCardinality(<element type>,<element id>,<new min>,<new max>)
     *              [element type= role/subgroup]
     *     
     * @throws CartagoException
     * @throws jason.asSyntax.parser.ParseException
     * @throws MoiseException 
     * @throws NoValueException 
     */
//    @OPERATION @LINK public void admCommand(String cmd) throws CartagoException, jason.asSyntax.parser.ParseException, NoValueException, MoiseException, ParseException {
//        if (!running) return;
//        // this operation is available only for the owner of the artifact
//        if ((!getOpUserName().equals(ownerAgent)) && !getOpUserName().equals("workspace-manager")) {   
//            failed("Error: agent '"+getOpUserName()+"' is not allowed to run "+cmd,"reason",new JasonTermWrapper("not_allowed_to_start(admCommand)"));
//        } else {
//            Literal lCmd = ASSyntax.parseLiteral(cmd);
//            if (lCmd.getFunctor().equals("goalDone")) {
//                goalDone(fixAgName(lCmd.getTerm(0).toString()), lCmd.getTerm(1).toString());
//            } else if (lCmd.getFunctor().equals("commitMission")) {
//                commitMission(fixAgName(lCmd.getTerm(0).toString()), lCmd.getTerm(1).toString());
//            } else if (lCmd.getFunctor().equals("goalSatisfied")) {
//                enableSatisfied(lCmd.getTerm(0).toString());
//            } else if (lCmd.getFunctor().equals("setCardinality")) {
//                setCardinality(lCmd.getTerm(0).toString(), lCmd.getTerm(1).toString(), (int)((NumberTerm)lCmd.getTerm(2)).solve(), (int)((NumberTerm)lCmd.getTerm(3)).solve());
//            }
//        }
//    }
    
    @Override
    public void setCardinality(String element, String id, int min, int max) throws MoiseException, ParseException {
        if (element.equals("mission")) {
            spec.setMissionCardinality(id, new Cardinality(min,max));
            postReorgUpdates(spec.getFS().getOS(), "scheme("+spec.getId()+")", "fs");
        } else {
            System.out.println("setCardinality not implemented for "+element+". Ask the developers to provide you this feature!");            
        }
    }
    
//    protected void enableSatisfied(final String goal) {
//        ora4masOperationTemplate(new Operation() {
//            public void exec() throws NormativeFailureException, Exception {
//                getSchState().setAsSatisfied(goal);
//                getSchState().computeSatisfiedGoals();
//                nengine.verifyNorms();
//                //updateMonitorScheme();
//    
//                updateGoalStateObsProp();
//            }
//        }, "Error setting goal "+goal+" as satisfied");
//    }
    
    // used by Maicon in the interaction implementation
//    @LINK public void interactionCommand(String cmd) throws CartagoException, jason.asSyntax.parser.ParseException {
//        Literal lCmd = ASSyntax.parseLiteral(cmd);
//        if (lCmd.getFunctor().equals("goalAchieved")) {
//            goalDone(fixAgName(lCmd.getTerm(0).toString()), lCmd.getTerm(1).toString());
//        }
//    }
    
//    Implemented as updateRolePlayers in the super class. It had to be changed due to the fact that spec is private in the super class.
    // TODO replace rambos_updateRolePlayers with updateRolePlayers when jacamo gets updated.
    // The commit 88c15740a0e44e8367efd7b1418df13c30d70dc5 on Moise's repository has fixed this.
    @LINK 
    protected void rambos_updateRolePlayers(final String grId, final Collection<Player> rp) throws NormativeFailureException, CartagoException {
        ora4masOperationTemplate(new Operation() {
            public void exec() throws NormativeFailureException, Exception {
                Group g = new Group(grId);
                for (Player p: rp)
                    g.addPlayer(p.getAg(), p.getTarget());
                g.addResponsibleForScheme(orgState.getId());
                
                boolean newLink = !getSchState().getGroupsResponsibleFor().contains(g);
                getSchState().addGroupResponsibleFor(g);
        
                nengine.verifyNorms();
        
                getObsProperty(obsPropGroups).updateValue(getSchState().getResponsibleGroupsAsProlog());
                if (newLink) {
                    // First time the group is linked to this scheme, so create normative board
                    String nbId = grId+"."+orgState.getId();
                    ArtifactId aid = makeArtifact(nbId, NormativeBoard.class.getName(), new ArtifactConfig() );  
                    
                    execLinkedOp(aid, "load", os2nopl.transform(spec, false));
                    execLinkedOp(aid, "doSubscribeDFP", orgState.getId());
                    
                    String nplProgram = spec.getFS().getOS().getNS().getNPLNorms();
                    if (nplProgram != null) {
                        StringBuilder out = new StringBuilder();
                        out.append("scope npl_norms_for_"+spec.getId()+" {\n");
                        out.append(nplProgram);
                        out.append("\n}");
                        execLinkedOp(aid, "load", out.toString());
                	}
                    execLinkedOp(aid, "load", djSpecFileUri);
                }
            }
        }, null);
    }
    
//    @LINK 
//    void removeResponsibleGroup(final String grId) throws CartagoException {
//        ora4masOperationTemplate(new Operation() {
//            public void exec() throws NormativeFailureException, Exception {
//                getSchState().removeGroupResponsibleFor( new Group(grId) );
//        
//                nengine.verifyNorms();
//                //updateMonitorScheme();
//    
//                getObsProperty(obsPropGroups).updateValue(getSchState().getResponsibleGroupsAsProlog());
//            }
//        }, null);
//    }
    
    // list of obs props for goal states
    private List<ObsProperty> goalStObsProps = new ArrayList<ObsProperty>();
    
    protected void updateGoalStateObsProp() {
        List<Literal> goals = getGoalStates();

        // remove goals in obs prop that is no more in goal states
        Iterator<ObsProperty> iop = goalStObsProps.iterator();
        while (iop.hasNext()) {
            ObsProperty op = iop.next();
            
            // search in goals
            boolean found = false;
            Iterator<Literal> i = goals.iterator();
            while (i.hasNext()) {
                Literal g = i.next();
                
                if (isObsPropEqualsGoal(g,op)) {
                    found = true;
                    i.remove(); // this goal does not be added                    
                    break;
                }
            }
            
            if (!found) { // remove
                iop.remove();
                removeObsPropertyByTemplate(op.getName(), op.getValues());
            }
        }
        
        // add the remaining as new obs prop
        for (Literal goal: goals) {
            Object[] terms = getTermsAsProlog(goal);
            defineObsProperty(goal.getFunctor(), terms);
            goalStObsProps.add( getObsPropertyByTemplate(goal.getFunctor(), terms));
        }
    }
    
    // TODO: remove
    static Object[] getTermsAsProlog(Literal o) {
        Object[] terms = new Object[o.getArity()];
        int i = 0;
        for (Term t: o.getTerms())
            terms[i++] = new JasonTermWrapper(t);
        return terms;
    }

    private boolean isObsPropEqualsGoal(Literal g, ObsProperty op) {
        if (!g.getFunctor().equals(op.getName()))
            return false;
        for (int i=0; i<g.getArity(); i++) 
            //if (! ((JasonTermWrapper)op.getValue(i)).getTerm().equals(g.getTerm(i)) )
            if (! ((JasonTermWrapper)op.getValue(i)).toString().equals(g.getTerm(i).toString()) )
                return false;
        return true;
    }


    /*
    private void updateMonitorScheme() throws CartagoException {
        if (monitorSchArt != null) {
            execLinkedOp(monitorSchArt, "updateMonitoredScheme", orgState);
        }
    }
    */
    
    /*
    public static List<String> computeAccomplisedMissions(String schId, Collection<Mission> missions, NPLInterpreter nengine) {
        Atom aSch = new Atom(schId);
        List<String> as = new ArrayList<String>();
        for (Mission m: missions) {
            boolean all = true;
            for (Goal g: m.getGoals()) {
                //System.out.println(m+" "+g);
                Atom aGoal  = new Atom(g.getId());
                if (!nengine.holds(ASSyntax.createLiteral("satisfied", aSch, aGoal))) {
                    all = false;
                    //System.out.println("not ok for "+aSch+" "+aGoal);
                    break;
                }
            }
            if (all)
                as.add(m.getId());
        }
        return as;
    }
    */

    private static final Atom aWaiting   = new Atom("waiting");
    private static final Atom aEnabled   = new Atom("enabled");
    private static final Atom aSatisfied = new Atom("satisfied");
        
    List<Literal> getGoalStates() {
        List<Literal> all = new ArrayList<Literal>();
        Term tSch = ASSyntax.createAtom(this.getId().getName());
        for (Goal g: spec.getGoals()) {
            Atom aGoal  = new Atom(g.getId());
            Literal lGoal = ASSyntax.createLiteral(g.getId());
            
            // add arguments
            // removed to keep it compatible with obligation event
            // goalArgValue obs property was added for that
            /*if (g.hasArguments()) {
                for (String arg: g.getArguments().keySet()) {
                    String value = getSchState().getGoalArgValue(g.getId(), arg);
                    if (value == null) {
                        lGoal.addTerm(new VarTerm(arg));
                    } else {
                        try {
                            lGoal.addTerm(ASSyntax.parseTerm(value));
                        } catch (jason.asSyntax.parser.ParseException e) {
                            lGoal.addTerm(new StringTermImpl(value));
                        }
                    }
                }
            }*/
                
            // state
            Atom aState = aWaiting;
            if (nengine.holds(new NPLLiteral(ASSyntax.createLiteral("satisfied", tSch, aGoal), orgState))) { 
                aState = aSatisfied;
            } else if (nengine.holds(ASSyntax.createLiteral("well_formed", tSch)) && 
                nengine.holds(ASSyntax.createLiteral("enabled", tSch, aGoal))) {
                aState = aEnabled;
            }              

            // performed by
            ListTerm lAchievedBy = new ListTermImpl();
            ListTerm tail = lAchievedBy;
            for (Literal p: getSchState().getDoneGoals()) {
                if (p.getTerm(1).equals(aGoal))
                    tail = tail.append(p.getTerm(2));
            }
            
            // create the literal
            Literal lGoalSt = ASSyntax.createLiteral(
                    piGoalState.getFunctor(),
                    tSch,
                    lGoal,
                    getSchState().getCommittedAgents(g), // lCommittedBy
                    lAchievedBy,
                    aState);
            all.add(lGoalSt);
        }
        return all;
    }

    @Override
    public String getNPLSrc() {
        if (spec != null)
            return os2nopl.header(spec)+os2nopl.transform(spec, true);
        else
            return super.getNPLSrc();
    }
    
//    protected String getStyleSheetName() {
//        return "noplSchemeInstance";                
//    }

    
    public Element getAsDOM(Document document) {
        Element schEle = (Element) document.createElement( SchemeInstance.getXMLTag());
        schEle.setAttribute("id", getSchState().getId());
        schEle.setAttribute("specification", spec.getId());
        schEle.setAttribute("root-goal", spec.getRoot().getId());
        schEle.setAttribute("owner", ownerAgent);

        Term aSch = ASSyntax.createAtom(this.getId().getName());

        // status
        Element wfEle = (Element) document.createElement("well-formed");
        if (nengine.holds(ASSyntax.createLiteral("well_formed", aSch))) {
            wfEle.appendChild(document.createTextNode("ok"));            
        } else {
            wfEle.appendChild(document.createTextNode("not ok"));  
        }
        schEle.appendChild(wfEle);
        
        // players
        if (!getSchState().getPlayers().isEmpty()) {
            Element plEle = (Element) document.createElement("players");
            for (Player p: getSchState().getPlayers()) {
                Element mpEle = (Element) document.createElement( MissionPlayer.getXMLTag());
                mpEle.setAttribute("mission", p.getTarget());
                mpEle.setAttribute("agent", p.getAg());
                plEle.appendChild(  mpEle );
            }
            schEle.appendChild(plEle);
        }

        // responsible groups
        Element rgEle = (Element) document.createElement("responsible-groups");
        for (Group g: getSchState().getGroupsResponsibleFor()) {
            Element gEle = (Element) document.createElement("group");
            gEle.setAttribute("id", g.getId());
            rgEle.appendChild(gEle);
        }
        schEle.appendChild(rgEle);

        // goals (with variable values)
        List<Literal> goals = getGoalStates();
        if (!goals.isEmpty()) {
            Element gsEle = (Element) document.createElement("goals");
            for (Literal lg: goals) {
                String gId = ((Literal)lg.getTerm(1)).getFunctor(); 
                Goal   gSpec = spec.getGoal(gId);
                Element giEle = (Element) document.createElement(GoalInstance.getXMLTag());
                giEle.setAttribute("specification", gId);
                giEle.setAttribute("state", lg.getTerm(4).toString());
                giEle.setAttribute("root", gSpec.isRoot()+"");
                giEle.setAttribute("committed-ags", lg.getTerm(2).toString());
                giEle.setAttribute("achieved-by", lg.getTerm(3).toString());
                StringBuilder spaces = new StringBuilder();
                for (int i=0; i<gSpec.getDepth(); i++)
                    spaces.append("  ");
                giEle.setAttribute("depth", spaces.toString());
                
                // arguments
                if (gSpec.hasArguments()) {
                    for (String arg: gSpec.getArguments().keySet()) {
                        String value = getSchState().getGoalArgValue(gId, arg);
                        Element argEle = (Element) document.createElement("argument");
                        argEle.setAttribute("id",arg);
                        if (value != null) {
                            argEle.setAttribute("value", value);
                        } else {
                            argEle.setAttribute("value", "undefined");                            
                        }
                        giEle.appendChild(argEle);                        
                    }
                }
                
                // plan
                if (gSpec.getPlan() != null) {
                    giEle.appendChild(gSpec.getPlan().getAsDOM(document));
                }
                // explicit dependencies
                
                for (Goal dg: gSpec.getPreConditionGoals()) {
                    Element ea = (Element) document.createElement("depends-on");
                    if (gSpec.hasDependence() && gSpec.getDependencies().contains(dg)) {
                        ea.setAttribute("explicit", "true");
                    }
                    ea.setAttribute("goal", dg.getId());
                    giEle.appendChild(ea);                
                }
                                
                gsEle.appendChild(giEle);
            }
            schEle.appendChild(gsEle);
        }

        return schEle;
    }
    
//    public String getAsDot() {
//        StringWriter so = new StringWriter();
//        
//        so.append("digraph "+getId()+" {ordering=out label=\""+getId()+": "+spec.getId()+"\" labelloc=t labeljust=r fontname=\"Italic\" \n");
//        so.append("    rankdir=BT; \n");
//        
//        // goals
//        so.append( os2dot.transform( spec.getRoot(), 0, this));
//
//        // missions 
//        for (Mission m: spec.getMissions()) {
//            so.append( os2dot.transform(m, spec));
//            for (Goal g: m.getGoals()) {
//                so.append("        "+m.getId()+" -> "+g.getId()+" [arrowsize=0.5];\n");
//            }                
//        }
//        for (Player p: getSchState().getPlayers()) {
//            so.append("        "+p.getAg()+ "[shape=plaintext];\n");
//            so.append("        "+p.getAg()+" -> "+p.getTarget()+" [arrowsize=0.5];\n");
//        }
//
//        so.append("}\n");
//        //System.out.println(so);
//        return so.toString();
//    }
}

