package com.photon.phresco.ui.phrescoexplorer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.util.Base64;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.handlers.HandlerUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.model.ProjectInfo;
import com.photon.phresco.commons.util.ActionType;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.MavenCommands.MavenCommand;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value;
import com.photon.phresco.plugins.util.MojoProcessor;
import com.photon.phresco.ui.resource.Messages;

public class Build extends AbstractHandler implements PhrescoConstants {
	
	private Button buildButton;
	private Button cancelButton;
	private	Button generateBuildButton;
	private Button deleteButton;
	private	Button generateBuildsaveButton;
	private Button generageBuildcancelButton;
	private Button checkBoxButton;	
	
	private Shell buildDialog;	
	private Shell createBuildDialog;
	private Shell generateDialog;
	private Shell generateBuildDialog;
	
	private Text nameText;
	private Text numberText;
	private Text passwordText;
	private Combo listLogs;
	
	@SuppressWarnings("unchecked")
	private static Map<String, Object> map = new HashedMap();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);
		final Shell buildDialog = new Shell(shell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);

		generateDialog = createGenearateBuildDialog(buildDialog);
		generateDialog.open();


		final Listener cancelListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				createBuildDialog.setVisible(false);
			}
		};

		final Listener saveListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				saveCongfiguration();
				build();
				cancelButton.addListener(SWT.Selection, cancelListener);
			}
		};

		Listener generatePopupListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				createBuildDialog = createBuildDialog(buildDialog);
				createBuildDialog.open();
				buildButton.addListener(SWT.Selection, saveListener);
			}
		};

		Listener generatePopupCancelListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				generateDialog.setVisible(false);
			}
		};

		generateBuildButton.addListener(SWT.Selection, generatePopupListener);
		generageBuildcancelButton.addListener(SWT.Selection, generatePopupCancelListener);


		return null;
	}

	private void build() {
		try {
			System.out.println("Inside the Build");
			MojoProcessor processor = new MojoProcessor(getConfigurationPath());

			List<Parameter> parameters =processor.getConfiguration("package").getParameters().getParameter();

			List<String> buildArgCmds = getMavenArgCommands(parameters);

			ProjectInfo info = readProjectInfo();
			ApplicationInfo applicationInfo = info.getAppInfos().get(0);
			String pomFileName = getPomFileName(applicationInfo);

			if(!POM_FILENAME.equals(pomFileName)) {
				buildArgCmds.add(pomFileName);
			}

			String workingDirectory = getProjectHome() + File.separator + "TestProject";
//			getApplicationProcessor().preBuild(getApplicationInfo());
			BufferedReader performAction = performAction(info, ActionType.BUILD, buildArgCmds, workingDirectory);
			String line;
			
	
			while ((line = performAction.readLine())!= null) {
					System.out.println(line);
			}
			
		} catch (PhrescoException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public String getPomFileName(ApplicationInfo appInfo) {
		File pomFile = new File(getProjectHome() + appInfo.getAppDirName() + File.separator + appInfo.getPomFile());
		if(pomFile.exists()) {
			return appInfo.getPomFile();
		}
		return POM_FILENAME;
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


	private Shell createGenearateBuildDialog(Shell dialog) {
		generateBuildDialog = new Shell(dialog, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		GridLayout layout = new GridLayout(2, false);
		layout.verticalSpacing = 6;

		generateBuildDialog.setText(Messages.BUILD);
		generateBuildDialog.setLocation(385, 130);
		generateBuildDialog.setSize(300, 300);
		generateBuildDialog.setLayout(layout);

		generateBuildButton = new Button(generateBuildDialog, SWT.BORDER | SWT.PUSH);
		generateBuildButton.setLayoutData(new GridData(75, 20));
		generateBuildButton.setText("GenerateBuild");

		deleteButton = new Button(generateBuildDialog, SWT.BORDER | SWT.PUSH);
		deleteButton.setLayoutData(new GridData(75, 20));
		deleteButton.setText("Delete Button");

		generateBuildsaveButton = new Button(generateBuildDialog, SWT.PUSH);
		generateBuildsaveButton.setText(Messages.OK);
		generateBuildsaveButton.setLayoutData(new GridData(75,20));
		generateBuildsaveButton.setLocation(500,505);


		generageBuildcancelButton = new Button(generateBuildDialog, SWT.PUSH);
		generageBuildcancelButton .setText(Messages.CANCEL);
		generageBuildcancelButton .setLayoutData(new GridData(75,20));

		return generateBuildDialog;

	}


	private Shell createBuildDialog(Shell dialog) {
		buildDialog = new Shell(dialog, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		GridLayout layout = new GridLayout(2, false);
		layout.verticalSpacing = 6;

		buildDialog.setText("Build");
		buildDialog.setLocation(385, 130);
		buildDialog.setSize(300, 300);
		buildDialog.setLayout(layout);

		File configurationPath = getConfigurationPath();
		try {
			MojoProcessor processor = new MojoProcessor(configurationPath);
			Configuration configuration = processor.getConfiguration("package");
			List<Parameter> parameters = configuration.getParameters().getParameter();
			for (Parameter parameter : parameters) {
				String type = parameter.getType();
				if (type.equalsIgnoreCase(STRING)) {
					Label buildNameLabel = new Label(buildDialog, SWT.NONE);
					buildNameLabel.setText(parameter.getKey());
					buildNameLabel.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					buildNameLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP,false, false));
					
					nameText = new Text(buildDialog, SWT.BORDER);
					nameText.setToolTipText(parameter.getKey());
					nameText.setMessage(parameter.getKey());
					nameText.setLayoutData(new GridData(100, 13));
					map.put(parameter.getKey(), nameText);

				} else if (type.equalsIgnoreCase(NUMBER)) {
					Label buildNumberLabel = new Label(buildDialog, SWT.NONE);
					buildNumberLabel.setText(parameter.getKey());
					buildNumberLabel.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					buildNumberLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP,false, false));
					
					numberText = new Text(buildDialog, SWT.BORDER);
					numberText.setToolTipText(parameter.getKey());
					numberText.setMessage(parameter.getKey());
					numberText.setLayoutData(new GridData(100, 13));
					map.put(parameter.getKey(), numberText);
					
				} else if (type.equalsIgnoreCase(BOOLEAN)) {
					Label defaults = new Label(buildDialog, SWT.LEFT);
					defaults.setText(parameter.getKey());
					defaults.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

					checkBoxButton = new Button(buildDialog, SWT.CHECK);
					checkBoxButton.setText(parameter.getType());
					checkBoxButton.setLayoutData(new GridData(75, 20));
					map.put(parameter.getKey(), checkBoxButton);
				}
				else if (type.equalsIgnoreCase(PASSWORD)) {
					Label defaults = new Label(buildDialog, SWT.LEFT);
					defaults.setText(parameter.getKey());
					defaults.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

					passwordText = new Text(buildDialog, SWT.PASSWORD | SWT.BORDER);
					passwordText.setToolTipText(PASSWORD);
					passwordText.setMessage(parameter.getKey());
					passwordText.setLayoutData(new GridData(100, 13));
					map.put(parameter.getKey(), passwordText);
				}
				 else if (type.equalsIgnoreCase(LIST)) {
					Label Logs = new Label(buildDialog, SWT.LEFT);
					Logs.setText(parameter.getKey());
					Logs.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					Logs.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,false));

					listLogs = new Combo(buildDialog, SWT.DROP_DOWN);
					listLogs.setText(parameter.getKey());
					List<Value> values = parameter.getPossibleValues().getValue();
					for (Value value : values) {
						listLogs.add(value.getValue());
					}
					map.put(parameter.getKey(), listLogs);
				}  
			}

			buildButton = new Button(buildDialog, SWT.PUSH);
			buildButton.setText("Build");
			buildButton.setLayoutData(new GridData(75,20));
			buildButton.setLocation(500,505);

			cancelButton = new Button(buildDialog, SWT.PUSH);
			cancelButton .setText(CANCEL);
			cancelButton .setLayoutData(new GridData(75,20));

		} catch (PhrescoException e) {
			e.printStackTrace();
		}
		return buildDialog;
	}

	private void saveCongfiguration()  {
		try {
			MojoProcessor processor = new MojoProcessor(getConfigurationPath());
			List<Parameter> parameters = processor.getConfiguration(PACKAGE_GOAL).getParameters().getParameter();
			if (CollectionUtils.isNotEmpty(parameters)) {
				for (Parameter parameter : parameters) {
					if (parameter.getType().equalsIgnoreCase(STRING)) {
						Text nameText = (Text) map.get(parameter.getKey());
						parameter.setValue(nameText.getText());
					} else if (parameter.getType().equalsIgnoreCase(NUMBER)) {
						Text numberText = (Text) map.get(parameter.getKey());
						parameter.setValue(numberText.getText());
					} else if (parameter.getType().equalsIgnoreCase(BOOLEAN)) {
						Button checkBoxButton = (Button) map.get(parameter.getKey());
						boolean selection = checkBoxButton.getSelection();
						parameter.setValue(String.valueOf(selection));
					} else if (parameter.getType().equalsIgnoreCase(PASSWORD)) {
						Text passwordText = (Text) map.get(parameter.getKey());
						String password = passwordText.getText();
						byte[] encodedPwd = Base64.encodeBase64(password.getBytes());
						String encodedString = new String(encodedPwd);
						parameter.setValue(encodedString);
					} else if (parameter.getType().equalsIgnoreCase(LIST)) {
						Combo list =  (Combo) map.get(parameter.getKey());
						parameter.setValue(list.getText()); 
					}
				}
			}
			processor.save();
		} catch (PhrescoException e) {
			e.printStackTrace();
		}
	}


	private File getProjectHome() {
		File projectPath = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString()	+ File.separator + PROJECTS);
		return projectPath;
	}

	private File getConfigurationPath() {
		File configPath = new File(getProjectHome() + File.separator + "TestProject" + File.separator + DOT_PHRESCO_FOLDER
				+ File.separator + "phresco-package-info.xml");
		return configPath;
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
}
