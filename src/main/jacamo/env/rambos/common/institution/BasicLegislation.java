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

import java.util.HashMap;
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
  private Map<Atom, Norm> norms = new ConcurrentHashMap<Atom, Norm>();
  // sanctionId -> sanction
  private Map<Atom, Sanction> sanctions = new ConcurrentHashMap<Atom, Sanction>();
  // normId -> [sanctionId0, sanctionId1, ..., sanctionIdn]
  private Map<Atom, Set<Atom>> links = new ConcurrentHashMap<>();

  @Override
  public Set<Norm> getNorms() {
    return new HashSet<>(norms.values());
  }

  @Override
  public Set<Sanction> getSanctions() {
    return new HashSet<>(sanctions.values());
  }

  @Override
  public Map<Atom, Set<Atom>> getLinks() {
    return new HashMap<>(links);
  }

  @Override
  public boolean addNorm(Norm norm) {
    if (!norms.containsKey(norm.getId())) {
      norms.put(norm.getId(), Norms.newInstance(norm));
      links.put(norm.getId(), new HashSet<>());
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
    Set<Atom> sanctions = links.get(normId);
    if (sanctions != null) {
      return sanctions.add(sanctionId);
    }
    return false;
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
    return sanctions.remove(sanctionId);
  }

  @Override
  public boolean removeLink(Atom normId, Atom sanctionId) {
    Set<Atom> sanctions = links.get(normId);
    if (sanctions != null) {
      return sanctions.remove(sanctionId) == true;
    }
    return false;
  }
}
