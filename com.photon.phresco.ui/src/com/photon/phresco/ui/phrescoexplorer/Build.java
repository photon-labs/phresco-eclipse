package com.photon.phresco.ui.phrescoexplorer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.photon.phresco.commons.ConfirmDialog;
import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.model.ProjectInfo;
import com.photon.phresco.commons.util.ConsoleViewManager;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.commons.util.ProjectManager;
import com.photon.phresco.dynamicParameter.DependantParameters;
import com.photon.phresco.dynamicParameter.DynamicPossibleValues;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.MavenCommands.MavenCommand;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value;
import com.photon.phresco.plugins.util.MojoProcessor;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.ui.model.ActionType;
import com.photon.phresco.ui.model.BaseAction;
import com.photon.phresco.ui.resource.Messages;

public class Build extends AbstractHandler implements PhrescoConstants {

	private Button buildButton;
	private Button cancelButton;

	private Shell buildDialog;	
	private Shell generateDialog;
	private Button envSelectionButton;

	@SuppressWarnings("unchecked")
	private static Map<String, Object> map = new HashedMap();

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);

		BaseAction baseAction = new BaseAction();
		ServiceManager serviceManager = PhrescoUtil.getServiceManager(baseAction.getUserId());
		if(serviceManager == null) {
			ConfirmDialog.getConfirmDialog().showConfirm(shell);
			return null;
		}

		final Shell buildDialog = new Shell(shell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);

		generateDialog = createBuildDialog(buildDialog);

		Listener generatePopupListener = new Listener() {
			@Override
			public void handleEvent(Event events) {
				saveCongfiguration();

				BusyIndicator.showWhile(null, new Runnable() {
					public void run() {
						build();
					}
				});

				generateDialog.close();
			}
		};

		Listener generatePopupCancelListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				generateDialog.setVisible(false);
			}
		};

		generateDialog.open();
		buildButton.addListener(SWT.Selection, generatePopupListener);
		cancelButton.addListener(SWT.Selection, generatePopupCancelListener);

		return null;
	}


	/**
	 * @wbp.parser.entryPoint
	 */
	 public Shell createBuildDialog(Shell dialog) {
		 buildDialog = new Shell(dialog, SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.MAX | SWT.RESIZE);

		 buildDialog.setText(BUILD);
		 buildDialog.setLocation(385, 130);
		 buildDialog.setSize(451,188);

		 try {
			 MojoProcessor processor = new MojoProcessor(PhrescoUtil.getPackageInfoConfigurationPath());
			 Configuration configuration = processor.getConfiguration(PACKAGE_GOAL);
			 List<Parameter> parameters = configuration.getParameters().getParameter();

			 ApplicationInfo applicationInfo = PhrescoUtil.getProjectInfo().getAppInfos().get(0);
			 DynamicPossibleValues possibleValues = new DynamicPossibleValues();
			 Map<String, DependantParameters> watcherMap = new HashMap<String, DependantParameters>();
			 Map<String, Object> maps = possibleValues.setPossibleValuesInReq(processor, applicationInfo, parameters, watcherMap, PACKAGE_GOAL);

			 for (Parameter parameter : parameters) {
				 if (parameter.getType().equalsIgnoreCase(DYNAMIC_PARAMETER)) {
					 int yaxis = 0;
					 String key = null;
					 Label Logs = new Label(buildDialog, SWT.LEFT);
					 Logs.setText(parameter.getName().getValue().get(0).getValue());
					 Logs.setBounds(24, 40, 80, 23);

					 Group group = new Group(buildDialog, SWT.SHADOW_IN);
					 group.setText(parameter.getName().getValue().get(0).getValue());
					 group.setLocation(146, 26);

					 final List<String> buttons = new ArrayList<String>();
					 Set<Entry<String,Object>> entrySet = maps.entrySet();

					 for (Entry<String, Object> entry : entrySet) {
						 key = entry.getKey();
						 if (key.equalsIgnoreCase(WATCHER_MAP)) {
							 continue;
						 }
						 List<Value> values = (List<Value>) entry.getValue();
						 if (CollectionUtils.isNotEmpty(values)) {
							 for (Value value : values) {
								 envSelectionButton = new Button(group, SWT.CHECK);
								 envSelectionButton.setText(value.getValue());
								 envSelectionButton.setLocation(20, 20+yaxis);
								 yaxis+=15;
								 envSelectionButton.addSelectionListener(new SelectionAdapter() {
									 @Override
									 public void widgetSelected(SelectionEvent e) {
										 Button button = (Button) e.widget;
										 boolean enabled = button.getSelection();
										 if (enabled) {
											 buttons.add(button.getText());
										 } else {
											 buttons.remove(button.getText());
										 }
									 }
								 });
								 envSelectionButton.pack();
							 }
						 }
					 }
					 map.put(key, buttons);
					 group.pack();
				 } 
			 }

			 buildButton = new Button(buildDialog, SWT.PUSH);
			 buildButton.setLocation(279, 121);
			 buildButton.setText("Build");
			 buildButton.setSize(74, 23);

			 cancelButton = new Button(buildDialog, SWT.NONE);
			 cancelButton.setBounds(359, 121, 74, 23);
			 cancelButton.setText("Cancel");

		 } catch (PhrescoException e) {
			 e.printStackTrace();
		 }
		 return buildDialog;
	 }

	 public void build() {
		 try {
			 MojoProcessor processor = new MojoProcessor(PhrescoUtil.getPackageInfoConfigurationPath());
			 List<Parameter> parameters =processor.getConfiguration(PACKAGE_GOAL).getParameters().getParameter();
			 List<String> buildArgCmds = getMavenArgCommands(parameters);
			 ProjectManager manager = new ProjectManager();
			 ProjectInfo info = PhrescoUtil.getProjectInfo();

			 ApplicationInfo applicationInfo = info.getAppInfos().get(0);
			 String pomFileName = PhrescoUtil.getPomFileName(applicationInfo);

			 if(!POM_FILENAME.equals(pomFileName)) {
				 buildArgCmds.add(pomFileName);
			 }
			 String workingDirectory = PhrescoUtil.getApplicationHome().toString();
			 manager.getApplicationProcessor().preBuild(applicationInfo);
			 BufferedReader performAction = performAction(info, ActionType.BUILD, buildArgCmds, workingDirectory);
			 
			 ConsoleViewManager.getDefault("Build Logs").println(performAction);

		 } catch (PhrescoException e) {
			 e.printStackTrace();
		 }
	 }


	 public BufferedReader performAction(ProjectInfo projectInfo, ActionType build, List<String> mavenArgCommands, String workingDirectory) throws PhrescoException {
		 StringBuilder command = buildMavenCommand(build, mavenArgCommands);
		 return executeMavenCommand(projectInfo, build, command, workingDirectory);
	 }

	 public StringBuilder buildMavenCommand(ActionType actionType, List<String> mavenArgCommands) {
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


	 protected List<String> getMavenArgCommands(List<Parameter> parameters) {
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

	 public void saveCongfiguration()  {
		 try {
			 MojoProcessor processor = new MojoProcessor(PhrescoUtil.getPackageInfoConfigurationPath());
			 List<Parameter> parameters = processor.getConfiguration(PACKAGE_GOAL).getParameters().getParameter();
			 if (CollectionUtils.isNotEmpty(parameters)) {
				 for (Parameter parameter : parameters) {
					 if (parameter.getType().equalsIgnoreCase(DYNAMIC_PARAMETER)) {
						 List<String> list =  (List<String>) map.get(parameter.getKey());
						 StringBuilder env = new StringBuilder();
						 if (CollectionUtils.isNotEmpty(list)) {
							 for (String string: list) {
								 env.append(string);
								 env.append(",");
							 }
							 String envValue = env.toString();
							 envValue = envValue.substring(0, envValue.lastIndexOf(","));
							 parameter.setValue(envValue); 
						 }
					 }
				 }
			 }
			 processor.save();
		 } catch (PhrescoException e) {
			 e.printStackTrace();
		 }
	 }
}
