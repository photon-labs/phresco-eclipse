package com.photon.phresco.ui.phrescoexplorer;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;

import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.model.ProjectInfo;
import com.photon.phresco.commons.util.ConsoleViewManager;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.commons.util.ProjectManager;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.MavenCommands.MavenCommand;
import com.photon.phresco.plugins.util.MojoProcessor;
import com.photon.phresco.ui.model.ActionType;

public class ExecuteAction implements PhrescoConstants  {
	
	File configurationFile = null;
	String goal; 
	ActionType type;
	String ConsoleName;

	public ExecuteAction(File configFile, String goal, ActionType type, String consoleName) {
		this.configurationFile = configFile;
		this.goal = goal;
		this.type = type;
		this.ConsoleName = consoleName;
	}

	public void execute() {
		 try {
			 List<String> buildArgCmds = null;
			 if (configurationFile.exists() && configurationFile.length() > 0) {
				 MojoProcessor processor = new MojoProcessor(configurationFile);
				 List<Parameter> parameters = processor.getConfiguration(goal).getParameters().getParameter();
				 buildArgCmds = getMavenArgCommands(parameters);
			 }
			 ProjectManager manager = new ProjectManager();
			 ProjectInfo info = PhrescoUtil.getProjectInfo();

			 ApplicationInfo applicationInfo = info.getAppInfos().get(0);
			 String pomFileName = PhrescoUtil.getPomFileName(applicationInfo);

			 if(!POM_FILENAME.equals(pomFileName)) {
				 buildArgCmds.add(pomFileName);
			 }
			 String workingDirectory = PhrescoUtil.getApplicationHome().toString();
			 manager.getApplicationProcessor().preBuild(applicationInfo);
			 BufferedReader performAction = performAction(info, type, buildArgCmds, workingDirectory);
			 
			 ConsoleViewManager.getDefault(ConsoleName).println(performAction);

		 } catch (PhrescoException e) {
			 e.printStackTrace();
		 }
	 }


	 private BufferedReader performAction(ProjectInfo projectInfo, ActionType build, List<String> mavenArgCommands, String workingDirectory) throws PhrescoException {
		 StringBuilder command = buildMavenCommand(build, mavenArgCommands);
		 return executeMavenCommand(projectInfo, build, command, workingDirectory);
	 }

	 private StringBuilder buildMavenCommand(ActionType actionType, List<String> mavenArgCommands) {
		 StringBuilder builder = new StringBuilder(MAVEN_COMMAND);
		 builder.append(STR_SPACE);
		 builder.append(actionType.getActionType());

		 if (CollectionUtils.isNotEmpty(mavenArgCommands)) {
			 for (String mavenArgCommand : mavenArgCommands) {
				 builder.append(STR_SPACE);
				 builder.append(mavenArgCommand);
			 }
		 }
		 return builder;
	 }

	 private BufferedReader executeMavenCommand(ProjectInfo projectInfo, ActionType action, StringBuilder command, String workingDirectory) throws PhrescoException {
		 Commandline cl = new Commandline(command.toString());
		 if (StringUtils.isNotEmpty(workingDirectory)) {
			 cl.setWorkingDirectory(workingDirectory);
		 } 
		 try {
			 Process process = cl.execute();
			 return new BufferedReader(new InputStreamReader(process.getInputStream()));
		 } catch (CommandLineException e) {
			 throw new PhrescoException(e);
		 }
	 }


	 private List<String> getMavenArgCommands(List<Parameter> parameters) {
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
	
}
