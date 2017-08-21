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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import moise.common.MoiseException;
import moise.os.OS;
import moise.os.fs.FS;
import moise.os.ns.NS;
import moise.os.ss.SS;
import moise.xml.DOMUtils;

/**
 * Static utility methods pertaining to {@link OS} instances.
 * 
 * @author igorcadelima
 *
 */
final class OrgSpecs {
  private OrgSpecs() {}

  /**
   * Extend {@code os} by adding all of the data contained in the passed organisational
   * specification file. In other words, this method will incorporate the given specification into
   * {@code os} as if they were originally one.
   * 
   * @param os organisational specification to be extended
   * @param osFile organisational specification file
   */
  static void extend(OS os, String osFile) {
    try {
      Document osDoc = NormSpecUtil.parseDocument(osFile);
      extend(os, osDoc);
    } catch (SAXException | IOException | ParserConfigurationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Extend {@code os} by adding all of the data contained in the passed organisational
   * specification document. In other words, this method will incorporate the given specification
   * into {@code os} as if they were originally one.
   * 
   * @param os organisational specification to be extended
   * @param osDoc organisational specification
   */
  static void extend(OS os, Document osDoc) {
    try {
      DOMUtils.getOSSchemaValidator()
              .validate(new DOMSource(osDoc));

      Element osElement = (Element) osDoc.getElementsByTagName(OS.getXMLTag())
                                         .item(0);
      os.setId(osElement.getAttribute("id"));
      os.setPropertiesFromDOM(osElement);

      Element specElement;

      // Extend structural specification
      specElement = DOMUtils.getDOMDirectChild(osElement, SS.getXMLTag());
      os.getSS()
        .setFromDOM(specElement);

      // Extend functional specification
      specElement = DOMUtils.getDOMDirectChild(osElement, FS.getXMLTag());
      os.getFS()
        .setFromDOM(specElement);

      // Extend normative specification
      specElement = DOMUtils.getDOMDirectChild(osElement, NS.getXMLTag());
      if (specElement != null) {
        os.getNS()
          .setFromDOM(specElement);
      }

    } catch (SAXException | IOException | MoiseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
