package rambos.oa;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;

import cartago.*;
import jason.asSyntax.Atom;
import jason.util.Config;
import moise.common.MoiseException;
import moise.os.OS;
import moise.xml.DOMUtils;
import moise.xml.ToXML;
import npl.parser.ParseException;
//import ora4mas.nopl.GroupBoard;
//import ora4mas.nopl.SchemeBoard;
import ora4mas.nopl.WebInterface;

public class OrgBoard extends ora4mas.nopl.OrgBoard {
	String osFile = null;

	Map<String, ArtifactId> aids = new HashMap<String, ArtifactId>();
	protected Logger logger = Logger.getLogger(OrgBoard.class.getName());

	/**
	 * Initialises the org board
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
	public void init(final String osFile) throws ParseException, MoiseException, OperationException {
//		super.init(osFile);
		this.osFile = osFile;
		
//		TODO: WebInterface is package private
//		OS os = OS.loadOSFromURI(osFile);
//
//		if (!"false".equals(Config.get().getProperty(Config.START_WEB_OI))) {
//			WebInterface w = WebInterface.get();
//			try {
//				String osSpec = specToStr(os, DOMUtils.getTransformerFactory().newTransformer(DOMUtils.getXSL("os")));
//				String oeId = getCreatorId().getWorkspaceId().getName();
//
//				w.registerOSBrowserView(oeId, os.getId(), osSpec);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
	}

//	public String specToStr(ToXML spec, Transformer transformer) throws Exception {
//		StringWriter so = new StringWriter();
//		InputSource si = new InputSource(new StringReader(DOMUtils.dom2txt(spec)));
//		transformer.transform(new DOMSource(getParser().parse(si)), new StreamResult(so));
//		return so.toString();
//	}

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
