/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.trimble.plugins.unittest.parsers;

/**
 *
 * @author jocs
 */
public class UnitTestResults {
  int tests = 0;
  int passed = 0;
  int skipped = 0;
  int failures = 0;
  int errors = 0;
  Long duration = 0L;
  
  void add(int tests, int passed, int skipped, int failures, int errors, Long duration) {
    this.tests += tests;
    this.passed += passed;
    this.skipped += skipped;
    this.failures += failures;
    this.errors += errors;
    this.duration += duration;
  }  

  public int getTests() {
    return this.tests;
  }

  public int getTestErrors() {
    return this.errors;
  }

  public int getTestFailures() {
    return this.failures;
  }

  public Integer getSkippedTests() {
    return this.skipped;
  }

  public Long getTestTime() {
    return this.duration;
  }
}
