package com.photon.phresco.ui.phrescoexplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
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
import com.photon.phresco.ui.resource.Messages;
import com.photon.phresco.util.Constants;
import com.phresco.pom.exception.PhrescoPomException;
import com.phresco.pom.util.PomProcessor;

public class Functional extends AbstractHandler implements PhrescoConstants {

	private Text nameText;
	private Text numberText;
	private Text passwordText;
	private Button functionalButton;
	private Button cancelButton;
	private Button checkBoxButton;	

	private Button envSelectionButton;
	private String path;

	Map<String, String> typeMaps = new HashMap<String, String>();
	private static Map<String, Object> map = new HashMap<String, Object>();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);
		final Shell functionalDialog = new Shell(shell, SWT.CENTER | SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
		functionalDialog.setLayout(new GridLayout(1, false));
		functionalDialog.setText(Messages.FUN_TEST_REPORT);
		
		ServiceManager serviceManager = PhrescoUtil.getServiceManager(PhrescoUtil.getUserId());
		if(serviceManager == null) {
			ConfirmDialog.getConfirmDialog().showConfirm(shell);
			return null;
		}

		try {
			PomProcessor pomProcessor = PhrescoUtil.getPomProcessor();
			String seleniumToolType = pomProcessor.getProperty(Constants.POM_PROP_KEY_FUNCTEST_SELENIUM_TOOL);
			String phrescoPluginInfoFilePath = PhrescoUtil.getPhrescoPluginInfoFilePath(Constants.PHASE_FUNCTIONAL_TEST, "");
			final MojoProcessor processor = new MojoProcessor(new File(phrescoPluginInfoFilePath));
			Configuration configuration = processor.getConfiguration(Constants.PHASE_FUNCTIONAL_TEST + HYPHEN + seleniumToolType);
			final List<Parameter> parameters = configuration.getParameters().getParameter();

			Button testButton = new Button(functionalDialog, SWT.PUSH);
			testButton.setText(Messages.TEST);
			final QualityUtil qualityUtil = new QualityUtil();
			testButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Shell generateDialog = createFunctionalDialog(functionalDialog, parameters, processor);
					generateDialog.open();
					super.widgetSelected(e);
				}
			});
			
			qualityUtil.getTestReport(functionalDialog, FUNCTIONAL, "", "");
		} catch (PhrescoException e1) {
			PhrescoDialog.exceptionDialog(functionalDialog, e1);
		} catch (PhrescoPomException e2) {
			PhrescoDialog.exceptionDialog(functionalDialog, e2);
		}
		
		Composite buttonComposite = new Composite(functionalDialog, SWT.RIGHT);
		GridLayout buttonLayout = new GridLayout(2, false);
		buttonComposite.setLayout(buttonLayout);
		buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.END, false, false, 1, 1));
		
		Button cancelButton = new Button(buttonComposite, SWT.PUSH);
		cancelButton.setText(Messages.CANCEL);
		
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				functionalDialog.close();
				super.widgetSelected(e);
			}
		});
		
		functionalDialog.open();
		return null;
	}

	/**
	 * @param functionalDialog
	 */
	private void runFunctionalTest(final Shell functionalDialog) {
		try {
			String goal = PhrescoUtil.getPomProcessor().getProperty(Constants.POM_PROP_KEY_FUNCTEST_SELENIUM_TOOL);
			String phrescoFunctionalFilePath = PhrescoUtil.getPhrescoPluginInfoFilePath(FUNCTIONAL, "");
			goal = Constants.PHASE_FUNCTIONAL_TEST + HYPHEN + goal;
			ExecuteAction action = new ExecuteAction(new File(phrescoFunctionalFilePath), goal, ActionType.FUNCTIONAL_TEST, "Functional Test");
			action.execute();
		} catch (PhrescoException e) {
			PhrescoDialog.exceptionDialog(functionalDialog, e);
		} catch (PhrescoPomException e) {
			PhrescoDialog.exceptionDialog(functionalDialog, e);
		}
	}


	public Shell createFunctionalDialog(final Shell dialog, final List<Parameter> parameters, MojoProcessor processor) {

		final Shell functionalDialog = new Shell(dialog, SWT.CLOSE | SWT.TITLE | SWT.RESIZE);
		functionalDialog.setText("Functional");
		
		int dialog_height = 130;
		int comp_height = 17;

		GridLayout gridLayout = new GridLayout(1, false);
		GridData data = new GridData(GridData.FILL_BOTH);
		functionalDialog.setLayout(gridLayout);
		functionalDialog.setLayoutData(data);
		
		Composite composite = new Composite(functionalDialog, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		try {
			ApplicationInfo applicationInfo = PhrescoUtil.getApplicationInfo();
			DynamicPossibleValues possibleValues = new DynamicPossibleValues();
			Map<String, DependantParameters> watcherMap = new HashMap<String, DependantParameters>();
			PomProcessor pomProcessor = PhrescoUtil.getPomProcessor();
			String seleniumToolType = pomProcessor.getProperty(Constants.POM_PROP_KEY_FUNCTEST_SELENIUM_TOOL);
			String goal = Constants.PHASE_FUNCTIONAL_TEST + HYPHEN + seleniumToolType;
			Map<String, Object> maps = possibleValues.setPossibleValuesInReq(processor, applicationInfo, parameters, watcherMap, goal);

			for (final Parameter parameter : parameters) {
				String type = parameter.getType();

				if (type.equalsIgnoreCase(STRING)) {
					Label buildNameLabel = new Label(composite, SWT.NONE);
					buildNameLabel.setText(parameter.getName().getValue().get(0).getValue());
					buildNameLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP,false, false));

					nameText = new Text(composite, SWT.BORDER);
					nameText.setToolTipText(parameter.getKey());
					data = new GridData(GridData.FILL_BOTH);
					nameText.setLayoutData(data);
					dialog_height = dialog_height + comp_height;
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
					dialog_height = dialog_height + comp_height;
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
					dialog_height = dialog_height + comp_height;
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
					dialog_height = dialog_height + comp_height;
					map.put(parameter.getKey(), passwordText);
					
				}	
				else if (type.equalsIgnoreCase(FILE_BROWSE)) {
				
					final Composite buttonComposite = new Composite(functionalDialog, SWT.NONE);
					GridLayout buttonLayout = new GridLayout(3, false);
					buttonComposite.setLayout(buttonLayout);
					buttonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					
					Label defaults = new Label(buttonComposite, SWT.LEFT);
					defaults.setText(parameter.getName().getValue().get(0).getValue());
					defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
					
					final Text text = new Text(buttonComposite, SWT.BORDER);
					text.setToolTipText(parameter.getKey());
					GridData fill_data = new GridData(GridData.FILL_HORIZONTAL);
					text.setLayoutData(fill_data);
					
					Button button = new Button(buttonComposite, SWT.PUSH);
					button.setText(BROWSE);
					button.setLayoutData(new GridData(SWT.RIGHT, SWT.END, false, false));
					
					button.addListener(SWT.Selection, new Listener() {
						@Override
						public void handleEvent(Event event) {
							FileDialog fileDialog = new FileDialog(new Shell(), SWT.SAVE);
							fileDialog.setFilterPath(PhrescoUtil.getApplicationHome() + File.separator + DO_NOT_CHECKIN_DIR + File.separator + TARGET);
							fileDialog.setText(BROWSE);
							path = fileDialog.open();
							text.setText(path);
							map.put(parameter.getKey(), text);
						}
					});
				}
				
				else if (type.equalsIgnoreCase(LIST)) {
					Label Logs = new Label(composite, SWT.LEFT);
					Logs.setText(parameter.getName().getValue().get(0).getValue());
					Logs.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,false));

					Combo listLogs = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);

					List<Value> values = parameter.getPossibleValues().getValue();
					for (Value value : values) {
						listLogs.add(value.getValue());
						typeMaps.put(value.getValue(), value.getKey());
					}
					data = new GridData(GridData.FILL_BOTH);
					listLogs.select(0);
					listLogs.setLayoutData(data);
					dialog_height = dialog_height + comp_height;
					map.put(parameter.getKey(), listLogs); 

				} else if (type.equalsIgnoreCase(DYNAMIC_PARAMETER)) {
					String key = null;
					Label Logs = new Label(composite, SWT.LEFT);
					Logs.setText(parameter.getName().getValue().get(0).getValue());

					Group group = new Group(composite, SWT.NONE);
					group.setText(Messages.ENVIRONMENT);
					group.setLayout(new GridLayout(1, false));
					group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

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
							dialog_height = dialog_height + comp_height;
							envSelectionButton.pack();
						}
					}
					map.put(parameter.getKey(), buttons);
					dialog_height = dialog_height + comp_height;
				} 
			}


			Composite buttonComposite = new Composite(functionalDialog, SWT.RIGHT);
			GridLayout buttonLayout = new GridLayout(2, false);
			buttonComposite.setLayout(buttonLayout);
			buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.END, true, true, 1, 1));
			
			functionalButton = new Button(buttonComposite, SWT.NONE);
			functionalButton.setText(Messages.TEST);

			cancelButton = new Button(buttonComposite, SWT.NONE);
			cancelButton.setText(Messages.CANCEL);
			
			cancelButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					functionalDialog.close();
					super.widgetSelected(e);
				}
			});

			functionalButton.addListener(SWT.Selection, new Listener() {

				@Override
				public void handleEvent(Event event) {
					try {
						saveCongfiguration();
					} catch (PhrescoException e) {
						PhrescoDialog.exceptionDialog(functionalDialog, e);
					}
					BusyIndicator.showWhile(null, new Runnable() {
						public void run() {
							runFunctionalTest(dialog);
						}
					});
					dialog.close();
				}
			});
			
		} catch (PhrescoException e) {
			PhrescoDialog.exceptionDialog(functionalDialog, e);
		} catch (PhrescoPomException e) {
			PhrescoDialog.exceptionDialog(functionalDialog, e);
		}
		functionalDialog.setSize(400, dialog_height);
		return functionalDialog;
	}

	public void saveCongfiguration() throws PhrescoException  {
		try {
			String phrescoPluginInfoFilePath = PhrescoUtil.getPhrescoPluginInfoFilePath(Constants.PHASE_FUNCTIONAL_TEST, "");
			MojoProcessor processor = new MojoProcessor(new File(phrescoPluginInfoFilePath));
			ApplicationInfo applicationInfo = PhrescoUtil.getApplicationInfo();
			String seleniumToolType = PhrescoUtil.getSeleniumToolType(applicationInfo);
			List<Parameter> parameters = processor.getConfiguration(Constants.PHASE_FUNCTIONAL_TEST + HYPHEN + seleniumToolType).getParameters().getParameter();
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
							parameter.setValue(String.valueOf(selection));
						}
					} else if (parameter.getType().equalsIgnoreCase(PASSWORD)) {
						Text passwordText = (Text) map.get(parameter.getKey());
						String password = passwordText.getText();
						byte[] encodedPwd = Base64.encodeBase64(password.getBytes());
						String encodedString = new String(encodedPwd);
						parameter.setValue(encodedString);
					} else if (parameter.getType().equalsIgnoreCase(FILE_BROWSE)) {
						Text text = (Text) map.get(parameter.getKey());
						if (text != null) {
							parameter.setValue(text.getText());
						}
					}
					else if (parameter.getType().equalsIgnoreCase(LIST)) {
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
						if(CollectionUtils.isNotEmpty(list)) {
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
			throw new PhrescoException(e);
		} catch (PhrescoPomException e) {
			throw new PhrescoException(e);
		}
	}
}
