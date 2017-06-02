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
package rambos.ora4mas.db;

import java.util.Map;
import java.util.Set;

import cartago.ArtifactId;
import cartago.OpFeedbackParam;
import cartago.OperationException;
import rambos.Norm;
import rambos.Sanction;

/**
 * @author igorcadelima
 *
 */
public interface IDeJureBuilder {

	/**
	 * @param name
	 *            name of the repository artefact
	 * @param out
	 *            output parameter
	 * @throws OperationException
	 */
	void build(String name, OpFeedbackParam<ArtifactId> out) throws OperationException;

	/**
	 * Set norms.
	 * 
	 * @param norms
	 *            mapping of norms ids to norms themselves
	 * @return builder after setting norms
	 */
	IDeJureBuilder setNorms(Map<String, Norm> norms);

	/**
	 * Set sanctions.
	 * 
	 * @param sanctions
	 *            mapping of sanctions ids to sanctions themselves
	 * @return builder after setting sanctions
	 */
	IDeJureBuilder setSanctions(Map<String, Sanction> sanctions);

	/**
	 * Set links between norms and sanctions.
	 * 
	 * @param links
	 *            mapping of norms ids to a set of sanctions ids
	 * @return builder after setting links
	 */
	IDeJureBuilder setLinks(Map<String, Set<String>> links);
}
