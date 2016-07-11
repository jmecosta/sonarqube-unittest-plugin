package org.trimble.plugins.unittest.utils;



import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;


public class XmlParserHelper {

  private final File file;
  private final InputStreamReader reader;
  private final XMLStreamReader stream;

  public XmlParserHelper(File file) throws FileNotFoundException, XMLStreamException, UnsupportedEncodingException {
      this.file = file;
      this.reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
      XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
      this.stream = xmlFactory.createXMLStreamReader(reader);
  }
  
  public boolean isAttributePresent(String name) {
    return getAttribute(name) != null;
  }  
  
  public void checkRootTag(String name) throws XMLStreamException {
    String rootTag = nextTag();

    if (!name.equals(rootTag)) {
      throw new XMLStreamException("Missing root element <" + name + ">");
    }
  }

  public String nextTag() throws XMLStreamException {
    try {
      while (stream.hasNext()) {
        if (stream.next() == XMLStreamConstants.START_ELEMENT) {
          return stream.getLocalName();
        }
      }

      return null;
    } catch (XMLStreamException e) {
      throw e;
    }
  }

  public void checkRequiredAttribute(String name, int expectedValue) throws XMLStreamException {
    int actualValue = getRequiredIntAttribute(name);
    if (expectedValue != actualValue) {
      throw new XMLStreamException("Expected \"" + expectedValue + "\" instead of \"" + actualValue + "\" for the \"" + name + "\" attribute");
    }
  }

  public int getRequiredIntAttribute(String name) throws XMLStreamException {
    String value = getRequiredAttribute(name);

    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      throw new XMLStreamException("Expected an integer instead of \"" + value + "\" for the attribute \"" + name + "\"");
    }
  }

  public String getRequiredAttribute(String name) throws XMLStreamException {
    String value = getAttribute(name);
    if (value == null) {
      throw new XMLStreamException("Missing attribute \"" + name + "\" in element <" + stream.getLocalName() + ">");
    }

    return value;
  }

  public String getAttribute(String name) {
    for (int i = 0; i < stream.getAttributeCount(); i++) {
      if (name.equals(stream.getAttributeLocalName(i))) {
        return stream.getAttributeValue(i);
      }
    }

    return null;
  }

  public Exception parseError(String message) {
    return new Exception(message + " in " + file.getAbsolutePath() + " at line " + stream.getLocation().getLineNumber());
  }

  public void close()  {
    try {
      reader.close();
      if (stream != null) {
          stream.close();
      }      
    } catch (Exception ex) {
    }
  }

  public XMLStreamReader stream() {
    return stream;
  }

}
