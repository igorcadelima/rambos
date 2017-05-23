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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cartago.Artifact;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.ArithExpr;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;
import jason.asSyntax.ArithExpr.ArithmeticOp;
import jason.asSyntax.parser.ParseException;
import npl.NormativeProgram;
import npl.TimeTerm;
import rambos.Norm;
import rambos.Sanction;
import rambos.SanctionCategory;
import rambos.SanctionDiscernability;
import rambos.SanctionIssuer;
import rambos.SanctionLocus;
import rambos.SanctionMode;
import rambos.SanctionPolarity;
import rambos.SanctionPurpose;
import rambos.Status;
import rambos.ora4mas.util.DJUtil;

/**
 * @author igorcadelima
 *
 */
public abstract class AbstractDeJureBuilder extends Artifact implements IDeJureBuilder {
	protected static final String NORMS_TAG = "norms";
	protected static final String SANCTIONS_TAG = "sanctions";
	protected static final String LINKS_TAG = "links";

	// normId -> norm
	protected Map<String, Norm> norms;
	// sanctionId -> sanction
	protected Map<String, Sanction> sanctions;
	// normId -> [sanctionId0, sanctionId1, ..., sanctionIdn]
	protected Map<String, Set<String>> links;

	/**
	 * Create a {@link DeJure} builder extracting data from the normative
	 * specification passed as argument if it is valid.
	 * 
	 * @param ns
	 *            normative specification
	 * @throws IOException
	 * @throws SAXException
	 */
	public void init(Document ns) throws SAXException, IOException {
		norms = new ConcurrentHashMap<String, Norm>();
		sanctions = new ConcurrentHashMap<String, Sanction>();
		links = new ConcurrentHashMap<String, Set<String>>();

		DJUtil.validate(ns, DJUtil.NS_SCHEMA_PATH);
		extractSpecData(ns);
	}

	/**
	 * Extract specification data from document.
	 * 
	 * @param doc
	 */
	private void extractSpecData(Document doc) {
		try {
			extractNorms(doc);
			extractSanctions(doc);
		} catch (DOMException | ParseException | npl.parser.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		extractLinks(doc);
	}

	/**
	 * Extract norms from document.
	 * 
	 * @param doc
	 * @throws DOMException
	 * @throws ParseException
	 * @throws npl.parser.ParseException
	 */
	private void extractNorms(Document doc) throws DOMException, ParseException, npl.parser.ParseException {
		Node normsRootEl = doc.getElementsByTagName(NORMS_TAG).item(0);
		List<Element> norms = getChildElements(normsRootEl);

		for (Element normEl : norms) {
			String id = normEl.getAttribute("id");
			boolean disabled = Boolean.valueOf(normEl.getAttribute("disabled"));
			NodeList properties = normEl.getChildNodes();
			addNorm(createNorm(properties, id, disabled));
		}
	}

	/**
	 * Extract sanctions from document.
	 * 
	 * @param doc
	 * @throws ParseException
	 * @throws DOMException
	 */
	private void extractSanctions(Document doc) throws DOMException, ParseException {
		Node sanctionsRootEl = doc.getElementsByTagName(SANCTIONS_TAG).item(0);
		List<Element> sanctions = getChildElements(sanctionsRootEl);

		for (Element sanctionEl : sanctions) {
			String id = sanctionEl.getAttribute("id");
			Status status = Status.ACTIVE;
			LogicalFormula condition = null;
			SanctionCategory category = null;

			List<Element> properties = getChildElements(sanctionEl);
			for (Element property : properties) {
				switch (property.getNodeName()) {
				case "status":
					status = Status.valueOf(property.getTextContent().toUpperCase());
					break;
				case "condition":
					condition = ASSyntax.parseFormula(property.getTextContent());
					break;
				case "category":
					category = parseSanctionCategory(property);
					break;
				}
			}
			addSanction(new Sanction(id, status, condition, category));
		}
	}

	/**
	 * Extract the category dimensions from given node, create a
	 * {@link SanctionCategory}, and return it.
	 * 
	 * @param category
	 * @return parsed {@link SanctionCategory} based on given node
	 */
	private SanctionCategory parseSanctionCategory(Node category) {
		NodeList dimensions = category.getChildNodes();
		SanctionPurpose purpose = null;
		SanctionIssuer issuer = null;
		SanctionLocus locus = null;
		SanctionMode mode = null;
		SanctionPolarity polarity = null;
		SanctionDiscernability discernability = null;

		for (int i = 0; i < dimensions.getLength(); i++) {
			Node dimension = dimensions.item(i);
			String dimensionContent = dimension.getTextContent().toUpperCase();

			switch (dimension.getNodeName()) {
			case "purpose":
				purpose = SanctionPurpose.valueOf(dimensionContent);
				break;
			case "issuer":
				issuer = SanctionIssuer.valueOf(dimensionContent);
				break;
			case "locus":
				locus = SanctionLocus.valueOf(dimensionContent);
				break;
			case "mode":
				mode = SanctionMode.valueOf(dimensionContent);
				break;
			case "polarity":
				polarity = SanctionPolarity.valueOf(dimensionContent);
				break;
			case "discernability":
				discernability = SanctionDiscernability.valueOf(dimensionContent);
				break;
			}
		}
		return new SanctionCategory(purpose, issuer, locus, mode, polarity, discernability);
	}

	/**
	 * Extract links from document.
	 * 
	 * @param doc
	 */
	private void extractLinks(Document doc) {
		Node linksRootEl = doc.getElementsByTagName(LINKS_TAG).item(0);
		NodeList linkNodes = linksRootEl.getChildNodes();
		for (int i = 0; i < linkNodes.getLength(); i++) {
			Node linkNode = linkNodes.item(i);

			if (linkNode.getNodeType() == Node.ELEMENT_NODE) {
				Node normIdNode = linkNode.getFirstChild();
				String normId = normIdNode.getTextContent();

				Node sanctionsNode = linkNode.getLastChild();
				NodeList sanctionIdsList = sanctionsNode.getChildNodes();
				for (int j = 0; j < sanctionIdsList.getLength(); j++) {
					Node sanctionIdNode = sanctionIdsList.item(j);

					if (sanctionIdNode.getNodeType() == Node.ELEMENT_NODE) {
						String sanctionId = sanctionIdNode.getTextContent();
						addLink(normId, sanctionId);
					}
				}
			}
		}
	}

	/**
	 * Extract and return parent node's child elements.
	 * 
	 * @param parent
	 *            parent node
	 * @return list of child elements
	 */
	private List<Element> getChildElements(Node parent) {
		List<Element> nodes = new ArrayList<Element>();
		NodeList childNodes = parent.getChildNodes();

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node normNode = childNodes.item(i);

			if (normNode.getNodeType() == Node.ELEMENT_NODE) {
				Element normEl = (Element) normNode;
				nodes.add(normEl);
			}
		}
		return nodes;
	}

	/**
	 * Create a norm based on properties passed as argument.
	 * 
	 * @param properties
	 * @param id
	 * @param disabled
	 * @return new norm
	 * @throws DOMException
	 * @throws ParseException
	 * @throws npl.parser.ParseException
	 */
	private Norm createNorm(NodeList properties, String id, boolean disabled)
			throws DOMException, ParseException, npl.parser.ParseException {
		LogicalFormula condition = null;
		String issuer = null;
		Literal content = null;

		for (int i = 0; i < properties.getLength(); i++) {
			Node prop = properties.item(i);
			String propContent = prop.getTextContent();

			switch (prop.getNodeName()) {
			case "condition":
				condition = ASSyntax.parseFormula(propContent);
				break;
			case "issuer":
				issuer = propContent;
				break;
			case "content":
				content = parseNormContent(propContent, id, issuer, condition);
				break;
			}
		}
		return new Norm(id, disabled, condition, issuer, content);
	}

	/**
	 * Parse content as literal.
	 * 
	 * @param content
	 * @param normId
	 * @param issuer
	 * @param normCondition
	 * @return literal form of {@code content}
	 * @throws ParseException
	 *             if {@code content} does not have a valid format
	 */
	private Literal parseNormContent(String content, String normId, String issuer, LogicalFormula normCondition)
			throws ParseException {
		Literal literal;
		try {
			literal = parseNormFailContent(content);
		} catch (ParseException e) {
			literal = parseNormObligationContent(content, normId, normCondition, issuer);
		}
		return literal;
	}

	/**
	 * Parse string to obligation literal.
	 * 
	 * @param obligation
	 * @param normId
	 * @param normCondition
	 * @param issuer
	 * @return obligation literal if {@code content} is a well-formed string
	 * @throws ParseException
	 */
	private Literal parseNormObligationContent(String obligation, String normId, LogicalFormula normCondition,
			String issuer) throws ParseException {
		String obligationRegex = "obligation\\s*\\(\\s*(\\w+)\\s*,\\s*(\\w+)\\s*,\\s*(.+?)\\s*,\\s*(.+?)\\s*\\)";
		Pattern pattern = Pattern.compile(obligationRegex);
		Matcher matcher = pattern.matcher(obligation);

		if (matcher.matches()) {
			String agent = matcher.group(1);
			String condition = matcher.group(2);
			String goal = matcher.group(3);
			String deadline = matcher.group(4);
			Literal literal = ASSyntax.createLiteral(NormativeProgram.OblFunctor);

			// Parse and add agent term
			literal.addTerm(parseAgent(agent));

			// Parse and add condition term
			literal.addTerm(parseCondition(condition, normId, normCondition));

			// Parse and add goal term
			literal.addTerm(ASSyntax.parseFormula(goal));

			// Solve and add deadline term
			literal.addTerm(solveTimeExpression(deadline));

			// Add annotations
			literal.addAnnot(ASSyntax.createStructure("norm", new Atom(normId)));
			literal.addAnnot(ASSyntax.createStructure("issuer", new Atom(issuer)));
			return literal;
		}
		throw new ParseException();
	}

	/**
	 * Parse {@code condition} using {@link ASSyntax#parseFormula(String)} and
	 * return the result as a {@link Term}.
	 * 
	 * @param condition
	 * @param normId
	 * @param normCondition
	 * @return parsed condition as a {@link Term}
	 * @throws ParseException
	 */
	private Term parseCondition(String condition, String normId, LogicalFormula normCondition) throws ParseException {
		LogicalFormula conditionLiteral = ASSyntax.parseFormula(condition);
		if (((Literal) conditionLiteral).getFunctor().equals(normId)) {
			return normCondition;
		} else {
			return conditionLiteral;
		}
	}

	/**
	 * Parse agent name to {@link VarTerm} or {@link Atom} depending on what it
	 * really represents.
	 * 
	 * @param agent
	 * @return term with given agent name.
	 */
	private Term parseAgent(String agent) {
		char agentInitial = agent.charAt(0);
		if (Character.isUpperCase(agentInitial)) {
			return new VarTerm(agent);
		} else {
			return new Atom(agent);
		}
	}

	/**
	 * Solve a time expression such as {@code `now` + `2 days`} and return the
	 * result as {@link TimeTerm}.
	 * 
	 * @param time
	 * @return resolved {@link TimeTerm}.
	 */
	private TimeTerm solveTimeExpression(String time) {
		String[] deadlineTerms = time.split("`");
		NumberTerm t1 = parseTimeTerm(deadlineTerms[1]);

		for (int k = 2; k < deadlineTerms.length; k += 2) {
			ArithmeticOp op = parseArithmeticOp(deadlineTerms[k]);
			NumberTerm t2 = parseTimeTerm(deadlineTerms[k + 1]);
			t1 = new ArithExpr(t1, op, t2);
		}
		return (TimeTerm) t1;
	}

	/**
	 * Parse string to fail literal.
	 * 
	 * @param content
	 * @return fail literal content if {@code content} is a well-formed string
	 * @throws ParseException
	 */
	private Literal parseNormFailContent(String content) throws ParseException {
		String failureRegex = "fail\\s*\\(\\s*(.+)\\s*\\)";
		Pattern pattern = Pattern.compile(failureRegex);
		Matcher matcher = pattern.matcher(content);

		if (matcher.matches()) {
			String resonStr = matcher.group(1);
			Atom reason = new Atom(resonStr);
			return ASSyntax.createLiteral(NormativeProgram.FailFunctor, reason);
		}
		throw new ParseException();
	}

	/**
	 * Parse argument to corresponding {@link ArithmeticOp} value.
	 * 
	 * @param operation
	 *            either {@code "+"} or {@code "-"}
	 * @return corresponding {@link ArithmeticOp} value of {@code operation}, or
	 *         {@code null} if {@code operation} is an invalid sign
	 */
	private ArithmeticOp parseArithmeticOp(String operation) {
		switch (operation) {
		case "+":
			return ArithmeticOp.plus;
		case "-":
			return ArithmeticOp.minus;
		default:
			return null;
		}
	}

	/**
	 * Parse argument to {@link TimeTerm}.
	 * 
	 * @param time
	 * @return {@code time} as an instance of {@link TimeTerm}
	 */
	private TimeTerm parseTimeTerm(String time) {
		TimeTerm term = null;
		String[] timeTerms = time.split(" ");
		if (timeTerms.length == 1) {
			term = new TimeTerm(-1, timeTerms[0]);
		} else {
			term = new TimeTerm(Long.parseLong(timeTerms[0]), timeTerms[1]);
		}
		return term;
	}

	/**
	 * Add norm to norms set.
	 * 
	 * @param n
	 *            norm to be added
	 */
	private void addNorm(Norm n) {
		if (norms.put(n.getId(), n) == n) {
			links.put(n.getId(), new HashSet<String>());
		}
	}

	/**
	 * Add sanction to sanctions set.
	 * 
	 * @param s
	 *            sanction to be added
	 */
	private void addSanction(Sanction s) {
		sanctions.put(s.getId(), s);
	}

	/**
	 * Add link to links set.
	 * 
	 * @param n
	 *            the norm
	 * @param s
	 *            the sanction
	 */
	private void addLink(Norm n, Sanction s) {
		assert n != null;
		assert s != null;

		Set<String> linkedSanctions = links.get(n.getId());
		if (linkedSanctions.add(s.getId())) {
			links.put(n.getId(), linkedSanctions);
		}
	}

	/**
	 * Add link to links set.
	 * 
	 * @param normId
	 *            the norm id
	 * @param sanctionId
	 *            the sanction id
	 */
	private void addLink(String normId, String sanctionId) {
		Norm n = norms.get(normId);
		Sanction s = sanctions.get(sanctionId);
		addLink(n, s);
	}

	@Override
	public IDeJureBuilder setNorms(Map<String, Norm> norms) {
		this.norms = norms;
		return this;
	}

	@Override
	public IDeJureBuilder setSanctions(Map<String, Sanction> sanctions) {
		this.sanctions = sanctions;
		return this;
	}

	@Override
	public IDeJureBuilder setLinks(Map<String, Set<String>> links) {
		this.links = links;
		return this;
	}
}
