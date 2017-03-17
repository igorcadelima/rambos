/**
 * 
 */
package rambos.oa;

import jason.asSyntax.Structure;
import npl.DeonticModality;
import rambos.oa.util.Parser;

/**
 * @author igorcadelima
 *
 */
public interface NormativeListener extends npl.NormativeListener {
	@Override
	default void created (DeonticModality obligation) {
	}
	
	@Override
	default void fulfilled (DeonticModality obligation) {
		
	}
	
	@Override
	default void unfulfilled (DeonticModality obligation) {
		
	}
	
	@Override
	default void inactive (DeonticModality obligation) {
		
	}
	
	@Override
	default void failure (Structure f) {
		
	}
	
}
