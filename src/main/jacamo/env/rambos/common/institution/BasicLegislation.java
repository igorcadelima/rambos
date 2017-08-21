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
package rambos.common.institution;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jason.asSyntax.Atom;
import rambos.common.norm.Norm;
import rambos.common.norm.Norms;
import rambos.common.sanction.Sanction;
import rambos.common.sanction.Sanctions;

final class BasicLegislation implements Legislation {
  // normId -> norm
  private Map<Atom, Norm> norms = new ConcurrentHashMap<>();
  // sanctionId -> sanction
  private Map<Atom, Sanction> sanctions = new ConcurrentHashMap<>();

  @Override
  public Norm getNorm(Atom id) {
    if (norms.containsKey(id)) {
      return Norms.newInstance(norms.get(id));
    }
    return null;
  }

  @Override
  public Sanction getSanction(Atom id) {
    if (sanctions.containsKey(id)) {
      return Sanctions.newInstance(sanctions.get(id));
    }
    return null;
  }

  @Override
  public Set<Norm> getNorms() {
    return new HashSet<>(norms.values());
  }

  @Override
  public Set<Sanction> getSanctions() {
    return new HashSet<>(sanctions.values());
  }

  @Override
  public boolean addNorm(Norm norm) {
    if (!norms.containsKey(norm.getId())) {
      Atom normId = norm.getId();
      norms.put(normId, Norms.newInstance(norm));
      return true;
    }
    return false;
  }

  @Override
  public boolean addSanction(Sanction sanction) {
    if (!sanctions.containsKey(sanction.getId())) {
      sanctions.put(sanction.getId(), Sanctions.newInstance(sanction));
      return true;
    }
    return false;
  }

  @Override
  public boolean addLink(Atom normId, Atom sanctionId) {
    Sanction sanction = sanctions.get(sanctionId);
    return norms.get(normId)
                .link(sanction);
  }

  @Override
  public boolean enableNorm(Atom normId) {
    if (norms.containsKey(normId)) {
      return norms.get(normId)
                  .enable();
    }
    return false;
  }

  @Override
  public boolean enableSanction(Atom sanctionId) {
    if (sanctions.containsKey(sanctionId)) {
      return sanctions.get(sanctionId)
                      .enable();
    }
    return false;
  }

  @Override
  public boolean disableNorm(Atom normId) {
    if (norms.containsKey(normId)) {
      return norms.get(normId)
                  .disable();
    }
    return false;
  }

  @Override
  public boolean disableSanction(Atom sanctionId) {
    if (sanctions.containsKey(sanctionId)) {
      return sanctions.get(sanctionId)
                      .disable();
    }
    return false;
  }

  @Override
  public Norm removeNorm(Atom normId) {
    return norms.remove(normId);
  }

  @Override
  public Sanction removeSanction(Atom sanctionId) {
    norms.values()
         .forEach(norm -> norm.unlink(sanctionId));
    return sanctions.remove(sanctionId);
  }

  @Override
  public boolean unlink(Atom normId, Atom sanctionId) {
    return norms.get(normId)
                .unlink(sanctionId);
  }
}
