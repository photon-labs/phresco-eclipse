package com.photon.phresco.ui.phrescoexplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.handlers.HandlerUtil;

import com.photon.phresco.commons.ConfirmDialog;
import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.PhrescoDialog;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.commons.util.QualityUtil;
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
import com.photon.phresco.util.Constants;
import com.phresco.pom.exception.PhrescoPomException;


public class Unit  extends AbstractHandler implements PhrescoConstants {

	private Button unitButton;
	private Button cancelButton;
	private Button checkBoxButton;	

	private Button envSelectionButton;

	private Text nameText;
	private Text numberText;
	private Text passwordText;
	private Table testReport;
	Map<String, String> typeMaps = new HashMap<String, String>();

	private static Map<String, Object> map = new HashMap<String, Object>();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final Shell shell = HandlerUtil.getActiveShell(event);
		
		BaseAction baseAction = new BaseAction();
		ServiceManager serviceManager = PhrescoUtil.getServiceManager(baseAction.getUserId());
		if(serviceManager == null) {
			ConfirmDialog.getConfirmDialog().showConfirm(shell);
			return null;
		}

		final Shell dialog = new Shell(shell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
		dialog.setText(Messages.UNIT_TEST_REPORT);
		dialog.setLayout(new GridLayout(1, false));
		
		try {
			Composite composite = new Composite(dialog, SWT.NONE);
			composite.setLayout(new GridLayout(3, false));
			composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			final QualityUtil qualityUtil = new QualityUtil();
			Button testButon = new Button(composite, SWT.PUSH);
			testButon.setText(Messages.TEST);
			ApplicationInfo appInfo = PhrescoUtil.getApplicationInfo();
			List<String> unitReportOptions = getUnitReportOptions(appInfo.getAppDirName());
			String techReport = "";
			
			final Composite reportComposite = new Composite(dialog, SWT.NONE);
			reportComposite.setLayout(new GridLayout(1, false));
			reportComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
			if(CollectionUtils.isNotEmpty(unitReportOptions)) {
				Label techLabel = new Label(composite, SWT.NONE);
				techLabel.setText(Messages.TECHNOLOGY);
				String[] optionArray = unitReportOptions.toArray(new String[unitReportOptions.size()]);
				final Combo techCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
				techCombo.setItems(optionArray);
				techCombo.select(0);
				techReport = techCombo.getText();
				techCombo.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						try {
							Point size = dialog.getSize();
							if(testReport != null && !testReport.isDisposed()) {
								testReport.dispose();
							}
							testReport = qualityUtil.getTestReport(reportComposite, UNIT, techCombo.getText(), "");
							testReport.setSize(size);
						} catch (PhrescoException e1) {
							PhrescoDialog.exceptionDialog(dialog, e1);
						}
						super.widgetSelected(e);
					}
				});
			}

			testButon.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Shell unitDialog = unitDialog(dialog);
					unitDialog.open();
					super.widgetSelected(e);
				}
			});
			testReport = qualityUtil.getTestReport(reportComposite,UNIT, techReport, "");
		} catch (PhrescoException e1) {
			e1.printErrorStack();
			PhrescoDialog.exceptionDialog(dialog, e1);
		}

		Composite buttonComposite = new Composite(dialog, SWT.NONE);
		GridLayout buttonLayout = new GridLayout(2, false);
		buttonComposite.setLayout(buttonLayout);
		buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.END, false, false, 1, 1));

		Button cancelButton = new Button(buttonComposite, SWT.PUSH);
		cancelButton.setText(Messages.CANCEL);

		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				dialog.close();
				super.widgetSelected(e);
			}
		});

		dialog.open();
		return dialog;
	}

	public void saveCongfiguration()  {
		try {
			File unitTestInfoConfigurationPath = PhrescoUtil.getUnitTestInfoConfigurationPath();
			if (!unitTestInfoConfigurationPath.exists() || unitTestInfoConfigurationPath.length() < 0) {
				return;
			}
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
						if (checkBoxButton != null) {
							boolean selection = checkBoxButton.getSelection();
							System.out.println("Selection = " + selection);
							parameter.setValue(String.valueOf(selection));
						}
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
						if (StringUtils.isNotEmpty(envValue)) {
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



	public Shell unitDialog(Shell dialog) {

		final Shell unitDialog = new Shell(dialog, SWT.CLOSE | SWT.TITLE);
		unitDialog.setText(Messages.UNIT_TEST_TITLE);
		int min_height = 140;
		int comp_count_height = 10;
		GridLayout gridLayout = new GridLayout(1, false);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		unitDialog.setLayout(gridLayout);
		unitDialog.setLayoutData(data);
		
		final Composite composite = new Composite(unitDialog, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		try {
			File unitTestInfoConfigurationPath = PhrescoUtil.getUnitTestInfoConfigurationPath();
			if (unitTestInfoConfigurationPath.exists() && unitTestInfoConfigurationPath.length() > 0) {
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
						Label buildNameLabel = new Label(composite, SWT.NONE);
						buildNameLabel.setText(parameter.getName().getValue().get(0).getValue());
						buildNameLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP,false, false));

						nameText = new Text(composite, SWT.BORDER);
						nameText.setToolTipText(parameter.getKey());
						data = new GridData(GridData.FILL_BOTH);
						nameText.setLayoutData(data);
						min_height = min_height + comp_count_height;
						map.put(parameter.getKey(), nameText);

					} else if (type.equalsIgnoreCase(NUMBER)) {
						Label buildNumberLabel = new Label(composite, SWT.NONE);
						buildNumberLabel.setText(parameter.getName().getValue().get(0).getValue());
						buildNumberLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP,false, false));

						numberText = new Text(composite, SWT.BORDER);
						numberText.setToolTipText(parameter.getKey());
						numberText.setMessage(parameter.getKey());
						data = new GridData(GridData.FILL_BOTH);
						numberText.setLayoutData(data);
						min_height = min_height + comp_count_height;
						map.put(parameter.getKey(), numberText);

					} else if (type.equalsIgnoreCase(BOOLEAN)) {
						if (parameter.getKey().equalsIgnoreCase(SHOW_SETTINGS)) {
							continue;
						}
						Label defaults = new Label(composite, SWT.LEFT);
						defaults.setText(parameter.getName().getValue().get(0).getValue());
						defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

						checkBoxButton = new Button(composite, SWT.CHECK);
						checkBoxButton.setLayoutData(new GridData(75, 20));
						data = new GridData(GridData.FILL_BOTH);
						checkBoxButton.setLayoutData(data);
						min_height = min_height + comp_count_height;
						map.put(parameter.getKey(), checkBoxButton);
					}
					else if (type.equalsIgnoreCase(PASSWORD)) {
						Label defaults = new Label(composite, SWT.LEFT);
						defaults.setText(parameter.getName().getValue().get(0).getValue());
						defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

						passwordText = new Text(composite, SWT.PASSWORD | SWT.BORDER);
						passwordText.setToolTipText(PASSWORD);
						passwordText.setMessage(parameter.getKey());
						passwordText.setLayoutData(new GridData(100, 13));
						data = new GridData(GridData.FILL_BOTH);
						passwordText.setLayoutData(data);
						min_height = min_height + comp_count_height;
						map.put(parameter.getKey(), passwordText);

					} else if (type.equalsIgnoreCase(LIST)) {
						Label Logs = new Label(composite, SWT.LEFT);
						Logs.setText(parameter.getName().getValue().get(0).getValue());
						Logs.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,false));

						Combo listLogs = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);

						List<Value> values = parameter.getPossibleValues().getValue();
						for (Value value : values) {
							listLogs.add(value.getValue());
							typeMaps.put(value.getValue(), value.getKey());
						}
						data = new GridData(GridData.FILL_HORIZONTAL);
						listLogs.select(0);
						listLogs.setLayoutData(data);
						min_height = min_height + comp_count_height;
						map.put(parameter.getKey(), listLogs); 

					} else if (type.equalsIgnoreCase(DYNAMIC_PARAMETER)) {
						int yaxis = 0;
						String key = null;
						Label Logs = new Label(composite, SWT.LEFT);
						Logs.setText(parameter.getName().getValue().get(0).getValue());
						Logs.setBounds(24, 40, 80, 23);

						Group group = new Group(composite, SWT.SHADOW_IN);
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
						min_height = min_height + comp_count_height + group.getSize().y;
						map.put(key, buttons);
						group.pack();
					} 
				}
			}

			Composite buttonComposite = new Composite(unitDialog, SWT.NONE);
			GridLayout buttonLayout = new GridLayout(2, false);
			buttonComposite.setLayout(buttonLayout);
			buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.END, true, true, 1, 1));

			unitButton = new Button(buttonComposite, SWT.NONE);
			unitButton.setText(Messages.TEST);

			cancelButton = new Button(buttonComposite, SWT.NONE);
			cancelButton.setText(Messages.CANCEL);

			cancelButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					unitDialog.close();
					super.widgetSelected(e);
				}
			});

			unitButton.addListener(SWT.Selection, new Listener() {

				@Override
				public void handleEvent(Event event) {
					saveCongfiguration();
					BusyIndicator.showWhile(null, new Runnable() {
						@Override
						public void run() {
							ExecuteAction action = new ExecuteAction(PhrescoUtil.getUnitTestInfoConfigurationPath(),
									UNIT_TEST_GOAL, ActionType.UNIT_TEST, "UnitLogs");
							action.execute();	
							unitDialog.close();
						}
					});
				}
			});

		} catch (PhrescoException e) {
			e.printStackTrace();
		}
		unitDialog.setSize(370,min_height);
		return unitDialog;
	}

	/**
	 * Gets the unit report options.
	 *
	 * @param appDirName the app dir name
	 * @return the unit report options
	 * @throws PhrescoException the phresco exception
	 */
	private List<String> getUnitReportOptions(String appDirName) throws PhrescoException {
		try {
			String unitTestReportOptions = getUnitTestReportOptions(appDirName);
			if (StringUtils.isNotEmpty(unitTestReportOptions)) {
				return Arrays.asList(unitTestReportOptions.split(Constants.COMMA));
			}
		} catch (Exception e) {
			throw new PhrescoException(e);
		}
		return null;
	}

	/**
	 * Gets the unit test report options.
	 *
	 * @param appDirName the app dir name
	 * @return the unit test report options
	 * @throws PhrescoException the phresco exception
	 */
	private String getUnitTestReportOptions(String appDirName) throws PhrescoException {
		try {
			return PhrescoUtil.getPomProcessor().getProperty(Constants.PHRESCO_UNIT_TEST);
		} catch (PhrescoPomException e) {
			throw new PhrescoException(e);
		}
	}
}
