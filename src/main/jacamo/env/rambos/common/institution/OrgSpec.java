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

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

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
final class OrgSpec extends moise.os.OS {
  private static final long serialVersionUID = 1L;

  /**
   * Create an {@link OrgSpec} instance from an {@link InputStream} source.
   * 
   * @param is {@link InputStream} instance which contains the organisational specification
   * @return new {@link OrgSpec} instance or {@code null} if an error occurs
   */
  static OrgSpec create(InputStream is) {
    try {
      Document doc = DOMUtils.getParser()
                             .parse(is);
      NormSpecUtil.validate(doc, NormSpecUtil.OS_SCHEMA_PATH);

      OrgSpec os = new OrgSpec();
      os.setFromDOM(doc);
      return os;
    } catch (IOException | MoiseException | SAXException | ParserConfigurationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Extend specification by adding all of the data contained in the passed organisational
   * specification file. In other words, this method will incorporate the given specification into
   * the current one as if they were originally one.
   * 
   * @param osFilePath path to organisational specification file
   */
  void extend(String osFilePath) {
    try {
      Document doc = NormSpecUtil.parseDocument(osFilePath);
      extend(doc);
    } catch (SAXException | IOException | ParserConfigurationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Extend specification by adding all of the data contained in the passed organisational
   * specification. In other words, this method will incorporate the given specification into the
   * current one as if they were originally one.
   * 
   * @param os organisational specification
   */
  void extend(Document os) {
    try {
      NormSpecUtil.validate(os, NormSpecUtil.OS_SCHEMA_PATH);

      Element osElement = (Element) os.getElementsByTagName(OrgSpec.getXMLTag())
                                      .item(0);
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

    } catch (SAXException | IOException | MoiseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
