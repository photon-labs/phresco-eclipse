package com.photon.phresco.ui.phrescoexplorer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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
import com.photon.phresco.commons.model.ArtifactGroupInfo;
import com.photon.phresco.commons.model.ArtifactInfo;
import com.photon.phresco.commons.model.Category;
import com.photon.phresco.commons.model.DownloadInfo;
import com.photon.phresco.commons.model.ProjectInfo;
import com.photon.phresco.commons.model.WebService;
import com.photon.phresco.commons.util.ApplicationManagerUtil;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.commons.util.ProjectManager;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.ui.model.BaseAction;
import com.photon.phresco.ui.resource.Messages;
import com.photon.phresco.ui.wizards.componets.DatabaseComponent;
import com.photon.phresco.ui.wizards.componets.ServerComponent;

/**
 * @author suresh_ma
 *
 */
public class EditProject extends AbstractHandler implements PhrescoConstants {
	
	private Text nameText;
	private Text codeText;
	private Text descText;
	private Text appDirText;
	private Text versionText;
	
	private List<ServerComponent> serverComponents = new ArrayList<ServerComponent>();
	private List<DatabaseComponent> dbComponents = new ArrayList<DatabaseComponent>();
	
	private Button[] webServiceButtons;
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final Shell shell = HandlerUtil.getActiveShell(event);
		final Shell buildDialog = new Shell(shell, SWT.APPLICATION_MODAL |  SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.TITLE | SWT.RESIZE);
		buildDialog.setText(Messages.EDIT_PROJECT_DIALOG_TITLE);
		GridLayout layout = new GridLayout(1, false);
		buildDialog.setLocation(385, 130);
		buildDialog.setLayout(layout);
		buildDialog.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		final ScrolledComposite scrolledComposite = new ScrolledComposite(buildDialog, SWT.V_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		scrolledComposite.setAlwaysShowScrollBars(true);
		
		final Composite composite = new Composite(scrolledComposite, SWT.NONE);
		GridLayout CompositeLayout = new GridLayout(1, true);
		composite.setLayout(CompositeLayout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		try {
			
			BaseAction baseAction = new BaseAction();
			ServiceManager serviceManager = PhrescoUtil.getServiceManager(baseAction.getUserId());
			if(serviceManager == null) {
				ConfirmDialog.getConfirmDialog().showConfirm(shell);
				return null;
			}
			
			ProjectInfo projectInfo = PhrescoUtil.getProjectInfo();
			final ApplicationInfo appInfo = PhrescoUtil.getApplicationInfo();
			final String customerId = PhrescoUtil.getCustomerId();
			final String techId = PhrescoUtil.getTechId();
			final String platform = PhrescoUtil.findPlatform();
			createAppInfoPage(composite, projectInfo, appInfo);
			
			final List<DownloadInfo> servers = serviceManager.getDownloads(customerId, techId, Category.SERVER.name(), platform);
			if(CollectionUtils.isNotEmpty(servers)) {
				final Group serverGroup = new Group(composite, SWT.NONE);
				serverGroup.setText(Messages.SERVERS);
				GridLayout serverLayout = new GridLayout(1, false);
				serverGroup.setLayout(serverLayout);
				serverGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				
				Composite serverComposite = new Composite(serverGroup, SWT.NONE);
				GridLayout gridLayout = new GridLayout(5, false);
				serverComposite.setLayout(gridLayout);
				serverComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				
				ServerComponent serverComponent = new ServerComponent();
				serverComponent.getServers(serverComposite, servers, customerId, techId, platform);
				serverComponents.add(serverComponent);
				
				Button serverAddButton = new Button(serverComposite, SWT.PUSH);
				serverAddButton.setText(PLUS_SYMBOL);
				
				serverAddButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						try {
							final ServerComponent serverComponent = new ServerComponent();
							final Composite serverComposite = new Composite(serverGroup, SWT.NONE);
							GridLayout gridLayout = new GridLayout(5, false);
							serverComposite.setLayout(gridLayout);
							serverComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
							
							serverComponent.getServers(serverComposite, servers, customerId, techId, platform);
							serverComponents.add(serverComponent);
							Button serverDeleteButton = new Button(serverComposite, SWT.PUSH);
							serverDeleteButton.setText(MINUS_SYMBOL);
							
							serverDeleteButton.addSelectionListener(new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent e) {
									serverComponents.remove(serverComponent);
									serverComposite.dispose();
									composite.pack();
									super.widgetSelected(e);
								}
							});
						} catch (PhrescoException e1) {
							PhrescoDialog.errorDialog(buildDialog, Messages.ERROR, e1.getLocalizedMessage());
						}
						serverGroup.redraw();
						serverGroup.pack();
						composite.redraw();
						composite.pack();
						reSize(composite, scrolledComposite);
						super.widgetSelected(e);
					}
				});
			}
			final List<DownloadInfo> dataBases = serviceManager.getDownloads(customerId, techId, Category.DATABASE.name(), platform);
			if(CollectionUtils.isNotEmpty(dataBases)) {
				final Group dbGroup = new Group(composite, SWT.NONE);
				dbGroup.setText(Messages.DATABASES);
				GridLayout dbLlayout = new GridLayout(1, false);
				dbGroup.setLayout(dbLlayout);
				dbGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				
				Composite dbComposite = new Composite(dbGroup, SWT.NONE);
				GridLayout gridLayout = new GridLayout(5, false);
				dbComposite.setLayout(gridLayout);
				dbComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				
				DatabaseComponent dbComponent = new DatabaseComponent();
				dbComponent.getDataBases(dbComposite, dataBases, customerId, techId, platform);
				dbComponents.add(dbComponent);
				
				Button dbAddButton = new Button(dbComposite, SWT.PUSH);
				dbAddButton.setText(PLUS_SYMBOL);
				
				dbAddButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						try {
							final Composite dbComposite = new Composite(dbGroup, SWT.NONE);
							GridLayout gridLayout = new GridLayout(5, false);
							dbComposite.setLayout(gridLayout);
							dbComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
							
							final DatabaseComponent dbComponent = new DatabaseComponent();
							dbComponent.getDataBases(dbComposite, dataBases, customerId, techId, platform);
							dbComponents.add(dbComponent);
							
							Button dbDeleteButton = new Button(dbComposite, SWT.PUSH);
							dbDeleteButton.setText(MINUS_SYMBOL);
							
							dbDeleteButton.addSelectionListener(new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent e) {
									dbComponents.remove(dbComponent);
									dbComposite.dispose();
									composite.pack();
									super.widgetSelected(e);
								}
							});
							
						} catch (PhrescoException e1) {
							PhrescoDialog.errorDialog(buildDialog, Messages.ERROR, e1.getLocalizedMessage());
						}
						dbGroup.redraw();
						composite.redraw();
						composite.pack();
						reSize(composite, scrolledComposite);
						super.widgetSelected(e);
					}
				});
			}
			getWebServices(serviceManager, composite);
			
			Composite updateComposite = new Composite(buildDialog, SWT.NONE);
			GridLayout gridLayout = new GridLayout(2, false);
			updateComposite.setLayout(gridLayout);
			updateComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.END, true, true, 1, 1));
			
			final Button updateButton = new Button(updateComposite, SWT.PUSH);
			updateButton.setText(Messages.UPDATE);
			
			Listener updateListener = new Listener() {
				@Override
				public void handleEvent(Event events) {
					boolean validate = validate(shell);
					if(!validate) {
						return;
					}
					//To set the basic information into applicationInfo
					ApplicationManagerUtil managerUtil = new ApplicationManagerUtil();
					appInfo.setAppDirName(appDirText.getText());
					appInfo.setCode(codeText.getText());
					appInfo.setName(nameText.getText());
					appInfo.setVersion(versionText.getText());
					if(StringUtils.isNotEmpty(descText.getText())) {
						appInfo.setDescription(descText.getText());
					}
					String oldAppDirName = PhrescoUtil.getProjectName();
					
					//To set the selected server
					setSelectedServers(appInfo);
					setSelectedDatabase(appInfo);
					
					List<String> webServiceIds = new ArrayList<String>();
					if(webServiceButtons != null) {
						List<Button> buttons = Arrays.asList(webServiceButtons);
						int i = 0;
						for (Button button : buttons) {
							if(button.getSelection()) {
								String webServiceId = (String) button.getData(button.getText());
								webServiceIds.add(webServiceId);
							}
							i = i+ 1;
						}
					}
					if(CollectionUtils.isNotEmpty(webServiceIds)) {
						appInfo.setSelectedWebservices(webServiceIds);
					}
					
					try {
						managerUtil.updateApplication(oldAppDirName, appInfo);
					} catch (PhrescoException e) {
						PhrescoDialog.exceptionDialog(shell, e);
					}
					if(!oldAppDirName.equals(appInfo.getAppDirName())) {
						ProjectManager.deleteProjectIntoWorkspace(oldAppDirName);
						ProjectManager.updateProjectIntoWorkspace(appInfo.getAppDirName());
					}
					serverComponents.clear();
					dbComponents.clear();
					buildDialog.close();
				}
			};
			
			updateButton.addListener(SWT.Selection, updateListener);
			
			Button cancelButton = new Button(updateComposite, SWT.PUSH);
			cancelButton.setText(Messages.CANCEL);
			
			cancelButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					buildDialog.close();
					serverComponents.clear();
					dbComponents.clear();
					super.widgetSelected(e);
				}
			});
			
		} catch (PhrescoException e) {
			PhrescoDialog.errorDialog(buildDialog, Messages.ERROR, e.getLocalizedMessage());
		}
		
		scrolledComposite.setContent(composite);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		reSize(composite, scrolledComposite);
		scrolledComposite.pack();
		scrolledComposite.redraw();
		Point size = scrolledComposite.getSize();
		int x = size.x;
		int y = size.y;
		buildDialog.setSize(x + 100, y + 100);
		buildDialog.open();
		return null;
	}
	
	/**
	 * @param buildDialog
	 */
	private void createAppInfoPage(Composite comp, ProjectInfo projectInfo, ApplicationInfo appInfo) {
		
		if(projectInfo == null || appInfo == null) {
			return;
		}
		Composite composite = new Composite(comp, SWT.BORDER);
		GridLayout gridlayout = new GridLayout(6, false);
		composite.setLayout(gridlayout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label nameLable = new Label(composite, SWT.NONE);
		nameLable.setText(Messages.NAME);

		nameText = new Text(composite, SWT.BORDER);
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nameText.setText(appInfo.getName());

		Label codeLable = new Label(composite, SWT.NONE);
		codeLable.setText(Messages.CODE);

		codeText = new Text(composite, SWT.BORDER);
		codeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		codeText.setText(appInfo.getCode());

		Label descLable = new Label(composite, SWT.NONE);
		descLable.setText(Messages.DESCRIPTION);

		descText = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData descGridData = new GridData();
		descGridData.widthHint = 100;
		descGridData.heightHint = 40;
		descText.setLayoutData(descGridData);
		if(StringUtils.isNotEmpty(appInfo.getDescription())) { 
			descText.setText(appInfo.getDescription());
		}

		Label appDirLabel = new Label(composite, SWT.NONE);
		appDirLabel.setText(Messages.APP_DIRECTORY);

		appDirText = new Text(composite, SWT.BORDER);
		appDirText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		appDirText.setText(appInfo.getAppDirName());

		Label versionLabel = new Label(composite, SWT.NONE);
		versionLabel.setText(Messages.VERSION);

		versionText = new Text(composite, SWT.BORDER);
		versionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		versionText.setText(appInfo.getVersion());

		Label techLabel = new Label(composite, SWT.NONE);
		techLabel.setText(Messages.TECHNOLOGY);

		Text techText = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		techText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		techText.setText(appInfo.getTechInfo().getName() + Messages.HYPHEN + appInfo.getTechInfo().getVersion());
	}

	
	private void getWebServices(ServiceManager serviceManager, Composite composite) {
		try {
			List<WebService> webServices = serviceManager.getWebServices();
			if(CollectionUtils.isEmpty(webServices)) {
				return;
			}
			Group webserviceGroup = new Group(composite, SWT.NONE);
			webserviceGroup.setText(Messages.WEBSERVICES);
			GridLayout webServiceLayout = new GridLayout(5, false);
			webserviceGroup.setLayout(webServiceLayout);
			webserviceGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			webServiceButtons = new Button[webServices.size()];
			int i =0;
			for (WebService webService : webServices) {
				webServiceButtons[i] = new Button(webserviceGroup, SWT.CHECK);
				webServiceButtons[i].setText(webService.getName());
				webServiceButtons[i].setData(webService.getName(), webService.getId());
				i = i + 1;
			}
		} catch (PhrescoException e) {
			PhrescoDialog.errorDialog(composite.getShell(), Messages.ERROR, e.getLocalizedMessage());
		}
	}

	private void reSize(final Composite composite, final ScrolledComposite scrolledComposite) {
		composite.addListener(SWT.Resize, new Listener() {
			int width = -1;
			@Override
			public void handleEvent(Event event) {
				int newWidth = composite.getSize().x;
				if (newWidth != width) {
			        scrolledComposite.setMinHeight(composite.computeSize(newWidth, SWT.DEFAULT).y);
			        width = newWidth;
			    }
			}
		});
	}
	
	private boolean validate(Shell shell) {
		if(StringUtils.isEmpty(nameText.getText())) {
			PhrescoDialog.errorDialog(shell, Messages.ERROR, "Name should no be empty");
			return false;
		} if(StringUtils.isEmpty(codeText.getText())) {
			PhrescoDialog.errorDialog(shell, Messages.ERROR, "Code should no be empty");
			return false;
		} if(StringUtils.isEmpty(appDirText.getText())) {
			PhrescoDialog.errorDialog(shell, Messages.ERROR, "App Directory should no be empty");
			return false;
		}  if(StringUtils.isEmpty(versionText.getText())) {
			PhrescoDialog.errorDialog(shell, Messages.ERROR, "Version should no be empty");
			return false;
		} 
		return true;
	}
	
	/**
	 * @param appInfo
	 * @param artifactGroupInfos
	 */
	private void setSelectedServers(final ApplicationInfo appInfo) {
		if(CollectionUtils.isNotEmpty(serverComponents)) {
			List<ArtifactGroupInfo> artifactGroupInfos = new ArrayList<ArtifactGroupInfo>();
			for (ServerComponent serverComponent : serverComponents) {
				String serverName = serverComponent.serverNameCombo.getText();
				String[] selectedVersions = serverComponent.serverVersionListBox.getSelection();
				String serverId = ServerComponent.serverIdMap.get(serverName);
				ArtifactGroupInfo artifactGroupInfo = new ArtifactGroupInfo();
				artifactGroupInfo.setArtifactGroupId(serverId);
				
				List<String> artifactInfoIds = new ArrayList<String>();
				List<ArtifactInfo> artifactInfos = ServerComponent.serverVersionMap.get(serverName);
				for (ArtifactInfo artifactInfo : artifactInfos) {
					List<String> selectedVersionsList = Arrays.asList(selectedVersions);
					if(selectedVersionsList.contains(artifactInfo.getVersion())) {
						artifactInfoIds.add(artifactInfo.getId());
					}
				}
				artifactGroupInfo.setArtifactInfoIds(artifactInfoIds);
				artifactGroupInfos.add(artifactGroupInfo);
				appInfo.setSelectedServers(artifactGroupInfos);
			}
		}
	}
	
	/**
	 * @param appInfo
	 * @param artifactGroupInfos
	 */
	private void setSelectedDatabase(final ApplicationInfo appInfo) {
		if(CollectionUtils.isNotEmpty(dbComponents)) {
			List<ArtifactGroupInfo> artifactGroupInfos = new ArrayList<ArtifactGroupInfo>();
			for (DatabaseComponent dbComponent : dbComponents) {
				String dataBaseName = dbComponent.dbNameCombo.getText();
				String[] selectedVersions = dbComponent.dbVersionListBox.getSelection();
				String serverId = DatabaseComponent.dbIdMap.get(dataBaseName);
				ArtifactGroupInfo artifactGroupInfo = new ArtifactGroupInfo();
				artifactGroupInfo.setArtifactGroupId(serverId);
				
				List<String> artifactInfoIds = new ArrayList<String>();
				List<ArtifactInfo> artifactInfos = DatabaseComponent.dbVersionMap.get(dataBaseName);
				for (ArtifactInfo artifactInfo : artifactInfos) {
					List<String> selectedVersionsList = Arrays.asList(selectedVersions);
					if(selectedVersionsList.contains(artifactInfo.getVersion())) {
						artifactInfoIds.add(artifactInfo.getId());
					}
				}
				artifactGroupInfo.setArtifactInfoIds(artifactInfoIds);
				artifactGroupInfos.add(artifactGroupInfo);
				appInfo.setSelectedDatabases(artifactGroupInfos);
			}
		}
	}
}
