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
