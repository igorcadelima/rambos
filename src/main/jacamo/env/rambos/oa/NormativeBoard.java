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

import java.io.IOException;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import cartago.ArtifactConfig;
import cartago.ArtifactId;
import cartago.INTERNAL_OPERATION;
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
	protected ArtifactId deJure;

	/**
	 * Initialise a NormativeBoard retrieving DeJure the organisation and
	 * deploying its norms to the normative engine.
	 * 
	 * @param orgName
	 *            name of the organisation
	 */
	public void init(String orgName) {
		super.init();

		OpFeedbackParam<ArtifactId> deJureOut = new OpFeedbackParam<ArtifactId>();
		execInternalOp("getDeJure", orgName, deJureOut);
		deJure = deJureOut.get();

		execInternalOp("deployDeJureNorms");
	}

	/**
	 * Get the De Jure repository from the {@code orgName} organisation and
	 * return it through an output parameter.
	 * 
	 * @param orgName
	 *            name of the organisation
	 * @param out
	 *            output parameter use to return the id of De Jure
	 * @throws OperationException
	 */
	@INTERNAL_OPERATION
	protected synchronized void getDeJure(String orgName, OpFeedbackParam<ArtifactId> out) throws OperationException {
		ArtifactId orgBoardId = lookupArtifact(orgName);
		execLinkedOp(orgBoardId, "getDeJureId", out);
	}

	/**
	 * Deploy available norms contained in De Jure to the normative engine.
	 * 
	 * @throws OperationException
	 */
	@INTERNAL_OPERATION
	protected synchronized void deployDeJureNorms() throws OperationException {
		OpFeedbackParam<Scope> scope = new OpFeedbackParam<Scope>();
		execLinkedOp(deJure, "createNPLScope", scope);
		nengine.loadNP(scope.get());
	}

	@LINK
	@OPERATION
	@Override
	public void load(String file) throws MoiseException, ParseException {
		try {
			// Validate spec file
			DJUtil.parseDocument(file);

			// Init DeJure
			String djId = getId() + ".dj";
			deJure = makeArtifact(djId, DeJure.class.getName(), new ArtifactConfig(file));

			// Load normative program into the interpreter
			OpFeedbackParam<Scope> scope = new OpFeedbackParam<Scope>();
			execLinkedOp(deJure, "createNPLScope", scope);
			nengine.loadNP(scope.get());
		} catch (SAXException | IOException | ParserConfigurationException e) {
			// Not a De Jure specification file
			super.load(file);
			// System.out.println(e);
		} catch (OperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Check whether a norm is applicable by testing the truth value of its
	 * activation condition.
	 * 
	 * @param norm
	 *            Norm whose condition will be checked against the environmental
	 *            facts
	 * @param ruled
	 *            Output parameter to return whether the given formula may be
	 *            derived from the environment
	 */
	@OPERATION
	protected void testCondition(Norm norm, OpFeedbackParam<Boolean> ruled) {
		LogicalFormula formula = norm.getCondition();
		Agent ag = nengine.getAg();
		Iterator<Unifier> i = formula.logicalConsequence(ag, new Unifier());
		ruled.set(i.hasNext());
	}

}
