/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.trimble.plugins.unittest.parsers;

import java.io.File;

/**
 *
 * @author jocs
 */
public interface ReportParser {
  Boolean parse(File report, UnitTestResults unitTestResults);
}
