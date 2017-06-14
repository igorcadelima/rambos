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
package rambos;

import java.util.Iterator;

import cartago.ArtifactObsProperty;
import jason.RevisionFailedException;
import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.parser.ParseException;
import npl.AbstractNorm;

import static jason.asSyntax.ASSyntax.parseLiteral;

public class Norm extends AbstractNorm implements INorm {
	protected boolean disabled = false;
	protected String issuer;

	private Norm() {
	}

	@Override
	public Literal getContent() {
		return getConsequence();
	}

	@Override
	public String getIssuer() {
		return issuer;
	}

	@Override
	public boolean isDisabled() {
		return disabled;
	}

	@Override
	public boolean match(ArtifactObsProperty event) {
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

	@Override
	public AbstractNorm clone() {
		Norm clone = new Norm();
		clone.id = id;
		clone.disabled = disabled;
		clone.condition = condition;
		clone.issuer = issuer;
		clone.consequence = consequence;
		return clone;
	}

	public static final class NormBuilder {
		private Norm norm = new Norm();

		public NormBuilder setId(String id) {
			norm.id = id;
			return this;
		}

		public NormBuilder setDisabled(boolean disabled) {
			norm.disabled = disabled;
			return this;
		}

		public NormBuilder setCondition(LogicalFormula condition) {
			norm.condition = condition;
			return this;
		}

		public NormBuilder setIssuer(String issuer) {
			norm.issuer = issuer;
			return this;
		}

		public NormBuilder setContent(Literal content) {
			norm.consequence = content;
			return this;
		}

		/**
		 * Build a {@link Norm} with the set building state.
		 * 
		 * In order to successfully build a {@link Norm}, the following
		 * properties should be different from {@code null}: {@code id},
		 * {@code condition}, {@code issuer}, {@code content}.
		 * 
		 * @return {@link Norm} instance with the set building state
		 */
		public Norm build() {
			if ((norm.id != null) && (norm.condition != null) && (norm.issuer != null) && (norm.consequence != null)) {
				return (Norm) norm.clone();
			}
			throw new RuntimeException("The following properties should be set: id, condition, issuer, content.");
		}
	}
}
