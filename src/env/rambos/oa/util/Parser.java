/**
 * 
 */
package rambos.oa.util;

import cartago.ArtifactId;
import cartago.ArtifactObsProperty;

/**
 * @author igorcadelima
 *
 */
public class Parser {

	public static String toLiteralStr(ArtifactId artifactId, ArtifactObsProperty property) {
		StringBuilder literalBuilder = new StringBuilder();
		literalBuilder.append(property.getName().replace("$", "S").replace("@", "a"));
		literalBuilder.append("(" + artifactId.toString());
		for (Object value : property.getValues()) {
			literalBuilder.append("," + value.toString().replace("$", "S").replace("@", "_at_"));
		}
		literalBuilder.append(")");
		return literalBuilder.toString();
	}
}
