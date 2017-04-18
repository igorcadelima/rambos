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
package rambos.mechanism;

import java.util.Iterator;

import cartago.ArtifactObsProperty;
import jason.RevisionFailedException;
import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.parser.ParseException;

import static jason.asSyntax.ASSyntax.parseLiteral;
import npl.NPLInterpreter;

public class Norm extends npl.Norm {
	protected boolean disabled;
	protected String issuer;

	/**
	 * @param id
	 * @param disabled
	 * @param condition
	 * @param issuer
	 * @param content
	 */
	public Norm(String id, boolean disabled, LogicalFormula condition, String issuer, Literal content) {
		super(id, content, condition);
		this.disabled = disabled;
		this.issuer = issuer;
	}

	/**
	 * @return norm's content
	 */
	public Literal getContent() {
		return getConsequence();
	}

	/**
	 * Check whether the data content of an event is ruled by the norm.
	 * 
	 * @param nEngine
	 * @param event
	 * @return true if event data is ruled by the norm.
	 */
	public boolean match(NPLInterpreter nEngine, ArtifactObsProperty event) {
		try {
			// Parse event data to Literal
			Literal propLiteral = parseLiteral(event.toString());

			// Add parsed Literal to BB to check norm condition against it
			Agent ag = new Agent();
			ag.addBel(propLiteral);

			// Get norm condition to see whether the event is ruled by it
			LogicalFormula condition = getCondition();
			Iterator<Unifier> i = condition.logicalConsequence(ag, new Unifier());

			return i.hasNext();
		} catch (ParseException | RevisionFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
}