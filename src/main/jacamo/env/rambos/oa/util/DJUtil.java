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
package rambos.oa.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @author igorcadelima
 *
 */
public class DJUtil {
	protected static final String NS_SCHEMA_PATH = "/xsd/rambos-ns.xsd";

	/**
	 * Try to parse, validate (if indicated via argument), and return
	 * {@link Document}.
	 * 
	 * @param filePath
	 *            path to the normative specification file
	 * @param validate
	 *            flag to indicate whether document should be validated before
	 *            being returned
	 * @return Document object
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Document parseDocument(String filePath, boolean validate)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);

		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		File file = new File(filePath);
		Document doc = documentBuilder.parse(file);
		
		if (validate) {
			validateSpec(doc);
		}
		return doc;
	}

	/**
	 * Try to parse, validate, and return {@link Document}.
	 * 
	 * @param filePath
	 *            path to the normative specification file
	 * @return Document object
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Document parseDocument(String filePath)
			throws ParserConfigurationException, SAXException, IOException {
		return parseDocument(filePath, true);
	}

	/**
	 * Try to validate a XML file with the normative specification against
	 * schema.
	 * 
	 * @param node
	 *            {@link Node} instance containing normative specification
	 * @return true if XML file is valid
	 * @throws SAXException
	 * @throws IOException
	 */
	public static void validateSpec(Node node) throws SAXException, IOException {
		Source xmlSource = new DOMSource(node);
		Schema schema = getSchema();
		Validator validator = schema.newValidator();
		validator.validate(xmlSource);
	}

	/**
	 * Get schema used to validate normative specification.
	 * 
	 * @return schema used to validate normative specification
	 * @throws IOException
	 * @throws SAXException
	 */
	public static Schema getSchema() throws IOException, SAXException {
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		URL schemaResource = DJUtil.class.getResource(NS_SCHEMA_PATH);
		InputStream schemaStream = schemaResource.openStream();
		StreamSource schemaSource = new StreamSource(schemaStream);
		return schemaFactory.newSchema(schemaSource);
	}

}
