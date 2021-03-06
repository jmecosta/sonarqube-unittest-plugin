package org.trimble.plugins.unittest.utils;


/**
 * Represents a unit test case. Has a couple of data items like name, status,
 * time etc. associated. Reports testcase details in sonar-conform XML
 */
public class TestCase {

  private static final String STATUS_OK = "ok";
  private static final String STATUS_ERROR = "error";
  private static final String STATUS_FAILURE = "failure";
  private static final String STATUS_SKIPPED = "skipped";

  private final String name;
  private String status = STATUS_OK;
  private final String stackTrace;
  private final String errorMessage;
  private int time = 0;
  private String classname = null;
  private String tcFilename = null;
  private String tsName = null;
  private String tsFilename = null;

  /**
   * Constructs a testcase instance out of following parameters
   *
   * @params name The name of this testcase
   * @params time The execution time in milliseconds
   * @params status The execution status of the testcase
   * @params stack The stack trace occurred while executing of this testcase;
   * pass "" if the testcase passed/skipped.
   * @params msg The error message accosiated with this testcase of the
   * execution was errouneous; pass "" if not.
   * @params classname The name of the class this testcase is implemented by
   * @params tcFilename The path of the file which implements the testcase
   * @params tsName The name of the testssuite this testcase is in.
   * @params tsFilename The path of the file which implements the testssuite
   * this testcase is in.
   */
  public TestCase(String name, int time, String status, String stack, String msg,
    String classname, String tcFilename, String tsName, String tsFilename) {
    this.name = name;
    this.time = time;
    this.stackTrace = stack;
    this.errorMessage = msg;
    this.status = status;
    this.classname = classname;
    this.tcFilename = tcFilename;
    this.tsName = tsName;
    this.tsFilename = tsFilename;
  }

  /**
   * Returns the name of the class which is implementing this testcase
   */
  public String getClassname() {
    return classname != null ? classname : tsName;
  }

  /**
   * Returns the name of the class which is implementing this testcase
   */
  public String getFullname() {
    return tsName + ":" + name;
  }

  /**
   * Returns the name of the file where this testcase is implemented
   */
  public String getFilename() {
    return tcFilename != null ? tcFilename : tsFilename;
  }

  /**
   * Returns true if this testcase is an error, false otherwise
   */
  public boolean isError() {
    return STATUS_ERROR.equals(status);
  }

  /**
   * Returns true if this testcase is a failure, false otherwise
   */
  public boolean isFailure() {
    return STATUS_FAILURE.equals(status);
  }

  /**
   * Returns true if this testcase has been skipped, failure, false otherwise
   */
  public boolean isSkipped() {
    return STATUS_SKIPPED.equals(status);
  }

  public int getTime() {
    return time;
  }
}
