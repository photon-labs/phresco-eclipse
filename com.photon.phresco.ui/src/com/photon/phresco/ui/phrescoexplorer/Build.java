package com.photon.phresco.ui.phrescoexplorer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
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
import org.eclipse.ui.handlers.HandlerUtil;

import com.photon.phresco.commons.ConfirmDialog;
import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.model.ApplicationInfo;
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

public class Build extends AbstractHandler implements PhrescoConstants {

	private Button buildButton;
	private Button cancelButton;

	private Shell buildDialog;	
	private Shell generateDialog;
//	private Button envSelectionButton;
	Map<String, String> typeMaps = new HashMap<String, String>();

	@SuppressWarnings("unchecked")
	private static Map<String, Object> map = new HashedMap();

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);

		BaseAction baseAction = new BaseAction();
		ServiceManager serviceManager = PhrescoUtil.getServiceManager(baseAction.getUserId());
		if(serviceManager == null) {
			ConfirmDialog.getConfirmDialog().showConfirm(shell);
			return null;
		}

		final Shell buildDialog = new Shell(shell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);

		generateDialog = createBuildDialog(buildDialog);

		Listener generatePopupListener = new Listener() {
			@Override
			public void handleEvent(Event events) {
				saveCongfiguration();
				BusyIndicator.showWhile(null, new Runnable() {
					public void run() {
						ExecuteAction action = new ExecuteAction(PhrescoUtil.getPackageInfoConfigurationPath(),
								PACKAGE_GOAL, ActionType.BUILD, "BuildLogs");
						action.execute();
					}
				});

				generateDialog.close();
			}
		};

		Listener generatePopupCancelListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				generateDialog.setVisible(false);
			}
		};

		generateDialog.open();
		buildButton.addListener(SWT.Selection, generatePopupListener);
		cancelButton.addListener(SWT.Selection, generatePopupCancelListener);

		return null;
	}


	/**
	 * @wbp.parser.entryPoint
	 */
	public Shell createBuildDialog(Shell dialog) {
		buildDialog = new Shell(dialog, SWT.CLOSE | SWT.TITLE);

		int dialog_height = 130;
		int comp_height = 22;

		buildDialog.setText(Messages.BUILD);
		buildDialog.setLayout(new GridLayout(1, false));
		buildDialog.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite composite = new Composite(buildDialog, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		try {
			MojoProcessor processor = new MojoProcessor(PhrescoUtil.getPackageInfoConfigurationPath());
			Configuration configuration = processor.getConfiguration(PACKAGE_GOAL);
			List<Parameter> parameters = configuration.getParameters().getParameter();

			ApplicationInfo applicationInfo = PhrescoUtil.getProjectInfo().getAppInfos().get(0);
			DynamicPossibleValues possibleValues = new DynamicPossibleValues();
			Map<String, DependantParameters> watcherMap = new HashMap<String, DependantParameters>();
			Map<String, Object> maps = possibleValues.setPossibleValuesInReq(processor, applicationInfo, parameters, watcherMap, PACKAGE_GOAL);

			for (Parameter parameter : parameters) {
				if (parameter.getKey().equalsIgnoreCase(PACK_MINIFIED_FILE)) {
					continue;
				}
				
				if (parameter.getType().equalsIgnoreCase(DYNAMIC_PARAMETER)) {
					String key = null;
					Label Logs = new Label(composite, SWT.LEFT);
					if (parameter.getKey().equalsIgnoreCase(ENVIRONMENT_NAMES)) {
						Logs.setText(parameter.getName().getValue().get(0).getValue() + ASTERICK);
					} else {
						Logs.setText(parameter.getName().getValue().get(0).getValue());
					}
					Group group = new Group(composite, SWT.SHADOW_IN);
					group.setText(parameter.getName().getValue().get(0).getValue());
					group.setLayout(new GridLayout(1, false));
					group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

					final List<String> buttons = new ArrayList<String>();
					Set<Entry<String,Object>> entrySet = maps.entrySet();

					for (Entry<String, Object> entry : entrySet) {
						key = entry.getKey();
						if (key.equalsIgnoreCase(WATCHER_MAP)) {
							continue;
						}
						@SuppressWarnings("unchecked")
						List<Value> values = (List<Value>) entry.getValue();
						if (CollectionUtils.isNotEmpty(values) && key.equals(parameter.getKey()) && Boolean.valueOf(parameter.getMultiple())) {
							for (Value value : values) {
								Button envSelectionButton = new Button(group, SWT.CHECK);
								envSelectionButton.setText(value.getValue());
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
								dialog_height = dialog_height + comp_height + 3;
								envSelectionButton.pack();
							}
						} else if (key.equals(parameter.getKey())) {
							final Combo dynParamCombo = new Combo(group, SWT.READ_ONLY | SWT.BORDER);
							for (Value value : values) {
								dynParamCombo.add(value.getValue());
							}
							dynParamCombo.select(0);
							buttons.add(0, dynParamCombo.getText());
							dynParamCombo.addSelectionListener(new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent e) {
									buttons.set(0, dynParamCombo.getText());
									super.widgetSelected(e);
								}
							});
							dialog_height = dialog_height + comp_height + 3;
						}
					}
					map.put(parameter.getKey(), buttons);
					group.pack();
				} else if (parameter.getType().equalsIgnoreCase(BOOLEAN)) {
					if (parameter.getKey().equalsIgnoreCase(SHOW_SETTINGS)) {
						continue;
					}
					Label defaults = new Label(composite, SWT.LEFT);
					defaults.setText(parameter.getName().getValue().get(0).getValue());
					defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

					Button checkBoxButton = new Button(composite, SWT.CHECK);
					checkBoxButton.setLayoutData(new GridData(75, 20));
					GridData data = new GridData(GridData.FILL_BOTH);
					checkBoxButton.setLayoutData(data);
					dialog_height = dialog_height + comp_height;
					map.put(parameter.getKey(), checkBoxButton);
				} else if (parameter.getKey().equalsIgnoreCase(PACKAGE_TYPE) &&  parameter.getType().equalsIgnoreCase(LIST)) {
					Label Logs = new Label(composite, SWT.LEFT);
					Logs.setText(parameter.getName().getValue().get(0).getValue());
					Logs.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,false));

					Combo listLogs = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);

					List<Value> values = parameter.getPossibleValues().getValue();
					for (Value value : values) {
						listLogs.add(value.getValue());
						typeMaps.put(value.getValue(), value.getKey());
					}
					GridData data = new GridData(GridData.FILL_HORIZONTAL);
					listLogs.select(0);
					listLogs.setLayoutData(data);
					dialog_height = dialog_height + comp_height;
					map.put(parameter.getKey(), listLogs); 
				}
			}

			Composite buttonComposite = new Composite(buildDialog, SWT.NONE);
			buttonComposite.setLayout(new GridLayout(2, false));
			buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.END, true, true, 1, 1));

			buildButton = new Button(buttonComposite, SWT.PUSH);
			buildButton.setText(Messages.BUILD);

			cancelButton = new Button(buttonComposite, SWT.NONE);
			cancelButton.setText(Messages.CANCEL);

		} catch (PhrescoException e) {
			e.printStackTrace();
		}
		
		int x = buildDialog.getSize().x - 500;
		int y = buildDialog.getSize().y - 360;
		
		buildDialog.setSize(400,dialog_height);
		Point location = new Point(x, y);
		buildDialog.setLocation(location);
		
		
		return buildDialog;
	}

	private void saveCongfiguration()  {
		try {
			MojoProcessor processor = new MojoProcessor(PhrescoUtil.getPackageInfoConfigurationPath());
			List<Parameter> parameters = processor.getConfiguration(PACKAGE_GOAL).getParameters().getParameter();
			if (CollectionUtils.isNotEmpty(parameters)) {
				for (Parameter parameter : parameters) {
					if (parameter.getType().equalsIgnoreCase(DYNAMIC_PARAMETER)) {
						List<String> list =  (List<String>) map.get(parameter.getKey());
						StringBuilder env = new StringBuilder();
						if (CollectionUtils.isNotEmpty(list)) {
							for (String string: list) {
								env.append(string);
								env.append(",");
							}
							String envValue = env.toString();
							envValue = envValue.substring(0, envValue.lastIndexOf(","));
							parameter.setValue(envValue); 
						}
					} else if (parameter.getType().equalsIgnoreCase(BOOLEAN)) {
						Button checkBoxButton = (Button) map.get(parameter.getKey());
						if (checkBoxButton != null && !checkBoxButton.isDisposed()) {
							boolean selection = checkBoxButton.getSelection();
							parameter.setValue(String.valueOf(selection));
						}
					}  else if (parameter.getType().equalsIgnoreCase(LIST)) {
						Combo list =  (Combo) map.get(parameter.getKey());
						if (list != null) {
							String[] items = list.getItems();
							for (String string : items) {
								if (list.getText().equalsIgnoreCase(string)) {
									String value = typeMaps.get(string);
									parameter.setValue(value);
								}
							}
						}
					}
				}
			}
			processor.save();
		} catch (PhrescoException e) {
			e.printStackTrace();
		}
	}
}
