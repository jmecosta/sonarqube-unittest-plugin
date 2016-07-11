package org.trimble.plugins.unittest.parsers;

import org.trimble.plugins.unittest.utils.TestCase;
import org.trimble.plugins.unittest.utils.TestFile;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestFileTest {

  TestFile testFile;

  @Before
  public void setUp() {
    InputFile inputFile = new DefaultInputFile("foo", "test.cpp");
    testFile = new TestFile(inputFile);
  }

  @Test
  public void newBornTestFileShouldHaveVirginStatistics() {
    assertEquals(testFile.getTests(), 0);
    assertEquals(testFile.getErrors(), 0);
    assertEquals(testFile.getFailures(), 0);
    assertEquals(testFile.getSkipped(), 0);
    assertEquals(testFile.getTime(), 0);
  }

  @Test
  public void addingTestCaseShouldIncrementStatistics() {
    int testBefore = testFile.getTests();
    long timeBefore = testFile.getTime();

    final int EXEC_TIME = 10;
    testFile.addTestCase(new TestCase("name", EXEC_TIME, "status", "stack", "msg",
      "classname", "tcfilename", "tsname", "tsfilename"));

    assertEquals(testFile.getTests(), testBefore + 1);
    assertEquals(testFile.getTime(), timeBefore + EXEC_TIME);
  }

  @Test
  public void addingAnErroneousTestCaseShouldIncrementErrorStatistic() {
    int errorsBefore = testFile.getErrors();
    TestCase error = mock(TestCase.class);
    when(error.isError()).thenReturn(true);

    testFile.addTestCase(error);

    assertEquals(testFile.getErrors(), errorsBefore + 1);
  }

  @Test
  public void addingAFailedTestCaseShouldIncrementFailedStatistic() {
    int failedBefore = testFile.getFailures();
    TestCase failedTC = mock(TestCase.class);
    when(failedTC.isFailure()).thenReturn(true);

    testFile.addTestCase(failedTC);

    assertEquals(testFile.getFailures(), failedBefore + 1);
  }

  @Test
  public void addingASkippedTestCaseShouldIncrementSkippedStatistic() {
    int skippedBefore = testFile.getSkipped();
    TestCase skippedTC = mock(TestCase.class);
    when(skippedTC.isSkipped()).thenReturn(true);

    testFile.addTestCase(skippedTC);

    assertEquals(testFile.getSkipped(), skippedBefore + 1);
  }
}
