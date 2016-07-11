package org.trimble.plugins.unittest;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import org.sonar.api.Plugin;
import org.sonar.api.Plugin.Context;

import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.trimble.plugins.unittest.parsers.XunitReportParser;

/**
 * {@inheritDoc}
 */
public final class UnitTestImporterPlugin implements Plugin {

  static final String SOURCE_FILE_SUFFIXES_KEY = "sonar.cxx.suffixes.sources";
  public static final String HEADER_FILE_SUFFIXES_KEY = "sonar.cxx.suffixes.headers";
  public static final String DEFINES_KEY = "sonar.cxx.defines";
  public static final String INCLUDE_DIRECTORIES_KEY = "sonar.cxx.includeDirectories";
  public static final String ERROR_RECOVERY_KEY = "sonar.cxx.errorRecoveryEnabled";
  public static final String FORCE_INCLUDE_FILES_KEY = "sonar.cxx.forceIncludes";
  public static final String C_FILES_PATTERNS_KEY = "sonar.cxx.cFilesPatterns";
  public static final String MISSING_INCLUDE_WARN = "sonar.cxx.missingIncludeWarnings";

  private static List<PropertyDefinition> testingAndCoverageProperties() {
    return new ArrayList<>(Arrays.asList(
      PropertyDefinition.builder(UnitTestImportSensor.REPORT_PATH_KEY)
      .name("Unit test execution report(s)")
      .description("Path to unit test execution report(s), relative to projects root."
        + " See <a href='https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Get-test-execution-metrics'>here</a> for supported formats."
        + " Use <a href='https://ant.apache.org/manual/dirtasks.html'>Ant-style wildcards</a> if neccessary.")
      .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
      .index(5)
      .build(),
      PropertyDefinition.builder(XunitReportParser.XSLT_URL_KEY)
      .name("XSLT transformer")
      .description("By default, the unit test execution reports are expected to be in the JUnitReport format."
        + " To import a report in an other format, set this property to an URL to a XSLT stylesheet which is able to perform the according transformation.")
      .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
      .index(6)
      .build()
    ));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void define(Context context) {
    List<Object> l = new ArrayList<>();
    l.add(UnitTestImportSensor.class);
    l.addAll(testingAndCoverageProperties());
    context.addExtensions(l);
  }
  
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
