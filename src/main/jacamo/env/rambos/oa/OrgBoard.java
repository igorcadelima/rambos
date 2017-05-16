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
package rambos.oa;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import cartago.*;
import jason.asSyntax.Atom;
import moise.common.MoiseException;
import moise.os.ns.NS;
import npl.parser.ParseException;
import rambos.mechanism.rep.DeJure;
import rambos.oa.util.DJUtil;
import rambos.os.OS;

public class OrgBoard extends ora4mas.nopl.OrgBoard {
	String osFile = null;
	protected ArtifactId deJure;
	protected OS os;

	Map<String, ArtifactId> aids = new HashMap<String, ArtifactId>();
	protected Logger logger = Logger.getLogger(OrgBoard.class.getName());

	/**
	 * Initialises the organisational board
	 * 
	 * @param osFile
	 *            the organisation specification file (path and file name)
	 *
	 * @throws ParseException
	 *             if the OS file is not correct
	 * @throws MoiseException
	 *             if grType was not specified
	 * @throws OperationException
	 *             if parentGroupId doesn't exit
	 */
	public void init(final String osFile) {
		this.osFile = osFile;
		
		try {
			Document doc = DJUtil.parseDocument(osFile);
			Node nsNode = doc.getElementsByTagName(NS.getXMLTag()).item(0);
			doc.getDocumentElement().removeChild(nsNode);
			
			Document nsDoc = DJUtil.nodeToDocument(nsNode);
			execInternalOp("createDeJure", nsDoc);
			
			createOS(doc);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Create De Jure repository by extracting data from a {@link Document}
	 * containing the normative specification.
	 * 
	 * @param ns normative specification
	 * @throws OperationException
	 */
	@INTERNAL_OPERATION
	public void createDeJure(Document ns) throws OperationException {
		String DJName = getId().getName() + ".DeJure";
		deJure = makeArtifact(DJName, DeJure.class.getName(), new ArtifactConfig(ns));
	}
	
	/**
	 * Create {@link OS} instance which will be shared with every
	 * {@link GroupBoard}, {@link SchemeBoard}, and {@link NormativeBoard} that
	 * belongs with this {@link OrgBoard} instance.
	 * 
	 * @param osDoc organisational specification
	 */
	protected void createOS(Document osDoc) {
		String mechanismOSFile = "/org/org.xml";
		InputStream mechanismOSResource = getClass().getResourceAsStream(mechanismOSFile);
		os = OS.create(mechanismOSResource);
		os.extend(osDoc);
	}

	@OPERATION
	public void createGroup(String id, String type, OpFeedbackParam<ArtifactId> gaid) throws OperationException {
		ArtifactId aid = makeArtifact(id, GroupBoard.class.getName(), new ArtifactConfig(osFile, type));
		aids.put(id, aid);
		defineObsProperty("group", new Atom(id), new Atom(type), aid);
		gaid.set(aid);
	}

	@OPERATION
	public void removeGroup(String id) {
		try {
			ArtifactId aid = aids.remove(id);
			if (aid == null) {
				failed("there is no group board for " + id);
				return;
			}
			removeObsPropertyByTemplate("group", new Atom(id), null, null);

			execLinkedOp(aid, "destroy");
			dispose(aid);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@OPERATION
	public void createScheme(String id, String type, OpFeedbackParam<ArtifactId> said) throws OperationException {
		ArtifactId aid = makeArtifact(id, SchemeBoard.class.getName(), new ArtifactConfig(osFile, type));
		aids.put(id, aid);
		defineObsProperty("scheme", new Atom(id), new Atom(type), aid);
		said.set(aid);
	}

	@OPERATION
	public void removeScheme(String id) {
		try {
			ArtifactId aid = aids.remove(id);
			if (aid == null) {
				failed("there is no scheme board for " + id);
				return;
			}
			removeObsPropertyByTemplate("scheme", new Atom(id), null, null);

			execLinkedOp(aid, "destroy");
			// dispose(aid); // TODO: does not work! (test with auction example)
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
