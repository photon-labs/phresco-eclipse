package com.photon.phresco.commons.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.photon.phresco.commons.FileListFilter;
import com.photon.phresco.commons.FrameworkConstants;
import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.ui.model.TestCase;
import com.photon.phresco.ui.model.TestCaseError;
import com.photon.phresco.ui.model.TestCaseFailure;
import com.photon.phresco.ui.model.TestSuite;
import com.photon.phresco.ui.resource.Messages;
import com.photon.phresco.util.Constants;
import com.photon.phresco.util.Utility;
import com.phresco.pom.exception.PhrescoPomException;
import com.phresco.pom.util.PomProcessor;

public class QualityUtil implements PhrescoConstants {
	
	/** The test suite. */
	private String testSuite = "";
	
	/** The set failure test cases. */
	private int setFailureTestCases;
	
	/** The error test cases. */
	private int errorTestCases;
	
	/** The node length. */
	private int nodeLength;
	
	/** The test suite map. */
	private static Map<String, Map<String, NodeList>> testSuiteMap = Collections
			.synchronizedMap(new HashMap<String, Map<String, NodeList>>(8));
	
	public boolean testResultAvail(String appDirName, List<String> testAgainsts, String action) throws PhrescoException {
		boolean resultAvailable = false;
        try {
        	String reportDir = "";
        	String resultExtension = "";
        	ApplicationInfo appInfo = PhrescoUtil.getApplicationInfo();
        	if (Constants.PHASE_PERFORMANCE_TEST.equals(action)) {
        		reportDir =PhrescoUtil.getPerformanceTestReportDir(appInfo);
        		resultExtension = PhrescoUtil.getPerformanceResultFileExtension(appDirName);
        	} else {
        		reportDir = PhrescoUtil.getLoadTestReportDir(appInfo);
        		resultExtension = PhrescoUtil.getLoadResultFileExtension(appInfo);
        	}
            for (String testAgainst: testAgainsts) {
            	StringBuilder sb = new StringBuilder(PhrescoUtil.getApplicationHome());
            	if (Constants.PHASE_PERFORMANCE_TEST.equals(action)) {
            		reportDir =PhrescoUtil.getPerformanceTestReportDir(appInfo);
            	} else {
            		reportDir = PhrescoUtil.getLoadTestReportDir(appInfo);
            	}
               
                if (StringUtils.isNotEmpty(reportDir) && StringUtils.isNotEmpty(testAgainst)) {
                    Pattern p = Pattern.compile(TEST_DIRECTORY);
                    Matcher matcher = p.matcher(reportDir);
                    reportDir = matcher.replaceAll(testAgainst);
                    sb.append(reportDir); 
                }
                
                File file = new File(sb.toString());
                if (StringUtils.isNotEmpty(resultExtension) && file.exists()) {
                	File[] children = file.listFiles(new XmlNameFileFilter(resultExtension));
                	if (!ArrayUtils.isEmpty(children)) {
                		resultAvailable = true;
                		break;
                	}
                }
            }
            
            if (CollectionUtils.isEmpty(testAgainsts) && Constants.PHASE_PERFORMANCE_TEST.equals(action) 
            			&& StringUtils.isNotEmpty(reportDir)) {
            	 StringBuilder sb = new StringBuilder(PhrescoUtil.getApplicationHome());
            	 sb.append(reportDir);
            	 File file = new File(sb.toString());
                 if (StringUtils.isNotEmpty(resultExtension)) {
                 	File[] children = file.listFiles(new XmlNameFileFilter(resultExtension));
                 	if (!ArrayUtils.isEmpty(children)) {
                 		resultAvailable = true;
                 	}
                 }
            }
            
        } catch(Exception e) {
        	throw new PhrescoException(e);
        }

        return resultAvailable;
    }
	
	public class XmlNameFileFilter implements FilenameFilter {
		private String filter_;
		public XmlNameFileFilter(String filter) {
			filter_ = filter;
		}

		public boolean accept(File dir, String name) {
			return name.endsWith(filter_);
		}
	}
	
	/**
	 * @throws PhrescoException
	 */
	public Table getTestReport(Shell functionalDialog, String testType, String techType, String moduleName) throws PhrescoException {
		QualityUtil qualityUtil = new QualityUtil();
		List<TestSuite> testSuites = qualityUtil.getTestSuite(testType, techType, moduleName);

		Table table = new Table(functionalDialog, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		if(CollectionUtils.isEmpty(testSuites)) {
			TableItem tableItem = new TableItem(table, SWT.NONE);
			tableItem.setText(0, Messages.REPORT_NOT_AVAILABLE);
			table.setSize(600, 400);
			table.setHeaderVisible(false);
			table.setLinesVisible(false);
			functionalDialog.pack();
			return table;
		}
		String[] columnValues = {Messages.TEST_SUITE, Messages.TOTAL, Messages.SUCCESS, Messages.FAILURE, Messages.ERROR};
		for (int i = 0; i < columnValues.length; i++) {
			TableColumn column = new TableColumn(table, SWT.FILL);
			column.setWidth(100);
			column.setText(columnValues[i]);
		}
		for (TestSuite testSuite : testSuites) {
			TableItem item = new TableItem(table, SWT.FILL);
			TableEditor editor = new TableEditor(table);
			editor.minimumWidth = 100;
			editor.minimumHeight = 30;
			editor.horizontalAlignment = SWT.CENTER;
			editor.grabHorizontal = true;
			String testSuiteName = testSuite.getName();
			float total = testSuite.getTotal();
			float success = testSuite.getSuccess();
			float failures = testSuite.getFailures();
			float errors = testSuite.getErrors();
			item.setText(0, testSuiteName);
			item.setText(1, Math.round(total) + "");
			item.setText(2, Math.round(success) + "");
			item.setText(3, Math.round(failures) + "");
			item.setText(4, Math.round(errors) + "");
		}
		table.setSize(600, 400);
		functionalDialog.pack();
		return table;
	}
	
	public List<TestSuite> getTestSuite(String testType, String techReport, String moduleName) throws PhrescoException {
		
		String baseDir = PhrescoUtil.getApplicationHome();
		Utility.killProcess(baseDir, testType);
		ApplicationInfo appInfo = PhrescoUtil.getApplicationInfo();

		String testSuitePath = getTestSuitePath(appInfo.getAppDirName(), testType, techReport);
		String testCasePath = getTestCasePath(appInfo.getAppDirName(), testType, techReport);
		List<TestSuite> testSuites = testSuites(appInfo.getAppDirName(), moduleName, testType, moduleName, techReport,
				testSuitePath, testCasePath, ALL);
		if(CollectionUtils.isNotEmpty(testSuites)) {
			return testSuites;
		}
		return null;
	}
	
	/**
	 * Test suites.
	 *
	 * @param appDirName the app dir name
	 * @param moduleName the module name
	 * @param testType the test type
	 * @param module the module
	 * @param techReport the tech report
	 * @param testSuitePath the test suite path
	 * @param testCasePath the test case path
	 * @param testSuite the test suite
	 * @return the list
	 * @throws PhrescoException the phresco exception
	 */
	private List<TestSuite> testSuites(String appDirName, String moduleName, String testType, String module, String techReport, String testSuitePath, String testCasePath, String testSuite) throws PhrescoException {
		setTestSuite(testSuite);
		List<TestSuite> suites = new ArrayList<TestSuite>();
		try {
			String testSuitesMapKey = appDirName + testType + module + techReport;
			String testResultPath = getTestResultPath(appDirName, moduleName, testType, techReport);
			File[] testResultFiles = getTestResultFiles(testResultPath);
			if (ArrayUtils.isEmpty(testResultFiles)) {
				return null;
			}
			getTestSuiteNames(appDirName, testType, moduleName, techReport, testResultPath, testSuitePath);
			Map<String, NodeList> testResultNameMap = testSuiteMap.get(testSuitesMapKey);
			if (MapUtils.isEmpty(testResultNameMap)) {
				return null;
			}
			Map<String, String> testSuitesResultMap = new HashMap<String, String>();
			float totalTestSuites = 0;
			float successTestSuites = 0;
			float failureTestSuites = 0;
			float errorTestSuites = 0;
			// get all nodelist of testType of a project
			Collection<NodeList> allTestResultNodeLists = testResultNameMap.values();
			for (NodeList allTestResultNodeList : allTestResultNodeLists) {
				if (allTestResultNodeList.getLength() > 0) {
					List<TestSuite> allTestSuites = getTestSuite(allTestResultNodeList);
					if (CollectionUtils.isNotEmpty(allTestSuites)) {
						for (TestSuite tstSuite : allTestSuites) {
							// testsuite values are set before calling
							// getTestCases value
							setTestSuite(tstSuite.getName());
							getTestCases(appDirName, allTestResultNodeList, testSuitePath, testCasePath);
							float tests = 0;
							float failures = 0;
							float errors = 0;
							tests = Float.parseFloat(String.valueOf(getNodeLength()));
							failures = Float.parseFloat(String.valueOf(getSetFailureTestCases()));
							errors = Float.parseFloat(String.valueOf(getErrorTestCases()));
							float success = 0;

							if (failures != 0 && errors == 0) {
								if (failures > tests) {
									success = failures - tests;
								} else {
									success = tests - failures;
								}
							} else if (failures == 0 && errors != 0) {
								if (errors > tests) {
									success = errors - tests;
								} else {
									success = tests - errors;
								}
							} else if (failures != 0 && errors != 0) {
								float failTotal = (failures + errors);
								if (failTotal > tests) {
									success = failTotal - tests;
								} else {
									success = tests - failTotal;
								}
							} else {
								success = tests;
							}

							totalTestSuites = totalTestSuites + tests;
							failureTestSuites = failureTestSuites + failures;
							errorTestSuites = errorTestSuites + errors;
							successTestSuites = successTestSuites + success;
							String rstValues = tests + Constants.COMMA + success + Constants.COMMA + failures + Constants.COMMA + errors;
							testSuitesResultMap.put(tstSuite.getName(), rstValues);

							TestSuite suite = new TestSuite();
							suite.setName(tstSuite.getName());
							suite.setSuccess(success);
							suite.setErrors(errors);
							suite.setTime(tstSuite.getTime());
							suite.setTotal(totalTestSuites);
							suite.setTestCases(tstSuite.getTestCases());
							suites.add(suite);
						}
					}
				}
			}
		} catch (PhrescoException e) {
			throw new PhrescoException(e);
		}
		return suites;
	}
	
	/**
	 * Gets the test suite path.
	 *
	 * @param appDirName the app dir name
	 * @param testType the test type
	 * @param techReport the tech report
	 * @return the test suite path
	 * @throws PhrescoException the phresco exception
	 */
	private String getTestSuitePath(String appDirName, String testType, String techReport) throws PhrescoException {
		String testSuitePath = "";
		if (testType.equals(UNIT)) {
			if (StringUtils.isNotEmpty(techReport)) {
				testSuitePath = getUnitTestSuitePath(appDirName, techReport);
			} else {
				testSuitePath = getUnitTestSuitePath(appDirName);
			}
		} else if (testType.equals(COMPONENT)) {
			testSuitePath = getComponentTestSuitePath(appDirName);
		} else if (testType.equals(FUNCTIONAL)) {
			testSuitePath = getFunctionalTestSuitePath(appDirName);
		} 
		
		return testSuitePath;
	}
	
	/**
	 * Gets the unit test suite path.
	 *
	 * @param appDirName the app dir name
	 * @param option the option
	 * @return the unit test suite path
	 * @throws PhrescoException the phresco exception
	 */
	public String getUnitTestSuitePath(String appDirName, String option) throws PhrescoException {
		try {
			return PhrescoUtil.getPomProcessor(appDirName).getProperty(
					Constants.POM_PROP_KEY_UNITTEST_TESTSUITE_XPATH_START + option
							+ Constants.POM_PROP_KEY_UNITTEST_TESTSUITE_XPATH_END);
		} catch (PhrescoPomException e) {
			throw new PhrescoException(e);
		}
	}
	
	/**
	 * Gets the unit test suite path.
	 *
	 * @param appDirName the app dir name
	 * @return the unit test suite path
	 * @throws PhrescoException the phresco exception
	 */
	public String getUnitTestSuitePath(String appDirName) throws PhrescoException {
		try {
			return PhrescoUtil.getPomProcessor(appDirName).getProperty(
					Constants.POM_PROP_KEY_UNITTEST_TESTSUITE_XPATH);
		} catch (PhrescoPomException e) {
			throw new PhrescoException(e);
		}
	}
	
	/**
	 * Gets the component test suite path.
	 *
	 * @param appDirName the app dir name
	 * @return the component test suite path
	 * @throws PhrescoException the phresco exception
	 */
	private String getComponentTestSuitePath(String appDirName) throws PhrescoException {
		try {
			return PhrescoUtil.getPomProcessor(appDirName).getProperty(
					Constants.POM_PROP_KEY_COMPONENTTEST_TESTSUITE_XPATH);
		} catch (PhrescoPomException e) {
			throw new PhrescoException(e);
		}
	}
	
	/**
	 * Gets the functional test suite path.
	 *
	 * @param appDirName the app dir name
	 * @return the functional test suite path
	 * @throws PhrescoException the phresco exception
	 */
	private String getFunctionalTestSuitePath(String appDirName) throws PhrescoException {
		try {
			return PhrescoUtil.getPomProcessor(appDirName).getProperty(
					Constants.POM_PROP_KEY_FUNCTEST_TESTSUITE_XPATH);
		} catch (PhrescoPomException e) {
			throw new PhrescoException(e);
		}
	}
	
	/**
	 * Gets the test case path.
	 *
	 * @param appDirName the app dir name
	 * @param testType the test type
	 * @param techReport the tech report
	 * @return the test case path
	 * @throws PhrescoException the phresco exception
	 */
	private String getTestCasePath(String appDirName, String testType, String techReport) throws PhrescoException {
		String testCasePath = "";
		if (testType.equals(UNIT)) {
			if (StringUtils.isNotEmpty(techReport)) {
				testCasePath = getUnitTestCasePath(appDirName, techReport);
			} else {
				testCasePath = getUnitTestCasePath(appDirName);
			}
		} else if (testType.equals(FUNCTIONAL)) {
			testCasePath = getFunctionalTestCasePath(appDirName);
		} else if (testType.equals(COMPONENT)) {
			testCasePath = getComponentTestCasePath(appDirName);
		}
		return testCasePath;
	}
	
	/**
	 * Gets the unit test case path.
	 *
	 * @param appDirName the app dir name
	 * @param option the option
	 * @return the unit test case path
	 * @throws PhrescoException the phresco exception
	 */
	private String getUnitTestCasePath(String appDirName, String option) throws PhrescoException {
		try {
			return PhrescoUtil.getPomProcessor(appDirName).getProperty(
					Constants.POM_PROP_KEY_UNITTEST_TESTCASE_PATH_START + option
							+ Constants.POM_PROP_KEY_UNITTEST_TESTCASE_PATH_END);
		} catch (PhrescoPomException e) {
			throw new PhrescoException(e);
		}
	}
	
	/**
	 * Gets the unit test case path.
	 *
	 * @param appDirName the app dir name
	 * @return the unit test case path
	 * @throws PhrescoException the phresco exception
	 */
	private String getUnitTestCasePath(String appDirName) throws PhrescoException {
		try {
			return PhrescoUtil.getPomProcessor(appDirName).getProperty(
					Constants.POM_PROP_KEY_UNITTEST_TESTCASE_PATH);
		} catch (PhrescoPomException e) {
			throw new PhrescoException(e);
		}
	}

	/**
	 * Gets the functional test case path.
	 *
	 * @param appDirName the app dir name
	 * @return the functional test case path
	 * @throws PhrescoException the phresco exception
	 */
	private String getFunctionalTestCasePath(String appDirName) throws PhrescoException {
		try {
			return PhrescoUtil.getPomProcessor(appDirName).getProperty(
					Constants.POM_PROP_KEY_FUNCTEST_TESTCASE_PATH);
		} catch (PhrescoPomException e) {
			throw new PhrescoException(e);
		}
	}

	/**
	 * Gets the component test case path.
	 *
	 * @param appDirName the app dir name
	 * @return the component test case path
	 * @throws PhrescoException the phresco exception
	 */
	private String getComponentTestCasePath(String appDirName) throws PhrescoException {
		try {
			return PhrescoUtil.getPomProcessor(appDirName).getProperty(
					Constants.POM_PROP_KEY_COMPONENTTEST_TESTCASE_PATH);
		} catch (PhrescoPomException e) {
			throw new PhrescoException(e);
		}
	}
	
	/**
	 * Gets the test result path.
	 *
	 * @param appDirName the app dir name
	 * @param moduleName the module name
	 * @param testType the test type
	 * @param techReport the tech report
	 * @return the test result path
	 * @throws PhrescoException the phresco exception
	 */
	private String getTestResultPath(String appDirName, String moduleName, String testType, String techReport)
			throws PhrescoException {
		String testResultPath = "";
		if (testType.equals(UNIT)) {
			testResultPath = getUnitTestResultPath(appDirName, moduleName, techReport);
		} else if (testType.equals(FUNCTIONAL)) {
			testResultPath = getFunctionalTestResultPath(appDirName, moduleName);
		} else if (testType.equals(COMPONENT)) {
			testResultPath = getComponentTestResultPath(appDirName, moduleName);
		} 
		
		return testResultPath;
	}
	
	/**
	 * Gets the unit test result path.
	 *
	 * @param appDirName the app dir name
	 * @param moduleName the module name
	 * @param techReport the tech report
	 * @return the unit test result path
	 * @throws PhrescoException the phresco exception
	 */
	private String getUnitTestResultPath(String appDirName, String moduleName, String techReport)
			throws PhrescoException {
		StringBuilder sb = new StringBuilder(Utility.getProjectHome() + appDirName);
		if (StringUtils.isNotEmpty(moduleName)) {
			sb.append(File.separatorChar);
			sb.append(moduleName);
		}
		// TODO Need to change this
		StringBuilder tempsb = new StringBuilder(sb);
		if (FrameworkConstants.JAVASCRIPT.equals(techReport)) {
			tempsb.append(FrameworkConstants.UNIT_TEST_QUNIT_REPORT_DIR);
			File file = new File(tempsb.toString());
			if (file.isDirectory() && file.list().length > 0) {
				sb.append(FrameworkConstants.UNIT_TEST_QUNIT_REPORT_DIR);
			} else {
				sb.append(FrameworkConstants.UNIT_TEST_JASMINE_REPORT_DIR);
			}
		} else {
			if (StringUtils.isNotEmpty(techReport)) {
				sb.append(getUnitTestReportDir(appDirName, techReport));
			} else {
				sb.append(getUnitTestReportDir(appDirName));
			}
		}
		return sb.toString();
	}
	
	/**
	 * Gets the unit test report dir.
	 *
	 * @param appDirName the app dir name
	 * @param option the option
	 * @return the unit test report dir
	 * @throws PhrescoException the phresco exception
	 */
	private String getUnitTestReportDir(String appDirName, String option) throws PhrescoException {
		try {
			PomProcessor pomProcessor = PhrescoUtil.getPomProcessor(appDirName);
			return pomProcessor.getProperty(Constants.POM_PROP_KEY_UNITTEST_RPT_DIR_START + option
					+ Constants.POM_PROP_KEY_UNITTEST_RPT_DIR_END);
		} catch (PhrescoPomException e) {
			throw new PhrescoException(e);
		}
	}
	
	/**
	 * Gets the unit test report dir.
	 *
	 * @param appDirName the app dir name
	 * @return the unit test report dir
	 * @throws PhrescoException the phresco exception
	 */
	public String getUnitTestReportDir(String appDirName) throws PhrescoException {
		try {
			return PhrescoUtil.getPomProcessor(appDirName).getProperty(
					Constants.POM_PROP_KEY_UNITTEST_RPT_DIR);
		} catch (PhrescoPomException e) {
			throw new PhrescoException(e);
		}
	}
	
	/**
	 * Gets the functional test result path.
	 *
	 * @param appDirName the app dir name
	 * @param moduleName the module name
	 * @return the functional test result path
	 * @throws PhrescoException the phresco exception
	 */
	private String getFunctionalTestResultPath(String appDirName, String moduleName) throws PhrescoException {

		StringBuilder sb = new StringBuilder();
		try {
			sb.append(Utility.getProjectHome() + appDirName);
			if (StringUtils.isNotEmpty(moduleName)) {
				sb.append(File.separatorChar);
				sb.append(moduleName);
			}
			sb.append(getFunctionalTestReportDir(appDirName));
		} catch (PhrescoException e) {
			throw new PhrescoException(e);
		}

		return sb.toString();
	}
	
	/**
	 * Gets the functional test report dir.
	 *
	 * @param appDirName the app dir name
	 * @return the functional test report dir
	 * @throws PhrescoException the phresco exception
	 */
	private String getFunctionalTestReportDir(String appDirName) throws PhrescoException {
		try {
			return PhrescoUtil.getPomProcessor(appDirName).getProperty(
					Constants.POM_PROP_KEY_FUNCTEST_RPT_DIR);
		} catch (PhrescoPomException e) {
			throw new PhrescoException(e);
		}
	}
	
	/**
	 * Gets the component test result path.
	 *
	 * @param appDirName the app dir name
	 * @param moduleName the module name
	 * @return the component test result path
	 * @throws PhrescoException the phresco exception
	 */
	private String getComponentTestResultPath(String appDirName, String moduleName) throws PhrescoException {

		StringBuilder sb = new StringBuilder();
		try {
			sb.append(Utility.getProjectHome() + appDirName);
			if (StringUtils.isNotEmpty(moduleName)) {
				sb.append(File.separatorChar);
				sb.append(moduleName);
			}
			sb.append(getComponentTestReportDir(appDirName));
		} catch (PhrescoException e) {
			throw new PhrescoException(e);
		}

		return sb.toString();
	}
	
	/**
	 * Gets the component test report dir.
	 *
	 * @param appDirName the app dir name
	 * @return the component test report dir
	 * @throws PhrescoException the phresco exception
	 */
	private String getComponentTestReportDir(String appDirName) throws PhrescoException {
		try {
			return PhrescoUtil.getPomProcessor(appDirName).getProperty(
					Constants.POM_PROP_KEY_COMPONENTTEST_RPT_DIR);
		} catch (PhrescoPomException e) {
			throw new PhrescoException(e);
		}
	}
	
	/**
	 * Gets the test result files.
	 *
	 * @param path the path
	 * @return the test result files
	 */
	private File[] getTestResultFiles(String path) {
		File testDir = new File(path);
		if (testDir.isDirectory()) {
			FilenameFilter filter = new FileListFilter("", FILE_EXTENSION_XML);
			return testDir.listFiles(filter);
		}
		return null;
	}
	
	/**
	 * Gets the test suite names.
	 *
	 * @param appDirName the app dir name
	 * @param testType the test type
	 * @param moduleName the module name
	 * @param techReport the tech report
	 * @param testResultPath the test result path
	 * @param testSuitePath the test suite path
	 * @return the test suite names
	 * @throws PhrescoException the phresco exception
	 */
	private List<String> getTestSuiteNames(String appDirName, String testType, String moduleName, String techReport,
			String testResultPath, String testSuitePath) throws PhrescoException {
		String testSuitesMapKey = appDirName + testType + moduleName + techReport;
		Map<String, NodeList> testResultNameMap = testSuiteMap.get(testSuitesMapKey);
		List<String> resultTestSuiteNames = null;
		if (MapUtils.isEmpty(testResultNameMap)) {
			File[] resultFiles = getTestResultFiles(testResultPath);
			if (!ArrayUtils.isEmpty(resultFiles)) {
				QualityUtil.sortResultFile(resultFiles);
				updateCache(appDirName, testType, moduleName, techReport, resultFiles, testSuitePath);
			}
			testResultNameMap = testSuiteMap.get(testSuitesMapKey);
		}
		if (testResultNameMap != null) {
			resultTestSuiteNames = new ArrayList<String>(testResultNameMap.keySet());
		}
		return resultTestSuiteNames;
	}
	
	/**
	 * Update cache.
	 *
	 * @param appDirName the app dir name
	 * @param testType the test type
	 * @param moduleName the module name
	 * @param techReport the tech report
	 * @param resultFiles the result files
	 * @param testSuitePath the test suite path
	 * @throws PhrescoException the phresco exception
	 */
	private void updateCache(String appDirName, String testType, String moduleName, String techReport,
			File[] resultFiles, String testSuitePath) throws PhrescoException {
		Map<String, NodeList> mapTestSuites = new HashMap<String, NodeList>(10);
		for (File resultFile : resultFiles) {
			Document doc = getDocument(resultFile);
			NodeList testSuiteNodeList = evaluateTestSuite(doc, testSuitePath);
			if (testSuiteNodeList.getLength() > 0) {
				List<TestSuite> allTestSuites = getTestSuite(testSuiteNodeList);
				for (TestSuite tstSuite : allTestSuites) {
					mapTestSuites.put(tstSuite.getName(), testSuiteNodeList);
				}
			}
		}
		String testSuitesKey = appDirName + testType + moduleName + techReport;
		testSuiteMap.put(testSuitesKey, mapTestSuites);
	}
	
	/**
	 * Gets the document.
	 *
	 * @param resultFile the result file
	 * @return the document
	 * @throws PhrescoException the phresco exception
	 */
	private Document getDocument(File resultFile) throws PhrescoException {
		InputStream fis = null;
		DocumentBuilder builder = null;
		try {
			fis = new FileInputStream(resultFile);
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(false);
			builder = domFactory.newDocumentBuilder();
			Document doc = builder.parse(fis);
			return doc;
		} catch (FileNotFoundException e) {
			throw new PhrescoException(e);
		} catch (ParserConfigurationException e) {
			throw new PhrescoException(e);
		} catch (SAXException e) {
			throw new PhrescoException(e);
		} catch (IOException e) {
			throw new PhrescoException(e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	/**
	 * Evaluate test suite.
	 *
	 * @param doc the doc
	 * @param testSuitePath the test suite path
	 * @return the node list
	 * @throws PhrescoException the phresco exception
	 */
	private NodeList evaluateTestSuite(Document doc, String testSuitePath) throws PhrescoException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression;
		NodeList testSuiteNode = null;
		try {
			xPathExpression = xpath.compile(testSuitePath);
			testSuiteNode = (NodeList) xPathExpression.evaluate(doc, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			throw new PhrescoException(e);
		}
		return testSuiteNode;
	}
	
	/**
	 * Gets the test suite.
	 *
	 * @param nodelist the nodelist
	 * @return the test suite
	 * @throws PhrescoException the phresco exception
	 */
	private List<TestSuite> getTestSuite(NodeList nodelist) throws PhrescoException {
		List<TestSuite> allTestSuites = new ArrayList<TestSuite>(2);
		TestSuite tstSuite = null;
		for (int i = 0; i < nodelist.getLength(); i++) {
			tstSuite = new TestSuite();
			Node node = nodelist.item(i);
			NamedNodeMap nameNodeMap = node.getAttributes();

			for (int k = 0; k < nameNodeMap.getLength(); k++) {
				Node attribute = nameNodeMap.item(k);
				String attributeName = attribute.getNodeName();
				String attributeValue = attribute.getNodeValue();
				if (FrameworkConstants.ATTR_ASSERTIONS.equals(attributeName)) {
					tstSuite.setAssertions(attributeValue);
				} else if (FrameworkConstants.ATTR_ERRORS.equals(attributeName)) {
					tstSuite.setErrors(Float.parseFloat(attributeValue));
				} else if (FrameworkConstants.ATTR_FAILURES.equals(attributeName)) {
					tstSuite.setFailures(Float.parseFloat(attributeValue));
				} else if (FrameworkConstants.ATTR_FILE.equals(attributeName)) {
					tstSuite.setFile(attributeValue);
				} else if (FrameworkConstants.ATTR_NAME.equals(attributeName)) {
					tstSuite.setName(attributeValue);
				} else if (FrameworkConstants.ATTR_TESTS.equals(attributeName)) {
					tstSuite.setTests(Float.parseFloat(attributeValue));
				} else if (FrameworkConstants.ATTR_TIME.equals(attributeName)) {
					tstSuite.setTime(attributeValue);
				}
			}
			allTestSuites.add(tstSuite);
		}
		return allTestSuites;
	}
	
	/**
	 * Gets the test cases.
	 *
	 * @param appDirName the app dir name
	 * @param testSuites the test suites
	 * @param testSuitePath the test suite path
	 * @param testCasePath the test case path
	 * @return the test cases
	 * @throws PhrescoException the phresco exception
	 */
	private List<TestCase> getTestCases(String appDirName, NodeList testSuites, String testSuitePath, String testCasePath) throws PhrescoException {
		InputStream fileInputStream = null;
		try {
			StringBuilder sb = new StringBuilder(); 
			sb.append(testSuitePath);
			sb.append(FrameworkConstants.NAME_FILTER_PREFIX);
			sb.append(getTestSuite());
			sb.append(FrameworkConstants.NAME_FILTER_SUFIX);
			sb.append(testCasePath);

			XPath xpath = XPathFactory.newInstance().newXPath();
			NodeList nodeList = (NodeList) xpath.evaluate(sb.toString(), testSuites.item(0).getParentNode(),
					XPathConstants.NODESET);
			// For tehnologies like php and drupal duoe to plugin change xml
			// testcase path modified
			if (nodeList.getLength() == 0) {
				StringBuilder sbMulti = new StringBuilder();
				sbMulti.append(testSuitePath);
				sbMulti.append(FrameworkConstants.NAME_FILTER_PREFIX);
				sbMulti.append(getTestSuite());
				sbMulti.append(FrameworkConstants.NAME_FILTER_SUFIX);
				sbMulti.append(FrameworkConstants.XPATH_TESTSUTE_TESTCASE);
				nodeList = (NodeList) xpath.evaluate(sbMulti.toString(), testSuites.item(0).getParentNode(),
						XPathConstants.NODESET);
			}

			// For technology sharepoint
			if (nodeList.getLength() == 0) {
				StringBuilder sbMulti = new StringBuilder(); 
				sbMulti.append(FrameworkConstants.XPATH_MULTIPLE_TESTSUITE);
				sbMulti.append(FrameworkConstants.NAME_FILTER_PREFIX);
				sbMulti.append(getTestSuite());
				sbMulti.append(FrameworkConstants.NAME_FILTER_SUFIX);
				sbMulti.append(testCasePath);
				nodeList = (NodeList) xpath.evaluate(sbMulti.toString(), testSuites.item(0).getParentNode(),
						XPathConstants.NODESET);
			}

			List<TestCase> testCases = new ArrayList<TestCase>();

			StringBuilder screenShotDir = new StringBuilder(Utility.getProjectHome() + appDirName);
			screenShotDir.append(File.separator);
			String sceenShotDir = getSceenShotDir(appDirName);
			if (StringUtils.isEmpty(sceenShotDir)) {
				screenShotDir.append(getFunctionalTestReportDir(appDirName));
				screenShotDir.append(File.separator);
				screenShotDir.append(FrameworkConstants.SCREENSHOT_DIR);
			} else {
				screenShotDir.append(sceenShotDir);
			}
			screenShotDir.append(File.separator);

			int failureTestCases = 0;
			int errorTestCases = 0;
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				NodeList childNodes = node.getChildNodes();
				NamedNodeMap nameNodeMap = node.getAttributes();
				TestCase testCase = new TestCase();
				for (int k = 0; k < nameNodeMap.getLength(); k++) {
					Node attribute = nameNodeMap.item(k);
					String attributeName = attribute.getNodeName();
					String attributeValue = attribute.getNodeValue();
					if (FrameworkConstants.ATTR_NAME.equals(attributeName)) {
						testCase.setName(attributeValue);
					} else if (FrameworkConstants.ATTR_CLASS.equals(attributeName) || FrameworkConstants.ATTR_CLASSNAME.equals(attributeName)) {
						testCase.setTestClass(attributeValue);
					} else if (FrameworkConstants.ATTR_FILE.equals(attributeName)) {
						testCase.setFile(attributeValue);
					} else if (FrameworkConstants.ATTR_LINE.equals(attributeName)) {
						testCase.setLine(Float.parseFloat(attributeValue));
					} else if (FrameworkConstants.ATTR_ASSERTIONS.equals(attributeName)) {
						testCase.setAssertions(Float.parseFloat(attributeValue));
					} else if (FrameworkConstants.ATTR_TIME.equals(attributeName)) {
						testCase.setTime(attributeValue);
					}
				}

				if (childNodes != null && childNodes.getLength() > 0) {
					for (int j = 0; j < childNodes.getLength(); j++) {
						Node childNode = childNodes.item(j);
						if (FrameworkConstants.ELEMENT_FAILURE.equals(childNode.getNodeName())) {
							failureTestCases++;
							TestCaseFailure failure = getFailure(childNode);
							if (failure != null) {
								File file = new File(screenShotDir.toString() + testCase.getName()
										+ FrameworkConstants.DOT + FrameworkConstants.IMG_PNG_TYPE);
								if (file.exists()) {
									failure.setHasFailureImg(true);
									fileInputStream = new FileInputStream(file);
									if (fileInputStream != null) {
										byte[] imgByte = null;
										imgByte = IOUtils.toByteArray(fileInputStream);
										byte[] encodedImage = Base64.encodeBase64(imgByte);
										failure.setScreenshotPath(new String(encodedImage));
									}
								}
								testCase.setTestCaseFailure(failure);
							}
						}

						if (FrameworkConstants.ELEMENT_ERROR.equals(childNode.getNodeName())) {
							errorTestCases++;
							TestCaseError error = getError(childNode);
							if (error != null) {
								File file = new File(screenShotDir.toString() + testCase.getName()
										+ FrameworkConstants.DOT + FrameworkConstants.IMG_PNG_TYPE);
								if (file.exists()) {
									error.setHasErrorImg(true);
									fileInputStream = new FileInputStream(file);
									if (fileInputStream != null) {
										byte[] imgByte = null;
										imgByte = IOUtils.toByteArray(fileInputStream);
										byte[] encodedImage = Base64.encodeBase64(imgByte);
										error.setScreenshotPath(new String(encodedImage));
									}
								}
								testCase.setTestCaseError(error);
							}
						}
					}
				}
				testCases.add(testCase);
			}
			setSetFailureTestCases(failureTestCases);
			setErrorTestCases(errorTestCases);
			setNodeLength(nodeList.getLength());
			return testCases;
		} catch (PhrescoException e) {
			throw e;
		} catch (XPathExpressionException e) {
			throw new PhrescoException(e);
		} catch (IOException e) {
			throw new PhrescoException(e);
		} finally {
			try {
				if (fileInputStream != null) {
					fileInputStream.close();
				}
			} catch (IOException e) {

			}
		}
	}
	
	/**
	 * Gets the sceen shot dir.
	 *
	 * @param appDirName the app dir name
	 * @return the sceen shot dir
	 * @throws PhrescoException the phresco exception
	 */
	private String getSceenShotDir(String appDirName) throws PhrescoException {
		try {
			return PhrescoUtil.getPomProcessor(appDirName).getProperty(
					Constants.POM_PROP_KEY_SCREENSHOT_DIR);
		} catch (PhrescoPomException e) {
			throw new PhrescoException(e);
		}
	}
	
	/**
	 * Gets the failure.
	 *
	 * @param failureNode the failure node
	 * @return the failure
	 */
	private static TestCaseFailure getFailure(Node failureNode) {
		TestCaseFailure failure = new TestCaseFailure();
		failure.setDescription(failureNode.getTextContent());
		failure.setFailureType(FrameworkConstants.REQ_TITLE_EXCEPTION);
		NamedNodeMap nameNodeMap = failureNode.getAttributes();

		if (nameNodeMap != null && nameNodeMap.getLength() > 0) {
			for (int k = 0; k < nameNodeMap.getLength(); k++) {
				Node attribute = nameNodeMap.item(k);
				String attributeName = attribute.getNodeName();
				String attributeValue = attribute.getNodeValue();

				if (FrameworkConstants.ATTR_TYPE.equals(attributeName)) {
					failure.setFailureType(attributeValue);
				}
			}
		}
		return failure;
	}
	
	/**
	 * Gets the error.
	 *
	 * @param errorNode the error node
	 * @return the error
	 */
	private static TestCaseError getError(Node errorNode) {
		TestCaseError tcError = new TestCaseError();
		tcError.setDescription(errorNode.getTextContent());
		tcError.setErrorType(FrameworkConstants.REQ_TITLE_ERROR);
		NamedNodeMap nameNodeMap = errorNode.getAttributes();
		if (nameNodeMap != null && nameNodeMap.getLength() > 0) {
			for (int k = 0; k < nameNodeMap.getLength(); k++) {
				Node attribute = nameNodeMap.item(k);
				String attributeName = attribute.getNodeName();
				String attributeValue = attribute.getNodeValue();

				if (FrameworkConstants.ATTR_TYPE.equals(attributeName)) {
					tcError.setErrorType(attributeValue);
				}
			}
		}
		return tcError;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void sortResultFile(File[] children) {
		Arrays.sort( children, new Comparator() {
        	public int compare(Object o1, Object o2) {
            	if (((File)o1).lastModified() > ((File)o2).lastModified()) {
            		return -1;
            	} else if (((File)o1).lastModified() < ((File)o2).lastModified()) {
            		return +1;
            	} else {
            		return 0;
            	}
        	}
        });
	}
	
	public String getTestSuite() {
		return testSuite;
	}

	public void setTestSuite(String testSuite) {
		this.testSuite = testSuite;
	}
	
	public int getSetFailureTestCases() {
		return setFailureTestCases;
	}

	public void setSetFailureTestCases(int setFailureTestCases) {
		this.setFailureTestCases = setFailureTestCases;
	}

	public int getErrorTestCases() {
		return errorTestCases;
	}

	public void setErrorTestCases(int errorTestCases) {
		this.errorTestCases = errorTestCases;
	}

	public int getNodeLength() {
		return nodeLength;
	}

	public void setNodeLength(int nodeLength) {
		this.nodeLength = nodeLength;
	}

}
