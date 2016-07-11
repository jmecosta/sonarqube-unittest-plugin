package org.trimble.plugins.unittest;


import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class TestUtils {

  private final static String OS = System.getProperty("os.name").toLowerCase();
  private final static boolean upperCaseRoot = Character.isUpperCase(System.getProperty("java.home").charAt(0));

  public static File loadResource(String resourceName) {
    URL resource = TestUtils.class.getResource(resourceName);
    File resourceAsFile = null;
    try {
      resourceAsFile = new File(resource.toURI());
    } catch (URISyntaxException e) {
      System.out.println("Cannot load resource: " + resourceName);
    }

    return resourceAsFile;
  }
}
