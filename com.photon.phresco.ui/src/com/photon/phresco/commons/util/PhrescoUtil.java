package com.photon.phresco.commons.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.model.BuildInfo;
import com.photon.phresco.commons.model.ProjectInfo;
import com.photon.phresco.commons.model.User;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.MavenCommands.MavenCommand;
import com.photon.phresco.service.client.api.ServiceContext;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.service.client.factory.ServiceClientFactory;
import com.photon.phresco.service.client.impl.ServiceManagerImpl;
import com.photon.phresco.ui.model.BaseAction;
import com.photon.phresco.util.Constants;
import com.photon.phresco.util.Utility;
import com.phresco.pom.exception.PhrescoPomException;
import com.phresco.pom.model.Model.Modules;
import com.phresco.pom.util.PomProcessor;


public class PhrescoUtil implements PhrescoConstants {

	static boolean isLoggedIn = false;
	static String loggedInUserId;
	public static final Map<String, ServiceManager> CONTEXT_MANAGER_MAP = new HashMap<String, ServiceManager>();
	
	public static boolean doLogin(String userName, String password, String serviceURL) throws PhrescoException {
		
		ServiceContext context = new ServiceContext();
		
		if (serviceURL != null) {
			context.put(SERVICE_URL, serviceURL);
		} else {
			context.put(SERVICE_URL, DEFAULT_SERVICE_URL);
		}
		
		context.put(SERVICE_USERNAME, userName);
		context.put(SERVICE_PASSWORD, password);
		
		try {
			ServiceManager serviceManager = new ServiceManagerImpl(context);
			CONTEXT_MANAGER_MAP.put(userName, serviceManager);
			isLoggedIn = true;
		} catch(Exception e) {
			isLoggedIn = false;
			e.printStackTrace();
		}
		try {
			ServiceManager serviceManager = ServiceClientFactory.getServiceManager(context);
			User userInfo = serviceManager.getUserInfo();
			loggedInUserId = userInfo.getId();
			isLoggedIn = true;
		} catch(Exception e) {
			isLoggedIn = false;
			throw new PhrescoException(e);
		}
		return isLoggedIn;
	}

	public static String getServiceURL(String jarPath) {

		try {
			////pass your jar file name which included manifest file
			JarFile jf = new JarFile(jarPath);
			////getting manifest file from jar file
			Manifest m = jf.getManifest();
			////getting all attribute from manifest file
			Attributes attr = m.getMainAttributes();
			////taking values from manifest file.
			////existing field
			String serviceURL = attr.getValue("serviceURL"); // Service URL	serviceURL
			return serviceURL;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static boolean isLoggedIn() {
		return isLoggedIn;
	}
	
	public static ServiceManager getServiceManager(String userId) {
		return CONTEXT_MANAGER_MAP.get(userId);
	}
	
	public static ServiceManager getServiceManager() {
		return CONTEXT_MANAGER_MAP.get(loggedInUserId);
	}
	
	

	public static String getApplicationHome() {
		IPath location = null ;
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getSelection();
		if (selection instanceof IStructuredSelection) {
			Object[] selectedObjects = ((IStructuredSelection)selection).toArray();
			for (Object object : selectedObjects) {
				if(object instanceof IProject) {
					IProject iProject = (IProject) object;
					location = iProject.getLocation();
				} 
				if(object instanceof IJavaProject) {
					IJavaProject project = (IJavaProject) object;
					project.getJavaModel().getPath();
					location = project.getProject().getLocation();
				}
			}
		}
		File path = new File(location.toOSString());
		return path.getPath();
	}
	
	public static String getProjectHome() {
		IPath location = null ;
		String workingPath = "";
		ISelection selection = null;
		IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if(workbenchWindow != null) {
			IWorkbenchPage activePage = workbenchWindow.getActivePage();
			selection = activePage.getSelection();
		}
		if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
			Object[] selectedObjects = ((IStructuredSelection)selection).toArray();
			for (Object object : selectedObjects) {
				if(object instanceof IProject) {
					IProject iProject = (IProject) object;
					location = iProject.getLocation();
				} 
				if(object instanceof IJavaProject) {
					IJavaProject project = (IJavaProject) object;
					project.getJavaModel().getPath();
					location = project.getProject().getLocation();
				}
			} 
			String dir = location.toOSString();
			workingPath = StringUtils.removeEnd(dir, location.lastSegment());
		} else {
			String workingDir = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString() + File.separatorChar + "projects";
			File filePath = new File(workingDir);
			if(!filePath.isDirectory()) {
				filePath.mkdir();
			}
			workingPath =  filePath.getPath() + File.separatorChar;
		}
		return workingPath;
	}

	public static String getProjectName() {
		File projectHome = new File(getApplicationHome());
		String fileName = projectHome.getName();
		return fileName;
	}

	public static File getBuildInfoPath() {
		File buildInfoPath = new File(getApplicationHome() + File.separator + DO_NOT_CHECKIN_DIR + File.separator + BUILD + File.separator + BUILD_INFO);
		return buildInfoPath;
	}
	
	public static List<BuildInfo> getBuildInfos() throws PhrescoException {
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(getBuildInfoPath()));
			Gson gson = new Gson();
			Type type = new TypeToken<List<BuildInfo>>(){}.getType();

			List<BuildInfo> buildInfos = gson.fromJson(bufferedReader, type);
			bufferedReader.close();
			return buildInfos;
		} catch (JsonIOException e) {
			throw new PhrescoException(e);
		} catch (JsonSyntaxException e) {
			throw new PhrescoException(e);
		} catch (FileNotFoundException e) {
			throw new PhrescoException(e);
		} catch (IOException e) {
			throw new PhrescoException(e);
		}
	}

	public static File getPackageInfoConfigurationPath() {
		File packageconfigPath = new File(getApplicationHome() + File.separator + DOT_PHRESCO_FOLDER + File.separator + PACKAGE_INFO_FILE);
		return packageconfigPath;
	}
	
	public static File getDeployInfoConfigurationPath() {
		File deployConfigPath = new File(getApplicationHome() + File.separator + DOT_PHRESCO_FOLDER + File.separator + DEPLOY_INFO_FILE);
		return deployConfigPath;
	}
	
	public static File getValidateCodeInfoConfigurationPath() {
		File validateCodeconfigPath = new File(getApplicationHome() + File.separator + DOT_PHRESCO_FOLDER + File.separator + VALIDATECODE_INFO_FILE );
		return validateCodeconfigPath;
	}
	
	public static File getUnitTestInfoConfigurationPath() {
		File unitTestconfigPath = new File(getApplicationHome() + File.separator + DOT_PHRESCO_FOLDER + File.separator + UNITTEST_INFO_FILE );
		return unitTestconfigPath;
	}
	
	public static ProjectInfo getProjectInfo() throws PhrescoException {
		try {
			File projectFilePath = new File(getApplicationHome() + File.separator + DOT_PHRESCO_FOLDER + File.separator + PROJECT_INFO);
			FileReader reader = new FileReader(projectFilePath);
			Gson  gson = new Gson();
			Type type = new TypeToken<ProjectInfo>() {}.getType();
			ProjectInfo info = gson.fromJson(reader, type);
			return info;
		} catch (FileNotFoundException e) {
			throw new PhrescoException(e);
		}
	}
	
	public static ApplicationInfo getApplicationInfo() throws PhrescoException {
		return getProjectInfo().getAppInfos().get(0);
	}
	
	public static File getConfigurationFile() throws PhrescoException {
		File envconfigFile = new File(getApplicationHome() + File.separator + DOT_PHRESCO_FOLDER + File.separator + ENVIRONMENT_CONFIG_FILE);
		return envconfigFile;
	}
	

	public static String getPomFileName(ApplicationInfo appInfo) {
		File pomFile = new File(getProjectHome()+ File.separator + appInfo.getAppDirName() + File.separator + appInfo.getPomFile());
		if(pomFile.exists()) {
			return appInfo.getPomFile();
		}
		return POM_FILENAME;
	}
	
	public static String getCustomerId() throws PhrescoException {
		return getProjectInfo().getCustomerIds().get(0);
	}
	
	public static String getUserId() {
		BaseAction  action = new BaseAction();
		return action.getUserId();
	}

	public static String getTechId() throws PhrescoException {
		return getApplicationInfo().getTechInfo().getId();
	}
	public static String findPlatform() {
		String osName = System.getProperty(OS_NAME);
		String osBit = System.getProperty(OS_ARCH);
		if (osName.contains(Constants.WINDOWS)) {
			osName = Constants.WINDOWS;
		} else if (osName.contains(LINUX)) {
			osName = LINUX;
		} else if (osName.contains(MAC)) {
			osName = MAC;
		} else if (osName.contains(SERVER)) {
			osName = SERVER;
		} else if (osName.contains(WINDOWS_7)) {
			osName = WINDOWS_7.replace(" ", "");
		}
		if (osBit.contains(BIT_64)) {
			osBit = BIT_64;
		} else {
			osBit = BIT_86;
		}
		return osName.concat(osBit);
	}
	
	public static PomProcessor getPomProcessor(String appDirName) throws PhrescoException {
		String applicationHome = getProjectHome() + File.separatorChar + appDirName;
		try {
			return new PomProcessor(new File(applicationHome + File.separatorChar + POM_FILENAME));
		} catch (PhrescoPomException e) {
			throw new PhrescoException(e);
		}
	}
	
	public static PomProcessor getPomProcessor() throws PhrescoException {
		String applicationHome = getApplicationHome();
		try {
			return new PomProcessor(new File(applicationHome + File.separatorChar + POM_FILENAME));
		} catch (PhrescoPomException e) {
			throw new PhrescoException(e);
		}
	}
	
	public static String getSqlFilePath(String appDirName) throws PhrescoException, PhrescoPomException {
		String sqlPath = getPomProcessor().getProperty(PHRESCO_SQL_PATH);
		return getApplicationHome() + File.separatorChar + sqlPath;
	}
	
	public static String getArchiveHome() {
        String phrescoHome = getProjectHome();
        StringBuilder builder = new StringBuilder(phrescoHome);
        builder.append(File.separator);
        builder.append(Constants.ARCHIVE_HOME);
        builder.append(File.separator);
        FileUtils.mkdir(builder.toString());
        return builder.toString();
    }
	
	public static List<String> getMavenArgCommands(List<Parameter> parameters) {
		List<String> buildArgCmds = new ArrayList<String>();	
		if(CollectionUtils.isEmpty(parameters)) {
			return buildArgCmds;
		}
		for (Parameter parameter : parameters) {
			if (parameter.getPluginParameter()!= null && FRAMEWORK.equalsIgnoreCase(parameter.getPluginParameter())) {
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
	
	public static List<String> getProjectModules(String appDirName) throws PhrescoException {
    	try {
            PomProcessor processor = getPomProcessor(appDirName);
    		Modules pomModule = processor.getPomModule();
    		if (pomModule != null) {
    			return pomModule.getModule();
    		}
    	} catch (PhrescoPomException e) {
    		 throw new PhrescoException(e);
    	}
    	
    	return null;
    }
	
	public static String getUnitTestReportDir(ApplicationInfo appInfo) throws PhrescoPomException, PhrescoException {
        return getPomProcessor().getProperty(Constants.POM_PROP_KEY_UNITTEST_RPT_DIR);
    }
	
	public static String getUnitTestReportOptions(ApplicationInfo appinfo) throws PhrescoException, PhrescoPomException {
		return getPomProcessor().getProperty(Constants.PHRESCO_UNIT_TEST);
	}
	
	public static String getUnitTestReportDir(ApplicationInfo appInfo, String option) throws PhrescoPomException, PhrescoException {
        return getPomProcessor().getProperty(Constants.POM_PROP_KEY_UNITTEST_RPT_DIR_START + option + Constants.POM_PROP_KEY_UNITTEST_RPT_DIR_END);
    }
	
	public static String getFunctionalTestReportDir(ApplicationInfo appInfo) throws PhrescoPomException, PhrescoException {
        return getPomProcessor().getProperty(Constants.POM_PROP_KEY_FUNCTEST_RPT_DIR);
    }
	
	public static String getComponentTestReportDir(ApplicationInfo appInfo) throws PhrescoPomException, PhrescoException {
        return getPomProcessor().getProperty(Constants.POM_PROP_KEY_COMPONENTTEST_RPT_DIR);
    }
	
	public static String isIphoneTagExists(ApplicationInfo appinfo) throws PhrescoException, PhrescoPomException {
        return getPomProcessor().getProperty(Constants.PHRESCO_CODE_VALIDATE_REPORT);
    }
	
	public static String getLoadTestReportDir(ApplicationInfo appinfo) throws PhrescoPomException, PhrescoException {
    	return getPomProcessor().getProperty(Constants.POM_PROP_KEY_LOADTEST_RPT_DIR);
    }
	
	public static String getPerformanceTestReportDir(ApplicationInfo appinfo) throws PhrescoException, PhrescoPomException {
        return getPomProcessor().getProperty(Constants.POM_PROP_KEY_PERFORMANCETEST_RPT_DIR);
    }
	
	public static String getPerformanceResultFileExtension(String appDirName) throws PhrescoException, PhrescoPomException {
		return getPomProcessor().getProperty(Constants.POM_PROP_KEY_PERFORMANCETEST_RESULT_EXTENSION);
	}
	
	public static String getLoadResultFileExtension(ApplicationInfo appinfo) throws PhrescoException, PhrescoPomException {
        return getPomProcessor().getProperty(Constants.POM_PROP_KEY_LOADTEST_RESULT_EXTENSION);
    }
	
	public static String getSeleniumToolType(ApplicationInfo appInfo) throws PhrescoException, PhrescoPomException {
        return getPomProcessor().getProperty(Constants.POM_PROP_KEY_FUNCTEST_SELENIUM_TOOL);
    }
	
	public static String getSonarUrl() throws PhrescoException, PhrescoPomException {
        return SONAR_HOST_URL;
    }
	
	public static String getPhrescoPluginInfoFilePath(String goal, String phase) throws PhrescoException {
		StringBuilder sb = new StringBuilder(getApplicationHome());
		sb.append(File.separator);
		sb.append(FOLDER_DOT_PHRESCO);
		sb.append(File.separator);
		sb.append(LBL_PHRESCO);
		sb.append(HYPHEN);
		// when phase is CI, it have to take ci info file for update dependency
		if (Constants.PHASE_CI.equals(phase)) {
			sb.append(phase);
		} else if (StringUtils.isNotEmpty(goal) && goal.contains(FUNCTIONAL)) {
			sb.append(Constants.PHASE_FUNCTIONAL_TEST);
		} else if (Constants.PHASE_RUNGAINST_SRC_START.equals(goal)|| Constants.PHASE_RUNGAINST_SRC_STOP.equals(goal) ) {
			sb.append(Constants.PHASE_RUNAGAINST_SOURCE);
		} else {
			sb.append(goal);
		}
		sb.append(Constants.INFO_XML);

		return sb.toString();
	}
	
	public static void addM2Repo(String path) throws PhrescoException {
		String command = "mvn -Declipse.workspace=" + path + " eclipse:configure-workspace";
		Utility.executeCommand(command, path);
	}
}
