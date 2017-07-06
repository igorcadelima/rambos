/*******************************************************************************
 * MIT License
 *
 * Copyright (c) Igor Conrado Alves de Lima <igorcadelima@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package rambos;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;

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
   * SanctionCategory constructor.
   * 
   * @param builder builder from which data should be obtained
   */
  public SanctionCategory(SanctionCategoryBuilder builder) {
    this.purpose = builder.purpose;
    this.issuer = builder.issuer;
    this.locus = builder.locus;
    this.mode = builder.mode;
    this.polarity = builder.polarity;
    this.discernability = builder.discernability;
  }

  /**
   * @return the purpose
   */
  public SanctionPurpose getPurpose() {
    return purpose;
  }


  /**
   * @return the issuer
   */
  public SanctionIssuer getIssuer() {
    return issuer;
  }

  /**
   * @return the locus
   */
  public SanctionLocus getLocus() {
    return locus;
  }

  /**
   * @return the mode
   */
  public SanctionMode getMode() {
    return mode;
  }

  /**
   * @return the polarity
   */
  public SanctionPolarity getPolarity() {
    return polarity;
  }

  /**
   * @return the discernability
   */
  public SanctionDiscernability getDiscernability() {
    return discernability;
  }

  @Override
  public String toString() {
    Literal l = ASSyntax.createLiteral("category");
    l.addTerm(ASSyntax.createAtom(discernability.lowercase()));
    l.addTerm(ASSyntax.createAtom(issuer.lowercase()));
    l.addTerm(ASSyntax.createAtom(locus.lowercase()));
    l.addTerm(ASSyntax.createAtom(mode.lowercase()));
    l.addTerm(ASSyntax.createAtom(polarity.lowercase()));
    l.addTerm(ASSyntax.createAtom(purpose.lowercase()));
    return l.toString();
  }

  public static final class SanctionCategoryBuilder implements rambos.Builder<SanctionCategory> {
    private SanctionPurpose purpose;
    private SanctionIssuer issuer;
    private SanctionLocus locus;
    private SanctionMode mode;
    private SanctionPolarity polarity;
    private SanctionDiscernability discernability;

    /**
     * @param purpose sanction purpose
     */
    public SanctionCategoryBuilder setPurpose(SanctionPurpose purpose) {
      this.purpose = purpose;
      return this;
    }

    /**
     * @param issuer sanction issuer
     */
    public SanctionCategoryBuilder setIssuer(SanctionIssuer issuer) {
      this.issuer = issuer;
      return this;
    }

    /**
     * @param locus sanction locus
     */
    public SanctionCategoryBuilder setLocus(SanctionLocus locus) {
      this.locus = locus;
      return this;
    }

    /**
     * @param mode sanction mode
     */
    public SanctionCategoryBuilder setMode(SanctionMode mode) {
      this.mode = mode;
      return this;
    }

    /**
     * @param polarity sanction polarity
     */
    public SanctionCategoryBuilder setPolarity(SanctionPolarity polarity) {
      this.polarity = polarity;
      return this;
    }

    /**
     * @param discernability sanction discernability
     */
    public SanctionCategoryBuilder setDiscernability(SanctionDiscernability discernability) {
      this.discernability = discernability;
      return this;
    }

    @Override
    public SanctionCategory build() {
      return new SanctionCategory(this);
    }
  }
}
