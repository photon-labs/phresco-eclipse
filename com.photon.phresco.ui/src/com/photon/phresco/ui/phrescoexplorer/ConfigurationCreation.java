package com.photon.phresco.ui.phrescoexplorer;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.model.ArtifactGroupInfo;
import com.photon.phresco.commons.model.DownloadInfo;
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

	private Button defaultCheckBoxButton;
	private Combo comboDropDown;

	private Text nameText;
	private Text numberText;
	private Text descText;

	private Map<String, Object> map = new HashMap<String, Object>();
	private Text passwordText;
	private Group typeGroup;
	private Combo typeList;
	private Combo environmentList;
	private String configName;

	private void createTemplateByTypes(Tree tree) {
		try {
			final Shell configDialog = new Shell(new Shell(), SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE | SWT.VERTICAL | SWT.V_SCROLL);
			configDialog.setText("Configuration");
			configDialog.setLocation(385,130);
			configDialog.setSize(900, 250);

			GridLayout CompositeLayout = new GridLayout(1, true);
			configDialog.setLayout(CompositeLayout);
			configDialog.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			GridLayout layout = new GridLayout(2, false);
			layout.verticalSpacing = 6;
			final Composite composite = new Composite(configDialog, 0);
			composite.setLayout(layout);

			Label name = new  Label(composite,  SWT.LEFT);
			name.setText("Name");
			name.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
			name.setLayoutData(new GridData(50,25));

			nameText = new Text(composite, SWT.BORDER); 
			nameText.setToolTipText("");
			nameText.setLayoutData(new GridData(140,25));

			Label desc = new  Label(composite,  SWT.LEFT);
			desc.setText("Description");
			desc.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
			desc.setLayoutData(new GridData(70,25));

			descText = new Text(composite, SWT.WRAP | SWT.BORDER); 
			descText.setToolTipText("");
			descText.setLayoutData(new GridData(200,50));

			Label environment = new  Label(composite,  SWT.LEFT);
			environment.setText("Environment");
			environment.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
			environment.setLayoutData(new GridData(75,25));

			File configurationFile = PhrescoUtil.getConfigurationFile();
			ConfigManagerImpl impl = new ConfigManagerImpl(configurationFile);
			List<Environment> environments = impl.getEnvironments();
			environmentList = new Combo(composite, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
			environmentList.setLayoutData(new GridData(60,25));			
			if (CollectionUtils.isNotEmpty(environments)) {
				for (Environment enviroName : environments) {
					environmentList.add(enviroName.getName());
				}
				environmentList.select(0);
			}

			Label tempType = new  Label(composite,  SWT.LEFT);
			tempType.setText("Type");
			tempType.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
			tempType.setLayoutData(new GridData(50,25));


			typeList = new Combo(composite, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
			typeList.setLayoutData(new GridData(60,25));		
			final ServiceManager serviceManager = PhrescoUtil.getServiceManager();
			PhrescoUtil.getApplicationInfo();

			List<SettingsTemplate> configTemplates = serviceManager.getConfigTemplates(PhrescoUtil.getCustomerId(), PhrescoUtil.getTechId());
			if (CollectionUtils.isNotEmpty(configTemplates)) {
				for (SettingsTemplate settingsTemplate : configTemplates) {
					typeList.add(settingsTemplate.getName());
				}
				typeList.select(0);
			}

			//For type selection
			typeGroup = new Group(configDialog, SWT.NONE);
			GridLayout newLayout = new GridLayout(7, false);
			typeGroup.setLayout(newLayout);
			typeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			final Group buttonGroup = new Group(configDialog, SWT.NONE | SWT.NO | SWT.SHADOW_OUT);
			GridLayout mainLayout = new GridLayout(2, false);
			buttonGroup.setLayout(mainLayout);

			renderConfigTypes(configDialog, composite, typeList,
					buttonGroup);

			typeList.addListener(SWT.Selection, new Listener() {

				@Override
				public void handleEvent(Event event) {

					renderConfigTypes(configDialog, composite, typeList, buttonGroup);

					buttonGroup.pack();
					buttonGroup.redraw();	

					configDialog.pack();
					configDialog.redraw();
				}
			});

			final GridData leftButtonData = new GridData(SWT.LEFT, SWT.CENTER, true, true);
			leftButtonData.grabExcessHorizontalSpace = true;
			leftButtonData.horizontalIndent = IDialogConstants.HORIZONTAL_MARGIN;
			buttonGroup.setLayoutData(leftButtonData);

			Button saveButton = new Button(buttonGroup, SWT.PUSH);
			saveButton.setText("Create");
			saveButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					SettingsTemplate serverTemplate;
					try {
						File envConfig = PhrescoUtil.getConfigurationFile();
						ConfigManagerImpl impl = new ConfigManagerImpl(envConfig);
						serverTemplate = serviceManager.getConfigTemplateByTechId(PhrescoUtil.getTechId(), typeList.getText());
						List<PropertyTemplate> propertyTemplates  = serverTemplate.getProperties();
						java.util.Properties properties = new java.util.Properties();
						for (PropertyTemplate propertyTemplate : propertyTemplates) {
							if ( CollectionUtils.isNotEmpty(propertyTemplate.getPossibleValues())) {
								Combo comboDropDown = (Combo) map.get(propertyTemplate.getKey());
								properties.put(propertyTemplate.getName().replaceAll("\\s", ""), comboDropDown.getText());
							} else if (propertyTemplate.getType().equalsIgnoreCase(STRING)) {
								if (propertyTemplate.getName().equalsIgnoreCase("Certificate") || propertyTemplate.getName().equalsIgnoreCase("server Type")
										|| propertyTemplate.getName().equalsIgnoreCase("DB Type") || propertyTemplate.getName().equalsIgnoreCase("VErsion")) {
									Combo comboDropDown = (Combo) map.get(propertyTemplate.getKey());
									properties.put(propertyTemplate.getName().replaceAll("\\s", ""), comboDropDown.getText());
								} 
								else {
									Text nameText = (Text) map.get(propertyTemplate.getKey());
									properties.put(propertyTemplate.getName().replaceAll("\\s", ""), nameText.getText());
								}				
							} 
							else if (propertyTemplate.getType().equalsIgnoreCase(NUMBER)) {
								Text numberText = (Text) map.get(propertyTemplate.getKey());
								properties.put(propertyTemplate.getName().replaceAll("\\s", ""), numberText.getText());
							} else if (propertyTemplate.getType().equalsIgnoreCase(BOOLEAN)) {
								Button checkBoxButton = (Button) map.get(propertyTemplate.getKey());
								boolean selection = checkBoxButton.getSelection();
								properties.put(propertyTemplate.getName().replaceAll("\\s", ""), (String.valueOf(selection)));
							} else if (propertyTemplate.getType().equalsIgnoreCase(PASSWORD)) {
								Text passwordText = (Text) map.get(propertyTemplate.getKey());
								String password = passwordText.getText();
								byte[] encodedPwd = Base64.encodeBase64(password.getBytes());
								String encodedString = new String(encodedPwd);
								properties.put(propertyTemplate.getName().replaceAll("\\s", ""), encodedString);
							} 
						}

						String environmentName = environmentList.getText();

						Configuration configuration = new Configuration();
						configuration.setEnvName(environmentName);
						configuration.setName(nameText.getText());
						configuration.setType(typeList.getText());
						configuration.setProperties(properties);
						impl.createConfiguration(environmentName, configuration);
						configDialog.setVisible(false);

						ConfigurationPage page = new ConfigurationPage();
						page.push();
					} catch (PhrescoException e) {
						e.printStackTrace();
					} catch (ConfigurationException e) {
						e.printStackTrace();
					}
				}
			});

			Button cancelButton = new Button(buttonGroup, SWT.PUSH);
			cancelButton.setText("Cancel");
			cancelButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					configDialog.close();
				}
			});

			typeGroup.pack();
			typeGroup.redraw();

			buttonGroup.pack();
			buttonGroup.redraw();	

			configDialog.pack();
			configDialog.redraw();

			configDialog.open();
		} catch (PhrescoException e) {
			e.printStackTrace();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	private void renderConfigTypes(final Shell configDialog,
			final Composite composite, final Combo typeList,
			final Group buttonGroup) {

		typeGroup.setText(typeList.getText());
		typeGroup.setLocation(250, 5);
		Control[] children = typeGroup.getChildren();
		for (int i = 0; i < children.length; i++) {
			children[i].dispose();
		}

		createDynamicDialog(typeGroup, typeList.getText());

		typeGroup.pack();
		typeGroup.redraw();

	}

	private Composite createDynamicDialog(final Group configDialog, String types) {

		Composite composite = new Composite(configDialog, SWT.NONE);
		try {
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
					map.put(propertyTemplate.getKey(), comboDropDown);
				} else if(type.equalsIgnoreCase(STRING) && CollectionUtils.isEmpty(propertyTemplate.getPossibleValues())){
					Label defaults = new  Label(composite,  SWT.LEFT);
					defaults.setText(propertyTemplate.getName());
					defaults.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
					try {
						if (propertyTemplate.getName().equalsIgnoreCase("Certificate")) {
							String name = propertyTemplate.getName();
							comboDropDown = new Combo(composite, SWT.DROP_DOWN | SWT.BORDER);
							comboDropDown.select(0);
							map.put(propertyTemplate.getKey(), comboDropDown);
						} else if (propertyTemplate.getName().equalsIgnoreCase("server Type")) {
							comboDropDown = new Combo(composite, SWT.DROP_DOWN | SWT.BORDER);
							comboDropDown.setLayoutData(data);
							ProjectInfo projectInfo = PhrescoUtil.getProjectInfo();
							List<ArtifactGroupInfo> selectedServers = projectInfo.getAppInfos().get(0).getSelectedServers();
							if (CollectionUtils.isNotEmpty(selectedServers)) {
								for (ArtifactGroupInfo artifactGroupInfo : selectedServers) {
									String artifactGroupId = artifactGroupInfo.getArtifactGroupId();
									DownloadInfo downloads = serviceManager.getDownloadInfo(artifactGroupId);
									comboDropDown.add(downloads.getName());
									comboDropDown.select(0);
								}
							}
							map.put(propertyTemplate.getKey(), comboDropDown);
						} else if (propertyTemplate.getName().equalsIgnoreCase("DB Type")) {
							comboDropDown = new Combo(composite, SWT.DROP_DOWN | SWT.BORDER);
							comboDropDown.setLayoutData(data);
							ProjectInfo projectInfo = PhrescoUtil.getProjectInfo();
							List<ArtifactGroupInfo> selectedDatabases = projectInfo.getAppInfos().get(0).getSelectedDatabases();
							if (CollectionUtils.isNotEmpty(selectedDatabases)) {
								for (ArtifactGroupInfo artifactGroupInfo : selectedDatabases) {
									String artifactGroupId = artifactGroupInfo.getArtifactGroupId();
									DownloadInfo downloads = serviceManager.getDownloadInfo(artifactGroupId);
									comboDropDown.add(downloads.getName());
									comboDropDown.select(0);
								}
							}
							map.put(propertyTemplate.getKey(), comboDropDown);
						}  else if (propertyTemplate.getName().equalsIgnoreCase("Version")) {
							comboDropDown = new Combo(composite, SWT.DROP_DOWN | SWT.BORDER);
							comboDropDown.setLayoutData(data);
							List<ArtifactGroupInfo> values = null;
							ProjectInfo projectInfo = PhrescoUtil.getProjectInfo();
							if (typeList.getText().equalsIgnoreCase("Server")) {
								values = projectInfo.getAppInfos().get(0).getSelectedServers();
							} else if (typeList.getText().equalsIgnoreCase("Database")) {
								values = projectInfo.getAppInfos().get(0).getSelectedDatabases();
							}
							if (CollectionUtils.isNotEmpty(values)) {
								for (ArtifactGroupInfo artifactGroupInfo : values) {
									for(String artifct: artifactGroupInfo.getArtifactInfoIds()){
										//										ArtifactInfo artifactInfo = serviceManager.getArtifactInfo(artifct);
										//										comboDropDown.add(artifactInfo.getVersion());
										comboDropDown.add("5.5.1");
										comboDropDown.select(0);
									}
								}
							}
							map.put(propertyTemplate.getKey(), comboDropDown);
						}
						else {
							String name = propertyTemplate.getName();
							nameText = new Text(composite, SWT.BORDER); 
							nameText.setToolTipText("");
							nameText.setLayoutData(new GridData(80,13));
							nameText.setLayoutData(data);
							map.put(propertyTemplate.getKey(), nameText);
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
					map.put(propertyTemplate.getKey(), numberText);
				}	else if (type.equalsIgnoreCase(BOOLEAN)) {
					Label defaults = new  Label(composite,  SWT.LEFT);
					defaults.setText(propertyTemplate.getName());
					defaults.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

					defaultCheckBoxButton = new Button(composite, SWT.CHECK);
					defaultCheckBoxButton.setLayoutData(new GridData(75,20));
					map.put(propertyTemplate.getKey(), defaultCheckBoxButton);

				} else if (type.equalsIgnoreCase(PASSWORD)) {

					Label defaults = new  Label(composite,  SWT.LEFT);
					defaults.setText(propertyTemplate.getName());
					defaults.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

					passwordText = new Text(composite, SWT.PASSWORD | SWT.BORDER); 
					passwordText.setToolTipText(ENVIRONMENT_NAME);
					passwordText.setLayoutData(new GridData(80,20));
					map.put(propertyTemplate.getKey(), passwordText);
				}
			}
			composite.layout();
		} catch (PhrescoException e) {
			e.printStackTrace();
		}
		return composite;
	}


	public void createTemplateByType(Shell configureDialogs, Tree tree) {
		configureDialogs.close();
		createTemplateByTypes(tree);
	}

	public void configure(TreeItem parent, TreeItem item) {
		editConfiguration(parent, item);
	}

	private void editConfiguration(final TreeItem parent, final TreeItem item) {
		try {
			File configureFile = PhrescoUtil.getConfigurationFile();
			ConfigManagerImpl impls = new ConfigManagerImpl(configureFile);
			List<Configuration> configs = impls.getConfigurations(parent.getText());
			final Shell configDialog = new Shell(new Shell(), SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE | SWT.VERTICAL | SWT.V_SCROLL);
			configDialog.setText("Configuration");
			configDialog.setLocation(385,130);
			configDialog.setSize(900, 250);

			GridLayout CompositeLayout = new GridLayout(1, true);
			configDialog.setLayout(CompositeLayout);
			configDialog.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			GridLayout layout = new GridLayout(2, false);
			layout.verticalSpacing = 6;
			final Composite composite = new Composite(configDialog, 0);
			composite.setLayout(layout);
			java.util.Properties prop = null;
			
			Configuration configuration = configs.get(0);
			
			configuration.getName().equalsIgnoreCase(item.getText());
			prop = configuration.getProperties();
			Label name = new  Label(composite,  SWT.LEFT);
			name.setText("Name");
			name.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
			name.setLayoutData(new GridData(50,25));

			nameText = new Text(composite, SWT.BORDER); 
			nameText.setToolTipText("");
			nameText.setLayoutData(new GridData(140,25));
			nameText.setText(configuration.getName());

			Label desc = new  Label(composite,  SWT.LEFT);
			desc.setText("Description");
			desc.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
			desc.setLayoutData(new GridData(70,25));

			descText = new Text(composite, SWT.WRAP | SWT.BORDER); 
			descText.setToolTipText("");
			descText.setLayoutData(new GridData(200,50));
			descText.setText(configuration.getDesc());

			Label environment = new  Label(composite,  SWT.LEFT);
			environment.setText("Environment");
			environment.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
			environment.setLayoutData(new GridData(75,25));

			environmentList = new Combo(composite, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
			environmentList.setLayoutData(new GridData(60,25));			
			environmentList.add(configuration.getEnvName());
			environmentList.select(0);

			Label tempType = new  Label(composite,  SWT.LEFT);
			tempType.setText("Type");
			tempType.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
			tempType.setLayoutData(new GridData(50,25));


			typeList = new Combo(composite, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
			typeList.setLayoutData(new GridData(60,25));	
			typeList.add(configuration.getType());
			typeList.select(0);
			
			//For type selection

			typeGroup = new Group(configDialog, SWT.NONE);
			GridLayout newLayout = new GridLayout(7, false);
			typeGroup.setLayout(newLayout);
			typeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			final Group buttonGroup = new Group(configDialog, SWT.NONE | SWT.NO | SWT.SHADOW_OUT);
			GridLayout mainLayout = new GridLayout(2, false);
			buttonGroup.setLayout(mainLayout);

			renderConfigTypes(configDialog, composite, typeList, buttonGroup);

			typeList.addListener(SWT.Selection, new Listener() {

				@Override
				public void handleEvent(Event event) {

					renderConfigTypes(configDialog, composite, typeList, buttonGroup);

					buttonGroup.pack();
					buttonGroup.redraw();	

					configDialog.pack();
					configDialog.redraw();
				}
			});

			renderConfigTypes(configDialog, composite, typeList, buttonGroup);

			final GridData leftButtonData = new GridData(SWT.LEFT, SWT.CENTER, true, true);
			leftButtonData.grabExcessHorizontalSpace = true;
			leftButtonData.horizontalIndent = IDialogConstants.HORIZONTAL_MARGIN;
			buttonGroup.setLayoutData(leftButtonData);

			Button saveButton = new Button(buttonGroup, SWT.PUSH);
			saveButton.setText("Update");

			final ServiceManager serviceManager = PhrescoUtil.getServiceManager();
			SettingsTemplate serverTemplate = serviceManager.getConfigTemplateByTechId(PhrescoUtil.getTechId(), typeList.getText());

			List<PropertyTemplate> propertyTemplates  = serverTemplate.getProperties();
			for (PropertyTemplate propertyTemplate : propertyTemplates) {
				if (propertyTemplate.getType().equalsIgnoreCase(STRING)) {
					if ( CollectionUtils.isNotEmpty(propertyTemplate.getPossibleValues())) {
						Combo comboDropDown = (Combo) map.get(propertyTemplate.getKey());
						comboDropDown.removeAll();
						String value = (String) prop.get(propertyTemplate.getName().replaceAll("\\s", ""));
						comboDropDown.add(value);
						comboDropDown.select(0);
					} else if (propertyTemplate.getName().equalsIgnoreCase("Certificate") || propertyTemplate.getName().equalsIgnoreCase("server Type")
							|| propertyTemplate.getName().equalsIgnoreCase("DB Type") || propertyTemplate.getName().equalsIgnoreCase("VErsion")) {
						Combo comboDropDown = (Combo) map.get(propertyTemplate.getKey());
						comboDropDown.removeAll();
						String value = (String) prop.get(propertyTemplate.getName().replaceAll("\\s", ""));
						comboDropDown.add(value);
						comboDropDown.select(0);
					} 
					else {
						Text text = (Text) map.get(propertyTemplate.getKey());
						String value = (String) prop.get(propertyTemplate.getName().replaceAll("\\s", ""));
						text.setText(value);
					}				
				} 
				else if (propertyTemplate.getType().equalsIgnoreCase(NUMBER)) {
					Text numberText = (Text) map.get(propertyTemplate.getKey());
					String value = (String) prop.get(propertyTemplate.getName().replaceAll("\\s", ""));
					numberText.setText(value);
				} else if (propertyTemplate.getType().equalsIgnoreCase(BOOLEAN)) {
					Button checkBoxButton = (Button) map.get(propertyTemplate.getKey());
					String value = (String) prop.get(propertyTemplate.getName().replaceAll("\\s", ""));
					checkBoxButton.setText(value);
				} else if (propertyTemplate.getType().equalsIgnoreCase(PASSWORD)) {
					Text passwordText = (Text) map.get(propertyTemplate.getKey());
					String value = (String) prop.get(propertyTemplate.getName().replaceAll("\\s", ""));
					passwordText.setText(value);
				} 

				typeGroup.pack();
				typeGroup.redraw();

				buttonGroup.pack();
				buttonGroup.redraw();	

				configDialog.pack();
				configDialog.redraw();

				configDialog.open();
			}
			saveButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					try {
						File envConfig = PhrescoUtil.getConfigurationFile();
						ConfigManagerImpl impl = new ConfigManagerImpl(envConfig);
						SettingsTemplate serverTemplates = serviceManager.getConfigTemplateByTechId(PhrescoUtil.getTechId(), typeList.getText());
						List<PropertyTemplate> propertyTemplates  = serverTemplates.getProperties();
						java.util.Properties properties = new java.util.Properties();
						for (PropertyTemplate propertyTemplate : propertyTemplates) {
							if ( CollectionUtils.isNotEmpty(propertyTemplate.getPossibleValues())) {
								Combo comboDropDown = (Combo) map.get(propertyTemplate.getKey());
								properties.put(propertyTemplate.getName().replaceAll("\\s", ""), comboDropDown.getText());
							} else if (propertyTemplate.getType().equalsIgnoreCase(STRING)) {
								if (propertyTemplate.getName().equalsIgnoreCase("Certificate") || propertyTemplate.getName().equalsIgnoreCase("server Type")
										|| propertyTemplate.getName().equalsIgnoreCase("DB Type") || propertyTemplate.getName().equalsIgnoreCase("Version")) {
									Combo comboDropDown = (Combo) map.get(propertyTemplate.getKey());
									properties.put(propertyTemplate.getName().replaceAll("\\s", ""), comboDropDown.getText());
								} 
								else {
									if (propertyTemplate.getKey().equalsIgnoreCase("Name")) {
										Text nameText = (Text) map.get(propertyTemplate.getKey());
										configName = nameText.getText();
									}
									Text nameText = (Text) map.get(propertyTemplate.getKey());
									properties.put(propertyTemplate.getName().replaceAll("\\s", ""), nameText.getText());
								}				
							} 
							else if (propertyTemplate.getType().equalsIgnoreCase(NUMBER)) {
								Text numberText = (Text) map.get(propertyTemplate.getKey());
								properties.put(propertyTemplate.getName().replaceAll("\\s", ""), numberText.getText());
							} else if (propertyTemplate.getType().equalsIgnoreCase(BOOLEAN)) {
								Button checkBoxButton = (Button) map.get(propertyTemplate.getKey());
								boolean selection = checkBoxButton.getSelection();
								properties.put(propertyTemplate.getName().replaceAll("\\s", ""), (String.valueOf(selection)));
							} else if (propertyTemplate.getType().equalsIgnoreCase(PASSWORD)) {
								Text passwordText = (Text) map.get(propertyTemplate.getKey());
								String password = passwordText.getText();
								byte[] encodedPwd = Base64.encodeBase64(password.getBytes());
								String encodedString = new String(encodedPwd);
								properties.put(propertyTemplate.getName().replaceAll("\\s", ""), encodedString);
							} 
						}

						String environmentName = parent.getText();
						Configuration configuration = new Configuration();
						configuration.setEnvName(environmentName);
						configuration.setType(typeList.getText());
						configuration.setProperties(properties);
						impl.updateConfiguration(environmentName, item.getText(), configuration);
						configDialog.setVisible(false);
						ConfigurationPage page = new ConfigurationPage();
						page.push();
					} catch (PhrescoException e) {
						e.printStackTrace();
					} catch (ConfigurationException e) {
						e.printStackTrace();
					}
				}
			});

			Button cancelButton = new Button(buttonGroup, SWT.PUSH);
			cancelButton.setText("Cancel");
			cancelButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					configDialog.close();
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
