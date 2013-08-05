package com.photon.phresco.ui.phrescoexplorer;

import java.io.BufferedReader;
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
import org.eclipse.swt.custom.BusyIndicator;
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


public class Unit  extends AbstractHandler implements PhrescoConstants {

	private Button unitButton;
	private Button cancelButton;
	private Button checkBoxButton;	

	private Shell unitTestDialog;	
	private Button envSelectionButton;

	private Text nameText;
	private Text numberText;
	private Text passwordText;
	private Combo listLogs;
	Map<String, String> typeMaps = new HashedMap();
	
	private static Map<String, Object> map = new HashedMap();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);
		
		BaseAction baseAction = new BaseAction();
		ServiceManager serviceManager = PhrescoUtil.getServiceManager(baseAction.getUserId());
		if(serviceManager == null) {
			ConfirmDialog.getConfirmDialog().showConfirm(shell);
			return null;
		}
		
		final Shell dialog = new Shell(shell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);

		Shell createCodeDialog = createCodeDialog(dialog);
		createCodeDialog.open();

		unitButton.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				saveCongfiguration();
				BusyIndicator.showWhile(null, new Runnable() {
					@Override
					public void run() {
						UnitTest();		
					}
				});
			}
		});
		return null;
	}

	public void UnitTest() {
		try {
			MojoProcessor processor = new MojoProcessor(PhrescoUtil.getUnitTestInfoConfigurationPath());
			List<Parameter> parameters = processor.getConfiguration(UNIT_TEST_GOAL).getParameters().getParameter();
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
			BufferedReader performAction = performAction(info, ActionType.UNIT_TEST, buildArgCmds, workingDirectory);
			
			ConsoleViewManager.getDefault(UNIT_LOGS).println(performAction);

		} catch (PhrescoException e) {
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
			MojoProcessor processor = new MojoProcessor(PhrescoUtil.getUnitTestInfoConfigurationPath());
			List<Parameter> parameters = processor.getConfiguration(UNIT_TEST_GOAL).getParameters().getParameter();
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
					}  else if (parameter.getType().equalsIgnoreCase(LIST)) {
						Combo list =  (Combo) map.get(parameter.getKey());
						String[] items = list.getItems();
						for (String string : items) {
							if (list.getText().equalsIgnoreCase(string)) {
								String value = typeMaps.get(string);
								parameter.setValue(value);
							}
						}
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

		unitTestDialog = new Shell(dialog, SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.MAX | SWT.RESIZE);
		unitTestDialog.setText("Code");
		unitTestDialog.setLocation(385, 130);
		unitTestDialog.setSize(451,188);

		GridLayout gridLayout = new GridLayout(2, false);
		GridData data = new GridData(GridData.FILL_BOTH);
		unitTestDialog.setLayout(gridLayout);
		unitTestDialog.setLayoutData(data);

		try {
			MojoProcessor processor = new MojoProcessor(PhrescoUtil.getUnitTestInfoConfigurationPath());
			Configuration configuration = processor.getConfiguration(UNIT_TEST_GOAL);
			List<Parameter> parameters = configuration.getParameters().getParameter();

			ApplicationInfo applicationInfo = PhrescoUtil.getProjectInfo().getAppInfos().get(0);
			DynamicPossibleValues possibleValues = new DynamicPossibleValues();
			Map<String, DependantParameters> watcherMap = new HashMap<String, DependantParameters>();
			Map<String, Object> maps = possibleValues.setPossibleValuesInReq(processor, applicationInfo, parameters, watcherMap, UNIT_TEST_GOAL);

			for (Parameter parameter : parameters) {
				String type = parameter.getType();

				if (type.equalsIgnoreCase(STRING)) {
					Label buildNameLabel = new Label(unitTestDialog, SWT.NONE);
					buildNameLabel.setText(parameter.getKey());
					buildNameLabel.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					buildNameLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP,false, false));

					nameText = new Text(unitTestDialog, SWT.BORDER);
					nameText.setToolTipText(parameter.getKey());
					data = new GridData(GridData.FILL_BOTH);
					nameText.setLayoutData(data);
					map.put(parameter.getKey(), nameText);

				} else if (type.equalsIgnoreCase(NUMBER)) {
					Label buildNumberLabel = new Label(unitTestDialog, SWT.NONE);
					buildNumberLabel.setText(parameter.getKey());
					buildNumberLabel.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					buildNumberLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP,false, false));

					numberText = new Text(unitTestDialog, SWT.BORDER);
					numberText.setToolTipText(parameter.getKey());
					numberText.setMessage(parameter.getKey());
					data = new GridData(GridData.FILL_BOTH);
					numberText.setLayoutData(data);
					map.put(parameter.getKey(), numberText);

				} else if (type.equalsIgnoreCase(BOOLEAN)) {
					Label defaults = new Label(unitTestDialog, SWT.LEFT);
					defaults.setText(parameter.getKey());
					defaults.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

					checkBoxButton = new Button(unitTestDialog, SWT.CHECK);
					checkBoxButton.setLayoutData(new GridData(75, 20));
					data = new GridData(GridData.FILL_BOTH);
					checkBoxButton.setLayoutData(data);

					map.put(parameter.getKey(), checkBoxButton);
				}
				else if (type.equalsIgnoreCase(PASSWORD)) {
					Label defaults = new Label(unitTestDialog, SWT.LEFT);
					defaults.setText(parameter.getKey());
					defaults.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

					passwordText = new Text(unitTestDialog, SWT.PASSWORD | SWT.BORDER);
					passwordText.setToolTipText(PASSWORD);
					passwordText.setMessage(parameter.getKey());
					passwordText.setLayoutData(new GridData(100, 13));
					data = new GridData(GridData.FILL_BOTH);
					passwordText.setLayoutData(data);
					map.put(parameter.getKey(), passwordText);
					
				} else if (type.equalsIgnoreCase(LIST)) {
					Label Logs = new Label(unitTestDialog, SWT.LEFT);
					Logs.setText(parameter.getKey());
					Logs.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					Logs.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,false));

					Combo listLogs = new Combo(unitTestDialog, SWT.DROP_DOWN);
					
					List<Value> values = parameter.getPossibleValues().getValue();
					for (Value value : values) {
						listLogs.add(value.getValue());
						typeMaps.put(value.getValue(), value.getKey());
					}
					data = new GridData(GridData.FILL_BOTH);
					listLogs.select(0);
					listLogs.setLayoutData(data);
					map.put(parameter.getKey(), listLogs); 

				} else if (type.equalsIgnoreCase("DynamicParameter")) {
					int yaxis = 0;
					String key = null;
					Label Logs = new Label(unitTestDialog, SWT.LEFT);
					Logs.setText("Environment:");
					Logs.setBounds(24, 40, 80, 23);

					Group group = new Group(unitTestDialog, SWT.SHADOW_IN);
					group.setText("Environment");
					group.setLocation(146, 26);

					final List<String> buttons = new ArrayList<String>();
					Set<Entry<String,Object>> entrySet = maps.entrySet();

					for (Entry<String, Object> entry : entrySet) {
						key = entry.getKey();
						if (key.equalsIgnoreCase(WATCHER_MAP)) {
							continue;
						}
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


			Composite composite = new Composite(unitTestDialog, SWT.BORDER);

			GridLayout layout = new GridLayout(2, true);
			GridData datas = new GridData(GridData.FILL_HORIZONTAL);
			composite.setLayout(layout);


			unitButton = new Button(composite, SWT.BORDER);
			unitButton.setText(VALIDATE);
			unitButton.setSize(74, 23);
			unitButton.setLayoutData(datas);
			
			cancelButton = new Button(composite, SWT.BORDER);
			cancelButton.setText(CANCEL);
			cancelButton.setSize(74, 23);
			cancelButton.setLayoutData(datas);
			
//			unitTestDialog.pack();

		} catch (PhrescoException e) {
			e.printStackTrace();
		}
		return unitTestDialog;
	}



}
