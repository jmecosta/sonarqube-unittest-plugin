/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2016 SonarOpenCommunity
 * http://github.com/SonarOpenCommunity/sonar-cxx
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.trimble.plugins.unittest.parsers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.codehaus.staxmate.in.ElementFilter;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.ParsingUtils;
import org.sonar.api.utils.log.Loggers;
import org.trimble.plugins.unittest.utils.TestCase;
import org.trimble.plugins.unittest.utils.EmptyReportException;
import org.trimble.plugins.unittest.utils.StaxParser;
import org.trimble.plugins.unittest.utils.StaxParser.XmlStreamHandler;

/**
 * {@inheritDoc}
 */
public class XunitReportParser implements ReportParser {
  public static final org.sonar.api.utils.log.Logger LOG = Loggers.get(XunitReportParser.class);
  public static final String XSLT_URL_KEY = "sonar.unittests.xunit.xsltURL";
  
  private final String xsltURL;

  public XunitReportParser(Settings settings) {
    xsltURL = settings.getString(XSLT_URL_KEY);
  }

  XunitReportParser() {
    xsltURL = null;
  }
  
  private static class Parser implements XmlStreamHandler {
    private final List<TestCase> testCases = new LinkedList<>();
    private UnitTestResults unitTestResults;

    private Parser(UnitTestResults unitTestResults) {
      this.unitTestResults = unitTestResults;
    }
    
  /**
   * {@inheritDoc}
   */
  @Override
  public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {
    SMInputCursor testSuiteCursor = rootCursor.constructDescendantCursor(new ElementFilter("testsuite"));
    try {
      testSuiteCursor.getNext();
    } catch (com.ctc.wstx.exc.WstxEOFException eofExc) {
      throw new EmptyReportException();
    }

    do {
      parseTestSuiteTag(testSuiteCursor);
    } while (testSuiteCursor.getNext() != null);
  }

  public void parseTestSuiteTag(SMInputCursor testSuiteCursor)
    throws XMLStreamException {
    String testSuiteName = testSuiteCursor.getAttrValue("name");
    String testSuiteFName = testSuiteCursor.getAttrValue("filename");

    SMInputCursor childCursor = testSuiteCursor.childElementCursor();
    while (childCursor.getNext() != null) {
      String elementName = childCursor.getLocalName();
      if ("testsuite".equals(elementName)) {
        parseTestSuiteTag(childCursor);
      } else if ("testcase".equals(elementName)) {
        testCases.add(parseTestCaseTag(childCursor, testSuiteName, testSuiteFName));
      }
    }
  }

  private TestCase parseTestCaseTag(SMInputCursor testCaseCursor, String tsName, String tsFilename)
    throws XMLStreamException {
    String classname = testCaseCursor.getAttrValue("classname");
    String tcFilename = testCaseCursor.getAttrValue("filename");
    String name = parseTestCaseName(testCaseCursor);
    Double time = parseTime(testCaseCursor);
    String status = "ok";
    String stack = "";
    String msg = "";

    // Googletest-reports mark the skipped tests with status="notrun"
    String statusattr = testCaseCursor.getAttrValue("status");
    if ("notrun".equals(statusattr)) {
      status = "skipped";
      this.unitTestResults.add(1, 0, 0, 1, 0, time.longValue());
    } else {
      SMInputCursor childCursor = testCaseCursor.childElementCursor();
      if (childCursor.getNext() != null) {
        String elementName = childCursor.getLocalName();
        if ("skipped".equals(elementName)) {
          this.unitTestResults.add(1, 0, 1, 0, 0, time.longValue());
          status = "skipped";
        } else if ("failure".equals(elementName)) {
          this.unitTestResults.add(1, 0, 0, 1, 0, time.longValue());
          status = "failure";
          msg = childCursor.getAttrValue("message");
          stack = childCursor.collectDescendantText();
        } else if ("error".equals(elementName)) {
          this.unitTestResults.add(1, 0, 0, 0, 1, time.longValue());
          status = "error";
          msg = childCursor.getAttrValue("message");
          stack = childCursor.collectDescendantText();
        }
      }
    }
    
    if (status.equals("ok")) {
      this.unitTestResults.add(1, 1, 0, 0, 0, time.longValue());
    }

    return new TestCase(name, time.intValue(), status, stack, msg, classname, tcFilename, tsName, tsFilename);
  }

  private double parseTime(SMInputCursor testCaseCursor)
    throws XMLStreamException {
    double time = 0.0;
    try {
      String sTime = testCaseCursor.getAttrValue("time");
      if (sTime != null && !sTime.isEmpty()) {
        Double tmp = ParsingUtils.parseNumber(sTime, Locale.ENGLISH);
        if (!Double.isNaN(tmp)) {
          time = ParsingUtils.scaleValue(tmp * 1000, 3);
        }
      }
    } catch (ParseException e) {
      throw new XMLStreamException(e);
    }

    return time;
  }

  private String parseTestCaseName(SMInputCursor testCaseCursor) throws XMLStreamException {
    String name = testCaseCursor.getAttrValue("name");
    String classname = testCaseCursor.getAttrValue("classname");
    if (classname != null) {
      name = classname + "/" + name;
    }
    return name;
  }
      
  }


  File transformReport(File report)
    throws java.io.IOException, javax.xml.transform.TransformerException {
    File transformed = report;
    if (xsltURL != null && report.length() > 0) {
      LOG.debug("Transforming the report using xslt '{}'", xsltURL);
      InputStream inputStream = this.getClass().getResourceAsStream("/xsl/" + xsltURL);
      if (inputStream == null) {
        LOG.debug("Transforming: try to access external XSLT via URL");
        URL url = new URL(xsltURL);
        inputStream = url.openStream();
      }

      Source xsl = new StreamSource(inputStream);
      TransformerFactory factory = TransformerFactory.newInstance();
      Templates template = factory.newTemplates(xsl);
      Transformer xformer = template.newTransformer();
      xformer.setOutputProperty(OutputKeys.INDENT, "yes");

      Source source = new StreamSource(report);
      transformed = new File(report.getAbsolutePath() + ".after_xslt");
      Result result = new StreamResult(transformed);
      xformer.transform(source, result);
    } else {
      LOG.debug("Transformation skipped: no xslt given");
    }

    return transformed;
  }  

  @Override
  public Boolean parse(File report, UnitTestResults unitTestResults) {

    Parser parserHandler = new Parser(unitTestResults);
    StaxParser parser = new StaxParser(parserHandler, false);
    LOG.info("Processing report '{}'", report);
      try {
        parser.parse(transformReport(report));
        
      } catch (IOException ex) {
        return false;
      } catch (TransformerException ex) {
        return false;
      } catch (XMLStreamException ex) {
        return false;
      }      
      
      return true;
  }  
}
