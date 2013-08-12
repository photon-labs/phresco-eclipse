package com.photon.phresco.ui.phrescoexplorer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.photon.phresco.commons.ConfirmDialog;
import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.PhrescoDialog;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.model.BuildInfo;
import com.photon.phresco.commons.model.Technology;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.dynamicParameter.DependantParameters;
import com.photon.phresco.dynamicParameter.DynamicPossibleValues;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value;
import com.photon.phresco.plugins.util.MojoProcessor;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.ui.model.ActionType;
import com.photon.phresco.ui.model.BaseAction;
import com.photon.phresco.ui.resource.Messages;

public class Deploy extends AbstractHandler implements PhrescoConstants {

	private Button deployButton;
	private Button cancelButton;
	private Button checkBoxButton;	

	private Shell deployDialog;	

	private Text nameText;
	private Text numberText;
	private Text passwordText;
	private Combo listLogs;

	Map<String, String> deploytypeMaps = new HashedMap();
	private static Map<String, Object> deploymap = new HashedMap();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		Shell shell = HandlerUtil.getActiveShell(event);

		BaseAction baseAction = new BaseAction();
		final ServiceManager serviceManager = PhrescoUtil.getServiceManager(baseAction.getUserId());
		if(serviceManager == null) {
			ConfirmDialog.getConfirmDialog().showConfirm(shell);
			return null;
		}
		
		try {
			Technology technology = serviceManager.getTechnology(PhrescoUtil.getTechId());
			List<String> options = technology.getOptions();
			if (CollectionUtils.isNotEmpty(options)) {
				if(!options.contains("Deploy")) {
					PhrescoDialog.errorDialog(shell, "Deploy", "Deploy is not Applicable");
					return null;
				}
			}
		} catch (PhrescoException e1) {
			e1.printStackTrace();
		}

		final Shell dialog = new Shell(shell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE);

		final Shell createDeployDialog = createDeployDialog(dialog);

		deployButton.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				try {
					saveCongfiguration();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				BusyIndicator.showWhile(null, new Runnable() {
					public void run() {
						ExecuteAction action = new ExecuteAction(PhrescoUtil.getDeployInfoConfigurationPath(),
								DEPLOY_GOAL, ActionType.DEPLOY, DEPLOY_LOGS);
						action.execute();
					}
				});
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

	public Shell createDeployDialog(Shell dialog) {
		deployDialog = new Shell(dialog, SWT.CLOSE | SWT.TITLE | SWT.RESIZE);
		deployDialog.setText(Messages.DEPLOY_DIALOG_TITLE);
		
		try {
			File buildInfoPath = PhrescoUtil.getBuildInfoPath();
			if(!buildInfoPath.exists()) {
				PhrescoDialog.errorDialog(dialog, Messages.WARNING, Messages.BUILD_NOT_AVAILABLE);
				return null;
			}
			List<BuildInfo> buildInfos = PhrescoUtil.getBuildInfos();
			if(CollectionUtils.isEmpty(buildInfos)) {
				PhrescoDialog.errorDialog(dialog, Messages.WARNING, Messages.BUILD_NOT_AVAILABLE);
				return null;
			}
		} catch (PhrescoException e1) {
			PhrescoDialog.exceptionDialog(dialog, e1);
			return null;
		}
		int dialog_height = 130;
		int comp_height = 17;
		
		GridLayout gridLayout = new GridLayout(1, false);
		GridData data = new GridData(GridData.FILL_BOTH);
		deployDialog.setLayout(gridLayout);
		deployDialog.setLayoutData(data);
		
		Composite composite = new Composite(deployDialog, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		try {
			if (PhrescoUtil.getDeployInfoConfigurationPath().exists()) {
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
						Label buildNameLabel = new Label(composite, SWT.NONE);
						buildNameLabel.setText(parameter.getName().getValue().get(0).getValue());

						nameText = new Text(composite, SWT.BORDER);
						nameText.setToolTipText(parameter.getKey());
						data = new GridData(GridData.FILL_BOTH);
						nameText.setLayoutData(data);
						dialog_height = dialog_height + comp_height;
						deploymap.put(parameter.getKey(), nameText);

					} else if (type.equalsIgnoreCase(NUMBER)) {
						Label buildNumberLabel = new Label(composite, SWT.NONE);
						buildNumberLabel.setText(parameter.getName().getValue().get(0).getValue());

						numberText = new Text(composite, SWT.BORDER);
						numberText.setToolTipText(parameter.getKey());
						numberText.setMessage(parameter.getKey());
						data = new GridData(GridData.FILL_BOTH);
						numberText.setLayoutData(data);
						dialog_height = dialog_height + comp_height;
						deploymap.put(parameter.getKey(), numberText);

					} else if (type.equalsIgnoreCase(BOOLEAN)) {
						Label defaults = new Label(composite, SWT.LEFT);
						defaults.setText(parameter.getName().getValue().get(0).getValue());

						checkBoxButton = new Button(composite, SWT.CHECK);
						data = new GridData(GridData.FILL_BOTH);
						checkBoxButton.setLayoutData(data);
						dialog_height = dialog_height + comp_height;
						deploymap.put(parameter.getKey(), checkBoxButton);
					}
					else if (type.equalsIgnoreCase(PASSWORD)) {
						Label defaults = new Label(composite, SWT.LEFT);
						defaults.setText(parameter.getName().getValue().get(0).getValue());

						passwordText = new Text(composite, SWT.PASSWORD | SWT.BORDER);
						passwordText.setToolTipText(PASSWORD);
						passwordText.setMessage(parameter.getKey());
						data = new GridData(GridData.FILL_BOTH);
						passwordText.setLayoutData(data);
						dialog_height = dialog_height + comp_height;
						deploymap.put(parameter.getKey(), passwordText);

					} else if (type.equalsIgnoreCase(LIST)) {

						Label Logs = new Label(composite, SWT.LEFT);
						Logs.setText(parameter.getName().getValue().get(0).getValue());

						Combo listLogs = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);

						List<Value> values = parameter.getPossibleValues().getValue();
						for (Value value : values) {
							listLogs.add(value.getValue());
							deploytypeMaps.put(value.getValue(), value.getKey());
						}
						data = new GridData(GridData.FILL_BOTH);
						listLogs.select(0);
						listLogs.setLayoutData(data);
						dialog_height = dialog_height + comp_height;
						deploymap.put(parameter.getKey(), listLogs); 

					} else if (type.equalsIgnoreCase(DYNAMIC_PARAMETER)) {
						final List<String> buttons = new ArrayList<String>();

						String isMultiple = checkMultiple(processor, DEPLOY);

						if (StringUtils.isNotEmpty(isMultiple) && isMultiple.equalsIgnoreCase("true")) {	
							
							Label Logs = new Label(composite, SWT.LEFT);
							Logs.setText(parameter.getName().getValue().get(0).getValue());
							
							Group group = new Group(composite, SWT.SHADOW_IN);
							group.setText(parameter.getName().getValue().get(0).getValue());
							List<Value> dynParamPossibleValues  = (List<Value>) maps.get(parameter.getKey());		
							for (Value value : dynParamPossibleValues) {
								dialog_height = dialog_height + comp_height;
								Button envSelectionButton = new Button(group, SWT.CHECK);
								envSelectionButton.setText(value.getValue());
								data = new GridData(GridData.FILL_BOTH);
								envSelectionButton.setLayoutData(data);
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
							data = new GridData(GridData.FILL_BOTH);
							final List<Value> dynParamPossibleValues  = (List<Value>) maps.get(parameter.getKey());
							
							if (parameter.getKey().equalsIgnoreCase("fetchSql")) {
								Label dynamicLabel = new Label(composite, SWT.LEFT);
								dynamicLabel.setText(parameter.getName().getValue().get(0).getValue());
								
								org.eclipse.swt.widgets.List list = new org.eclipse.swt.widgets.List(composite,  SWT.BORDER | SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL);
								list.setLayoutData(data);
								if (CollectionUtils.isNotEmpty(dynParamPossibleValues)) {
									for (Value value : dynParamPossibleValues) {
										list.add(value.getValue());
									}
								}
								dynamicLabel.setVisible(false);
								list.setVisible(false);
								deploymap.put(parameter.getKey() + "Label", dynamicLabel);
								deploymap.put(parameter.getKey(), list);
								
							} else {
								Label dynamicLabel = new Label(composite, SWT.LEFT);
								dynamicLabel.setText(parameter.getName().getValue().get(0).getValue());
								listLogs = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL);
								listLogs.setLayoutData(data);
								if (CollectionUtils.isNotEmpty(dynParamPossibleValues)) {
									for (Value value : dynParamPossibleValues) {
										listLogs.add(value.getValue());
									}
								}
								listLogs.select(0);
								deploymap.put(parameter.getKey(), listLogs);
								if (parameter.getKey().equalsIgnoreCase("dataBase")) {
									listLogs.setVisible(false);
									dynamicLabel.setVisible(false);
									deploymap.put(parameter.getKey() + "Label", dynamicLabel);
								}
							}

							listLogs.addListener(SWT.Selection, new Listener() {

								@Override
								public void handleEvent(Event event) {
									Combo combo = (Combo)event.widget;
									String value = combo.getText();
									Map<String, DependantParameters> map = changeEveDependancyListener(parameter.getKey(), value, maps);
									String dependency = parameter.getDependency();
									if (StringUtils.isNotEmpty(dependency)) {
										String[] split = dependency.split(",");
										Combo typecombo = null;
										for (String dep : split) {
											try {
												List<Value> updateDependancy = updateDependancy(parameter.getKey(),dep, map);
												if (dep.equalsIgnoreCase("fetchSql")) {
													org.eclipse.swt.widgets.List list = (org.eclipse.swt.widgets.List) deploymap.get(dep);
													list.removeAll();
													for (Value val : updateDependancy) {
														list.add(val.getValue());
													}
													deploymap.put(dep, list);
													
												} else {
													if (dep.equalsIgnoreCase("dataBase")) {
														typecombo = (Combo) deploymap.get(dep);
														typecombo.removeAll();
														for (Value val : updateDependancy) {
															typecombo.add(val.getValue());
														}
														typecombo.select(0);
														deploymap.put(dep, typecombo);
													}
												}
											} catch (IOException e) {
												e.printStackTrace();
											}
										}
									}
								}
							});
							
							if (checkBoxButton != null) {
								checkBoxButton.addListener(SWT.Selection, new Listener() {
									
									@Override
									public void handleEvent(Event event) {
										Button button = (Button) event.widget;
										boolean selection = button.getSelection();
										org.eclipse.swt.widgets.List list = (org.eclipse.swt.widgets.List) deploymap.get("fetchSql");
										Label fetchSqllabel = (Label) deploymap.get("fetchSqlLabel");
										Combo combolist = (Combo) deploymap.get("dataBase");
										Label databaseLabel = (Label) deploymap.get("dataBaseLabel");
										if (selection) {
											list.setVisible(true);
											fetchSqllabel.setVisible(true);
											combolist.setVisible(true);
											databaseLabel.setVisible(true);
										} else {
											list.setVisible(false);
											fetchSqllabel.setVisible(false);
											combolist.setVisible(false);
											databaseLabel.setVisible(false);
										}
									}
								});
							}
							
							dialog_height = dialog_height + comp_height;
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
		deployDialog.setSize(381,dialog_height);
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

			List<Parameter> parameters = mojo.getParameters(DEPLOY);
			for (Parameter parameter : parameters) {
				if (dependency != null) {
					// Get the values from the dynamic parameter class
					Parameter dependentParameter = mojo.getParameter(DEPLOY, dependency);
					if (dependentParameter.getDynamicParameter() != null) {
						DynamicPossibleValues possibleValues = new DynamicPossibleValues();

						Map<String, Object> constructMapForDynVals = possibleValues.constructMapForDynVals(applicationInfo, watcherMaps, dependency);
						constructMapForDynVals.put(MOJO, mojo);
						constructMapForDynVals.put(GOAL, DEPLOY);
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

			File deployFileConfigurationPath = PhrescoUtil.getDeployInfoConfigurationPath();
			if (!deployFileConfigurationPath.exists() && deployFileConfigurationPath.length() > 0) {
				return;
			}
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
						String isMultiple = checkMultiple(processor, DEPLOY);
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
							if (parameter.getKey().equalsIgnoreCase("database")) {
								Object object = deploymap.get(parameter.getKey());
								if (object instanceof Combo) {
									Combo list =  (Combo) deploymap.get(parameter.getKey());
									parameter.setValue(list.getText());
									databaseName = list.getText();
								}
							}
							JSONArray sqlarray = new JSONArray();
							if (parameter.getKey().equalsIgnoreCase("fetchSql")) {
								org.eclipse.swt.widgets.List list = (org.eclipse.swt.widgets.List) deploymap.get(parameter.getKey());
								String[] items = list.getItems();
								for (String string : items) {
									sqlarray.add(string);
								}
								jsonObject.put(databaseName, sqlarray);
								parameter.setValue(jsonObject.toJSONString());
							} else {
								Object object = deploymap.get(parameter.getKey());
								if (object instanceof Combo) {
									Combo list =  (Combo) deploymap.get(parameter.getKey());
									parameter.setValue(list.getText());
								}
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
