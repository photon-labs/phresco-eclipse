package com.photon.phresco.ui.phrescoexplorer;

import java.io.BufferedReader;
import java.io.File;
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
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.w3c.dom.Element;

import com.photon.phresco.commons.ConfirmDialog;
import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.PhrescoDialog;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.model.ProjectInfo;
import com.photon.phresco.commons.util.ConsoleViewManager;
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
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.ui.model.ActionType;
import com.photon.phresco.ui.model.BaseAction;
import com.photon.phresco.ui.model.CodeValidationReportType;
import com.photon.phresco.ui.resource.Messages;
import com.phresco.pom.exception.PhrescoPomException;
import com.phresco.pom.model.Model;
import com.phresco.pom.model.Profile;
import com.phresco.pom.model.Profile.Properties;
import com.phresco.pom.util.PomProcessor;

public class Code extends AbstractHandler implements PhrescoConstants {

	private Button codeButton;
	private Button cancelButton;
	private Button checkBoxButton;	

	private Button envSelectionButton;

	private Text nameText;
	private Text numberText;
	private Text passwordText;
//	private Combo listLogs;
	private static Map<String, Object> map = new HashedMap();
	private static Map<String, String> techValues = new HashedMap();
	Map<String, String> typeMaps = new HashedMap();
	private Shell dialog;
	private Shell codeDialog;
	private Shell createConfigureDialog;
	private Browser browser;
	private Combo reportType;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final Shell shell = HandlerUtil.getActiveShell(event);
		
		BaseAction baseAction = new BaseAction();
		ServiceManager serviceManager = PhrescoUtil.getServiceManager(baseAction.getUserId());
		if(serviceManager == null) {
			ConfirmDialog.getConfirmDialog().showConfirm(shell);
			return null;
		}
		
		Shell createSonarDialog = createSonarDialog(shell);
		createSonarDialog.open();
		return null;
	}

	private Shell createSonarDialog(final Shell shell) {
		codeDialog = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
		codeDialog.setText(SONAR_DIALOG_NAME);
		codeDialog.setLocation(385,130);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		codeDialog.setLayout(gridLayout);


		Button testButton = new Button(codeDialog, SWT.PUSH);
		testButton.setText("Test");

		Label reportTypeLabel = new Label(codeDialog, SWT.NONE);
		reportTypeLabel.setText("Report Type");

		
		
		reportType =  new Combo(codeDialog, SWT.READ_ONLY | SWT.BORDER);
		List<CodeValidationReportType> codeValidationReportTypes = SonarUtil.getCodeValidationReportTypes();
		if (CollectionUtils.isNotEmpty(codeValidationReportTypes)) {
			for (CodeValidationReportType codeValidationReportType : codeValidationReportTypes) {
				List<Value> options = codeValidationReportType.getOptions();
				if (CollectionUtils.isNotEmpty(options)) {
					for (Value value : options) {
						reportType.add(value.getValue());
						techValues.put(value.getValue(), value.getKey());
					}
				}
			}
			boolean ifFunctional = SonarUtil.checkFunctionalDir();
			if (ifFunctional) {
				reportType.add("source");
				reportType.add("functional");
			}
			reportType.select(0);
		}		

		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.horizontalSpan = 1;
		data.grabExcessHorizontalSpace = true;
		reportType.setLayoutData(data);
		
		try {
			browser = new Browser(codeDialog, SWT.NONE);
		} catch (SWTError e) {
			codeDialog.pack();
			return codeDialog;
		}
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.horizontalSpan = 3;
		browser.setLayoutData(data);
		setBrowserUrl();
		reportType.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				setBrowserUrl();
			}
		});
		
		

		Composite cancelComposite = new Composite(codeDialog, SWT.NONE);
		GridLayout subLayout = new GridLayout(1, false);
		codeDialog.setLayout(subLayout);
		cancelComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false));
		testButton.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				codeDialog.setVisible(false);
				createConfigureDialog  = new Shell(shell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
				try {
					int status = SonarUtil.getSonarServerStatus();
					if (status == 200) {
						dialog = createCodeDialog(createConfigureDialog);
						dialog.open();
					} else {
						PhrescoDialog.errorDialog(shell, SONAR_STATUS, SONAR_STATUS_MESSAGE);
						return;
					}
				} catch (PhrescoException e) {
					e.printStackTrace();
				}
		
				codeButton.addListener(SWT.Selection, new Listener() {
		
					@Override
					public void handleEvent(Event event) {
						saveCongfiguration();
						BusyIndicator.showWhile(null, new Runnable() {
							@Override
							public void run() {
								ValidateCode();
							}
						});
						
						dialog.setVisible(false);
						codeDialog.setVisible(true);
						setBrowserUrl();
					}
				});
			}
		});
		
		cancelButton = new Button(cancelComposite, SWT.PUSH);
		cancelButton.setText(Messages.CANCEL);
		cancelButton.setSize(74, 23);
		
		Listener cancelListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				codeDialog.close();				
			}
		};
		
		cancelButton.addListener(SWT.Selection, cancelListener);

		return codeDialog;
	}

	private void setBrowserUrl() {
		String url = SONAR_REPORT_URL + getReportUrl();
		try {
			int sonarServerStatus = SonarUtil.getSonarServerStatus(url);
			if (sonarServerStatus == 200) {
				browser.setUrl(url);
			} else {
				browser.setText(SONAR_REPORT_NOTAVAILABLE);
			}
		} catch (PhrescoException e) {
			e.printStackTrace();
		}
	}
	
	private String getReportUrl() {
		try {
			String appDirName = PhrescoUtil.getApplicationInfo().getAppDirName();
			PomProcessor processor = PhrescoUtil.getPomProcessor(appDirName);
			StringBuilder builder = new StringBuilder();
			String branchValue = "";
			String sourceType = "";
			String functionalDir = processor.getProperty("phresco.functionalTest.dir");
			boolean startsWith = functionalDir.startsWith("/");
			
			if (StringUtils.isNotEmpty(functionalDir) && !startsWith) {
				functionalDir = File.separator + functionalDir;
			}
			sourceType = reportType.getText();
			if (sourceType.contains("functional")) {
				PomProcessor pomProcessor = PhrescoUtil.getPomProcessor(appDirName + functionalDir);
				Model model = pomProcessor.getModel();
				model.getGroupId();
				builder.append(model.getGroupId());
				builder.append(COLON);
				builder.append(model.getArtifactId());
				builder.append(COLON);
				builder.append("functional");
			} else {
				Model model = processor.getModel();
				builder.append(model.getGroupId());
				builder.append(COLON);
				builder.append(model.getArtifactId());
				String key = reportType.getText();
				String profile = techValues.get(key);
				if (StringUtils.isNotEmpty(profile)) {
					Profile typeProfile = processor.getProfile(profile);
					Properties properties = typeProfile.getProperties();
					if (properties != null) {
						List<Element> elements = properties.getAny();
						if (CollectionUtils.isNotEmpty(elements)) {
							for (Element element : elements) {
								if (element.getTagName().equals(SONAR_BRANCH)) {
									branchValue = element.getTextContent();
									builder.append(COLON);
									builder.append(branchValue);
								}
							}
						}
					}
				}
			}
			return builder.toString();
		} catch (PhrescoException e) {
			e.printStackTrace();
		} catch (PhrescoPomException e) {
			e.printStackTrace();
		}
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
			String workingDirectory = PhrescoUtil.getApplicationHome().toString();

			manager.getApplicationProcessor().preBuild(applicationInfo);
			BufferedReader performAction = performAction(info, ActionType.CODE_VALIDATE, buildArgCmds, workingDirectory);
			
			ConsoleViewManager.getDefault(SONAR_LOGS).println(performAction);
			
			createConfigureDialog.setVisible(false);
			codeDialog.setVisible(true);
			
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

		createConfigureDialog = new Shell(dialog, SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.MAX | SWT.RESIZE);
		createConfigureDialog.setText("Code");
		createConfigureDialog.setLocation(385, 130);

		GridLayout gridLayout = new GridLayout(2, false);
		GridData data = new GridData(GridData.FILL_BOTH);
		createConfigureDialog.setLayout(gridLayout);
		createConfigureDialog.setLayoutData(data);

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
					Label buildNameLabel = new Label(createConfigureDialog, SWT.NONE);
					buildNameLabel.setText(parameter.getName().getValue().get(0).getValue());
					buildNameLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP,false, false));

					nameText = new Text(createConfigureDialog, SWT.BORDER);
					nameText.setToolTipText(parameter.getKey());
					data = new GridData(GridData.FILL_BOTH);
					nameText.setLayoutData(data);
					map.put(parameter.getKey(), nameText);

				} else if (type.equalsIgnoreCase(NUMBER)) {
					Label buildNumberLabel = new Label(createConfigureDialog, SWT.NONE);
					buildNumberLabel.setText(parameter.getName().getValue().get(0).getValue());
					buildNumberLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP,false, false));

					numberText = new Text(createConfigureDialog, SWT.BORDER);
					numberText.setToolTipText(parameter.getKey());
					numberText.setMessage(parameter.getKey());
					data = new GridData(GridData.FILL_BOTH);
					numberText.setLayoutData(data);
					map.put(parameter.getKey(), numberText);

				} else if (type.equalsIgnoreCase(BOOLEAN)) {
					Label defaults = new Label(createConfigureDialog, SWT.LEFT);
					defaults.setText(parameter.getName().getValue().get(0).getValue());
					defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

					checkBoxButton = new Button(createConfigureDialog, SWT.CHECK);
					checkBoxButton.setLayoutData(new GridData(75, 20));
					data = new GridData(GridData.FILL_BOTH);
					checkBoxButton.setLayoutData(data);

					map.put(parameter.getKey(), checkBoxButton);
				}
				else if (type.equalsIgnoreCase(PASSWORD)) {
					Label defaults = new Label(createConfigureDialog, SWT.LEFT);
					defaults.setText(parameter.getName().getValue().get(0).getValue());
					defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

					passwordText = new Text(createConfigureDialog, SWT.PASSWORD | SWT.BORDER);
					passwordText.setToolTipText(PASSWORD);
					passwordText.setMessage(parameter.getKey());
					passwordText.setLayoutData(new GridData(100, 13));
					data = new GridData(GridData.FILL_BOTH);
					passwordText.setLayoutData(data);
					map.put(parameter.getKey(), passwordText);
				}	else if (type.equalsIgnoreCase(LIST)) {
					Label Logs = new Label(createConfigureDialog, SWT.LEFT);
					Logs.setText(parameter.getName().getValue().get(0).getValue());
					Logs.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,false));

					Combo listLogs = new Combo(createConfigureDialog, SWT.DROP_DOWN);
					
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
					Label Logs = new Label(createConfigureDialog, SWT.LEFT);
					Logs.setText(Messages.ENVIRONMENT);
					Logs.setBounds(24, 40, 80, 23);

					Group group = new Group(createConfigureDialog, SWT.SHADOW_IN);
					group.setText(Messages.ENVIRONMENT);
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


			Composite composite = new Composite(createConfigureDialog, SWT.NONE);

			GridLayout layout = new GridLayout(2, true);
			GridData datas = new GridData();
			datas.horizontalAlignment = SWT.RIGHT;
			composite.setLayout(layout);
			composite.setLayoutData(datas);


			codeButton = new Button(composite, SWT.PUSH);
			codeButton.setText(VALIDATE);
			codeButton.setSize(74, 23);

			cancelButton = new Button(composite, SWT.PUSH);
			cancelButton.setText(Messages.CANCEL);
			cancelButton.setSize(74, 23);
			
			
			Listener cancelListener = new Listener() {
				
				@Override
				public void handleEvent(Event event) {
					createConfigureDialog.close();
				}
			};
			cancelButton.addListener(SWT.Selection, cancelListener);
			createConfigureDialog.pack();

		} catch (PhrescoException e) {
			e.printStackTrace();
		}
		return createConfigureDialog;
	}


}
