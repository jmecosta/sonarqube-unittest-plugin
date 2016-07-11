package org.trimble.plugins.unittest.parsers;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;
import org.trimble.plugins.unittest.TestUtils;

public class XunitReportParserTest {

  XunitReportParser parserHandler = new XunitReportParser();

  String pathPrefix = "/org/sonar/plugins/reports-project/xunit-reports/";

  @Test
  public void testParse() throws javax.xml.stream.XMLStreamException {

    Map<String, Integer> ioMap = new TreeMap<>();
    ioMap.put("xunit-result-2.xml", 5);
    ioMap.put("xunit-result-SAMPLE_with_fileName.xml", 3);
    ioMap.put("xunit-result-SAMPLE.xml", 3);
    ioMap.put("xunit-result-skippedonly.xml", 1);
    ioMap.put("xunit-result_with_emptyFileName.xml", 3);
    ioMap.put("nested_testsuites.xml", 2);

    for (Map.Entry<String, Integer> entry : ioMap.entrySet()) {
      parserHandler = new XunitReportParser();

      File report = TestUtils.loadResource(pathPrefix + entry.getKey());
      UnitTestResults results = new UnitTestResults();
      parserHandler.parse(report, results);
      assertEquals((int)entry.getValue(), results.getTests());
    }
  }

  @Test
  public void shouldThrowWhenGivenInvalidTime() {
      parserHandler = new XunitReportParser();

      File report = TestUtils.loadResource(pathPrefix + "invalid-time-xunit-report.xml");
      UnitTestResults results = new UnitTestResults();
      assertEquals(parserHandler.parse(report, results), false);
  }
}
