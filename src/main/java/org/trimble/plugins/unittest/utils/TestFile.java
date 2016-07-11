package org.trimble.plugins.unittest.utils;

import java.util.ArrayList;
import java.util.List;
import org.sonar.api.batch.fs.InputFile;

/**
 * Represents a test file in Sonar, i.e. a source code file which implements
 * tests. Holds all test cases along with all measures collected from the
 * reports.
 */
public class TestFile {

  private int errors = 0;
  private int skipped = 0;
  private int tests = 0;
  private long time = 0;
  private int failures = 0;
  private final List<TestCase> testCases;
  private InputFile inputFile = null;

  /**
   * Creates a test file instance which corresponds and represents the passed
   * InputFile instance
   *
   * @param inputFile The InputFile in SQ which this TestFile proxies
   */
  public TestFile(InputFile inputFile) {
    this.inputFile = inputFile;
    this.testCases = new ArrayList<>();
  }

  public String getKey() {
    return inputFile.absolutePath();
  }

  public int getErrors() {
    return errors;
  }

  public int getSkipped() {
    return skipped;
  }

  public int getTests() {
    return tests;
  }

  public long getTime() {
    return time;
  }

  public int getFailures() {
    return failures;
  }

  /**
   * Adds the given test case to this test file maintaining the internal
   * statistics
   *
   * @param tc the test case to add
   */
  public void addTestCase(TestCase tc) {
    if (tc.isSkipped()) {
      skipped++;
    } else if (tc.isFailure()) {
      failures++;
    } else if (tc.isError()) {
      errors++;
    }
    tests++;
    time += tc.getTime();
    testCases.add(tc);
  }

  public InputFile getInputFile() {
    return inputFile;
  }
}
