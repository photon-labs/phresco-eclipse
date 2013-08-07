package com.photon.phresco.ui.phrescoexplorer;

import java.io.BufferedInputStream;
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
import com.photon.phresco.commons.PhrescoDialog;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.model.ProjectInfo;
import com.photon.phresco.commons.util.ConsoleViewManager;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.commons.util.QualityUtil;
import com.photon.phresco.dynamicParameter.DependantParameters;
import com.photon.phresco.dynamicParameter.DynamicPossibleValues;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.framework.api.ActionType;
import com.photon.phresco.framework.api.ApplicationManager;
import com.photon.phresco.framework.impl.ApplicationManagerImpl;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value;
import com.photon.phresco.plugins.util.MojoProcessor;
import com.photon.phresco.service.client.api.ServiceManager;
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

	Map<String, String> typeMaps = new HashMap<String, String>();
	private static Map<String, Object> map = new HashMap<String, Object>();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);
		final Shell functionalDialog = new Shell(shell, SWT.CENTER | SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
		functionalDialog.setLayout(new GridLayout(1, false));
		
		ServiceManager serviceManager = PhrescoUtil.getServiceManager(PhrescoUtil.getUserId());
		if(serviceManager == null) {
			ConfirmDialog.getConfirmDialog().showConfirm(shell);
			return null;
		}

		try {
			ApplicationInfo applicationInfo = PhrescoUtil.getApplicationInfo();
			PomProcessor pomProcessor = PhrescoUtil.getPomProcessor(applicationInfo.getAppDirName());
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
					/*try {
						qualityUtil.getTestReport(functionalDialog, FUNCTIONAL, "", "");
					} catch (PhrescoException e1) {
						PhrescoDialog.exceptionDialog(functionalDialog, e1);
					}*/
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
		buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.END, true, true, 1, 1));
		
		Button cancelButton = new Button(buttonComposite, SWT.PUSH);
		cancelButton.setText(Messages.CANCEL);
		
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				functionalDialog.close();
				super.widgetSelected(e);
			}
		});
		
		functionalDialog.setSize(600, 400);
		functionalDialog.open();
		return null;
	}

	/**
	 * @param functionalDialog
	 */
	private void runFunctionalTest(List<Parameter> parameters, final Shell functionalDialog) {
		ApplicationManager applicationManager = new ApplicationManagerImpl();
		try {
			ProjectInfo projectInfo = PhrescoUtil.getProjectInfo();
			String applicationHome = PhrescoUtil.getApplicationHome();
			List<String> mavenArgCommands = PhrescoUtil.getMavenArgCommands(parameters);
			BufferedInputStream performAction = applicationManager.performAction(projectInfo, ActionType.FUNCTIONAL_TEST, mavenArgCommands, applicationHome);

			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(performAction));
			ConsoleViewManager.getDefault("functional test").println(bufferedReader);
		} catch (PhrescoException e) {
			PhrescoDialog.exceptionDialog(functionalDialog, e);
		}
	}


	public Shell createFunctionalDialog(final Shell dialog, final List<Parameter> parameters, MojoProcessor processor) {

		final Shell functionalTestDialog = new Shell(dialog, SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.MAX | SWT.RESIZE);
		functionalTestDialog.setText("Functional");
		functionalTestDialog.setLocation(385, 130);
		functionalTestDialog.setSize(451,188);

		GridLayout gridLayout = new GridLayout(2, false);
		GridData data = new GridData(GridData.FILL_BOTH);
		functionalTestDialog.setLayout(gridLayout);
		functionalTestDialog.setLayoutData(data);

		try {
			ApplicationInfo applicationInfo = PhrescoUtil.getApplicationInfo();
			DynamicPossibleValues possibleValues = new DynamicPossibleValues();
			Map<String, DependantParameters> watcherMap = new HashMap<String, DependantParameters>();
			Map<String, Object> maps = possibleValues.setPossibleValuesInReq(processor, applicationInfo, parameters, watcherMap, "functional-test-webdriver");

			for (Parameter parameter : parameters) {
				String type = parameter.getType();

				if (type.equalsIgnoreCase(STRING)) {
					Label buildNameLabel = new Label(functionalTestDialog, SWT.NONE);
					buildNameLabel.setText(parameter.getKey());
					buildNameLabel.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					buildNameLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP,false, false));

					nameText = new Text(functionalTestDialog, SWT.BORDER);
					nameText.setToolTipText(parameter.getKey());
					data = new GridData(GridData.FILL_BOTH);
					nameText.setLayoutData(data);
					map.put(parameter.getKey(), nameText);

				} else if (type.equalsIgnoreCase(NUMBER)) {
					Label buildNumberLabel = new Label(functionalTestDialog, SWT.NONE);
					buildNumberLabel.setText(parameter.getKey());
					buildNumberLabel.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					buildNumberLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP,false, false));

					numberText = new Text(functionalTestDialog, SWT.BORDER);
					numberText.setToolTipText(parameter.getKey());
					numberText.setMessage(parameter.getKey());
					data = new GridData(GridData.FILL_BOTH);
					numberText.setLayoutData(data);
					map.put(parameter.getKey(), numberText);

				} else if (type.equalsIgnoreCase(BOOLEAN)) {
					Label defaults = new Label(functionalTestDialog, SWT.LEFT);
					defaults.setText(parameter.getKey());
					defaults.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

					checkBoxButton = new Button(functionalTestDialog, SWT.CHECK);
					checkBoxButton.setLayoutData(new GridData(75, 20));
					data = new GridData(GridData.FILL_BOTH);
					checkBoxButton.setLayoutData(data);

					map.put(parameter.getKey(), checkBoxButton);
				}
				else if (type.equalsIgnoreCase(PASSWORD)) {
					Label defaults = new Label(functionalTestDialog, SWT.LEFT);
					defaults.setText(parameter.getKey());
					defaults.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

					passwordText = new Text(functionalTestDialog, SWT.PASSWORD | SWT.BORDER);
					passwordText.setToolTipText(PASSWORD);
					passwordText.setMessage(parameter.getKey());
					passwordText.setLayoutData(new GridData(100, 13));
					data = new GridData(GridData.FILL_BOTH);
					passwordText.setLayoutData(data);
					map.put(parameter.getKey(), passwordText);
				}	else if (type.equalsIgnoreCase(LIST)) {
					Label Logs = new Label(functionalTestDialog, SWT.LEFT);
					Logs.setText(parameter.getKey());
					Logs.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					Logs.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,false));

					Combo listLogs = new Combo(functionalTestDialog, SWT.DROP_DOWN | SWT.READ_ONLY);

					List<Value> values = parameter.getPossibleValues().getValue();
					for (Value value : values) {
						listLogs.add(value.getValue());
						typeMaps.put(value.getValue(), value.getKey());
					}
					data = new GridData(GridData.FILL_BOTH);
					listLogs.select(0);
					listLogs.setLayoutData(data);
					map.put(parameter.getKey(), listLogs); 

				} else if (type.equalsIgnoreCase(DYNAMIC_PARAMETER)) {
					int yaxis = 0;
					String key = null;
					Label Logs = new Label(functionalTestDialog, SWT.LEFT);
					Logs.setText(Messages.ENVIRONMENT + Messages.COLAN);
					Logs.setBounds(24, 40, 80, 23);

					Group group = new Group(functionalTestDialog, SWT.SHADOW_IN);
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


			Composite buttonComposite = new Composite(functionalTestDialog, SWT.RIGHT);
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
					functionalTestDialog.close();
					super.widgetSelected(e);
				}
			});

			functionalButton.addListener(SWT.Selection, new Listener() {

				@Override
				public void handleEvent(Event event) {
					try {
						saveCongfiguration();
					} catch (PhrescoException e) {
						PhrescoDialog.exceptionDialog(functionalTestDialog, e);
					}
					BusyIndicator.showWhile(null, new Runnable() {
						public void run() {
							runFunctionalTest(parameters, dialog);
						}
					});
					dialog.close();
				}
			});
			
		} catch (PhrescoException e) {
			PhrescoDialog.exceptionDialog(functionalTestDialog, e);
		}
		return functionalTestDialog;
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
						boolean selection = checkBoxButton.getSelection();
						parameter.setValue(String.valueOf(selection));
					} else if (parameter.getType().equalsIgnoreCase(PASSWORD)) {
						Text passwordText = (Text) map.get(parameter.getKey());
						String password = passwordText.getText();
						byte[] encodedPwd = Base64.encodeBase64(password.getBytes());
						String encodedString = new String(encodedPwd);
						parameter.setValue(encodedString);
					}else if (parameter.getType().equalsIgnoreCase(LIST)) {
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
