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

public class Norm extends npl.Norm{
	protected Status status;
	protected String issuer;

	/**
	 * @param id
	 * @param status
	 * @param conditions
	 * @param issuer
	 * @param content
	 */
	public Norm(String id, Status status, LogicalFormula conditions, String issuer, Literal content) {
		super(id, content, conditions);
		this.status = status;
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
			
			// Add parsed Literal to the BB in order to check the norm condition against it
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

