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
package rambos.ora4mas.db;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.xml.sax.SAXException;

import cartago.Artifact;
import cartago.ArtifactConfig;
import cartago.ArtifactId;
import cartago.LINK;
import cartago.OPERATION;
import cartago.OpFeedbackParam;
import cartago.OperationException;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.parser.ParseException;
import npl.Scope;
import rambos.Norm;
import rambos.Sanction;

/**
 * @author igorcadelima
 *
 */
public class DeJure extends Artifact {
	// normId -> norm
	private Map<String, Norm> norms;
	// sanctionId -> sanction
	private Map<String, Sanction> sanctions;
	// normId -> [sanctionId0, sanctionId1, ..., sanctionIdn]
	private Map<String, Set<String>> links;

	/**
	 * Initialise a {@link DeJure} repository based on data from the
	 * {@link DeJureBuilder} passed as argument.
	 * 
	 * @param builder
	 *            builder from which data should be obtained
	 */
	@SuppressWarnings("unused")
	private void init(DeJureBuilder builder) throws SAXException, IOException {
		norms = builder.norms;
		sanctions = builder.sanctions;
		links = builder.links;
	}

	/**
	 * Add norm to norms set.
	 * 
	 * @param n
	 *            norm o be added
	 * @return true if norm is added successfully
	 */
	@LINK
	@OPERATION
	public synchronized boolean addNorm(Norm n) {
		// TODO: check whether operator agent is a legislator
		if (norms.put(n.getId(), n) == n) {
			links.put(n.getId(), new HashSet<String>());
			return true;
		}
		return false;
	}

	/**
	 * Remove norm from norms set.
	 * 
	 * @param n
	 * @return true if norm is removed successfully
	 */
	@LINK
	@OPERATION
	public synchronized boolean removeNorm(Norm n) {
		// TODO: check whether operator agent is a legislator
		if (norms.remove(n.getId()) == n) {
			links.remove(n.getId());
			return true;
		}
		return false;
	}

	/**
	 * Add sanction to sanctions set.
	 * 
	 * @param s
	 *            sanction to be added
	 * @return true if sanction is added successfully
	 */
	@LINK
	@OPERATION
	public synchronized boolean addSanction(Sanction s) {
		// TODO: check whether operator agent is a legislator
		if (sanctions.put(s.getId(), s) == s) {
			return true;
		}
		return false;
	}

	/**
	 * Remove sanction from sanctions set.
	 * 
	 * @param s
	 * @return true if sanction is removed successfully
	 */
	@LINK
	@OPERATION
	public synchronized boolean removeSanction(Sanction s) {
		// TODO: check whether operator agent is a legislator
		if (sanctions.remove(s.getId()) == s) {
			return true;
		}
		return false;
	}

	/**
	 * Add link to links set.
	 * 
	 * @param normId
	 *            the norm id
	 * @param sanctionId
	 *            the sanction id
	 * @return true if link is created successfully
	 */
	@LINK
	@OPERATION
	public synchronized boolean addLink(String normId, String sanctionId) {
		// TODO: check whether operator agent is a legislator
		Norm n = norms.get(normId);
		Sanction s = sanctions.get(sanctionId);
		return addLink(n, s);
	}

	/**
	 * Add link to links set.
	 * 
	 * @param n
	 *            the norm
	 * @param s
	 *            the sanction
	 * @return true if link is created successfully
	 */
	@LINK
	@OPERATION
	public synchronized boolean addLink(Norm n, Sanction s) {
		// TODO: check whether operator agent is a legislator
		if (n == null || s == null)
			return false;

		Set<String> linkedSanctions = links.get(n.getId());
		if (linkedSanctions.add(s.getId())) {
			links.put(n.getId(), linkedSanctions);
			return true;
		}

		return false;
	}

	/**
	 * Remove link from links set.
	 * 
	 * @param normId
	 *            the norm id
	 * @param sanctionId
	 *            the sanction id
	 * @return true if link is destroyed successfully
	 */
	@LINK
	@OPERATION
	public synchronized boolean removeLink(String normId, String sanctionId) {
		// TODO: check whether operator agent is a legislator
		Norm n = norms.get(normId);
		Sanction s = sanctions.get(sanctionId);
		return removeLink(n, s);
	}

	/**
	 * Remove link from links set.
	 * 
	 * @param n
	 *            the norm
	 * @param s
	 *            the sanction
	 * @return true if link is destroyed successfully
	 */
	@LINK
	@OPERATION
	public synchronized boolean removeLink(Norm n, Sanction s) {
		// TODO: check whether operator agent is a legislator
		if (n == null || s == null)
			return false;

		Set<String> linkedSanctions = links.get(n.getId());
		if (linkedSanctions.remove(s.getId())) {
			links.put(n.getId(), linkedSanctions);
			return true;
		}

		return false;
	}

	/**
	 * Get norms set as a {@link Map<{@link String}, {@link Norm}>}, whose keys
	 * are norms ids.
	 * 
	 * @return copy of the norms
	 */
	@LINK
	@OPERATION
	public Map<String, Norm> getNorms() {
		return new ConcurrentHashMap<String, Norm>(norms);
	}

	/**
	 * Get sanction set as a {@code {@link Map}<{@link String},
	 * {@link Sanction}>}, whose keys are sanctions ids.
	 * 
	 * @return copy of the sanctions
	 */
	@LINK
	@OPERATION
	public Map<String, Sanction> getSanctions() {
		return new ConcurrentHashMap<String, Sanction>(sanctions);
	}

	/**
	 * Get links set as a {@code {@link Map}<{@link String},
	 * {@link Set}<{@link String}>>}, whose keys are norms ids.
	 * 
	 * @return copy of the links
	 */
	@LINK
	@OPERATION
	public Map<String, Set<String>> getLinks() {
		return new ConcurrentHashMap<String, Set<String>>(links);
	}

	/**
	 * Create a NPL Scope based on the repository's properties.
	 * 
	 * @param dj
	 *            the DeJure repository
	 * @return a NPL Scope extracted from the repository's properties
	 */
	@LINK
	@OPERATION
	public Scope createNPLScope(OpFeedbackParam<Scope> outScope) {
		try {
			Literal id = ASSyntax.parseLiteral("np");
			Scope scope = new Scope(id, null);
			for (Norm n : getNorms().values()) {
				if (!n.isDisabled())
					scope.addNorm(n);
			}
			outScope.set(scope);
			return scope;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static class DeJureBuilder extends AbstractDeJureBuilder {
		@Override
		@LINK
		public void build(String name, OpFeedbackParam<ArtifactId> out) throws OperationException {
			out.set(makeArtifact(name, DeJure.class.getName(), new ArtifactConfig(this)));
		}

		/**
		 * Destroy this artefact by calling
		 * {@link Artifact#dispose(ArtifactId)}.
		 * 
		 * @throws OperationException
		 */
		@LINK
		public void destroy() throws OperationException {
			try {
				dispose(getId());
			} catch (Exception e) {
				/*
				 * CArtAgO's bug. An issue has been created in
				 * https://github.com/CArtAgO-lang/cartago/issues/2
				 */
			}
		}
	}
}