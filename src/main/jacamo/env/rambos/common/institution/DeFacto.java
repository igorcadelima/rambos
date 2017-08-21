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
import java.util.Set;

import cartago.Artifact;
import cartago.LINK;
import cartago.OPERATION;
import cartago.ObsProperty;
import jason.asSyntax.ASSyntax;
import rambos.common.registry.Registries;
import rambos.common.registry.Registry;
import rambos.common.registry.Registry.Efficacy;
import rambos.common.util.Enums;

/**
 * @author igorcadelima
 *
 */
public final class DeFacto extends Artifact {
  private Set<Registry> registries = new HashSet<>();

  /**
   * Add new registry into the registries set.
   * 
   * Only instances of {@link Registry} or {@link String} should be passed as argument. For both
   * cases, the registry will be added using {@link #addRegistry(Registry)}. If {@code f} is an
   * instance of {@link String}, then it will be parsed using {@link Registries#parse(String)}
   * before being added to the registries set.
   * 
   * @param registry registry to be added
   */
  @LINK
  @OPERATION
  public <T> void addRegistry(T registry) {
    if (registry instanceof Registry)
      addRegistry((Registry) registry);
    else if (registry instanceof String)
      addRegistry(Registries.parse((String) registry));
    else
      failed("Expected " + String.class.getCanonicalName() + " or "
          + Registry.class.getCanonicalName() + " but got " + registry.getClass()
                                                                      .getCanonicalName());
  }

  /** Add new {@code registry} into the registries set. */
  private void addRegistry(Registry registry) {
    registries.add(registry);
    defineObsProperty(registry.getFunctor(), (Object[]) registry.toLiteral()
                                                                .getTermsArray());
  }

  /**
   * Update the efficacy value of an existing registry.
   * 
   * @param registry string with literal format of the registry to be updated
   * @param efficacy new efficacy value
   */
  @LINK
  @OPERATION
  public void updateEfficacy(String registry, String efficacy) {
    Efficacy efficacyObj = Enums.lookup(Efficacy.class, efficacy);
    if (efficacyObj == null) {
      failed(efficacy + " is not a valid efficacy value");
    }

    Registry registryObj = Registries.parse(registry);
    ObsProperty prop =
        getObsPropertyByTemplate(registryObj.getFunctor(), (Object[]) registryObj.toLiteral()
                                                                                 .getTermsArray());
    registries.remove(registryObj);
    registryObj.setEfficacy(efficacyObj);
    registries.add(registryObj);
    prop.updateValue(6, ASSyntax.createAtom(efficacy));
  }

  /** Remove a registry from the registries set. */
  @LINK
  @OPERATION
  public void removeRegistry(Registry registry) {
    registries.remove(registry);
    removeObsPropertyByTemplate(registry.getFunctor(), (Object[]) registry.toLiteral()
                                                                          .getTermsArray());
  }
}
