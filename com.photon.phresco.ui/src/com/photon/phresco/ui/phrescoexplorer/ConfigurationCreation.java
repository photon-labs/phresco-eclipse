package com.photon.phresco.ui.phrescoexplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.model.ArtifactGroupInfo;
import com.photon.phresco.commons.model.ProjectInfo;
import com.photon.phresco.commons.model.PropertyTemplate;
import com.photon.phresco.commons.model.SettingsTemplate;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.configuration.Configuration;
import com.photon.phresco.configuration.Environment;
import com.photon.phresco.exception.ConfigurationException;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.impl.ConfigManagerImpl;
import com.photon.phresco.service.client.api.ServiceManager;

public class ConfigurationCreation  implements PhrescoConstants {

	private static Button defaultCheckBoxButton;
	private static Button configCancelButton;
	private static Button configenvSaveButton;
	private static Combo comboDropDown;
	private static Button saveButton;
	private static Button cancelButton;

	private static Text nameText;
	private static Text numberText;

	private TreeItem itemTemplate;
	private static String name;
	private static Map<PropertyTemplate, Object> map = new HashedMap();
	private static Text passwordText;

	private void createTemplateByType(String type) {
		try {
			final Shell configDialog = new Shell(new Shell(), SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE);
			configDialog.setText("Configuration");
			configDialog.setLocation(385,130);
			configDialog.setSize(400, 230);

			GridLayout subLayout = new GridLayout(1, false);
			subLayout.verticalSpacing = 20;
			subLayout.horizontalSpacing = 60;
			configDialog.setLayout(subLayout);
			
			Composite composite = new Composite(configDialog, SWT.NONE);
			GridLayout layout = new GridLayout(2, false);
			composite.setLayout(layout);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			
			Label name = new  Label(composite,  SWT.LEFT);
			name.setText("Name");
			name.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
			name.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

			Text nameText = new Text(composite, SWT.BORDER); 
			nameText.setToolTipText("");
			nameText.setLayoutData(data);

			Label desc = new  Label(composite,  SWT.LEFT);
			desc.setText("Description");
			desc.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
			desc.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

			Text descText = new Text(composite, SWT.BORDER); 
			descText.setToolTipText("");
			descText.setLayoutData(data);

			Label environment = new  Label(composite,  SWT.LEFT);
			environment.setText("Environment");
			environment.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
			environment.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

			File configurationFile = PhrescoUtil.getConfigurationFile();
			ConfigManagerImpl impl = new ConfigManagerImpl(configurationFile);
			List<Environment> environments = impl.getEnvironments();
			Combo environmentList = new Combo(composite, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
			environmentList.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));			
			if (CollectionUtils.isNotEmpty(environments)) {
				for (Environment enviroName : environments) {
					environmentList.add(enviroName.getName());
				}
				environmentList.select(0);
			}

			Label tempType = new  Label(composite,  SWT.LEFT);
			tempType.setText("Type");
			tempType.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
			tempType.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));


			final Combo typeList = new Combo(composite, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
			typeList.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
			ServiceManager serviceManager = PhrescoUtil.getServiceManager();
			PhrescoUtil.getApplicationInfo();

			List<SettingsTemplate> configTemplates = serviceManager.getConfigTemplates(PhrescoUtil.getCustomerId(), PhrescoUtil.getTechId());
			if (CollectionUtils.isNotEmpty(configTemplates)) {
				for (SettingsTemplate settingsTemplate : configTemplates) {
					typeList.add(settingsTemplate.getName());
				}
				typeList.select(0);
			}

			typeList.addListener(SWT.Selection, new Listener() {

				@Override
				public void handleEvent(Event event) {
					createDynamicDialog(configDialog, typeList.getText());
				}
			});

			configDialog.pack();

			configDialog.open();
		} catch (PhrescoException e) {
			e.printStackTrace();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}


	private void createDynamicDialog(Shell configDialog, String types) {
		try {
			Composite composite = new Composite(configDialog, SWT.NONE);
			GridLayout subLayout = new GridLayout(2, false);
			composite.setLayout(subLayout);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
		
			ServiceManager serviceManager = PhrescoUtil.getServiceManager();
			SettingsTemplate serverTemplate = serviceManager.getConfigTemplateByTechId(PhrescoUtil.getTechId(), types);
			List<PropertyTemplate> propertyTemplates  = serverTemplate.getProperties();

			for (PropertyTemplate propertyTemplate : propertyTemplates) {
				String type = propertyTemplate.getType();

				if ( CollectionUtils.isNotEmpty(propertyTemplate.getPossibleValues())) {
					Label defaults = new  Label(composite,  SWT.LEFT);
					defaults.setText(propertyTemplate.getName());
					defaults.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

					comboDropDown = new Combo(composite, SWT.DROP_DOWN | SWT.BORDER);
					comboDropDown.setLayoutData(data);
					List<String> possibleValues = propertyTemplate.getPossibleValues();
					for (String string : possibleValues) {
						comboDropDown.add(string);
					}
					map.put(propertyTemplate, comboDropDown);
				} else if(type.equalsIgnoreCase(STRING) && CollectionUtils.isEmpty(propertyTemplate.getPossibleValues())){
					Label defaults = new  Label(composite,  SWT.LEFT);
					defaults.setText(propertyTemplate.getName());
					defaults.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
					try {
						if (propertyTemplate.getName().equalsIgnoreCase("Certificate") || propertyTemplate.getName().equalsIgnoreCase("Protocol")) {
							String name = propertyTemplate.getName();
							System.out.println("Name in combo = " + name);
							comboDropDown = new Combo(composite, SWT.DROP_DOWN | SWT.BORDER);
							comboDropDown.setLayoutData(data);
							ProjectInfo projectInfo = PhrescoUtil.getProjectInfo();
							List<ArtifactGroupInfo> selectedServers = projectInfo.getAppInfos().get(0).getSelectedServers();
							if (CollectionUtils.isNotEmpty(selectedServers)) {
								for (ArtifactGroupInfo artifactGroupInfo : selectedServers) {
									comboDropDown.add(artifactGroupInfo.getDisplayName());
								}
							}
							map.put(propertyTemplate, comboDropDown);
						} else {
							String name = propertyTemplate.getName();
							nameText = new Text(composite, SWT.BORDER); 
							nameText.setToolTipText("");
							nameText.setLayoutData(new GridData(80,13));
							nameText.setLayoutData(data);
							map.put(propertyTemplate, nameText);
						}					
					} catch (PhrescoException e) {
						e.printStackTrace();
					}
				}	else if (type.equalsIgnoreCase(NUMBER)) {
					Label defaults = new  Label(composite,  SWT.LEFT);
					defaults.setText(propertyTemplate.getName());
					defaults.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

					numberText = new Text(composite, SWT.BORDER); 
					numberText.setToolTipText("");
					numberText.setLayoutData(new GridData(80,13));
					map.put(propertyTemplate, numberText);
				}	else if (type.equalsIgnoreCase(BOOLEAN)) {
					Label defaults = new  Label(composite,  SWT.LEFT);
					defaults.setText(propertyTemplate.getName());
					defaults.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

					defaultCheckBoxButton = new Button(composite, SWT.CHECK);
					defaultCheckBoxButton.setLayoutData(new GridData(75,20));
					map.put(propertyTemplate, defaultCheckBoxButton);

				} else if (type.equalsIgnoreCase(PASSWORD)) {

					Label defaults = new  Label(composite,  SWT.LEFT);
					defaults.setText(propertyTemplate.getName());
					defaults.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

					passwordText = new Text(composite, SWT.PASSWORD); 
					passwordText.setToolTipText(ENVIRONMENT_NAME);
					passwordText.setLayoutData(new GridData(80,13));
					map.put(propertyTemplate, passwordText);
				}
			}

			GridData datas = new GridData();
			datas.horizontalAlignment = SWT.RIGHT;


			saveButton = new Button(composite, SWT.PUSH);
			saveButton.setLocation(279, 121);
			saveButton.setText("Save");
			saveButton.setSize(74, 23);
			saveButton.setLayoutData(datas);

			cancelButton = new Button(composite, SWT.PUSH);
			cancelButton.setLocation(279, 121);
			cancelButton.setText("Cancel");
			cancelButton.setSize(74, 23);
			cancelButton.setLayoutData(datas);

		} catch (PhrescoException e) {
			e.printStackTrace();
		}
	}


	public void createTemplateByType(Tree tree) {
		createTemplateByType("type");
	}
}
