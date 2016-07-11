/*
 * from: https://github.com/SonarSource/sonar-dotnet-tests-library/blob/master/src/main/java/org/sonar/plugins/dotnet/tests/NUnitTestResultsFileParser.java
*/
package org.trimble.plugins.unittest.parsers;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import javax.xml.stream.XMLStreamException;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.trimble.plugins.unittest.utils.XmlParserHelper;

public class NUnitTestResultsParser implements ReportParser {
  public static final Logger LOG = Loggers.get(NUnitTestResultsParser.class);

  @Override
  public Boolean parse(File file, UnitTestResults unitTestResults) {
    LOG.info("Parsing the NUnit Test Results file " + file.getAbsolutePath());
    return new Parser(file, unitTestResults).parse();
  }

  private static class Parser {

    private final File file;
    private XmlParserHelper xmlParserHelper;
    private final UnitTestResults unitTestResults;

    public Parser(File file, UnitTestResults unitTestResults) {
      this.file = file;
      this.unitTestResults = unitTestResults;
    }

    public Boolean parse() {

      try {
          xmlParserHelper = new XmlParserHelper(file);

        xmlParserHelper.nextTag();
        handleTestResultsTags();
      } catch (FileNotFoundException | UnsupportedEncodingException | XMLStreamException ex) {
        if (xmlParserHelper != null) {
          xmlParserHelper.close();
        }

        return false;
      }
      
      if (xmlParserHelper != null) {
        xmlParserHelper.close();
      }

      return true;
    }

    private void handleTestResultsTags() throws XMLStreamException {
      LOG.info("Parsing the NUnit Test Results file: " + file.getAbsolutePath());
      int total = 0;
      int errors = 0;
      int failures = 0;
      int inconclusive = 0;
      int ignored = 0;
      int passed = 0;
      int skipped = 0;

      total = xmlParserHelper.getRequiredIntAttribute("total");

      if(xmlParserHelper.isAttributePresent("failures")) {
        failures = xmlParserHelper.getRequiredIntAttribute("failures");
      } else {
        if(xmlParserHelper.isAttributePresent("failed")) {
          failures = xmlParserHelper.getRequiredIntAttribute("failed");
        }
      }

      if(xmlParserHelper.isAttributePresent("inconclusive")) {
        inconclusive = xmlParserHelper.getRequiredIntAttribute("inconclusive");
      }

      if(xmlParserHelper.isAttributePresent("errors")) {
        errors = xmlParserHelper.getRequiredIntAttribute("errors");
      }

      if(xmlParserHelper.isAttributePresent("ignored")) {
        ignored = xmlParserHelper.getRequiredIntAttribute("ignored");
      } else {
        if(xmlParserHelper.isAttributePresent("not-run")) {
          ignored = xmlParserHelper.getRequiredIntAttribute("not-run");
        }
      }

      if(xmlParserHelper.isAttributePresent("skipped")) {
        skipped = xmlParserHelper.getRequiredIntAttribute("skipped");
      }

      if(xmlParserHelper.isAttributePresent("errors")) {
        errors = xmlParserHelper.getRequiredIntAttribute("errors");
      }

      int tests = total - inconclusive;
      if (passed == 0) {
        passed = total - errors - failures - inconclusive;
      }
      
      if (skipped == 0) {
        skipped = inconclusive + ignored;
      }

      unitTestResults.add(tests, passed, skipped, failures, errors, 0L);
    }
  }
}