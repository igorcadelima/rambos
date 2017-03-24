/**
 * 
 */
package rambos.os;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import moise.common.MoiseException;
import moise.os.fs.FS;
import moise.os.ns.NS;
import moise.os.ss.SS;
import moise.xml.DOMUtils;

/**
 * @author igorcadelima
 *
 */
public class OS extends moise.os.OS {

	private static final long serialVersionUID = 1L;

	/**
	 * Create an {@link OS} instance from an {@link InputStream} source.
	 * 
	 * @param is
	 *            {@link InputStream} instance which contains the organisational
	 *            specification
	 * @return new {@link OS} instance or {@code null} if an error occurs
	 */
	public static OS create(InputStream is) {
		try {
			Document doc = DOMUtils.getParser().parse(is);
			DOMUtils.getOSSchemaValidator().validate(new DOMSource(doc));

			OS os = new OS();
			os.setFromDOM(doc);
			return os;
		} catch (IOException | MoiseException | SAXException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Extend specification by adding all of the data contained in the passed
	 * organisational specification file. In other words, this method will
	 * incorporate the given specification into the current one as if they were
	 * originally one.
	 * 
	 * @param osFilePath
	 *            path to organisational specification file
	 */
	public void extend(String osFilePath) {
		try {
			Document doc = DOMUtils.getParser().parse(osFilePath);
			DOMUtils.getOSSchemaValidator().validate(new DOMSource(doc));

			Element osElement = (Element) doc.getElementsByTagName(OS.getXMLTag()).item(0);
			this.setId(osElement.getAttribute("id"));
			this.setPropertiesFromDOM(osElement);

			Element specElement;

			// Extend structural specification
			specElement = DOMUtils.getDOMDirectChild(osElement, SS.getXMLTag());
			ss.setFromDOM(specElement);

			// Extend functional specification
			specElement = DOMUtils.getDOMDirectChild(osElement, FS.getXMLTag());
			fs.setFromDOM(specElement);

			// Extend normative specification
			specElement = DOMUtils.getDOMDirectChild(osElement, NS.getXMLTag());
			if (specElement != null) {
				ns.setFromDOM(specElement);
			}

		} catch (SAXException | IOException | ParserConfigurationException | MoiseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
