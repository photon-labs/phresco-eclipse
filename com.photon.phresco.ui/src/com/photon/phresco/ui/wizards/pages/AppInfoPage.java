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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.photon.phresco.commons.model.User;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.framework.PhrescoFrameworkFactory;
import com.photon.phresco.framework.api.ProjectAdministrator;
import com.photon.phresco.model.ApplicationType;
import com.photon.phresco.model.ProjectInfo;
import com.photon.phresco.model.Technology;
import com.photon.phresco.model.WebService;
import com.photon.phresco.ui.Activator;
import com.photon.phresco.ui.dialog.ServerDialog;
import com.photon.phresco.ui.preferences.PreferenceConstants;
import com.photon.phresco.util.Credentials;

/**
 * App info page
 * 
 * @author arunachalam.lakshmanan@photoninfotech.net
 *
 */
public class AppInfoPage extends WizardPage implements IWizardPage {

	
	//project name;
	public Text projectTxt;
	//project code
	public Text codeTxt;
	//project description
	public StyledText descriptionTxt;
	//project version
	public Text versionTxt;
	//project Application Type	
	public String appTypeConstant;
	//project Technology
	public Combo technologyCombo; 
	//project Technologies
	public List<Technology> technologies;
	//project Technology version
	public Combo technologyVersionCombo;
	//project pilot
	public Combo pilotProjectCombo;
	// pilots
	public List<ProjectInfo> pilots = new ArrayList<ProjectInfo>();
	
	List<Technology> webTechnologies = new ArrayList<Technology>();
	List<Technology> mobTechnologies = new ArrayList<Technology>();
	List<Technology> webServiceTechnologies = new ArrayList<Technology>();
	
	//Table Titles
	String titles[] = new String[] {"Server", "version"};
	//private DefaultTableModel dataModel;
	//private JTable table;
	//private JScrollPane scrollPane;
	private boolean nextPage = false;
	
	
	public boolean isNextPage() {
		return nextPage;
	}
	
	public void setNextPage(boolean nextPage) {
		this.nextPage = nextPage;
		setPageComplete(nextPage);
	}



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
		
		setPageComplete(false);
		
		Composite parentComposite = new Composite(parent, SWT.NULL);
		parentComposite.setLayout(new GridLayout(2,false));
		parentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label projectName = new Label(parentComposite, SWT.NONE);
		projectName.setText("Project name");

		projectTxt = new Text(parentComposite,SWT.BORDER);
		projectTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		projectTxt.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				if(projectTxt.getText().trim().length()>0){
					setNextPage(true);
				}else{
					setNextPage(false);
				}
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		
		Label code = new Label(parentComposite, SWT.NONE);
		code.setText("Code");

		codeTxt = new Text(parentComposite, SWT.BORDER);
		//		codeTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label description = new Label(parentComposite, SWT.NONE);
		description.setText("Description");

		descriptionTxt = new StyledText(parentComposite, SWT.BORDER);
		descriptionTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Label version = new Label(parentComposite, SWT.NONE);
		version.setText("Version");

		versionTxt = new Text(parentComposite, SWT.BORDER);
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
		//composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		technologyComposite.setLayout(new GridLayout(4, false));

		final Label lblTechnology = new Label(technologyComposite, SWT.NONE);
		GridData gd_technology = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_technology.widthHint = 96;
		lblTechnology.setLayoutData(gd_technology);
		lblTechnology.setText("Technology");

		technologyCombo = new Combo(technologyComposite, SWT.NONE);
		technologyCombo.setText("SELECT");
		GridData gd_technologyCombo = new GridData(GridData.FILL_BOTH);
		gd_technologyCombo.widthHint = 154;
		technologyCombo.setLayoutData(gd_technologyCombo);


		final Label lblTechnologyVersion = new Label(technologyComposite, SWT.NONE);
		lblTechnologyVersion.setText("Version");

		technologyVersionCombo = new Combo(technologyComposite, SWT.BORDER);
		technologyVersionCombo.setText("SELECT VERSION");

		Label lblPilotProject = new Label(parentComposite, SWT.NONE);
		lblPilotProject.setText("Pilot Project");

		pilotProjectCombo = new Combo(parentComposite, SWT.NONE);
		//String[] pilotProjectComboItems = {"None","PhpBlog"};
		GridData gd_pilotProjectCombo = new GridData();
		gd_pilotProjectCombo.widthHint = 154;
		pilotProjectCombo.setLayoutData(gd_pilotProjectCombo);
		//pilotProjectCombo.setItems(pilotProjectComboItems);

		Label supportedServers = new Label(parentComposite, SWT.NONE);
		supportedServers.setText("SupportedServers");

		
		
		//TODO Popup changes starts here
		
		Button button = new Button (parentComposite, SWT.PUSH);
		button.setText("Configure");
		
		
		
		button.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event e) {
				ServerDialog dialog = new ServerDialog(null);
				dialog.create();
				if (dialog.open() == Window.OK) {
				  System.out.println(dialog.getServer());
				  System.out.println(dialog.getVersion());
				} 
			}
		});
		
		//TODO Popup changes end here
		
		/**Composite serverComposite = new Composite(parentComposite, SWT.NONE);
		serverComposite.setLayoutData(new GridData(SWT.LEFT, SWT.LEFT, true, true, 1, 1));
		serverComposite.setLayout(new GridLayout(4, false));
		final Table rightTable = new Table (serverComposite, SWT.NONE);		 
		Button button = new Button (serverComposite, SWT.PUSH);
		button.setText("Add");
		final Table leftTable = new Table (serverComposite, SWT.NONE);
		
		button.addListener (SWT.Selection, new Listener () {
			List<TableItem> items = new ArrayList<TableItem>();
			public void handleEvent (Event e) {	
			 	TableItem item = new TableItem(rightTable, SWT.NONE);
			 	items.add(item);
			 	for(int i=0; i< items.size(); i++){
			 		item.setText(i, "Tomcat 7.0");
			 	}
			 	openCompliancePreferencePage();
			 	System.out.println("size is :: " + items.size());
		    }
		});
		
		*/
		  //final TableColumn [] columns = table.getColumns ();
		  
		
		/*Object[][] stats = getTableRecords();
		dataModel = new DefaultTableModel(stats, titles){
			
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		table = new JTable(dataModel);
		defineTableColumn(table);
		scrollPane = new JScrollPane();
		scrollPane.setViewportView(table);
		//parentComposite
		scrollPane.setBounds(0, 0, 0, 0);
		*/
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
			//Create Phresco Project
			ProjectAdministrator admin = PhrescoFrameworkFactory.getProjectAdministrator();
			
			
		
			
			final List<ApplicationType> applicationTypes = admin.getApplicationTypes("photon");
			for(ApplicationType app: applicationTypes){
				if(app.getId().equalsIgnoreCase("apptype-webapp")){
					webTechnologies = app.getTechnologies();
				}else if(app.getId().equalsIgnoreCase("apptype-mobile")){
					mobTechnologies = app.getTechnologies();
				}else if(app.getId().equalsIgnoreCase("apptype-web-services")){
					webServiceTechnologies = app.getTechnologies();
				}
			}
		

			for (final ApplicationType appTypes : applicationTypes) {

				if(appTypes.getId().equals("apptype-webapp")){
					btnWeb.setText(appTypes.getName());
				}
				if(appTypes.getId().equals("apptype-mobile")){
					btnMobileApp.setBounds(0, 0, 91, 18);
					btnMobileApp.setText(appTypes.getName());
				}
				if(appTypes.getId().equals("apptype-web-services")){
					btnWebServices.setBounds(0, 0, 91, 18);
					btnWebServices.setText(appTypes.getName());
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
					appTypeConstant = "apptype-webapp";
					technologies = webTechnologies;
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
					appTypeConstant = "apptype-mobile";
					technologies = mobTechnologies;
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
					appTypeConstant = "apptype-web-services";
					technologies = webServiceTechnologies;
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
					//List<ProjectInfo> pilots = new ArrayList<ProjectInfo>();
					for (ApplicationType appType : applicationTypes) {
						List<Technology> technologies = appType.getTechnologies();
						for (final Technology technology : technologies) {
							if(technologyCombo.getText().equals("iPhone Native") || technologyCombo.getText().equals("iPhone Hybrid")){
								technologyVersionCombo.setVisible(false);
								lblTechnologyVersion.setVisible(false);
							}
							if(technologyCombo.getText().equals(technology.getName())) {
								versions = technology.getVersions();
								pilots = getPilots(technology.getId());
								List<WebService> webServices = getServices(technology.getId()); //technology.getWebservices();
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
							/*addSupportedServersBtn.addSelectionListener(new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent e) {
									if(technology.getName().equals(technologyCombo.getItem(technologyCombo.getSelectionIndex()))){
										List<Database> databases = technology.getDatabases();
										for (Database database : databases) {
											System.out.println("Database======> " +database.getName());
										}
									}
								}
							});*/
						}
					}
					if(versions !=null && versions.size() >0){
						String[] version = new String[versions.size()];
						version = versions.toArray(version);
						technologyVersionCombo.setItems(version);
					}
					
					if(pilots !=null && pilots.size() >0){
						String[] pilotNames = new String[pilots.size()];
						for(int i=0; i < pilots.size(); i++){
							pilotNames[i] = pilots.get(i).getName();
						}
						pilotProjectCombo.setItems(pilotNames);
					}
					
				}
			});
			
			

		} catch (PhrescoException e1) {
			
		}
		setControl(parentComposite);
	}
	

	@Override
	public boolean canFlipToNextPage() {
		return nextPage;
	}
	
	public Object[][] getTableRecords() {
	    Object[][] results = new Object[0][titles.length];
	    return results;
	}
	
	private List<ProjectInfo> getPilots(String technology){
		try{
			ProjectAdministrator admin = PhrescoFrameworkFactory.getProjectAdministrator();
	        List<ProjectInfo> pi = admin.getPilots( technology, "photon");
	        return pi;
			
		}catch(PhrescoException ex){
			ex.printStackTrace();
		}
		return null;
		
	}
	
	private List<WebService> getServices(String technology){
		try{
			ProjectAdministrator admin = PhrescoFrameworkFactory.getProjectAdministrator();
			List<WebService> pi = admin.getWebServices( technology, "photon" );
	        return pi;
			
		}catch(PhrescoException ex){
			ex.printStackTrace();
		}
		return null;
		
	}
	
	
	//private 
	
	
	
	
}