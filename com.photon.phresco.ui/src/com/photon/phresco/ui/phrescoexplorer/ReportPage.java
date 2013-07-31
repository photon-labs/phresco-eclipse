package com.photon.phresco.ui.phrescoexplorer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.model.ProjectInfo;
import com.photon.phresco.commons.model.Technology;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.Childs.Child;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.MavenCommands.MavenCommand;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value;
import com.photon.phresco.plugins.util.MojoProcessor;
import com.photon.phresco.ui.model.ActionType;
import com.photon.phresco.util.Constants;
import com.photon.phresco.util.Utility;

public class ReportPage  extends AbstractHandler implements PhrescoConstants {

	private String selectedProjectHome;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
//		selectedProjectHome = PhrescoUtil.getSelectedProjectHome(event);
		/*IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
	    IWorkbenchPage activePage = window.getActivePage();
	    ISelection selection = activePage.getSelection();
	    
		if (selection instanceof IStructuredSelection) { 
			Object[] selectedObjects = ((IStructuredSelection)selection).toArray(); 
			for (Object object : selectedObjects) {
				if (object instanceof IProject) {
					IProject iProject = (IProject) object;
					System.out.println(" Relative Path : " + iProject.getProjectRelativePath());
					System.out.println(" Project Name : " + iProject.getName());
					System.out.println(" project full path : " + iProject.getFullPath());
					System.out.println(" project location : " + iProject.getLocation());
					break;
				}
			}
		}*/
	    return null;

	}
	
	private String getProjectHome() {
		return selectedProjectHome;
	}
	
	private ProjectInfo readProjectInfo() throws PhrescoException {
		try {
			File projectFilePath = new File(getProjectHome() + File.separator + "TestProject" + File.separator + DOT_PHRESCO_FOLDER + File.separator + PROJECT_INFO);
			FileReader reader = new FileReader(projectFilePath);
			Gson  gson = new Gson();
			Type type = new TypeToken<ProjectInfo>() {}.getType();
			ProjectInfo info = gson.fromJson(reader, type);
			return info;
		} catch (FileNotFoundException e) {
			throw new PhrescoException(e);
		}
	}
	
	private File getConfigurationPath() {
		File configPath = new File(getProjectHome() + File.separator + "TestProject" + File.separator + DOT_PHRESCO_FOLDER
				+ File.separator + "phresco-package-info.xml");
		return configPath;
	}
	
    protected List<Parameter> getMojoParameters(MojoProcessor mojo, String goal) throws PhrescoException {
		com.photon.phresco.plugins.model.Mojos.Mojo.Configuration mojoConfiguration = mojo.getConfiguration(goal);
		if (mojoConfiguration != null) {
		    return mojoConfiguration.getParameters().getParameter();
		}
		
		return null;
	}
    
	/*public String generatePDF() {

		try {
			
			ProjectInfo projectInfo = readProjectInfo();
			ApplicationInfo applicationInfo = projectInfo.getAppInfos().get(0);

			Technology technology = PhrescoUtil.getServiceManager().getTechnology(applicationInfo.getTechInfo().getId());
			
			MojoProcessor mojo = new MojoProcessor(getConfigurationPath());
			
			List<Parameter> parameters = getMojoParameters(mojo, PHASE_PDF_REPORT);
			String sonarUrl = (String) getReqAttribute(REQ_SONAR_URL);
			
	        if (CollectionUtils.isNotEmpty(parameters)) {
	            for (Parameter parameter : parameters) {
	            	String key = parameter.getKey();
	            	if (REQ_REPORT_TYPE.equals(key)) {
	            		parameter.setValue(reportDataType);
	            	} else if (REQ_TEST_TYPE.equals(key)) {
	            		if (StringUtils.isEmpty(fromPage)) {
	            			setFromPage(FROMPAGE_ALL);
	            		}
	            		parameter.setValue(getFromPage());
	            	} else if (REQ_SONAR_URL.equals(key)) {
	            		parameter.setValue(sonarUrl);
	            	} else if ("logo".equals(key)) {
	            		parameter.setValue(getLogoImageString());
	            	} else if ("theme".equals(key)) {
	            		parameter.setValue(getThemeColorJson());
	            	} else if ("technologyName".equals(key)) {
	            		parameter.setValue(technology.getName());
	            	}
	            }
	        }
	        mojo.save();
	        
			List<String> buildArgCmds = getMavenArgCommands(parameters);
			buildArgCmds.add(HYPHEN_N);
			String pomFileName = Utility.getPomFileName(applicationInfo);
			if(!Constants.POM_NAME.equals(pomFileName)) {
				buildArgCmds.add(Constants.HYPHEN_F);
				buildArgCmds.add(pomFileName);
			}
			String workingDirectory = getAppDirectoryPath(applicationInfo);
			BufferedReader reader = applicationManager.performAction(projectInfo, ActionType.PDF_REPORT, buildArgCmds, workingDirectory);
			String line;
			line = reader.readLine();
			while (line != null) {
				line = reader.readLine();
				System.out.println("Restart Start Console : " + line);
			}
			setReqAttribute(REQ_APPINFO, applicationInfo);
			setReqAttribute(REQ_FROM_PAGE, getFromPage());
            setReqAttribute(REQ_REPORT_STATUS, getText(SUCCESS_REPORT_STATUS));
        } catch (Exception e) {
        	S_LOGGER.error("Entered into catch block of Quality.printAsPdf()"+ e);
        	if (e.getLocalizedMessage().contains(getText(ERROR_REPORT_MISSISNG_FONT_MSG))) {
        		setReqAttribute(REQ_REPORT_STATUS, getText(ERROR_REPORT_MISSISNG_FONT));
        	} else {
        		setReqAttribute(REQ_REPORT_STATUS, getText(ERROR_REPORT_STATUS));
        	}
        }
        return showGeneratePdfPopup();
    }*/

}
