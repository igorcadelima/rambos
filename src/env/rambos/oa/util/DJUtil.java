/**
 * 
 */
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
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author igorcadelima
 *
 */
public class DJUtil {
	protected static final String DJ_SPEC_SCHEMA_URI = "/resources/xsd/dejure-specification.xsd";
	
	/**
	 * @param djSpec the URI to the DeJure specification file
	 * @return Document object
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Document parseSpec(String djSpec) throws ParserConfigurationException, SAXException, IOException {
		File file = new File(djSpec);
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
		        .newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		return documentBuilder.parse(file);
	}
	
	/**
	 * @param djSpec the DeJure specification Source
	 * @return true if XML file is valid
	 * @throws SAXException 
	 * @throws IOException 
	 */
	public static void validateSpec(Source djSpec) throws SAXException, IOException {
		SchemaFactory schemaFactory = SchemaFactory
		    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		
		URL schemaResource = DJUtil.class.getResource(DJ_SPEC_SCHEMA_URI);
		if (schemaResource == null) {
			System.out.println("Null");
		}
		InputStream schemaStream = schemaResource.openStream();
		StreamSource schemaSource = new StreamSource(schemaStream);
		
		Schema schema = schemaFactory.newSchema(schemaSource);
		Validator validator = schema.newValidator();
		validator.validate(djSpec);
	}
	
}
