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
package rambos.ora4mas;

import java.util.Iterator;

import cartago.ArtifactId;
import cartago.INTERNAL_OPERATION;
import cartago.OPERATION;
import cartago.OpFeedbackParam;
import cartago.OperationException;
import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.LogicalFormula;
import npl.Scope;
import rambos.Norm;
import rambos.ora4mas.db.DeJure;

/**
 * @author igorcadelima
 *
 */
public class NormativeBoard extends ora4mas.nopl.NormativeBoard {
	protected ArtifactId deJure;

	/**
	 * Initialise a {@link NormativeBoard} retrieving {@link DeJure} from the
	 * organisation and deploying its norms to the normative engine.
	 * 
	 * @param orgName
	 *            name of the organisation
	 * @see OrgBoard
	 */
	public void init(String orgName) {
		super.init();
		execInternalOp("setup", orgName);
	}

	@INTERNAL_OPERATION
	protected void setup(String orgName) throws OperationException {
		deJure = getDeJure(orgName);
		deployDeJureNorms();
	}

	/**
	 * Get id of {@link DeJure} repository from the {@code orgName}
	 * organisation.
	 * 
	 * @param orgName
	 *            name of the organisation
	 * @return {@link DeJure} id
	 * @throws OperationException
	 * @see OrgBoard
	 */
	protected ArtifactId getDeJure(String orgName) throws OperationException {
		ArtifactId orgBoardId = lookupArtifact(orgName);
		OpFeedbackParam<ArtifactId> out = new OpFeedbackParam<ArtifactId>();
		execLinkedOp(orgBoardId, "getDeJureId", out);
		return out.get();
	}

	/**
	 * Deploy available norms contained in {@link DeJure} to the normative
	 * engine.
	 * 
	 * @throws OperationException
	 */
	protected void deployDeJureNorms() throws OperationException {
		assert deJure != null;
		OpFeedbackParam<Scope> scope = new OpFeedbackParam<Scope>();
		execLinkedOp(deJure, "createNPLScope", scope);
		nengine.loadNP(scope.get());
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