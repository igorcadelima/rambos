package rambos.oa;

import java.io.IOException;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import cartago.ArtifactConfig;
import cartago.ArtifactId;
import cartago.LINK;
import cartago.OPERATION;
import cartago.OpFeedbackParam;
import cartago.OperationException;
import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.LogicalFormula;
import moise.common.MoiseException;
import npl.Scope;
import npl.parser.ParseException;
import rambos.mechanism.Norm;
import rambos.mechanism.rep.DeJure;
import rambos.oa.util.DJUtil;

/**
 * @author igorcadelima
 *
 */
public class NormativeBoard extends ora4mas.nopl.NormativeBoard {
	protected RuleEngine ruleEngine;
	protected ArtifactId deJureRep;

	@LINK
	@OPERATION
	@Override
	public void load(String file) throws MoiseException, ParseException {
		try {
			// Validate spec file
			Document doc = DJUtil.parseSpec(file);
			DJUtil.validateSpec(new DOMSource(doc));
			
			// Init DeJure
			String djId = getId() + ".dj";
			deJureRep = makeArtifact(djId, DeJure.class.getName(), new ArtifactConfig(file, this));
			
			// Load normative program into the interpreter
			OpFeedbackParam<Scope> scope = new OpFeedbackParam<Scope>();
			execLinkedOp(deJureRep, "createNPLScope", scope);
			nengine.loadNP(scope.get());
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// Not a De Jure specification file
			super.load(file);
		} catch (OperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @param ruleEngine Output parameter to return the rule engine
	 */
	@OPERATION
	protected void getRuleEngine(OpFeedbackParam<RuleEngine> ruleEngine) {
		ruleEngine.set(this.ruleEngine);
	}
	
	/**
	 * Check whether a norm is applicable by testing the truth value of its conditions.
	 * 
	 * @param norm Norm whose conditions will be checked against the environmental facts
	 * @param ruled Output parameter to return whether the given formula may be derived from the environment
	 */
	@OPERATION
	protected void testCondition(Norm norm, OpFeedbackParam<Boolean> ruled) {
		LogicalFormula formula = norm.getCondition();
		Agent ag = nengine.getAg();
		Iterator<Unifier> i = formula.logicalConsequence(ag, new Unifier());
		ruled.set(i.hasNext());
	}
	
	
}

