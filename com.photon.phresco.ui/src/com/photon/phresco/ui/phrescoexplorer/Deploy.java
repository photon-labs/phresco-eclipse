package com.photon.phresco.ui.phrescoexplorer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.model.BuildInfo;
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
import com.photon.phresco.ui.model.ActionType;
import com.photon.phresco.ui.resource.Messages;

public class Deploy extends AbstractHandler implements PhrescoConstants {

	private Button deployButton;
	private Button cancelButton;
	private Button checkBoxButton;	

	private Shell deployDialog;	

	private Text nameText;
	private Text numberText;
	private Text passwordText;

	Map<String, String> deploytypeMaps = new HashedMap();
	private static Map<String, Object> deploymap = new HashedMap();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		Shell shell = HandlerUtil.getActiveShell(event);
		final Shell dialog = new Shell(shell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		if (!PhrescoUtil.getDeployInfoConfigurationPath().exists()) {
			return null;
		}
		final Shell createDeployDialog = createDeployDialog(dialog);

		deployButton.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				try {
					saveCongfiguration();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				deploy();
				dialog.close();
			}
		});
		createDeployDialog.open();


		Listener generatePopupCancelListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				createDeployDialog.close();
			}
		};
		cancelButton.addListener(SWT.Selection, generatePopupCancelListener);

		return null;
	}

	public void deploy() {
		try {
			MojoProcessor processor = new MojoProcessor(PhrescoUtil.getDeployInfoConfigurationPath());
			List<Parameter> parameters = processor.getConfiguration("deploy").getParameters().getParameter();
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
			BufferedReader performAction = performAction(info, ActionType.DEPLOY, buildArgCmds, workingDirectory);

			ConsoleViewManager.getDefault("Deploy Logs").println(performAction);

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


	public Shell createDeployDialog(Shell dialog) {
		deployDialog = new Shell(dialog, SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.MAX | SWT.RESIZE);
		deployDialog.setText(Messages.DEPLOY_DIALOG_TITLE);
		deployDialog.setLocation(385, 130);
		deployDialog.setSize(451,188);

		GridLayout gridLayout = new GridLayout(2, false);
		GridData data = new GridData(GridData.FILL_BOTH);
		deployDialog.setLayout(gridLayout);
		deployDialog.setLayoutData(data);

		try {
			if (!PhrescoUtil.getDeployInfoConfigurationPath().exists()) {
				MojoProcessor processor = new MojoProcessor(PhrescoUtil.getDeployInfoConfigurationPath());
				Configuration configuration = processor.getConfiguration(DEPLOY_GOAL);
				List<Parameter> parameters = configuration.getParameters().getParameter();

				ApplicationInfo applicationInfo = PhrescoUtil.getProjectInfo().getAppInfos().get(0);
				DynamicPossibleValues possibleValues = new DynamicPossibleValues();
				Map<String, DependantParameters> watcherMap = new HashMap<String, DependantParameters>();
				final Map<String, Object> maps = possibleValues.setPossibleValuesInReq(processor, applicationInfo, parameters, watcherMap, DEPLOY_GOAL);

				for (final Parameter parameter : parameters) {
					String type = parameter.getType();

					if (type.equalsIgnoreCase(STRING)) {
						Label buildNameLabel = new Label(deployDialog, SWT.NONE);
						buildNameLabel.setText(parameter.getKey());
						buildNameLabel.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
						buildNameLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP,false, false));

						nameText = new Text(deployDialog, SWT.BORDER);
						nameText.setToolTipText(parameter.getKey());
						data = new GridData(GridData.FILL_BOTH);
						nameText.setLayoutData(data);
						deploymap.put(parameter.getKey(), nameText);

					} else if (type.equalsIgnoreCase(NUMBER)) {
						Label buildNumberLabel = new Label(deployDialog, SWT.NONE);
						buildNumberLabel.setText(parameter.getKey());
						buildNumberLabel.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
						buildNumberLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP,false, false));

						numberText = new Text(deployDialog, SWT.BORDER);
						numberText.setToolTipText(parameter.getKey());
						numberText.setMessage(parameter.getKey());
						data = new GridData(GridData.FILL_BOTH);
						numberText.setLayoutData(data);
						deploymap.put(parameter.getKey(), numberText);

					} else if (type.equalsIgnoreCase(BOOLEAN)) {
						Label defaults = new Label(deployDialog, SWT.LEFT);
						defaults.setText(parameter.getKey());
						defaults.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
						defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

						checkBoxButton = new Button(deployDialog, SWT.CHECK);
						checkBoxButton.setLayoutData(new GridData(75, 20));
						data = new GridData(GridData.FILL_BOTH);
						checkBoxButton.setLayoutData(data);

						deploymap.put(parameter.getKey(), checkBoxButton);
					}
					else if (type.equalsIgnoreCase(PASSWORD)) {
						Label defaults = new Label(deployDialog, SWT.LEFT);
						defaults.setText(parameter.getKey());
						defaults.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
						defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

						passwordText = new Text(deployDialog, SWT.PASSWORD | SWT.BORDER);
						passwordText.setToolTipText(PASSWORD);
						passwordText.setMessage(parameter.getKey());
						passwordText.setLayoutData(new GridData(100, 13));
						data = new GridData(GridData.FILL_BOTH);
						passwordText.setLayoutData(data);
						deploymap.put(parameter.getKey(), passwordText);

					} else if (type.equalsIgnoreCase(LIST)) {

						Label Logs = new Label(deployDialog, SWT.LEFT);
						Logs.setText(parameter.getKey());
						Logs.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
						Logs.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,false));

						Combo listLogs = new Combo(deployDialog, SWT.DROP_DOWN | SWT.READ_ONLY);

						List<Value> values = parameter.getPossibleValues().getValue();
						for (Value value : values) {
							listLogs.add(value.getValue());
							deploytypeMaps.put(value.getValue(), value.getKey());
						}
						data = new GridData(GridData.FILL_BOTH);
						listLogs.select(0);
						listLogs.setLayoutData(data);
						deploymap.put(parameter.getKey(), listLogs); 

					} else if (type.equalsIgnoreCase("DynamicParameter")) {
						int yaxis = 0;
						String key = null;
						Label Logs = new Label(deployDialog, SWT.LEFT);
						Logs.setText(parameter.getKey() + ":");
						Logs.setBounds(24, 40, 80, 23);
						final List<String> buttons = new ArrayList<String>();

						String isMultiple = checkMultiple(processor, "deploy");

						if (StringUtils.isNotEmpty(isMultiple) && isMultiple.equalsIgnoreCase("true")) {						
							Group group = new Group(deployDialog, SWT.SHADOW_IN);
							group.setText(parameter.getKey());
							group.setLocation(146, 26);
							List<Value> dynParamPossibleValues  = (List<Value>) maps.get(parameter.getKey());		
							for (Value value : dynParamPossibleValues) {
								Button envSelectionButton = new Button(group, SWT.CHECK);
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
							deploymap.put(parameter.getKey(), buttons);
							group.pack();

						} else {
							Combo listLogs = new Combo(deployDialog,SWT.DROP_DOWN | SWT.READ_ONLY);
							final List<Value> dynParamPossibleValues  = (List<Value>) maps.get(parameter.getKey());
							if (CollectionUtils.isNotEmpty(dynParamPossibleValues)) {
								for (Value value : dynParamPossibleValues) {
									listLogs.add(value.getValue());
								}
							}
							data = new GridData(GridData.FILL_BOTH);
							listLogs.select(0);
							listLogs.setLayoutData(data);
							deploymap.put(parameter.getKey(), listLogs);

							listLogs.addListener(SWT.Selection, new Listener() {

								@Override
								public void handleEvent(Event event) {
									Combo combo = (Combo)event.widget;
									String value = combo.getText();
									Map<String, DependantParameters> map = changeEveDependancyListener(parameter.getKey(), value, maps);
									String dependency = parameter.getDependency();
									String[] split = dependency.split(",");
									Combo typecombo = null;
									for (String dep : split) {
										try {
											List<Value> updateDependancy = updateDependancy(parameter.getKey(),dep, map);
											typecombo = (Combo) deploymap.get(dep);
											typecombo.removeAll();
											for (Value val : updateDependancy) {
												typecombo.add(val.getValue());
											}
											typecombo.select(0);
											deploymap.put(parameter.getKey(), typecombo);

										} catch (IOException e) {
											e.printStackTrace();
										}
									}
								}
							});
						}
					} 
				}
			}

			Composite buttonComposite = new Composite(deployDialog, SWT.RIGHT);
			GridLayout buttonLayout = new GridLayout(2, false);
			buttonComposite.setLayout(buttonLayout);
			buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.END, true, true, 1, 1));

			deployButton = new Button(buttonComposite, SWT.RIGHT | SWT.PUSH);
			deployButton.setText(Messages.DEPLOY_BTN);

			cancelButton = new Button(buttonComposite, SWT.RIGHT | SWT.PUSH);
			cancelButton.setText(Messages.CANCEL);

		} catch (PhrescoException e) {
			e.printStackTrace();
		}
		return deployDialog;
	}


	public Map<String, DependantParameters> changeEveDependancyListener(String currentParemeterKey, String currentSelection, Map<String, Object> maps) {
		Map<String, DependantParameters> watcherMap = (Map<String, DependantParameters>) maps.get(WATCHER_MAP);
		DependantParameters currentParameters = watcherMap.get(currentParemeterKey); //parameter key
		if (currentParameters == null) {
			currentParameters = new DependantParameters();
		}
		currentParameters.setValue(currentSelection);
		watcherMap.put(currentParemeterKey, currentParameters);
		return watcherMap;
	}

	public List<Value> updateDependancy(String paramKey, String dependency, Map<String, DependantParameters> watcherMaps) throws IOException {
		List<Value> dependentPossibleValues = null;
		try {
			ApplicationInfo applicationInfo = PhrescoUtil.getApplicationInfo();
			MojoProcessor mojo = new MojoProcessor(PhrescoUtil.getDeployInfoConfigurationPath());

			List<Parameter> parameters = mojo.getParameters("deploy");
			for (Parameter parameter : parameters) {
				if (dependency != null) {
					// Get the values from the dynamic parameter class
					Parameter dependentParameter = mojo.getParameter("deploy", dependency);
					if (dependentParameter.getDynamicParameter() != null) {
						DynamicPossibleValues possibleValues = new DynamicPossibleValues();

						Map<String, Object> constructMapForDynVals = possibleValues.constructMapForDynVals(applicationInfo, watcherMaps, dependency);
						constructMapForDynVals.put("mojo", mojo);
						constructMapForDynVals.put("goal", "deploy");
						dependentPossibleValues = possibleValues.getDynamicPossibleValues(constructMapForDynVals, dependentParameter);

						if (CollectionUtils.isNotEmpty(dependentPossibleValues) && watcherMaps.containsKey(dependency)) {
							DependantParameters dependantParameters = (DependantParameters) watcherMaps.get(dependency);
							dependantParameters.setValue(dependentPossibleValues.get(0).getValue());
						} else {
							DependantParameters dependantParameters = (DependantParameters) watcherMaps.get(dependency);
							dependantParameters.setValue("");
						}
						if (CollectionUtils.isNotEmpty(dependentPossibleValues) && watcherMaps.containsKey(dependentPossibleValues.get(0).getDependency())) {
							possibleValues.addValueDependToWatcher(watcherMaps, dependentParameter.getKey(), dependentPossibleValues, "");
							if (CollectionUtils.isNotEmpty(dependentPossibleValues)) {
								possibleValues.addWatcher(watcherMaps, dependentParameter.getDependency(), 
										dependentParameter.getKey(), dependentPossibleValues.get(0).getValue());
							}
						}
					}
				} 
			}
		} catch (PhrescoException e) {
			e.printStackTrace();
		}
		return dependentPossibleValues;
	}


	private String checkMultiple(MojoProcessor processor, String goal) {
		List<Parameter> parameters = processor.getParameters(goal);
		String multiple = null;
		if (CollectionUtils.isNotEmpty(parameters)) {
			for (Parameter parameter : parameters) {
				String type = parameter.getType();
				if (type.equalsIgnoreCase(LIST)) {
					multiple = parameter.getMultiple();
				}
			}
		}
		return multiple;
	}

	public void saveCongfiguration() throws FileNotFoundException  {
		try {
			String databaseName = null;
			JSONObject jsonObject = new JSONObject();

			MojoProcessor processor = new MojoProcessor(PhrescoUtil.getDeployInfoConfigurationPath());
			List<Parameter> parameters = processor.getConfiguration(DEPLOY_GOAL).getParameters().getParameter();
			if (CollectionUtils.isNotEmpty(parameters)) {
				for (Parameter parameter : parameters) {
					if (parameter.getType().equalsIgnoreCase(STRING)) {
						Text nameText = (Text) deploymap.get(parameter.getKey());
						parameter.setValue(nameText.getText());
					}  else if (parameter.getType().equalsIgnoreCase(NUMBER)) {
						Text numberText = (Text) deploymap.get(parameter.getKey());
						parameter.setValue(numberText.getText());
					} else if (parameter.getType().equalsIgnoreCase(BOOLEAN)) {
						Button checkBoxButton = (Button) deploymap.get(parameter.getKey());
						boolean selection = checkBoxButton.getSelection();
						parameter.setValue(String.valueOf(selection));
					} else if (parameter.getType().equalsIgnoreCase(PASSWORD)) {
						Text passwordText = (Text) deploymap.get(parameter.getKey());
						String password = passwordText.getText();
						byte[] encodedPwd = Base64.encodeBase64(password.getBytes());
						String encodedString = new String(encodedPwd);
						parameter.setValue(encodedString);
					}  else if (parameter.getType().equalsIgnoreCase(LIST)) {
						Combo list =  (Combo) deploymap.get(parameter.getKey());
						String[] items = list.getItems();
						for (String string : items) {
							if (list.getText().equalsIgnoreCase(string)) {
								String value = deploytypeMaps.get(string);
								parameter.setValue(value);
							}
						}
					} else if (parameter.getType().equalsIgnoreCase(DYNAMIC_PARAMETER)) {
						String isMultiple = checkMultiple(processor, "deploy");
						if (StringUtils.isNotEmpty(isMultiple) && isMultiple.equalsIgnoreCase("true")) {				
							List<String> list =  (List<String>) deploymap.get(parameter.getKey());
							StringBuilder env = new StringBuilder();
							for (String string: list) {
								env.append(string);
								env.append(",");
							}
							String envValue = env.toString();
							envValue = envValue.substring(0, envValue.lastIndexOf(","));
							parameter.setValue(envValue); 
						} else {
							Combo list =  (Combo) deploymap.get(parameter.getKey());
							if (parameter.getKey().equalsIgnoreCase("database")) {
								databaseName = list.getText();
							}
							JSONArray sqlarray = new JSONArray();
							if (parameter.getKey().equalsIgnoreCase("fetchSql")) {
								String[] items = list.getItems();
								for (String string : items) {
									sqlarray.add(string);
								}
								jsonObject.put(databaseName, sqlarray);
								parameter.setValue(jsonObject.toJSONString());
							} else {
								parameter.setValue(list.getText());
							}
						}
					} else if (parameter.getType().equalsIgnoreCase("Hidden")) {
						int buildNo = getLatestBuildNo();
						if (buildNo != 0) {
							parameter.setValue(String.valueOf(buildNo));
						}
					} 
				}
			}
			processor.save();
		} catch (PhrescoException e) {
			e.printStackTrace();
		}
	}

	private int getLatestBuildNo() throws FileNotFoundException {
		int buildNo = 1;
		try {
			File buildInfoPath = PhrescoUtil.getBuildInfoPath();
			Type type = new TypeToken<List<BuildInfo>>() {}  .getType();
			FileReader reader = new FileReader(buildInfoPath);
			Gson gson = new Gson();
			List<BuildInfo> buildInfos = (List<BuildInfo>)gson.fromJson(reader, type);
			if (CollectionUtils.isNotEmpty(buildInfos)) {
				Collections.sort(buildInfos, new BuildComparator());
				buildNo = buildInfos.get(0).getBuildNo();
			}
		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		}
		return buildNo;
	}

	class BuildComparator implements Comparator<BuildInfo> {
		public int compare(BuildInfo buildInfo1, BuildInfo buildInfo2) {
			DateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy hh:mm:ss");
			Date  buildTime1 = new Date();
			Date buildTime2 = new Date();
			try {
				buildTime1 = (Date)formatter.parse(buildInfo1.getTimeStamp());
				buildTime2 = (Date)formatter.parse(buildInfo2.getTimeStamp());
			} catch (ParseException e) {
			}
			return buildTime2.compareTo(buildTime1);
		}
	}

}
