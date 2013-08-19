package com.photon.phresco.ui.phrescoexplorer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.handlers.HandlerUtil;

import com.google.gson.Gson;
import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.PhrescoDialog;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.model.Customer;
import com.photon.phresco.commons.model.ProjectInfo;
import com.photon.phresco.commons.model.Technology;
import com.photon.phresco.commons.util.ConsoleViewManager;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.commons.util.QualityUtil;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.framework.api.ActionType;
import com.photon.phresco.framework.api.ApplicationManager;
import com.photon.phresco.framework.impl.ApplicationManagerImpl;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.MavenCommands.MavenCommand;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value;
import com.photon.phresco.plugins.util.MojoProcessor;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.ui.resource.Messages;
import com.photon.phresco.util.Constants;
import com.photon.phresco.util.Utility;
import com.phresco.pom.exception.PhrescoPomException;
import com.phresco.pom.model.Model.Modules;
import com.phresco.pom.util.PomProcessor;

public class ReportPage  extends AbstractHandler implements PhrescoConstants {

	private Combo reportTypeCombo;
	private Text pdfReportNameText;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		Shell shell = HandlerUtil.getActiveShell(event);
		final Shell reportDialog = new Shell(shell, SWT.APPLICATION_MODAL |  SWT.DIALOG_TRIM | SWT.MIN | SWT.TITLE);
		reportDialog.setText(Messages.PDF_REPORT_DIALOG_TITLE);
		GridLayout layout = new GridLayout(1, false);
		reportDialog.setLocation(reportDialog.getLocation());
		reportDialog.setLayout(layout);
		reportDialog.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Composite composite = new Composite(reportDialog, SWT.NONE);
		GridLayout compLayout = new GridLayout(2, false);
		composite.setLayout(compLayout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label reportTypeLable = new Label(composite, SWT.NONE);
		reportTypeLable.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		reportTypeLable.setText(Messages.PDF_REPORT_TYPE_LBL);
		
		reportTypeCombo = new Combo(composite, SWT.READ_ONLY | SWT.BORDER);
		String[] reportItems = {Messages.PDF_REPORT_DETAILED, Messages.PDF_REPORT_OVERALL};
		reportTypeCombo.setItems(reportItems);
		reportTypeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		reportTypeCombo.select(0);
		
		Label pdfReportNameLabel = new Label(composite, SWT.NONE);
		pdfReportNameLabel.setText(Messages.PDF_REPORT_NAME);
		pdfReportNameLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		pdfReportNameText = new Text(composite, SWT.BORDER);
		pdfReportNameText.setMessage(Messages.PDF_REPORT_NAME);
		pdfReportNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Composite buttonComposite = new Composite(reportDialog, SWT.RIGHT);
		GridLayout buttonLayout = new GridLayout(2, false);
		buttonComposite.setLayout(buttonLayout);
		buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.END, true, true, 1, 1));
		
		Listener generateListener = new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				try {
					boolean reportAvailable = isReportAvailable(reportDialog);
					if(!reportAvailable) {
						return;
					}
					printAsPdf(reportDialog);
					reportDialog.close();
				} catch (PhrescoException e) {
					PhrescoDialog.exceptionDialog(reportDialog, e);
				}
			}
		};
		
		Listener closeListener = new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				reportDialog.close();
			}
		};
		Button generate = new Button(buttonComposite, SWT.PUSH | SWT.CENTER);
		generate.setText(Messages.GENERATE);
		
		generate.addListener(SWT.Selection, generateListener);
		
		Button close = new Button(buttonComposite, SWT.PUSH | SWT.CENTER);
		close.setText(Messages.CANCEL);
		
		close.addListener(SWT.Selection, closeListener);
		
		int x = reportDialog.getSize().x - 400;
		int y = reportDialog.getSize().y - 360;
		
		reportDialog.setSize(x, y);
		Point location = new Point(x, y);
		reportDialog.setLocation(location);
		
		reportDialog.open();
	    return reportDialog;
	}
	
    private List<Parameter> getMojoParameters(MojoProcessor mojo, String goal) throws PhrescoException {
		com.photon.phresco.plugins.model.Mojos.Mojo.Configuration mojoConfiguration = mojo.getConfiguration(goal);
		if (mojoConfiguration != null) {
		    return mojoConfiguration.getParameters().getParameter();
		}
		
		return null;
	}
    
    private boolean isReportAvailable(Shell shell) throws PhrescoException {
    	ApplicationInfo appInfo = PhrescoUtil.getApplicationInfo();
    	boolean isReportAvailable = false;
    	if(getSonarReport()) {
    		isReportAvailable =  getSonarReport();
    	} if(isTestReportAvailable(appInfo)) {
    		isReportAvailable = true;
    	} else {
    		PhrescoDialog.errorDialog(shell, Messages.ERROR, Messages.PDF_REPORT_MSG);
    		isReportAvailable = false;
    	}
    	return isReportAvailable;
    }
    
    private boolean getSonarReport() {
    	boolean isSonarReportAvailable = false;
    	StringBuilder sb = new StringBuilder(PhrescoUtil.getApplicationHome()).append(DO_NOT_CHECKIN_DIR).append(File.separatorChar).append(
				STATIC_ANALYSIS_REPORT);
		File indexPath = new File(sb.toString());
		if (indexPath.exists() && indexPath.isDirectory()) {
			File[] listFiles = indexPath.listFiles();
			for (int i = 0; i < listFiles.length; i++) {
				File file = listFiles[i];
				File htmlFileCheck = new File(file, INDEX_HTML);
				if (htmlFileCheck.exists()) {
					isSonarReportAvailable = true;
				}
			}
		}
		return isSonarReportAvailable;
    }
    
	private boolean isTestReportAvailable(ApplicationInfo appInfo) throws PhrescoException {
		boolean xmlResultsAvailable = false;
		File file = null;
		StringBuilder sb = new StringBuilder(Utility.getProjectHome());
		sb.append(appInfo.getAppDirName());
		try {
			String isIphone = PhrescoUtil.isIphoneTagExists(appInfo);
			// unit xml check
			if (!xmlResultsAvailable) {
				List<String> moduleNames = new ArrayList<String>();
				PomProcessor processor = PhrescoUtil.getPomProcessor();
				Modules pomModules = processor.getPomModule();
				List<String> modules = null;
				// check multimodule or not
				if (pomModules != null) {
					modules = PhrescoUtil.getProjectModules(appInfo.getAppDirName());
					for (String module : modules) {
						if (StringUtils.isNotEmpty(module)) {
							moduleNames.add(module);
						}
					}
					for (String moduleName : moduleNames) {
						String moduleXmlPath = sb.toString() + File.separator + moduleName
								+ PhrescoUtil.getUnitTestReportDir(appInfo);
						file = new File(moduleXmlPath);
						xmlResultsAvailable = xmlFileSearch(file, xmlResultsAvailable);
					}
				} else {
					if (StringUtils.isNotEmpty(isIphone)) {	
						String unitIphoneTechReportDir = PhrescoUtil.getUnitTestReportDir(appInfo);
						file = new File(sb.toString() + unitIphoneTechReportDir);
					} else {
						String unitTechReports = PhrescoUtil.getUnitTestReportOptions(appInfo);
						if (StringUtils.isEmpty(unitTechReports)) {
							file = new File(sb.toString() + PhrescoUtil.getUnitTestReportDir(appInfo));
						} else {
							List<String> unitTestTechs = Arrays.asList(unitTechReports.split(","));
							for (String unitTestTech : unitTestTechs) {
								unitTechReports = PhrescoUtil.getUnitTestReportDir(appInfo, unitTestTech);
								if (StringUtils.isNotEmpty(unitTechReports)) {
									file = new File(sb.toString() + unitTechReports);
									xmlResultsAvailable = xmlFileSearch(file, xmlResultsAvailable);
								}
							}
						}
					}
					xmlResultsAvailable = xmlFileSearch(file, xmlResultsAvailable);
				}
			}

			// functional xml check
			if (!xmlResultsAvailable) {
				file = new File(sb.toString() + PhrescoUtil.getFunctionalTestReportDir(appInfo));
				xmlResultsAvailable = xmlFileSearch(file, xmlResultsAvailable);
			}

			// component xml check
			if (!xmlResultsAvailable) {
				String componentDir = PhrescoUtil.getComponentTestReportDir(appInfo);
				if (StringUtils.isNotEmpty(componentDir)) {
					file = new File(sb.toString() + componentDir);
					xmlResultsAvailable = xmlFileSearch(file, xmlResultsAvailable);
				}
			}

			// performance xml check
			if (StringUtils.isEmpty(isIphone)) {
				if (!xmlResultsAvailable) {
					QualityUtil qualityUtil = new QualityUtil();
					MojoProcessor mojo = new MojoProcessor(new File(PhrescoUtil.getPhrescoPluginInfoFilePath(Constants.PHASE_PERFORMANCE_TEST, 
							Constants.PHASE_PERFORMANCE_TEST)));
					List<String> testAgainsts = new ArrayList<String>();
					Parameter testAgainstParameter = mojo.getParameter(Constants.PHASE_PERFORMANCE_TEST, REQ_TEST_AGAINST);
					if (testAgainstParameter != null && TYPE_LIST.equalsIgnoreCase(testAgainstParameter.getType())) {
						List<Value> values = testAgainstParameter.getPossibleValues().getValue();
						for (Value value : values) {
							testAgainsts.add(value.getKey());
						}
					}
					xmlResultsAvailable =qualityUtil.testResultAvail(appInfo.getAppDirName(), testAgainsts, Constants.PHASE_PERFORMANCE_TEST);
				}
			}

			// load xml check
			if (StringUtils.isEmpty(isIphone)) {
				if (!xmlResultsAvailable) {
					 xmlResultsAvailable = loadTestResultAvail(appInfo);
				}
			}
		} catch (PhrescoException e) {
			throw new PhrescoException(e);
		} catch (PhrescoPomException e) {
			throw new PhrescoException(e);
		}
		return xmlResultsAvailable;
	}
	
	public boolean loadTestResultAvail(ApplicationInfo appInfo) throws PhrescoException {
		boolean isResultFileAvailable = false;
		try {
			String baseDir = Utility.getProjectHome() + appInfo.getAppDirName();
			List<String> testResultsTypes = new ArrayList<String>();
			testResultsTypes.add("server");
			testResultsTypes.add("webservice");
			for (String testResultsType : testResultsTypes) {
				StringBuilder sb = new StringBuilder(baseDir.toString());
				String loadReportDir = PhrescoUtil.getLoadTestReportDir(appInfo);
				if (StringUtils.isNotEmpty(loadReportDir) && StringUtils.isNotEmpty(testResultsType)) {
					Pattern p = Pattern.compile("dir_type");
					Matcher matcher = p.matcher(loadReportDir);
					loadReportDir = matcher.replaceAll(testResultsType);
					sb.append(loadReportDir);
				}
				File file = new File(sb.toString());
				File[] children = file.listFiles(new XmlNameFileFilter(FILE_EXTENSION_XML));
				if (!ArrayUtils.isEmpty(children)) {
					isResultFileAvailable = true;
					break;
				}
			}
		} catch (Exception e) {
			throw new PhrescoException(e);
		}

		return isResultFileAvailable;
	}
	
	public boolean performanceTestResultAvail(ApplicationInfo appInfo) throws PhrescoException {
		boolean isResultFileAvailable = false;
		try {
			String baseDir = Utility.getProjectHome() + appInfo.getAppDirName();
			List<String> testResultsTypes = new ArrayList<String>();
			testResultsTypes.add("server");
			testResultsTypes.add("database");
			testResultsTypes.add("webservice");
			for (String testResultsType : testResultsTypes) {
				StringBuilder sb = new StringBuilder(baseDir.toString());
				String performanceReportDir = PhrescoUtil.getPerformanceTestReportDir(appInfo);
				if (StringUtils.isNotEmpty(performanceReportDir) && StringUtils.isNotEmpty(testResultsType)) {
					Pattern p = Pattern.compile("dir_type");
					Matcher matcher = p.matcher(performanceReportDir);
					performanceReportDir = matcher.replaceAll(testResultsType);
					sb.append(performanceReportDir);
				}
				File file = new File(sb.toString());
				String resultExtension = PhrescoUtil.getPerformanceResultFileExtension(appInfo.getAppDirName());
				if (StringUtils.isNotEmpty(resultExtension)) {
					File[] children = file.listFiles(new XmlNameFileFilter(resultExtension));
					if (!ArrayUtils.isEmpty(children)) {
						isResultFileAvailable = true;
						break;
					}
				}
			}
		} catch (Exception e) {
			throw new PhrescoException(e);
		}

		return isResultFileAvailable;
	}

	
	private boolean xmlFileSearch(File file, boolean xmlResultsAvailable) {
		File[] children = file.listFiles(new XmlNameFileFilter(FILE_EXTENSION_XML));
		if (children != null && children.length > 0) {
			xmlResultsAvailable = true;
		}
		return xmlResultsAvailable;
	}
	
	/**
	 * The Class XmlNameFileFilter.
	 */
	public class XmlNameFileFilter implements FilenameFilter {
		
		/** The filter_. */
		private String filter_;

		/**
		 * Instantiates a new xml name file filter.
		 *
		 * @param filter the filter
		 */
		public XmlNameFileFilter(String filter) {
			filter_ = filter;
		}
		
		public boolean accept(File dir, String name) {
			return name.endsWith(filter_);
		}
	}
	
	public void printAsPdf (Shell reportDialog) throws PhrescoException {
		try {
			String userId = PhrescoUtil.getUserId();
			String fromPage = "ALL";
			String pdfName = pdfReportNameText.getText();
			String report = reportTypeCombo.getText();
			String reportDataType = "crisp";
			if("Detailed".equals(report)) {
				reportDataType = "detail";
			}
			ApplicationManager applicationManager = new ApplicationManagerImpl();
			ProjectInfo projectInfo = PhrescoUtil.getProjectInfo();
			String customerId = projectInfo.getCustomerIds().get(0);

			ApplicationInfo applicationInfo = projectInfo.getAppInfos().get(0);
			String techId = applicationInfo.getTechInfo().getId();
			ServiceManager serviceManager = PhrescoUtil.getServiceManager(userId);
			if(serviceManager == null) {
				PhrescoDialog.errorDialog(reportDialog, Messages.WARNING, Messages.PHRESCO_LOGIN_WARNING);
				return;
			}
			Technology technology = serviceManager.getTechnology(techId);
			StringBuilder sb = new StringBuilder(PhrescoUtil.getApplicationHome());
			sb.append(File.separator);
			sb.append(FOLDER_DOT_PHRESCO);
			sb.append(File.separator);
			sb.append(LBL_PHRESCO);
			sb.append(HYPHEN);
			sb.append(Constants.PHASE_PDF_REPORT);
			sb.append(Constants.INFO_XML);
			MojoProcessor mojo = new MojoProcessor(new File(sb.toString()));
			List<Parameter> parameters = getMojoParameters(mojo, Constants.PHASE_PDF_REPORT);
			//String sonarUrl = (String) getReqAttribute(REQ_SONAR_URL);
			if (CollectionUtils.isNotEmpty(parameters)) {
				for (Parameter parameter : parameters) {
					String key = parameter.getKey();
					if (REQ_REPORT_TYPE.equals(key)) {
						parameter.setValue(reportDataType);
					} else if (SONAR_URL.equals(key)) {
	            		parameter.setValue(PhrescoUtil.getSonarUrl());
	            	} else if (LOGO.equals(key)) {
	            		parameter.setValue(getLogoImageString(userId, customerId));
	            	} else if (THEME.equals(key)) {
	            		parameter.setValue(getThemeColorJson(userId, customerId));
	            	} else if (REQ_REPORT_NAME.equals(key)) {
	            		parameter.setValue(pdfName);
	            	} else if (TECHNOLOGY_NAME.equals(key)) {
	            		parameter.setValue(technology.getName());
	            	}
				}
			}
			mojo.save();
			List<String> buildArgCmds = getMavenArgCommands(parameters);
			buildArgCmds.add(HYPHEN_N);
			String workingDirectory = PhrescoUtil.getApplicationHome();
			BufferedInputStream inputStream = applicationManager.performAction(projectInfo, ActionType.PDF_REPORT, buildArgCmds, workingDirectory);
			ConsoleViewManager.getDefault("PDF Report Console").println(new BufferedReader(new InputStreamReader(inputStream)));
		} catch (Exception e) {
			throw new PhrescoException("exception occured in the Print As PDF functionality");
		}
	}
	
	private String getLogoImageString(String username, String customerId) throws PhrescoException {
		String encodeImg = "";
		try {
			InputStream fileInputStream = null;
			fileInputStream = PhrescoUtil.getServiceManager(username).getIcon(customerId);
			if (fileInputStream != null) {
				byte[] imgByte = null;
				imgByte = IOUtils.toByteArray(fileInputStream);
				byte[] encodedImage = Base64.encodeBase64(imgByte);
				encodeImg = new String(encodedImage);
			}
		} catch (Exception e) {
			throw new PhrescoException(e);
		}
		return encodeImg;
	}
	
	protected String getThemeColorJson(String username, String customerId) throws PhrescoException {
		String themeJsonStr = "";
		try {
			Customer customer = PhrescoUtil.getServiceManager(username).getCustomer(customerId);
			if (customer != null) {
				Map<String, String> frameworkTheme = customer.getFrameworkTheme();
				if (frameworkTheme != null) {
					Gson gson = new Gson();
					themeJsonStr = gson.toJson(frameworkTheme);
				}
			}
		} catch (Exception e) {
			throw new PhrescoException(e);
		}
		return themeJsonStr;
	}
	
	protected List<String> getMavenArgCommands(List<Parameter> parameters) throws PhrescoException {
		List<String> buildArgCmds = new ArrayList<String>();	
		if(CollectionUtils.isEmpty(parameters)) {
			return buildArgCmds;
		}
		for (Parameter parameter : parameters) {
			if (parameter.getPluginParameter()!= null && PLUGIN_PARAMETER_FRAMEWORK.equalsIgnoreCase(parameter.getPluginParameter())) {
				List<MavenCommand> mavenCommand = parameter.getMavenCommands().getMavenCommand();
				for (MavenCommand mavenCmd : mavenCommand) {
					if (StringUtils.isNotEmpty(parameter.getValue()) && parameter.getValue().equalsIgnoreCase(mavenCmd.getKey())) {
						buildArgCmds.add(mavenCmd.getValue());
					}
				}
			}
		}
		return buildArgCmds;
	}
}
