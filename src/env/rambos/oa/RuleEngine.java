/**
 * 
 */
package rambos.oa;

import cartago.AbstractWSPRuleEngine;
import cartago.ArtifactId;
import cartago.ArtifactObsProperty;

/**
 * @author igorcadelima
 *
 */
public class RuleEngine extends AbstractWSPRuleEngine {

	@Override
	protected void processObsPropertyAdded(ArtifactId artifactId, ArtifactObsProperty[] properties) {
		for (ArtifactObsProperty op : properties) {
		}
	}
}
