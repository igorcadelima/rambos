/**
 * 
 */
package rambos.mechanism;

/**
 * @author igorcadelima
 *
 */
public class SanctionCategory {
	protected SanctionPurpose purpose;
	protected SanctionIssuer issuer;
	protected SanctionLocus locus;
	protected SanctionMode mode;
	protected SanctionPolarity polarity;
	protected SanctionDiscernability discernability;
	
	/**
	 * @param purpose
	 * @param issuer
	 * @param locus
	 * @param mode
	 * @param polarity
	 * @param discernability
	 */
	public SanctionCategory(SanctionPurpose purpose, SanctionIssuer issuer, SanctionLocus locus, SanctionMode mode,
			SanctionPolarity polarity, SanctionDiscernability discernability) {
		super();
		this.purpose = purpose;
		this.issuer = issuer;
		this.locus = locus;
		this.mode = mode;
		this.polarity = polarity;
		this.discernability = discernability;
	}

	/**
	 * @return the purpose
	 */
	public SanctionPurpose getPurpose() {
		return purpose;
	}

	/**
	 * @param purpose the purpose to set
	 */
	public void setPurpose(SanctionPurpose purpose) {
		this.purpose = purpose;
	}

	/**
	 * @return the issuer
	 */
	public SanctionIssuer getIssuer() {
		return issuer;
	}

	/**
	 * @param issuer the issuer to set
	 */
	public void setIssuer(SanctionIssuer issuer) {
		this.issuer = issuer;
	}

	/**
	 * @return the locus
	 */
	public SanctionLocus getLocus() {
		return locus;
	}

	/**
	 * @param locus the locus to set
	 */
	public void setLocus(SanctionLocus locus) {
		this.locus = locus;
	}

	/**
	 * @return the mode
	 */
	public SanctionMode getMode() {
		return mode;
	}

	/**
	 * @param mode the mode to set
	 */
	public void setMode(SanctionMode mode) {
		this.mode = mode;
	}

	/**
	 * @return the polarity
	 */
	public SanctionPolarity getPolarity() {
		return polarity;
	}

	/**
	 * @param polarity the polarity to set
	 */
	public void setPolarity(SanctionPolarity polarity) {
		this.polarity = polarity;
	}

	/**
	 * @return the discernability
	 */
	public SanctionDiscernability getDiscernability() {
		return discernability;
	}

	/**
	 * @param discernability the discernability to set
	 */
	public void setDiscernability(SanctionDiscernability discernability) {
		this.discernability = discernability;
	}
	
}
