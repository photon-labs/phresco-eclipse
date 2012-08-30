/*
 * ###
 * 
 * Copyright (C) 1999 - 2012 Photon Infotech Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ###
 */
package com.photon.phresco.ui.wizards.pages;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.CollectionUtils;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.launch.FrameworkFactory;

import com.photon.phresco.commons.FrameworkConstants;
import com.photon.phresco.configuration.Environment;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.framework.FrameworkConfiguration;
import com.photon.phresco.framework.PhrescoFrameworkFactory;
import com.photon.phresco.framework.api.Project;
import com.photon.phresco.framework.api.ProjectAdministrator;
import com.photon.phresco.model.I18NString;
import com.photon.phresco.model.PropertyTemplate;
import com.photon.phresco.model.SettingsTemplate;

/**
 * Project configuration page
 * @author arunachalam.lakshmanan@photoninfotech.net
 *
 */
public class ConfigurationsPage extends WizardPage implements IWizardPage {

	private Text nameTxt;
	private Text descriptionTxt;
	
//	private Combo protocolCombo;
//	private Text serverHostTxt;
//	private Text serverPortTxt;
//	private Text adminUsernameTxt;
//	private Text adminPasswordText;
//	private Text certificateTxt;
//	private Combo serverTypeCombo;
//	private Combo serverVersionCombo;
//	private Text deployDirectoryTxt;
//	private Text rootContextText;
//	private Text additionalContextPathTxt;
	
	private Combo possibleValueCombo;
	
	private Composite configComposite;
	
	private Composite parentComposite;
	
	private ProjectAdministrator administrator;
	public ConfigurationsPage(String pageName) {
		super(pageName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createControl(Composite parent) {
		
		final Composite parentComposite = new Composite(parent, SWT.NULL);
		parentComposite.setLayout(new GridLayout(2, false));
		parentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label nameLbl = new Label(parentComposite, SWT.NONE);
		nameLbl.setText("Name");
		
		nameTxt = new Text(parentComposite, SWT.BORDER);
		nameTxt.setMessage("Name of the configuration");
		
		Label descriptionLbl = new Label(parentComposite, SWT.NONE);
		descriptionLbl.setText("Description");
		
		descriptionTxt = new Text(parentComposite, SWT.BORDER | SWT.WRAP | SWT.MULTI);
		descriptionTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label environmentLbl = new Label(parentComposite, SWT.NONE);
		environmentLbl.setText("Environment");
			
		Combo environmentCombo = new Combo(parentComposite, SWT.READ_ONLY);
		
		List<Environment> environments = getEnvironment("PHR_Drupal614");
		String[] environmentNames = new String[environments.size()];
		
		int i = 0;
		for (Environment environment : environments) {
				environmentNames[i] = environment.getName();
				i = i + 1;
		}
		environmentCombo.setItems(environmentNames);
		environmentCombo.select(0);
		
		Label typeLbl = new Label(parentComposite, SWT.NONE);
		typeLbl.setText("Type");
		
		final Combo typeCombo = new Combo(parentComposite, SWT.READ_ONLY);
		List<SettingsTemplate> types = getSettingsTemplates("photon");
		String[] typeName = new String[types.size()];
		i = 0;
		for (SettingsTemplate type : types) {
			typeName[i] = type.getType();
			i = i + 1;
		}
//		SettingsTemplate settingsTemplate = getSettingsTemplate("Server", "photon");
//		String type = settingsTemplate.getType();
		typeCombo.setItems(typeName);
		typeCombo.select(0);

		configComposite = new Composite(parentComposite, SWT.NONE);
		configComposite.setLayout(new GridLayout(2,false));
		configComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
//		SettingsTemplate settingsTemplate = getSettingsTemplate("Server");
//		List<PropertyTemplate> properties = settingsTemplate.getProperties();
//		List<String> possibleValues = new ArrayList<String>();
//		for (PropertyTemplate propertyTemplate : properties) {
//			possibleValues = propertyTemplate.getPossibleValues();
//			String key = propertyTemplate.getKey();
//			label = new Label(basicComposite, SWT.NONE);
//			I18NString I18NStringName = propertyTemplate.getName();
//			String name = I18NStringName.get("en-US").getValue();
//			label.setText(name);
//			I18NString I18NStringDescription = propertyTemplate.getDescription();
//			String description = I18NStringDescription.get("en-US").getValue();
//			if (key.equals(FrameworkConstants.ADMIN_FIELD_PASSWORD) || key.equals(FrameworkConstants.PASSWORD)) {
//				System.out.println("Key===> " + key);
//				text = new Text(basicComposite, SWT.PASSWORD);
//				text.setMessage(description);
//			} else if (possibleValues != null && !possibleValues.isEmpty() && possibleValues.size() > 0) {
//				String[] possibleValueArray = new String[possibleValues.size()];
//				possibleValueArray = possibleValues.toArray(possibleValueArray);
//				possibleValueCombo = new Combo(basicComposite, SWT.READ_ONLY);
//				possibleValueCombo.setItems(possibleValueArray);
//				possibleValueCombo.select(0);
//			} else {
//				text = new Text(basicComposite, SWT.BORDER);
//				text.setMessage(description);
//			} 
//			text = new Text(basicComposite, SWT.BORDER);
//			text.setMessage(description);
//		}

		typeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SettingsTemplate settingsTemplate = getSettingsTemplate(typeCombo.getText(),"photon");
				List<PropertyTemplate> properties = settingsTemplate.getProperties();
				List<String> possibleValues = new ArrayList<String>();
				Label label;
				Text text;
				
				if(configComposite.getChildren() != null && configComposite.getChildren().length >0) { 
					configComposite.dispose();
				}
				if(configComposite.isDisposed()) { 
					configComposite = new Composite(parentComposite, SWT.NONE);
					configComposite.setLayout(new GridLayout(2,false));
					configComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
				}
				for (PropertyTemplate propertyTemplate : properties) {
					possibleValues = propertyTemplate.getPossibleValues();
					String key = propertyTemplate.getKey();
					label = new Label(configComposite, SWT.NONE);
					I18NString I18NStringName = propertyTemplate.getName();
					String name = I18NStringName.get("en-US").getValue();
					label.setText(name);
					I18NString I18NStringDescription = propertyTemplate.getDescription();
					String description = I18NStringDescription.get("en-US").getValue();
					if (key.equals(FrameworkConstants.ADMIN_FIELD_PASSWORD) || key.equals(FrameworkConstants.PASSWORD)) {
						text = new Text(configComposite, SWT.PASSWORD);
						text.setMessage(description);
					} else if (possibleValues != null && !possibleValues.isEmpty() && possibleValues.size() > 0) {
						String[] possibleValueArray = new String[possibleValues.size()];
						possibleValueArray = possibleValues.toArray(possibleValueArray);
						possibleValueCombo = new Combo(configComposite, SWT.READ_ONLY);
						possibleValueCombo.setItems(possibleValueArray);
						possibleValueCombo.select(0);
					} else {
						text = new Text(configComposite, SWT.BORDER);
						text.setMessage(description);
					}
					configComposite.setData(propertyTemplate);
				}
				parentComposite.layout(true,true);
			}
		});
		setControl(parentComposite);
	}
	
	@Override
	public boolean canFlipToNextPage() {
		return true;
	}
	
	public List<Environment> getEnvironment(String environmentName) { 
		try {
			administrator = PhrescoFrameworkFactory.getProjectAdministrator();
			Project project = administrator.getProject(environmentName);
			List<Environment> environments = administrator.getEnvironments(project);
			return environments;
	} catch(Exception e) {
		
	}
		return null;
	}
	public List<SettingsTemplate> getSettingsTemplates(String custometId) { 
		try {
			administrator = PhrescoFrameworkFactory.getProjectAdministrator();
			List<SettingsTemplate> settingsTemplates = administrator.getSettingsTemplates(custometId);
			return settingsTemplates;
	} catch(Exception e) {
		
	}
		return null;
	}
	
	private SettingsTemplate getSettingsTemplate(String type,String customerId) { 
		try {
			administrator = PhrescoFrameworkFactory.getProjectAdministrator();
			SettingsTemplate settingsTemplate = administrator.getSettingsTemplate(type,customerId);
			return settingsTemplate;
		} catch (PhrescoException e) {
			e.printStackTrace();
		}
		return null;
	}
}
