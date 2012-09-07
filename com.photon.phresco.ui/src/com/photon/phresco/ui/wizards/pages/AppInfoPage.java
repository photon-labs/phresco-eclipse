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

import javax.ws.rs.core.MediaType;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import com.photon.phresco.commons.FrameworkConstants;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.framework.FrameworkConfiguration;
import com.photon.phresco.framework.PhrescoFrameworkFactory;
import com.photon.phresco.framework.api.ProjectAdministrator;
import com.photon.phresco.framework.api.ServiceManager;
import com.photon.phresco.framework.impl.ClientHelper;
import com.photon.phresco.model.ApplicationType;
import com.photon.phresco.model.Database;
import com.photon.phresco.model.ModuleGroup;
import com.photon.phresco.model.ProjectInfo;
import com.photon.phresco.model.Server;
import com.photon.phresco.model.Technology;
import com.photon.phresco.model.WebService;
import com.photon.phresco.ui.dialog.DataBaseDialog;
import com.photon.phresco.ui.dialog.ServerDialog;
import com.photon.phresco.ui.widgets.CustomItemList;
import com.photon.phresco.ui.widgets.CustomListLabelDecorator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Color;

/**
 * App info page
 * 
 * @author arunachalam.lakshmanan@photoninfotech.net
 *
 */

public class AppInfoPage extends WizardPage implements IWizardPage{

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

	public Button webServiceBtn;

	private boolean nextPage = false;
	
	private List<ModuleGroup> coreModules;
	
	private List<ModuleGroup> customModules;
	
	private List<ModuleGroup> jsLibraries ;
	
	private Button tech;
	
	private String techId;
	
	/**
	 *  Initialization of Feature pages
	 *
	 */
	private CoreModuleFeaturesPage coreModuleFeaturesPage;

	private CustomModuleFeaturesPage customModuleFeaturesPage;

	private JsLibraryFeaturePage jsLibraryFeaturePage;
	
	private ServerDialog serverDialog;
	
	private DataBaseDialog dbDialog;


	/**
	 * @wbp.parser.constructor
	 */
//	public AppInfoPage(String pageName) {
//		super(pageName);
//		setTitle("{Phresco}");
//		setDescription("Project Creation Page");
//	}
	
	/**
	 * @wbp.parser.constructor
	 */
	public AppInfoPage(String pageName,CoreModuleFeaturesPage featuresPageCoreModule, CustomModuleFeaturesPage featuresPageCustomModule,JsLibraryFeaturePage featurePageJsLibrary) {
		super(pageName);
		setTitle("{Phresco}");
		setDescription("Project Creation Page");
		setFeaturesPageCoreModule(featuresPageCoreModule);
		setFeaturesPageCustomModule(featuresPageCustomModule);
		setFeaturePageJsLibrary(featurePageJsLibrary);
	}
	
	/**
	 *  Getter and Setters for Feature pages.
	 *
	 */
	public JsLibraryFeaturePage getFeaturePageJsLibrary() {
		return jsLibraryFeaturePage;
	}

	public void setFeaturePageJsLibrary(JsLibraryFeaturePage featurePageJsLibrary) {
		this.jsLibraryFeaturePage = featurePageJsLibrary;
	}

	public CustomModuleFeaturesPage getFeaturesPageCustomModule() {
		return customModuleFeaturesPage;
	}

	public void setFeaturesPageCustomModule(
			CustomModuleFeaturesPage featuresPageCustomModule) {
		this.customModuleFeaturesPage = featuresPageCustomModule;
	}

	public CoreModuleFeaturesPage getFeaturesPageCoreModule() {
		return coreModuleFeaturesPage;
	}

	public void setFeaturesPageCoreModule(
			CoreModuleFeaturesPage featuresPageCoreModule) {
		this.coreModuleFeaturesPage = featuresPageCoreModule;
	}

	/**
	 *  Enable and disable the Next Button
	 *
	 */

	public boolean isNextPage() {
		return nextPage;
	}

	public void setNextPage(boolean nextPage) {
		this.nextPage = nextPage;
		setPageComplete(nextPage);
	}


	@Override
	public void createControl(Composite parent) {
		setPageComplete(false);
		serverDialog = new ServerDialog(null);
		dbDialog = new DataBaseDialog(null);
		Composite parentComposite = new Composite(parent, SWT.NULL);
		parentComposite.setLayout(new GridLayout(1,false));
		parentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		final Composite basicComposite = new Composite(parentComposite, SWT.NULL);
		basicComposite.setLayout(new GridLayout(2,false));
		basicComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label projectName = new Label(basicComposite, SWT.NONE);
		projectName.setText("Project name *");

		projectTxt = new Text(basicComposite,SWT.BORDER);
		projectTxt.setMessage("Name of the project");
		projectTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		projectTxt.setFocus();

		projectTxt.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) { 
				if(projectTxt.getText().trim().length()>0) {
					setNextPage(true);
				} else {
					setNextPage(false);
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		Label code = new Label(basicComposite, SWT.NONE);
		code.setText("Code");

		codeTxt = new Text(basicComposite, SWT.BORDER);
		codeTxt.setMessage("Project Code");

		Label description = new Label(basicComposite, SWT.NONE);
		description.setText("Description");

		descriptionTxt = new StyledText(basicComposite, SWT.BORDER);
		descriptionTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Label version = new Label(basicComposite, SWT.NONE);
		version.setText("Version");

		versionTxt = new Text(basicComposite, SWT.BORDER);
		versionTxt.setText("1.0.0");

		new Label(basicComposite, SWT.NONE);		

		Composite composite = new Composite(basicComposite, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		composite.setLayout(new GridLayout(3, false));

		final Button btnWeb = new Button(composite, SWT.RADIO);
		btnWeb.setSelection(true);
		Button btnMobileApp = new Button(composite, SWT.RADIO);
		Button btnWebServices = new Button(composite, SWT.RADIO);

		final Label lblTechnology = new Label(basicComposite, SWT.NONE);
		lblTechnology.setText("Technology");

		Composite technologyComposite = new Composite(basicComposite, SWT.NULL);
		technologyComposite.setLayout(new GridLayout(3, false));

		technologyCombo = new Combo(technologyComposite, SWT.NONE|SWT.READ_ONLY);
		technologyCombo.setText("SELECT");
		GridData gd_technologyCombo = new GridData(GridData.FILL_BOTH);
		gd_technologyCombo.widthHint = 170;
		technologyCombo.setLayoutData(gd_technologyCombo);
		technologyCombo.setItems(getTechnologies("apptype-webapp","photon"));
//		technologyCombo.select(0);
		
		final Label lblTechnologyVersion = new Label(technologyComposite, SWT.NONE);
		lblTechnologyVersion.setText("Version");

		technologyVersionCombo = new Combo(technologyComposite, SWT.BORDER | SWT.READ_ONLY);
		technologyVersionCombo.select(0);

		Label lblPilotProject = new Label(basicComposite, SWT.NONE);
		lblPilotProject.setText("Pilot Project");

		pilotProjectCombo = new Combo(basicComposite, SWT.NONE | SWT.READ_ONLY);
		
		GridData gd_pilotProjectCombo = new GridData();
		gd_pilotProjectCombo.widthHint = 154;
		pilotProjectCombo.setLayoutData(gd_pilotProjectCombo);
		
		Label supportedServers = new Label(basicComposite, SWT.NONE);
		supportedServers.setText("SupportedServers");
		
		final Composite serverComposite = new Composite(basicComposite, SWT.NONE);
		serverComposite.setLayout(new GridLayout(2,false));
		serverComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		final Button addSupportedServersBtn = new Button(serverComposite, SWT.PUSH);
		addSupportedServersBtn.setText("Add");
		final CustomItemList customItemListforServer = new CustomItemList(serverComposite, SWT.NONE);
		customItemListforServer.setDecorator(new CustomListLabelDecorator() {
			@Override
			public String getdisplayName(Object element) {
				if(element instanceof String){
					return element.toString();
				}
				return "";
			}
		});
		customItemListforServer.setVisible(false);
		final List<Object> elementListforServer = new ArrayList<Object>();
		addSupportedServersBtn.addListener (SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				serverDialog.create();
				if (serverDialog.open() == Window.OK) {
					customItemListforServer.setVisible(true);
					elementListforServer.add(serverDialog.getServer() +" "+ serverDialog.getVersion());
					customItemListforServer.addElement(elementListforServer);
					customItemListforServer.getParent().layout(true,true);
				} 
			}
		});

		Label supportedDatebases = new Label(basicComposite, SWT.NONE);
		supportedDatebases.setText("SupportedDatebases");
		
		Composite dbComposite = new Composite(basicComposite, SWT.NONE);
		dbComposite.setLayout(new GridLayout(2,false));
		dbComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Button addSupportedDatabasesBtn = new Button(dbComposite, SWT.NONE);
		addSupportedDatabasesBtn.setText("Add");
		
		final CustomItemList customItemListforDb = new CustomItemList(dbComposite, SWT.NONE);
		customItemListforDb.setDecorator(new CustomListLabelDecorator() {
			@Override
			public String getdisplayName(Object element) {
				if(element instanceof String){
					return element.toString();
				}
				return "";
			}
		});
		customItemListforDb.setVisible(false);
		final List<Object> elementListforDB = new ArrayList<Object>();
		addSupportedDatabasesBtn.addListener (SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				dbDialog.create();
				if (dbDialog.open() == Window.OK) {
					customItemListforDb.setVisible(true);
					elementListforDB.add(dbDialog.getdataBase() +" "+ dbDialog.getVersion());
					customItemListforDb.addElement(elementListforDB);
				} 
			}
		});

		Label lblConsumes = new Label(basicComposite, SWT.NONE);
		lblConsumes.setText("Consumes");

		final ScrolledComposite webServiceScrolledComposite = new ScrolledComposite(basicComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		webServiceScrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label email = new Label(basicComposite, SWT.NONE);
		email.setText("Email");

		Button emailCheckButton = new Button(basicComposite, SWT.CHECK);
		emailCheckButton.setText("Email");

		try {
			getWizard().getContainer().run(true, true, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException,
				InterruptedException {
				}
			});
		} catch (InvocationTargetException e2) {
		} catch (InterruptedException e2) {
		}
		try {
			final ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
			final List<ApplicationType> applicationTypes = administrator.getApplicationTypes("photon");
			
			for (final ApplicationType appTypes : applicationTypes) {
				if(appTypes.getName().equals("Web Application")) {
					btnWeb.setText(appTypes.getName());
				}
				if(appTypes.getName().equals("Mobile Applications")){
					btnMobileApp.setBounds(0, 0, 91, 18);
					btnMobileApp.setText(appTypes.getName());
				}
				if(appTypes.getName().equals("Web Services")) {
					btnWebServices.setBounds(0, 0, 91, 18);
					btnWebServices.setText(appTypes.getName());
				}
			}
			
			btnWeb.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					technologyCombo.setItems(getTechnologies("apptype-webapp","photon"));
					technologyCombo.select(0);
				}
			});

			btnMobileApp.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					technologyCombo.setItems(getTechnologies("apptype-mobile","photon"));
					technologyCombo.select(0);
				}
			});

			btnWebServices.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					technologyCombo.setItems(getTechnologies("apptype-web-services","photon"));
					technologyCombo.select(0);
				}
			});

			technologyCombo.addModifyListener(new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent e) {
					coreModuleFeaturesPage.setTech(technologyCombo.getText());
					customModuleFeaturesPage.setTech(technologyCombo.getText());
					jsLibraryFeaturePage.setTech(technologyCombo.getText());
					technologyVersionCombo.setVisible(true);
					lblTechnologyVersion.setVisible(true);
					List<String> versions = new ArrayList<String>();
					
					for (ApplicationType appType : applicationTypes) {
						List<Technology> technologies = appType.getTechnologies();						
						for (final Technology technology : technologies) {
							if(technologyCombo.getText().equals(technology.getName())) {
								techId = technology.getId();
								versions = technology.getVersions();
								pilots = getPilots(techId,"photon");
//							}
							if(versions.size() <= 0) {
								technologyVersionCombo.setVisible(false);
								lblTechnologyVersion.setVisible(false);
							}
//							if(technologyCombo.getText().equals(technology.getName())) {
								try { 
								List<WebService> webServices = administrator.getWebServices(techId, "photon");
								Group webServiceGroup = new Group(webServiceScrolledComposite, SWT.BAR);
								webServiceGroup.setLayout(new GridLayout(1,false));
								webServiceGroup.setBackground(new Color(null, 255, 255, 255));
								webServiceGroup.setText("WebServices");
								int size = 5;
								for (WebService webService : webServices) {
									webServiceBtn = new Button(webServiceGroup, SWT.CHECK);
									webServiceBtn.setText(webService.getName()+ " "+ webService.getVersion());
									webServiceBtn.setBackground(new Color(null, 255, 255, 255));
									size = size + 21;
								}	
								int vertical_scroll_size = size;
								webServiceScrolledComposite.setContent(webServiceGroup);
								webServiceScrolledComposite.setExpandHorizontal(true);
								webServiceScrolledComposite.setExpandVertical(true);
								webServiceScrolledComposite.setMinSize(webServiceScrolledComposite.computeSize(SWT.H_SCROLL, vertical_scroll_size));
							} catch(PhrescoException pe) {
								
							}
							} 
						}
					}
					if(versions !=null && versions.size() >0) {
						String[] version = new String[versions.size()];
						version = versions.toArray(version);
						technologyVersionCombo.setItems(version);
						technologyVersionCombo.select(0);
					}

					coreModules = getCoreModules(techId,"photon");
					if(coreModules != null && coreModules.size() > 0){
						coreModuleFeaturesPage.setFeatures(coreModules);
					} else {
						coreModuleFeaturesPage.setFeatures(null);
					}

					customModules = getCustomModules(techId,"photon");
					if(customModules != null && customModules.size() > 0) {
						customModuleFeaturesPage.setFeatures(customModules);
					} else {
						customModuleFeaturesPage.setFeatures(null);
					}

					jsLibraries = getjsLibraries(techId,"photon");
					if(jsLibraries != null && jsLibraries.size() > 0){
						jsLibraryFeaturePage.setFeatures(jsLibraries);
					} else {
						jsLibraryFeaturePage.setFeatures(null);
					}

					if(pilots !=null && pilots.size() >0) {
						String[] pilotNames = new String[pilots.size()];
						for(int i=0; i < pilots.size();i++) {
							pilotNames[i] = pilots.get(i).getName();
						}
						pilotProjectCombo.setItems(pilotNames);
						pilotProjectCombo.select(0);
					} 
					serverDialog.setTechId(techId);
					dbDialog.setTechId(techId);
				}
			});
			technologyCombo.select(0);
			pilotProjectCombo.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					coreModuleFeaturesPage.setPilotProject(pilotProjectCombo.getText());
					coreModuleFeaturesPage.getPilotProjectName();
					List<ProjectInfo> pilots = getPilots(techId, "photon");
					for (ProjectInfo pilotProjectInfo : pilots) {
							List<Database> databases = pilotProjectInfo.getTechnology().getDatabases();
							for (Database database : databases) {
								elementListforDB.add(database.getName() +" "+ database.getVersions());
							}
						    List<Server> servers = pilotProjectInfo.getTechnology().getServers();
						    for (Server server : servers) {
						    	elementListforServer.add(server.getName() +" "+ server.getVersions());
							}
					}
					customItemListforServer.setVisible(true);
					customItemListforServer.addElement(elementListforServer);
					customItemListforServer.getParent().layout(true,true);
					customItemListforDb.setVisible(true);
					customItemListforDb.addElement(elementListforDB);
					customItemListforDb.getParent().layout(true,true);
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

	private List<ProjectInfo> getPilots(String techId,String customerId){
		try{
			ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
			List<ProjectInfo> pi = administrator.getPilots(techId, customerId);
			if(pi != null) {
			return pi;
			}
		}catch(PhrescoException ex){
			ex.printStackTrace();
		}
		return null;
	}	

	public String[] getTechnologies(String appType,String customerId) {

		try {
			ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
			technologies = administrator.getTechnologies(appType, customerId);
			List<String> listTechnologyItems = new ArrayList<String>();
			for (Technology technology : technologies) {
				listTechnologyItems.add(technology.getName());
			}
			String[] technologyItems = new String[listTechnologyItems.size()];
			technologyItems = listTechnologyItems.toArray(technologyItems);
			return technologyItems;
		}catch (PhrescoException e) {
		}
		return null;
	}

//	private List<ModuleGroup> getModules() {
//		try {
//			ServiceManager serviceManager = PhrescoFrameworkFactory.getServiceManager();
//			List<ApplicationType> applicationTypes = serviceManager.getApplicationTypes();
//			for (ApplicationType applicationType : applicationTypes) {
//				List<Technology> technologies = applicationType.getTechnologies();
//				for (Technology technology : technologies) {
//					if(technology.getName().equals(technologyCombo.getText())) {
//						List<ModuleGroup> modules = technology.getModules();
//						return modules;
//					}
//				}
//			}
//
//		} catch (PhrescoException e) {
//		} 
//		return null;
//	}

	private List<ModuleGroup> getCoreModules(String techId, String customerId) {
		try {
			ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
			List<ModuleGroup> coreModules = administrator.getCoreModules(techId, customerId);
			return coreModules;
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}

	public List<ModuleGroup> getCustomModules(String techId,String customerId) {
		try {
			ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
			List<ModuleGroup> customModules = administrator.getCustomModules(techId, customerId);
			return customModules;
		} catch (Exception e) {
		} 
		return null;
	}

	public List<ModuleGroup> getjsLibraries(String techId,String customerId) {
		try {
			ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
			List<ModuleGroup> jsLibs = administrator.getJSLibs(techId, customerId);
			return jsLibs;
		} catch (PhrescoException e) {
		} 
		return null;
	}
	
	@Override
	public IWizardPage getNextPage() {
//		if(coreModules == null || coreModules.isEmpty()) {
//			return customModuleFeaturesPage; 
//		}
//		if(customModules == null || customModules.isEmpty()) {
//			System.out.println("custom===>" + customModules.size());
//			return jsLibraryFeaturePage;
//		}
//		if(jsLibraries == null || jsLibraries.isEmpty()){
//			System.out.println("Js===>" + jsLibraries.size());
//			return coreModuleFeaturesPage;
//		}
		return super.getNextPage();
	}
	
//	public void getFeatures (String technologyName) {
//		
//		coreModuleFeaturesPage.setTech(technologyName);
//		customModuleFeaturesPage.setTech(technologyName);
//		jsLibraryFeaturePage.setTech(technologyName);
//		
//		coreModules = getCoreModules();
//		if(coreModules != null && coreModules.size() > 0){
//			coreModuleFeaturesPage.populateCoreModules(coreModules);
//		} else {
//			coreModuleFeaturesPage.populateCoreModules(null);
//		}
//
//		customModules = getCustomModules();
//		if(customModules != null && customModules.size() > 0) {
//			customModuleFeaturesPage.populateCustomModules(customModules);
//		} else {
//			customModuleFeaturesPage.populateCustomModules(null);
//		}
//
//		jsLibraries = getjsLibraries();
//		if(jsLibraries != null && jsLibraries.size() > 0){
//			jsLibraryFeaturePage.populateJsLibraries(jsLibraries);
//		} else {
//			jsLibraryFeaturePage.populateJsLibraries(null);
//		}
//	}	
}