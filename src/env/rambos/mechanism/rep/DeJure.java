/**
 * 
 */
package rambos.mechanism.rep;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cartago.Artifact;
import cartago.LINK;
import cartago.OPERATION;
import cartago.OpFeedbackParam;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.ArithExpr;
import jason.asSyntax.ArithExpr.ArithmeticOp;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.VarTerm;
import jason.asSyntax.parser.ParseException;
import npl.DynamicFactsProvider;
import npl.LiteralFactory;
import npl.NPLLiteral;
import npl.NormativeProgram;
import npl.Scope;
import npl.TimeTerm;
import rambos.mechanism.Norm;
import rambos.mechanism.Sanction;
import rambos.mechanism.SanctionCategory;
import rambos.mechanism.SanctionDiscernability;
import rambos.mechanism.SanctionIssuer;
import rambos.mechanism.SanctionLocus;
import rambos.mechanism.SanctionMode;
import rambos.mechanism.SanctionPolarity;
import rambos.mechanism.SanctionPurpose;
import rambos.mechanism.Status;
import rambos.oa.util.DJUtil;

/**
 * @author igorcadelima
 *
 */
public class DeJure extends Artifact {
	protected static final String NORMS_TAG = "norms";
	protected static final String NORM_TAG = "norm";
	protected static final String SANCTIONS_TAG = "sanctions";
	protected static final String SANCTION_TAG = "sanction";
	protected static final String LINKS_TAG = "links";
	protected static final String LINK_TAG = "link";
	
	// normId -> norm
	private Map<String, Norm> norms;
	// sanctionId -> sanction
	private Map<String, Sanction> sanctions;
	// normId -> [sanctionId0, sanctionId1, ..., sanctionIdn]
	private Map<String, Set<String>> links;
	
	private DynamicFactsProvider dfp;

	public void init(String djSpecFile, DynamicFactsProvider dfp) {
		this.dfp = dfp;
		
		norms = new ConcurrentHashMap<String, Norm>();
		sanctions = new ConcurrentHashMap<String, Sanction>();
		links = new ConcurrentHashMap<String, Set<String>>();
		
		Document doc = null;
		try {
			doc = DJUtil.parseSpec(djSpecFile);
			DJUtil.validateSpec(new DOMSource(doc));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		extractSpecData(doc);
	}

	/**
	 * @param doc
	 */
	protected void extractSpecData(Document doc) {
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
	 * @param doc
	 * @throws ParseException 
	 * @throws DOMException 
	 * @throws npl.parser.ParseException 
	 */
	protected void extractNorms(Document doc) throws DOMException, ParseException, npl.parser.ParseException {
		Node normsRootEl = doc.getElementsByTagName(NORMS_TAG).item(0);
		NodeList normNodes = normsRootEl.getChildNodes();
		
		for (int i = 0; i < normNodes.getLength(); i++) {
			Node normNode = normNodes.item(i);
			
			if (normNode.getNodeType() == Node.ELEMENT_NODE) {
				Element normEl = (Element) normNode;
				
				String id = normEl.getAttribute("id");
				Status status = Status.ACTIVE;
				LogicalFormula conditions = null;
				String issuer = null;
				Literal content = null;
				
				NodeList normPropsList = normEl.getChildNodes();
				for (int j = 0; j < normPropsList.getLength(); j++) {
					Node prop = normPropsList.item(j);
					if (prop.getNodeName().equals("status")) {
						status = Status.valueOf(prop.getTextContent().toUpperCase());
					} else if (prop.getNodeName().equals("conditions")) {
						conditions = ASSyntax.parseFormula(prop.getTextContent());
					} else if (prop.getNodeName().equals("issuer")) {
						issuer = prop.getTextContent();
					} else if (prop.getNodeName().equals("content")) {
						//`(never|now|\d+\s+(?:millisecond|second|minute|hour|day|year)s?)`(?:\s*(\+|\-)\s*`(never|now|\d+\s+(?:millisecond|second|minute|hour|day|year)s?)`)*
						LiteralFactory literalFactory = NPLLiteral.getFactory();
						Literal literal = null;
						
						String obligationRegex = "obligation\\s*\\(\\s*(\\w+)\\s*,\\s*(\\w+)\\s*,\\s*(.+?)\\s*,\\s*(.+?)\\s*\\)";
//						String contentRegex = String.format("%s|%s", obligationRegex, failureRegex);
						
						Pattern pattern = Pattern.compile(obligationRegex);
						Matcher matcher = pattern.matcher(prop.getTextContent());
						
						// if obligation
						if (matcher.matches()) {
							String agent = matcher.group(1);
							String reason = matcher.group(2);
							String goal = matcher.group(3);
							String deadline = matcher.group(4);
							
							literal = ASSyntax.createLiteral(NormativeProgram.OblFunctor);
							
							// Interpret agent argument
							char agentInitial = agent.charAt(0); 
							if (Character.isUpperCase(agentInitial)) {
								literal.addTerm(new VarTerm(agent));
							} else {
								literal.addTerm(new Atom(agent));
							}
							
							// Interpret reason argument
							if (reason.equals(id)) {
								literal.addTerm(conditions);
							} else {
								literal.addTerm(new Atom(id));
							}
							
							// Interpret goal argument
							literal.addTerm(new Atom(ASSyntax.createLiteral(goal)));
							
							// Interpret deadline argument
							String[] deadlineTerms = deadline.split("`");
							NumberTerm t1 = parseTimeTerm(deadlineTerms[1]);
							for (int k = 2; k < deadlineTerms.length; k+=2) {
								ArithmeticOp op = parseArithmeticOp(deadlineTerms[k]);
								NumberTerm t2 = parseTimeTerm(deadlineTerms[k+1]);
								t1 = new ArithExpr(t1, op, t2);
							}
							literal.addTerm((TimeTerm)t1);
							
							// Add annotations
							literal.addAnnot(ASSyntax.createStructure("norm", new Atom(id)));
							literal.addAnnot(ASSyntax.createStructure("issuer", new Atom(issuer)));
						}  else { // failure
							String failureRegex = "fail\\s*\\(\\s*(.+)\\s*\\)";
							pattern = Pattern.compile(failureRegex);
							matcher = pattern.matcher(prop.getTextContent());
							matcher.matches();
							
							String resonStr = matcher.group(1);
							
							Atom reason = new Atom(resonStr);
							literal = ASSyntax.createLiteral(NormativeProgram.FailFunctor, reason);
						}
						content = literalFactory.createNPLLiteral(literal, dfp);
					}
				}
				
				addNorm(new Norm(id, status, conditions, issuer, content));
			}
		}
	}
	
	private ArithmeticOp parseArithmeticOp(String op) {
		switch (op) {
		case "+":
			return ArithmeticOp.plus;
		default:
			return ArithmeticOp.minus;
		}
	}
	
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
	 * @param doc
	 * @throws ParseException 
	 * @throws DOMException 
	 */
	protected void extractSanctions(Document doc) throws DOMException, ParseException {
		Node sanctionsRootEl = doc.getElementsByTagName(SANCTIONS_TAG).item(0);
		NodeList sanctionNodes = sanctionsRootEl.getChildNodes();
		
		for (int i = 0; i < sanctionNodes.getLength(); i++) {
			Node sanctionNode = sanctionNodes.item(i);
			
			if (sanctionNode.getNodeType() == Node.ELEMENT_NODE) {
				Element sanctionEl = (Element) sanctionNode;
				
				String id = sanctionEl.getAttribute("id");
				Status status = Status.ACTIVE;
				LogicalFormula conditions = null;
				
				SanctionCategory category = null;
				SanctionPurpose purpose = null;
				SanctionIssuer issuer = null;
				SanctionLocus locus = null;
				SanctionMode mode = null;
				SanctionPolarity polarity = null;
				SanctionDiscernability discernability = null;
				
				NodeList sanctionProperties = sanctionEl.getChildNodes();
				for (int j = 0; j < sanctionProperties.getLength(); j++) {
					Node propertyNode = sanctionProperties.item(j);
					
					if (propertyNode.getNodeType() == Node.ELEMENT_NODE) {
						if (propertyNode.getNodeName().equals("status")) {
							status = Status.valueOf(propertyNode.getTextContent().toUpperCase());
						} else if (propertyNode.getNodeName().equals("conditions")) {
							conditions = ASSyntax.parseFormula(propertyNode.getTextContent());
						} else if (propertyNode.getNodeName().equals("category")) {
							NodeList dimensions = propertyNode.getChildNodes();
							
							for (int k = 0; k < dimensions.getLength(); k++) {
								Node dimension = dimensions.item(k);
								String dimensionContent = dimension.getTextContent().toUpperCase();
								
								if (dimension.getNodeName().equals("purpose")) {
									purpose = SanctionPurpose.valueOf(dimensionContent);
								} else if (dimension.getNodeName().equals("issuer")) {
									issuer = SanctionIssuer.valueOf(dimensionContent);
								} else if (dimension.getNodeName().equals("locus")) {
									locus = SanctionLocus.valueOf(dimensionContent);
								} else if (dimension.getNodeName().equals("mode")) {
									mode = SanctionMode.valueOf(dimensionContent);
								} else if (dimension.getNodeName().equals("polarity")) {
									polarity = SanctionPolarity.valueOf(dimensionContent);
								} else if (dimension.getNodeName().equals("discernability")) {
									discernability = SanctionDiscernability.valueOf(dimensionContent);
								}
							}
							category = new SanctionCategory(purpose, issuer, locus, mode, polarity, discernability);
						}
					}
				}
				addSanction(new Sanction(id, status, conditions, category));
			}
		}
	}
	
	/**
	 * @param doc
	 */
	protected void extractLinks(Document doc) {
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
	 * @param n
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
	 * @param s
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
	 * @param normId The norm id
	 * @param sanctionId The sanction id
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
	 * @param n The norm
	 * @param s The sanction
	 * @return true if link is created successfully
	 */
	@LINK
	@OPERATION
	public synchronized boolean addLink(Norm n, Sanction s) {
		// TODO: check whether operator agent is a legislator
		if (n == null | s == null) return false;

		Set<String> linkedSanctions = links.get(n.getId());
		if (linkedSanctions.add(s.getId())) {
			links.put(n.getId(), linkedSanctions);
			return true;
		}
		
		return false;
	}
	
	/**
	 * @param normId The norm id
	 * @param sanctionId The sanction id
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
	 * @param n The norm
	 * @param s The sanction
	 * @return true if link is destroyed successfully
	 */
	@LINK
	@OPERATION
	public synchronized boolean removeLink(Norm n, Sanction s) {
		// TODO: check whether operator agent is a legislator
		if (n == null | s == null) return false;

		Set<String> linkedSanctions = links.get(n.getId());
		if (linkedSanctions.remove(s.getId())) {
			links.put(n.getId(), linkedSanctions);
			return true;
		}
		
		return false;
	}

	/**
	 * @return copy of the norms
	 */
	@LINK
	@OPERATION
	public Map<String, Norm> getNorms() {
		return new ConcurrentHashMap<String, Norm>(norms);
	}

	/**
	 * @return copy of the sanctions
	 */
	@LINK
	@OPERATION
	public Map<String, Sanction> getSanctions() {
		return new ConcurrentHashMap<String, Sanction>(sanctions);
	}

	/**
	 * @return copy of the links
	 */
	@LINK
	@OPERATION
	public Map<String, Set<String>> getLinks() {
		return new ConcurrentHashMap<String, Set<String>>(links);
	}
	
	/**
	 * @param dj the DeJure repository
	 * @return a NPL Scope extracted from the repository's properties
	 */
	@LINK
	@OPERATION
	public Scope createNPLScope(OpFeedbackParam<Scope> outScope) {
		try {
			Literal id = ASSyntax.parseLiteral("np");
			Scope scope = new Scope(id, null);
			for (Norm n : getNorms().values()) {
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
	
}
