package com.photon.phresco.ui.phrescoexplorer;

import java.io.File;
import java.util.List;
import java.util.Map;

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
import com.photon.phresco.commons.model.ProjectInfo;
import com.photon.phresco.commons.model.PropertyTemplate;
import com.photon.phresco.commons.model.SettingsTemplate;
import com.photon.phresco.commons.util.PhrescoUtil;
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

	private static Text nameText;
	private static Text numberText;

	private TreeItem itemTemplate;
	private static String name;
	private static Map<PropertyTemplate, Object> map = new HashedMap();
	private static Text passwordText;
	private Group typeGroup;
	private void createTemplateByType(String type) {
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

			Text nameText = new Text(composite, SWT.BORDER); 
			nameText.setToolTipText("");
			nameText.setLayoutData(new GridData(140,25));

			Label desc = new  Label(composite,  SWT.LEFT);
			desc.setText("Description");
			desc.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
			desc.setLayoutData(new GridData(70,25));

			Text descText = new Text(composite, SWT.WRAP | SWT.BORDER); 
			descText.setToolTipText("");
			descText.setLayoutData(new GridData(200,50));

			Label environment = new  Label(composite,  SWT.LEFT);
			environment.setText("Environment");
			environment.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
			environment.setLayoutData(new GridData(75,25));

			File configurationFile = PhrescoUtil.getConfigurationFile();
			ConfigManagerImpl impl = new ConfigManagerImpl(configurationFile);
			List<Environment> environments = impl.getEnvironments();
			Combo environmentList = new Combo(composite, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
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


			final Combo typeList = new Combo(composite, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
			typeList.setLayoutData(new GridData(60,25));		
			ServiceManager serviceManager = PhrescoUtil.getServiceManager();
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
					
					renderConfigTypes(configDialog, composite, typeList,
							buttonGroup);
					
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
			    }
		    });
		    			
			Button cancelButton = new Button(buttonGroup, SWT.PUSH);
			cancelButton.setText("Cancel");
			cancelButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
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
					map.put(propertyTemplate, comboDropDown);
				} else if(type.equalsIgnoreCase(STRING) && CollectionUtils.isEmpty(propertyTemplate.getPossibleValues())){
					Label defaults = new  Label(composite,  SWT.LEFT);
					defaults.setText(propertyTemplate.getName());
					defaults.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
					defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
					try {
						if (propertyTemplate.getName().equalsIgnoreCase("Certificate") || propertyTemplate.getName().equalsIgnoreCase("Protocol")) {
							String name = propertyTemplate.getName();
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
					passwordText.setLayoutData(new GridData(80,20));
					map.put(propertyTemplate, passwordText);
				}
			}

			composite.layout();
		} catch (PhrescoException e) {
			e.printStackTrace();
		}
		return composite;
	}


	public void createTemplateByType(Tree tree) {
		createTemplateByType("type");
	}
}
