/**
 * 
 */
package rambos.mechanism;

import jason.asSyntax.LogicalFormula;

/**
 * @author igorcadelima
 *
 */
public class Sanction {
	protected String id;
	protected Status status;
	protected LogicalFormula conditions;
	protected SanctionCategory category;
	
	/**
	 * @param id
	 * @param status
	 * @param conditions
	 * @param category
	 */
	public Sanction(String id, Status status, LogicalFormula conditions, SanctionCategory category) {
		this.id = id;
		this.status = status;
		this.conditions = conditions;
		this.category = category;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
}
