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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.framework.PhrescoFrameworkFactory;
import com.photon.phresco.framework.api.ServiceManager;
import com.photon.phresco.model.ApplicationType;
import com.photon.phresco.model.Database;
import com.photon.phresco.model.Technology;
import com.photon.phresco.model.WebService;

/**
 * App info page
 * 
 * @author arunachalam.lakshmanan@photoninfotech.net
 *
 */
public class AppInfoPage extends WizardPage implements IWizardPage {

	
	public Label projectName;
	public Text projectTxt;
	
	/**
	 * @wbp.parser.constructor
	 */
	public AppInfoPage(String pageName) {
		super(pageName);
		setTitle("{Phresco}");
		setDescription("Project Creation Page");
	}
	
	

	@Override
	public void createControl(Composite parent) {
		Composite parentComposite = new Composite(parent, SWT.NULL);
		parentComposite.setLayout(new GridLayout(2,false));
		parentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		projectName = new Label(parentComposite, SWT.NONE);
		projectName.setText("Project name");

		projectTxt = new Text(parentComposite,SWT.BORDER);
		projectTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label code = new Label(parentComposite, SWT.NONE);
		code.setText("Code");

		Text codeTxt = new Text(parentComposite, SWT.BORDER);
		//		codeTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label description = new Label(parentComposite, SWT.NONE);
		description.setText("Description");

		StyledText descriptionTxt = new StyledText(parentComposite, SWT.BORDER);
		descriptionTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Label version = new Label(parentComposite, SWT.NONE);
		version.setText("Version");

		Text versionTxt = new Text(parentComposite, SWT.BORDER);
		//		versionTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		new Label(parentComposite, SWT.NONE);		

		Composite composite = new Composite(parentComposite, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		composite.setLayout(new GridLayout(3, false));

		final Button btnWeb = new Button(composite, SWT.RADIO);
		Button btnMobileApp = new Button(composite, SWT.RADIO);
		Button btnWebServices = new Button(composite, SWT.RADIO);

		Composite technologyComposite = new Composite(parentComposite, SWT.NULL);
		technologyComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		//		composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		technologyComposite.setLayout(new GridLayout(4, false));

		final Label lblTechnology = new Label(technologyComposite, SWT.NONE);
		GridData gd_technology = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_technology.widthHint = 96;
		lblTechnology.setLayoutData(gd_technology);
		lblTechnology.setText("Technology");

		final Combo technologyCombo = new Combo(technologyComposite, SWT.NONE);
		technologyCombo.setText("SELECT");
		GridData gd_technologyCombo = new GridData(GridData.FILL_BOTH);
		gd_technologyCombo.widthHint = 154;
		technologyCombo.setLayoutData(gd_technologyCombo);


		final Label lblTechnologyVersion = new Label(technologyComposite, SWT.NONE);
		lblTechnologyVersion.setText("Version");

		final Combo technologyVersionCombo = new Combo(technologyComposite, SWT.BORDER);
		technologyVersionCombo.setText("SELECT VERSION");

		Label lblPilotProject = new Label(parentComposite, SWT.NONE);
		lblPilotProject.setText("Pilot Project");

		Combo pilotProjectCombo = new Combo(parentComposite, SWT.NONE);
		String[] pilotProjectComboItems = {"None","PhpBlog"};
		GridData gd_pilotProjectCombo = new GridData();
		gd_pilotProjectCombo.widthHint = 154;
		pilotProjectCombo.setLayoutData(gd_pilotProjectCombo);
		pilotProjectCombo.setItems(pilotProjectComboItems);

		Label supportedServers = new Label(parentComposite, SWT.NONE);
		supportedServers.setText("SupportedServers");

		final Button addSupportedServersBtn = new Button(parentComposite, SWT.NONE);
		addSupportedServersBtn.setText("Add");

		Label supportedDatebases = new Label(parentComposite, SWT.NONE);
		supportedDatebases.setText("SupportedDatebases");

		Button addSupportedDatabasesBtn = new Button(parentComposite, SWT.NONE);
		addSupportedDatabasesBtn.setText("Add");

		Label lblConsumes = new Label(parentComposite, SWT.NONE);
		lblConsumes.setText("Consumes");
		
		final Canvas consumesCanvas = new Canvas(parentComposite, SWT.BORDER);
		consumesCanvas.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		GridData gd_consumesCanvas = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_consumesCanvas.heightHint = 83;
		gd_consumesCanvas.widthHint = 204;
		consumesCanvas.setLayoutData(gd_consumesCanvas);
		consumesCanvas.setEnabled(false);
		
		final Button restJsonCheckButton = new Button(consumesCanvas, SWT.CHECK);
		restJsonCheckButton.setBounds(0, 0, 105, 16);
		restJsonCheckButton.setVisible(false);
		
		final Button restXmlCheckButton = new Button(consumesCanvas, SWT.CHECK);
		restXmlCheckButton.setBounds(0, 22, 105, 16);
		restXmlCheckButton.setVisible(false);

		final Button soapCheckButton = new Button(consumesCanvas, SWT.CHECK);
		soapCheckButton.setBounds(0, 41, 105, 16);
		soapCheckButton.setVisible(false);

		final Button soap1CheckButton = new Button(consumesCanvas, SWT.CHECK);
		soap1CheckButton.setBounds(0, 63, 105, 16);
		soap1CheckButton.setVisible(false);

		Label email = new Label(parentComposite, SWT.NONE);
		email.setText("Email");

		Button emailCheckButton = new Button(parentComposite, SWT.CHECK);
		emailCheckButton.setText("Email");

		try {
			getWizard().getContainer().run(true, true, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException,
				InterruptedException {
				}
			});
		} catch (InvocationTargetException e2) {
			e2.printStackTrace();
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
		try {
			final ServiceManager serviceManager = PhrescoFrameworkFactory.getServiceManager();
			final List<ApplicationType> applicationTypes = serviceManager.getApplicationTypes();

			final ApplicationType applicationTypeWeb = serviceManager.getApplicationType("apptype-webapp");
			final List<Technology> webTechnologies = applicationTypeWeb.getTechnologies();

			final ApplicationType applicationTypeMob = serviceManager.getApplicationType("apptype-mobile");
			final List<Technology> mobTechnologies = applicationTypeMob.getTechnologies();
			
			final ApplicationType applicationTypeWebService = serviceManager.getApplicationType("apptype-web-services");
			final List<Technology> webServiceTechnologies = applicationTypeWebService.getTechnologies();

			for (final ApplicationType appTypes : applicationTypes) {

				if(appTypes.getName().equals("apptype-webapp")){
					btnWeb.setText(appTypes.getDisplayName());
				}
				if(appTypes.getName().equals("apptype-mobile")){
					btnMobileApp.setBounds(0, 0, 91, 18);
					btnMobileApp.setText(appTypes.getDisplayName());
				}
				if(appTypes.getName().equals("apptype-web-services")){
					btnWebServices.setBounds(0, 0, 91, 18);
					btnWebServices.setText(appTypes.getDisplayName());
				}
			}

			btnWeb.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					List<String> listTechnologyItems = new ArrayList<String>();
					for (Technology technology : webTechnologies) {
						listTechnologyItems.add(technology.getName());
					}
					String[] technologyItems = new String[listTechnologyItems.size()];
					technologyItems = listTechnologyItems.toArray(technologyItems);

					technologyCombo.setItems(technologyItems);
				}
			});
			
			btnMobileApp.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					List<String> listTechnologyItems = new ArrayList<String>();
					for (Technology technology : mobTechnologies) {
						listTechnologyItems.add(technology.getName());
					}
					String[] technologyItems = new String[listTechnologyItems.size()];
					technologyItems = listTechnologyItems.toArray(technologyItems);

					technologyCombo.setItems(technologyItems);
				}
			});
			
			btnWebServices.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					List<String> listTechnologyItems = new ArrayList<String>();
					for (Technology technology : webServiceTechnologies) {
						listTechnologyItems.add(technology.getName());
					}
					String[] technologyItems = new String[listTechnologyItems.size()];
					technologyItems = listTechnologyItems.toArray(technologyItems);

					technologyCombo.setItems(technologyItems);
				}
			});

			technologyCombo.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					technologyVersionCombo.setVisible(true);
					lblTechnologyVersion.setVisible(true);
					consumesCanvas.setEnabled(true);
					restJsonCheckButton.setVisible(true);
					restXmlCheckButton.setVisible(true);
					soapCheckButton.setVisible(true);
					soap1CheckButton.setVisible(true);
					List<String> versions = new ArrayList<String>();
					for (ApplicationType appType : applicationTypes) {
						List<Technology> technologies = appType.getTechnologies();
						for (final Technology technology : technologies) {
							if(technologyCombo.getText().equals(technology.getName())) {
								versions = technology.getVersions();
							}
							if(technologyCombo.getText().equals("iPhone Native") || technologyCombo.getText().equals("iPhone Hybrid")){
								technologyVersionCombo.setVisible(false);
								lblTechnologyVersion.setVisible(false);
							}
							if(technologyCombo.getText().equals(technology.getName())) {
								List<WebService> webServices = technology.getWebservices();
								for (WebService webService : webServices) {
									if("REST/JSON".equals(webService.getName())){
										restJsonCheckButton.setText(webService.getName());
									}
									if("REST/XML".equals(webService.getName())){
										restXmlCheckButton.setText(webService.getName());
									}
									if("SOAP1.1".equals(webService.getName()+webService.getVersion())){
										soapCheckButton.setText(webService.getName() + webService.getVersion());
									}
									if("SOAP1.2".equals(webService.getName()+webService.getVersion())){
										soap1CheckButton.setText(webService.getName() + webService.getVersion());
									}
								}								
							}
							addSupportedServersBtn.addSelectionListener(new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent e) {
									if(technology.getName().equals(technologyCombo.getItem(technologyCombo.getSelectionIndex()))){
										List<Database> databases = technology.getDatabases();
										for (Database database : databases) {
											System.out.println("Database======> " +database.getName());
										}
									}
								}
							});
						}
					}
					if(versions !=null && versions.size() >0){
						String[] version = new String[versions.size()];
						version = versions.toArray(version);
						technologyVersionCombo.setItems(version);
					}
					
				}
			});
			
			

		} catch (PhrescoException e1) {
			
		}
		setControl(parentComposite);
	}

	@Override
	public boolean canFlipToNextPage() {
		return true;
	}
}