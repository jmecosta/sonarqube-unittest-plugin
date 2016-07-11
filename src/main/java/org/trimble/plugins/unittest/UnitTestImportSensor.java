package org.trimble.plugins.unittest;

import org.trimble.plugins.unittest.parsers.XunitReportParser;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.apache.tools.ant.DirectoryScanner;
import org.sonar.api.batch.sensor.Sensor;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.utils.ParsingUtils;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.trimble.plugins.unittest.parsers.NUnitTestResultsParser;
import org.trimble.plugins.unittest.parsers.ReportParser;
import org.trimble.plugins.unittest.parsers.UnitTestResults;

/**
 * {@inheritDoc}
 */
public class UnitTestImportSensor implements Sensor {
  public static final Logger LOG = Loggers.get(UnitTestImportSensor.class);
  public static final String REPORT_PATH_KEY = "sonar.unittest.reportPath";
  private static final double PERCENT_BASE = 100d;
  private final List<ReportParser> parsers = new LinkedList<>();
  private final Settings settings;
  
  /**
   * {@inheritDoc}
   */
  public UnitTestImportSensor(Settings settings) {
    this.settings = settings;
    ReportParser xunit = new XunitReportParser(settings);
    ReportParser nunit = new NUnitTestResultsParser();
    this.parsers.add(xunit);
    this.parsers.add(nunit);
  }

  protected String reportPathKey() {
    return REPORT_PATH_KEY;
  }

  public void describe(SensorDescriptor descriptor) {
    descriptor.name("UnitTestImportSensor");
  }
  
  /**
   * {@inheritDoc}
   */
  public void execute(SensorContext context) {    
    String moduleKey = context.settings().getString("sonar.moduleKey");
    if (moduleKey != null) {
        LOG.debug("Runs unit test import sensor only at top level project skip : Module Key = '{}'", moduleKey);
        return;        
    }
    
    LOG.debug("Root module imports test metrics: Module Key = '{}'", context.module());    
    
    List<File> reports = getReports(settings, context.fileSystem().baseDir(), REPORT_PATH_KEY);
    if (!reports.isEmpty()) {

      UnitTestResults results = new UnitTestResults();
      for(File report : reports) {
        for (ReportParser parser : this.parsers) {
          if (parser.parse(report, results)) {
            break;
          }
        }          
      }

      saveMetrics(context, results);
    } else {
      LOG.debug("No reports found, nothing to process");
    }
  }

  public static List<File> getReports(Settings settings, final File moduleBaseDir,
      String reportPathPropertyKey) {

    List<File> reports = new ArrayList<>();

    List<String> reportPaths = Arrays.asList(settings.getStringArray(reportPathPropertyKey));
    if (!reportPaths.isEmpty()) {
      List<String> includes = new ArrayList<>();
      for (String reportPath : reportPaths) {
        // Normalization can return null if path is null, is invalid, or is a path with back-ticks outside known directory structure
        String normalizedPath = FilenameUtils.normalize(reportPath);
        if (normalizedPath != null && new File(normalizedPath).isAbsolute()) {
          includes.add(normalizedPath);
          continue;
        }

        // Prefix with absolute module base dir, attempt normalization again -- can still get null here
        normalizedPath = FilenameUtils.normalize(moduleBaseDir.getAbsolutePath() + File.separator + reportPath);
        if (normalizedPath != null) {
          includes.add(normalizedPath);
          continue;
        }

        LOG.debug("Not a valid report path '{}'", reportPath);
      }

      LOG.debug("Normalized report includes to '{}'", includes);

      // Includes array cannot contain null elements
      DirectoryScanner directoryScanner = new DirectoryScanner();
      directoryScanner.setIncludes(includes.toArray(new String[includes.size()]));
      directoryScanner.scan();

      String [] includeFiles = directoryScanner.getIncludedFiles();
      LOG.info("Scanner found '{}' report files", includeFiles.length);
      for (String found : includeFiles) {        
        reports.add(new File(found));
      }

      if (reports.isEmpty()) {
        LOG.warn("Cannot find a report for '{}'", reportPathPropertyKey);
      } else {
        LOG.info("Parser will parse '{}' report files", reports.size());
      }
    } else {
      LOG.info("Undefined report path value for key '{}'", reportPathPropertyKey);
    }

    return reports;
  }
  
  private void saveMetrics(final SensorContext context, UnitTestResults results) {
        
    if (results.getTests() > 0) {
      double testsPassed = results.getTests() - results.getTestErrors() - results.getTestFailures();
      double successDensity = testsPassed * PERCENT_BASE / results.getTests();

      context.<Integer>newMeasure()
         .forMetric(CoreMetrics.TESTS)
         .on(context.module())
         .withValue(results.getTests())
         .save();
       context.<Integer>newMeasure()
         .forMetric(CoreMetrics.TEST_ERRORS)
         .on(context.module())
         .withValue(results.getTestErrors())
         .save();
       context.<Integer>newMeasure()
         .forMetric(CoreMetrics.TEST_FAILURES)
         .on(context.module())
         .withValue(results.getTestFailures())
         .save();
       context.<Integer>newMeasure()
         .forMetric(CoreMetrics.SKIPPED_TESTS)
         .on(context.module())
         .withValue(results.getSkippedTests())
         .save();
       context.<Double>newMeasure()
         .forMetric(CoreMetrics.TEST_SUCCESS_DENSITY)
         .on(context.module())
         .withValue(ParsingUtils.scaleValue(successDensity))
         .save();
        context.<Long>newMeasure()
         .forMetric(CoreMetrics.TEST_EXECUTION_TIME)
         .on(context.module())
         .withValue(results.getTestTime())
         .save();
    } else {
      LOG.debug("The reports contain no testcases");
    }      

  }

}
