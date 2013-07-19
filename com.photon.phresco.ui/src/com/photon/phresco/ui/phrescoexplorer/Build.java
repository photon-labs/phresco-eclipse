package com.photon.phresco.ui.phrescoexplorer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.collections.CollectionUtils;
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
import org.springframework.mock.web.MockHttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.model.ProjectInfo;
import com.photon.phresco.commons.util.ActionType;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.Childs.Child;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.MavenCommands.MavenCommand;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value;
import com.photon.phresco.plugins.util.MojoProcessor;

public class Build extends AbstractHandler implements PhrescoConstants {
	private Shell buildDialog;
	private Button saveButton;
	private Button cancelButton;
	private Shell createBuildDialog;
	private Shell generateDialog;
	private	Button generateBuildButton;
	private Button deleteButton;
	private	Button generateBuildsaveButton;
	private Button generageBuildcancelButton;
	private Shell generateBuildDialog;
	private MockHttpServletRequest request;

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
				saveButton.addListener(SWT.Selection, saveListener);
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

		generateBuildDialog.setText("Build");
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
		generateBuildsaveButton.setText("OK");
		generateBuildsaveButton.setLayoutData(new GridData(75,20));
		generateBuildsaveButton.setLocation(500,505);


		generageBuildcancelButton = new Button(generateBuildDialog, SWT.PUSH);
		generageBuildcancelButton .setText(CANCEL);
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

				if (type.equalsIgnoreCase(STRING)|| type.equalsIgnoreCase(NUMBER)) {
					Label envLabel = new Label(buildDialog, SWT.NONE);
					envLabel.setText(parameter.getKey());
					envLabel.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					envLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP,false, false));

					Text nameText = new Text(buildDialog, SWT.BORDER);
					nameText.setToolTipText(ENVIRONMENT_NAME);
					nameText.setMessage(NAME);
					nameText.setLayoutData(new GridData(100, 13));

				} else if (type.equalsIgnoreCase(BOOLEAN)) {

					Label defaults = new Label(buildDialog, SWT.LEFT);
					defaults.setText(parameter.getKey());
					defaults.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

					Button defaultCheckBoxButton = new Button(buildDialog, SWT.CHECK);
					defaultCheckBoxButton.setLayoutData(new GridData(75, 20));

				} else if (type.equalsIgnoreCase(PASSWORD)) {
					Label defaults = new Label(buildDialog, SWT.LEFT);
					defaults.setText(parameter.getKey());
					defaults.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

					Button defaultCheckBoxButton = new Button(buildDialog, SWT.PASSWORD);
					defaultCheckBoxButton.setLayoutData(new GridData(75, 20));

				} else if (type.equalsIgnoreCase("List")) {
					Label Logs = new Label(buildDialog, SWT.LEFT);
					Logs.setText(parameter.getKey());
					Logs.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					Logs.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,false));

					Combo combo = new Combo(buildDialog, SWT.DROP_DOWN);
					combo.setText(parameter.getKey());
					List<Value> values = parameter.getPossibleValues().getValue();
					for (Value value : values) {
						combo.add(value.getValue());
					}
				} else if (type.equalsIgnoreCase("Browser")) {
					Label fileBrowser = new Label(buildDialog, SWT.LEFT);
					fileBrowser.setText(parameter.getKey());
					fileBrowser.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					fileBrowser.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

					Button browser = new Button(buildDialog, SWT.BORDER);
					browser.setLayoutData(new GridData(90, 20));

				} else if (type.equalsIgnoreCase("environmentName")) {
					Label environmentName = new Label(buildDialog, SWT.LEFT);
					environmentName.setText(parameter.getKey());
					environmentName.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					environmentName.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

					Button browser = new Button(buildDialog, SWT.BORDER);
					browser.setLayoutData(new GridData(90, 20));
				}
			}

			saveButton = new Button(buildDialog, SWT.PUSH);
			saveButton.setText(SAVE);
			saveButton.setLayoutData(new GridData(75,20));
			saveButton.setLocation(500,505);


			cancelButton = new Button(buildDialog, SWT.PUSH);
			cancelButton .setText(CANCEL);
			cancelButton .setLayoutData(new GridData(75,20));

		} catch (PhrescoException e) {
			e.printStackTrace();
		}

		return buildDialog;

	}

	private void saveCongfiguration() {
		request = new MockHttpServletRequest();
		request.setParameter("buildName", "Sample");
		request.setParameter("buildNumber", "1");
		request.setParameter("showSettings", "false");
		request.setParameter("environmentName", "Production");
		request.setParameter("skipTest", "true");
		request.setParameter("logs", "showErrors");
		try {
			persistValuesToXml(request, "package");
		} catch (PhrescoException e) {
			e.printStackTrace();
		}
	}

	protected void persistValuesToXml(MockHttpServletRequest request, String goal) throws PhrescoException {
		try {
			MojoProcessor processor = new MojoProcessor(getConfigurationPath());

			List<Parameter> parameters =processor.getConfiguration("package").getParameters().getParameter();
			if (CollectionUtils.isNotEmpty(parameters)) {
				for (Parameter parameter : parameters) {
					StringBuilder csParamVal = new StringBuilder();
					if (Boolean.parseBoolean(parameter.getMultiple())) {
						if (getReqParameterValues(parameter.getKey()) == null) {
							parameter.setValue("");
						} else {
							String[] parameterValues = getReqParameterValuess(parameter.getKey());
							for (String parameterValue : parameterValues) {
								csParamVal.append(parameterValue);
								csParamVal.append(",");
							}
							String csvVal = csParamVal.toString();
							parameter.setValue(csvVal.toString().substring(0, csvVal.lastIndexOf(",")));
						}
					} else if (BOOLEAN.equalsIgnoreCase(parameter.getType())) {
						if (getReqParameter(parameter.getKey()) != null) {
							parameter.setValue(getReqParameter(parameter.getKey()));
						} else {
							parameter.setValue(Boolean.FALSE.toString());
						}
					} else if (parameter.getType().equalsIgnoreCase("map")) {
						List<Child> childs = parameter.getChilds().getChild();
						String[] keys = getReqParameterValuess(childs.get(0).getKey());
						String[] values = getReqParameterValuess(childs.get(1).getKey());
						Properties properties = new Properties();
						for (int i = 0; i < keys.length; i++) {
							properties.put(keys[i], values[i]);
						}
						StringWriter writer = new StringWriter();
						properties.store(writer, "");
						String value = writer.getBuffer().toString();
						parameter.setValue(value);
					}  else if(parameter.getType().equalsIgnoreCase("Password")) {
						byte[] encodedPwd = Base64.encodeBase64(getReqParameter(parameter.getKey()).getBytes());
						String encodedString = new String(encodedPwd);
						parameter.setValue(encodedString);
					} else {
						parameter.setValue(StringUtils.isNotEmpty(getReqParameter(parameter.getKey())) ? (String)getReqParameter(parameter.getKey()) : "");
					}
				}
			}
			processor.save();	
		} catch (IOException e) {
			throw new PhrescoException(e);
		}
	}

	protected String getReqParameterValues(String key) {
		return request.getParameter(key);
	}

	protected String getReqParameter(String key) {
		return request.getParameter(key);
	}

	protected String[] getReqParameterValuess(String Key) {
		return request.getParameterValues(Key);
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
