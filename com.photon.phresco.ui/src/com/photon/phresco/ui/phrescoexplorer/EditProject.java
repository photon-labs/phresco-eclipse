package com.photon.phresco.ui.phrescoexplorer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.handlers.HandlerUtil;

import com.photon.phresco.commons.PhrescoDialog;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.model.ArtifactInfo;
import com.photon.phresco.commons.model.Category;
import com.photon.phresco.commons.model.DownloadInfo;
import com.photon.phresco.commons.model.ProjectInfo;
import com.photon.phresco.commons.model.WebService;
import com.photon.phresco.commons.util.BaseAction;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.ui.resource.Messages;

/**
 * @author suresh_ma
 *
 */
public class EditProject extends AbstractHandler {
	
	public Combo serverNameCombo;
	public org.eclipse.swt.widgets.List serverVersionListBox;
	public org.eclipse.swt.widgets.List dbVersionListBox;
	public Combo dbNameCombo;
	
	public static Map<String, List<String>> serverVersionMap = new HashMap<String, List<String>>();
	public static Map<String, List<String>> dbVersionMap = new HashMap<String, List<String>>();
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final Shell shell = HandlerUtil.getActiveShell(event);
		final Shell buildDialog = new Shell(shell, SWT.APPLICATION_MODAL |  SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.TITLE | SWT.RESIZE);
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
			String userId = baseAction.getUserId();
			ProjectInfo projectInfo = PhrescoUtil.getProjectInfo();
			ApplicationInfo appInfo = PhrescoUtil.getApplicationInfo();
			final ServiceManager serviceManager = PhrescoUtil.getServiceManager(userId);
			if(serviceManager == null) {
				PhrescoDialog.errorDialog(buildDialog, Messages.WARNING, Messages.PHRESCO_LOGIN_WARNING);
				return null;
			}
			final String customerId = PhrescoUtil.getCustomerId();
			final String techId = PhrescoUtil.getTechId();
			final String platform = PhrescoUtil.findPlatform();
			createAppInfoPage(composite, projectInfo, appInfo);
			
			final Group serverGroup = new Group(composite, SWT.NONE);
			serverGroup.setText(Messages.SERVERS);
			GridLayout serverLayout = new GridLayout(5, false);
			serverGroup.setLayout(serverLayout);
			serverGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			getServers(serverGroup, serviceManager, customerId, techId, platform);
			
			Button serverAddButton = new Button(serverGroup, SWT.PUSH);
			serverAddButton.setText("+");
			serverAddButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						getServers(serverGroup, serviceManager, customerId, techId, platform);
						
						Button serverDeleteButton = new Button(serverGroup, SWT.PUSH);
						serverDeleteButton.setText("-");
						
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
			
			final Group dbGroup = new Group(composite, SWT.NONE);
			dbGroup.setText(Messages.DATABASES);
			GridLayout dbLlayout = new GridLayout(5, false);
			dbGroup.setLayout(dbLlayout);
			dbGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			getDataBases(dbGroup, serviceManager, customerId, techId, platform);
			
			Button dbAddButton = new Button(dbGroup, SWT.PUSH);
			dbAddButton.setText("+");
			
			dbAddButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						getServers(dbGroup, serviceManager, customerId, techId, platform);
						
						Button dbDeleteButton = new Button(dbGroup, SWT.PUSH);
						dbDeleteButton.setText("-");
						
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
			
			getWebServices(serviceManager, composite);
			
			Composite updateComposite = new Composite(buildDialog, SWT.NONE);
			GridLayout gridLayout = new GridLayout(2, false);
			updateComposite.setLayout(gridLayout);
			updateComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			Button updateButton = new Button(updateComposite, SWT.PUSH);
			updateButton.setText(Messages.UPDATE);
			
			Button cancelButton = new Button(updateComposite, SWT.PUSH);
			cancelButton.setText(Messages.CANCEL);
			
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

		Text nameText = new Text(composite, SWT.BORDER);
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nameText.setText(projectInfo.getName());

		Label codeLable = new Label(composite, SWT.NONE);
		codeLable.setText(Messages.CODE);

		Text codeText = new Text(composite, SWT.BORDER);
		codeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		codeText.setText(appInfo.getCode());

		Label descLable = new Label(composite, SWT.NONE);
		descLable.setText(Messages.DESCRIPTION);

		Text descText = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData descGridData = new GridData();
		descGridData.widthHint = 100;
		descGridData.heightHint = 40;
		descText.setLayoutData(descGridData);
		if(StringUtils.isNotEmpty(appInfo.getDescription())) { 
			descText.setText(appInfo.getDescription());
		}

		Label appDirLabel = new Label(composite, SWT.NONE);
		appDirLabel.setText(Messages.APP_DIRECTORY);

		Text appDirText = new Text(composite, SWT.BORDER);
		appDirText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		appDirText.setText(appInfo.getAppDirName());

		Label versionLabel = new Label(composite, SWT.NONE);
		versionLabel.setText(Messages.VERSION);

		Text versionText = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		versionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		versionText.setText(projectInfo.getVersion());

		Label techLabel = new Label(composite, SWT.NONE);
		techLabel.setText(Messages.TECHNOLOGY);

		Text techText = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		techText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		techText.setText(appInfo.getTechInfo().getName() + Messages.HYPHEN + appInfo.getTechInfo().getVersion());
	}

	/**
	 * @param serverGroup
	 * @param serviceManager
	 * @param customerId
	 * @param techId
	 * @param platform
	 * @throws PhrescoException
	 */
	private void getServers(final Group serverGroup,
			ServiceManager serviceManager, String customerId, String techId, String platform) throws PhrescoException {
		List<DownloadInfo> servers = serviceManager.getDownloads(customerId, techId, Category.SERVER.name(), platform);
		if(CollectionUtils.isEmpty(servers)) {
			return;
		}
		List<String> serverNames = new ArrayList<String>();
		for (DownloadInfo downloadInfo : servers) {
			serverNames.add(downloadInfo.getName());
			List<ArtifactInfo> versions = downloadInfo.getArtifactGroup().getVersions();
			List<String> versionList = new ArrayList<String>();
			for (ArtifactInfo artifactInfo : versions) {
				versionList.add(artifactInfo.getVersion());
			}
			serverVersionMap.put(downloadInfo.getName(), versionList);
		}
		if(CollectionUtils.isNotEmpty(serverNames)) {
			Label serverLabel = new Label(serverGroup, SWT.NONE);
			serverLabel.setText(Messages.SERVER);
			
			String[] serverNamesArray = serverNames.toArray(new String[serverNames.size()]);
			serverNameCombo = new Combo(serverGroup, SWT.BORDER | SWT.READ_ONLY);
			serverNameCombo.setItems(serverNamesArray);
			serverNameCombo.select(0);
		}
		
		List<String> serverVersions = serverVersionMap.get(serverNames.get(0));
		if(CollectionUtils.isNotEmpty(serverVersions)) {
			Label versionLanel = new Label(serverGroup, SWT.NONE);
			versionLanel.setText(Messages.VERSIONS);
			String[] serverVersionsArray = serverVersions.toArray(new String[serverVersions.size()]);
			serverVersionListBox = new org.eclipse.swt.widgets.List(serverGroup, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL);
			serverVersionListBox.setItems(serverVersionsArray);
			serverVersionListBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			serverVersionListBox.select(0);
		}
		
		serverNameCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				List<String> versions = serverVersionMap.get(serverNameCombo.getText());
				String[] versionsArray = versions.toArray(new String[versions.size()]);
				serverVersionListBox.removeAll();
				serverVersionListBox.setItems(versionsArray);
				serverVersionListBox.select(0);
				serverGroup.redraw();
				super.widgetSelected(e);
			}
		});
	}
	
	/**
	 * @param dbGroup
	 * @param serviceManager
	 * @param customerId
	 * @param techId
	 * @param platform
	 * @throws PhrescoException
	 */
	private void getDataBases(final Group dbGroup,
			ServiceManager serviceManager, String customerId, String techId, String platform) throws PhrescoException {
		List<DownloadInfo> dataBases = serviceManager.getDownloads(customerId, techId, Category.DATABASE.name(), platform);
		if(CollectionUtils.isEmpty(dataBases)) {
			return;
		}
		List<String> dbNames = new ArrayList<String>();
		for (DownloadInfo downloadInfo : dataBases) {
			dbNames.add(downloadInfo.getName());
			List<ArtifactInfo> versions = downloadInfo.getArtifactGroup().getVersions();
			List<String> versionList = new ArrayList<String>();
			for (ArtifactInfo artifactInfo : versions) {
				versionList.add(artifactInfo.getVersion());
			}
			dbVersionMap.put(downloadInfo.getName(), versionList);
		}
		if(CollectionUtils.isNotEmpty(dbNames)) {
			Label serverLabel = new Label(dbGroup, SWT.NONE);
			serverLabel.setText(Messages.DATABASE);
			
			String[] serverNamesArray = dbNames.toArray(new String[dbNames.size()]);
			dbNameCombo = new Combo(dbGroup, SWT.BORDER | SWT.READ_ONLY);
			dbNameCombo.setItems(serverNamesArray);
			dbNameCombo.select(0);
		}
		
		List<String> dbVersions = dbVersionMap.get(dbNames.get(0));
		if(CollectionUtils.isNotEmpty(dbVersions)) {
			Label versionLanel = new Label(dbGroup, SWT.NONE);
			versionLanel.setText(Messages.VERSIONS);
			String[] serverVersionsArray = dbVersions.toArray(new String[dbVersions.size()]);
			dbVersionListBox = new org.eclipse.swt.widgets.List(dbGroup, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL);
			dbVersionListBox.setItems(serverVersionsArray);
			GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.heightHint = 40;
			dbVersionListBox.setLayoutData(gridData);
			dbVersionListBox.select(0);
		}
		
		dbNameCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				List<String> versions = dbVersionMap.get(dbNameCombo.getText());
				String[] versionsArray = versions.toArray(new String[versions.size()]);
				dbVersionListBox.removeAll();
				dbVersionListBox.setItems(versionsArray);
				dbVersionListBox.select(0);
				dbGroup.redraw();
				super.widgetSelected(e);
			}
		});
	}
	
	private void getWebServices(ServiceManager serviceManager, Composite composite) {
		Group webserviceGroup = new Group(composite, SWT.NONE);
		webserviceGroup.setText(Messages.WEBSERVICES);
		GridLayout webServiceLayout = new GridLayout(5, false);
		webserviceGroup.setLayout(webServiceLayout);
		webserviceGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		try {
			List<WebService> webServices = serviceManager.getWebServices();
			for (WebService webService : webServices) {
				Button webServiceButton = new Button(webserviceGroup, SWT.CHECK);
				webServiceButton.setText(webService.getName());
				webServiceButton.setData(webService.getName(), webService.getId());
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
}
