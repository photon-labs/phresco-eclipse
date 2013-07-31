package com.photon.phresco.ui.phrescoexplorer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.handlers.HandlerUtil;

import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.PhrescoDialog;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.model.ProjectInfo;
import com.photon.phresco.commons.util.ActionType;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.commons.util.ProjectManager;
import com.photon.phresco.commons.util.SonarUtil;
import com.photon.phresco.dynamicParameter.DependantParameters;
import com.photon.phresco.dynamicParameter.DynamicPossibleValues;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.MavenCommands.MavenCommand;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value;
import com.photon.phresco.plugins.util.MojoProcessor;

public class Code extends AbstractHandler implements PhrescoConstants {

	private Button codeButton;
	private Button cancelButton;
	private Button checkBoxButton;	

	private Shell codeDialog;	
	private Button envSelectionButton;

	private Text nameText;
	private Text numberText;
	private Text passwordText;
	private Combo listLogs;
	private static Map<String, Object> map = new HashedMap();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final Shell shell = HandlerUtil.getActiveShell(event);
		final Shell dialog = new Shell(shell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);

		try {
			int status = SonarUtil.getSonarServerStatus();
			if (status == 200) {
				final Shell createCodeDialog = createCodeDialog(dialog);
				createCodeDialog.open();
			} else {
				PhrescoDialog.errorDialog(shell, "Sonar Status", "Sonar is not Yet Started . Start the sonar to continue");
				return "";
			}
		} catch (PhrescoException e) {
			e.printStackTrace();
		}

		codeButton.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				saveCongfiguration();
				ValidateCode();
			}
		});
		return null;
	}

	public void ValidateCode() {
		try {
			MojoProcessor processor = new MojoProcessor(PhrescoUtil.getValidateCodeInfoConfigurationPath());
			List<Parameter> parameters = processor.getConfiguration(VALIDATE_CODE_GOAL).getParameters().getParameter();
			List<String> buildArgCmds = getMavenArgCommands(parameters);
			ProjectManager manager = new ProjectManager();
			ProjectInfo info = PhrescoUtil.getProjectInfo();

			ApplicationInfo applicationInfo = info.getAppInfos().get(0);
			String pomFileName = PhrescoUtil.getPomFileName(applicationInfo);

			if(!POM_FILENAME.equals(pomFileName)) {
				buildArgCmds.add(pomFileName);
			}
			String workingDirectory = PhrescoUtil.getProjectHome().toString();
			manager.getApplicationProcessor().preBuild(applicationInfo);
			BufferedReader performAction = performAction(info, ActionType.CODE_VALIDATE, buildArgCmds, workingDirectory);
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


	public void saveCongfiguration()  {
		try {
			MojoProcessor processor = new MojoProcessor(PhrescoUtil.getValidateCodeInfoConfigurationPath());
			List<Parameter> parameters = processor.getConfiguration(VALIDATE_CODE_GOAL).getParameters().getParameter();
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
					} else if (parameter.getType().equalsIgnoreCase(DYNAMIC_PARAMETER)) {
						List<String> list =  (List<String>) map.get(parameter.getKey());
						StringBuilder env = new StringBuilder();
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
			processor.save();
		} catch (PhrescoException e) {
			e.printStackTrace();
		}
	}


	public Shell createCodeDialog(Shell dialog) {

		codeDialog = new Shell(dialog, SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.MAX | SWT.RESIZE);
		codeDialog.setText("Code");
		codeDialog.setLocation(385, 130);

		GridLayout gridLayout = new GridLayout(2, false);
		GridData data = new GridData(GridData.FILL_BOTH);
		codeDialog.setLayout(gridLayout);
		codeDialog.setLayoutData(data);

		try {
			MojoProcessor processor = new MojoProcessor(PhrescoUtil.getValidateCodeInfoConfigurationPath());
			Configuration configuration = processor.getConfiguration(VALIDATE_CODE_GOAL);
			List<Parameter> parameters = configuration.getParameters().getParameter();

			ApplicationInfo applicationInfo = PhrescoUtil.getProjectInfo().getAppInfos().get(0);
			DynamicPossibleValues possibleValues = new DynamicPossibleValues();
			Map<String, DependantParameters> watcherMap = new HashMap<String, DependantParameters>();
			Map<String, Object> maps = possibleValues.setPossibleValuesInReq(processor, applicationInfo, parameters, watcherMap, VALIDATE_CODE_GOAL);

			for (Parameter parameter : parameters) {
				String type = parameter.getType();

				if (type.equalsIgnoreCase(STRING)) {
					Label buildNameLabel = new Label(codeDialog, SWT.NONE);
					buildNameLabel.setText(parameter.getKey());
					buildNameLabel.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					buildNameLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP,false, false));

					nameText = new Text(codeDialog, SWT.BORDER);
					nameText.setToolTipText(parameter.getKey());
					data = new GridData(GridData.FILL_BOTH);
					nameText.setLayoutData(data);
					map.put(parameter.getKey(), nameText);

				} else if (type.equalsIgnoreCase(NUMBER)) {
					Label buildNumberLabel = new Label(codeDialog, SWT.NONE);
					buildNumberLabel.setText(parameter.getKey());
					buildNumberLabel.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					buildNumberLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP,false, false));

					numberText = new Text(codeDialog, SWT.BORDER);
					numberText.setToolTipText(parameter.getKey());
					numberText.setMessage(parameter.getKey());
					data = new GridData(GridData.FILL_BOTH);
					numberText.setLayoutData(data);
					map.put(parameter.getKey(), numberText);

				} else if (type.equalsIgnoreCase(BOOLEAN)) {
					Label defaults = new Label(codeDialog, SWT.LEFT);
					defaults.setText(parameter.getKey());
					defaults.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

					checkBoxButton = new Button(codeDialog, SWT.CHECK);
					checkBoxButton.setLayoutData(new GridData(75, 20));
					data = new GridData(GridData.FILL_BOTH);
					checkBoxButton.setLayoutData(data);

					map.put(parameter.getKey(), checkBoxButton);
				}
				else if (type.equalsIgnoreCase(PASSWORD)) {
					Label defaults = new Label(codeDialog, SWT.LEFT);
					defaults.setText(parameter.getKey());
					defaults.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

					passwordText = new Text(codeDialog, SWT.PASSWORD | SWT.BORDER);
					passwordText.setToolTipText(PASSWORD);
					passwordText.setMessage(parameter.getKey());
					passwordText.setLayoutData(new GridData(100, 13));
					data = new GridData(GridData.FILL_BOTH);
					passwordText.setLayoutData(data);
					map.put(parameter.getKey(), passwordText);
				}
				else if (type.equalsIgnoreCase(LIST)) {
					Label Logs = new Label(codeDialog, SWT.LEFT);
					Logs.setText(parameter.getKey());
					Logs.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					Logs.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,false));

					listLogs = new Combo(codeDialog, SWT.DROP_DOWN);
					List<Value> values = parameter.getPossibleValues().getValue();
					for (Value value : values) {
						listLogs.add(value.getValue());
					}
					data = new GridData(GridData.FILL_BOTH);
					listLogs.setLayoutData(data);
					map.put(parameter.getKey(), listLogs); 

				} else if (type.equalsIgnoreCase("DynamicParameter")) {
					int yaxis = 0;
					String key = null;
					Label Logs = new Label(codeDialog, SWT.LEFT);
					Logs.setText("Environment:");
					Logs.setBounds(24, 40, 80, 23);

					Group group = new Group(codeDialog, SWT.SHADOW_IN);
					group.setText("Environment");
					group.setLocation(146, 26);

					final List<String> buttons = new ArrayList<String>();
					Set<Entry<String,Object>> entrySet = maps.entrySet();

					for (Entry<String, Object> entry : entrySet) {
						key = entry.getKey();
						List<Value> values = (List<Value>) entry.getValue();
						for (Value value : values) {
							envSelectionButton = new Button(group, SWT.CHECK);
							envSelectionButton.setText(value.getValue());
							envSelectionButton.setLocation(20, 20+yaxis);
							data = new GridData(GridData.FILL_BOTH);
							envSelectionButton.setLayoutData(data);
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
					map.put(key, buttons);
					group.pack();
				} 
			}


			Composite composite = new Composite(codeDialog, SWT.BORDER);

			GridLayout layout = new GridLayout(2, true);
			GridData datas = new GridData();
			datas.horizontalAlignment = SWT.RIGHT;
			composite.setLayout(layout);
			composite.setLayoutData(datas);


			codeButton = new Button(composite, SWT.BORDER);
			codeButton.setText(VALIDATE);
			codeButton.setSize(74, 23);

			cancelButton = new Button(composite, SWT.BORDER);
			cancelButton.setText(CANCEL);
			cancelButton.setSize(74, 23);

			codeDialog.pack();

		} catch (PhrescoException e) {
			e.printStackTrace();
		}
		return codeDialog;
	}


}
