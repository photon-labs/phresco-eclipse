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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.handlers.HandlerUtil;

import com.photon.phresco.commons.PhrescoDialog;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.model.ArtifactInfo;
import com.photon.phresco.commons.model.DownloadInfo;
import com.photon.phresco.commons.model.ProjectInfo;
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
	public Combo serverVersionCombo;
	
	public static Map<String, List<String>> serverVersionMap = new HashMap<String, List<String>>();
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);
		final Shell buildDialog = new Shell(shell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		GridLayout layout = new GridLayout(1, false);
		buildDialog.setLocation(385, 130);
		buildDialog.setSize(526, 350);
		buildDialog.setLayout(layout);
		buildDialog.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		try {
			BaseAction baseAction = new BaseAction();
			String userId = baseAction.getUserId();
			ProjectInfo projectInfo = PhrescoUtil.getProjectInfo();
			ApplicationInfo appInfo = PhrescoUtil.getApplicationInfo();
			ServiceManager serviceManager = PhrescoUtil.getServiceManager(userId);
			if(serviceManager == null) {
				PhrescoDialog.errorDialog(buildDialog, Messages.WARNING, Messages.PHRESCO_LOGIN_WARNING);
				return null;
			}
			String customerId = PhrescoUtil.getCustomerId();
			String techId = PhrescoUtil.getTechId();
			serviceManager = PhrescoUtil.getServiceManager(userId);
			createAppInfoPage(buildDialog, projectInfo, appInfo);
			
			Group serverGroup = new Group(buildDialog, SWT.NONE);
			serverGroup.setText(Messages.SERVERS);
			GridLayout serverLlayout = new GridLayout(5, false);
			serverGroup.setLayout(serverLlayout);
			serverGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			getServers(serverGroup, serviceManager, customerId, techId);
			
		} catch (PhrescoException e) {
			PhrescoDialog.errorDialog(buildDialog, Messages.ERROR, e.getLocalizedMessage());
		}
		buildDialog.open();
		return null;
	}

	/**
	 * @param buildDialog
	 * @param serviceManager
	 * @param customerId
	 * @param techId
	 * @throws PhrescoException
	 */
	private void getServers(final Group serverGroup,
			ServiceManager serviceManager, String customerId, String techId) throws PhrescoException {
		List<DownloadInfo> servers = serviceManager.getServers(customerId, techId);
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
			serverVersionCombo = new Combo(serverGroup, SWT.BORDER | SWT.READ_ONLY | SWT.RESIZE);
			serverVersionCombo.setItems(serverVersionsArray);
			serverVersionCombo.select(0);
		}
		
		serverNameCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				List<String> versions = serverVersionMap.get(serverNameCombo.getText());
				String[] versionsArray = versions.toArray(new String[versions.size()]);
				serverVersionCombo.removeAll();
				serverVersionCombo.setItems(versionsArray);
				serverVersionCombo.select(0);
				serverGroup.redraw();
				super.widgetSelected(e);
			}
		});
	}

	/**
	 * @param buildDialog
	 */
	private void createAppInfoPage(Shell buildDialog, ProjectInfo projectInfo, ApplicationInfo appInfo) {
		
		if(projectInfo == null || appInfo == null) {
			return;
		}
		Composite composite = new Composite(buildDialog, SWT.BORDER);
		GridLayout gridlayout = new GridLayout(6, false);
		composite.setLayout(gridlayout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label nameLable = new Label(composite, SWT.NONE);
		nameLable.setText(Messages.NAME);

		Text nameText = new Text(composite, SWT.BORDER);
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nameText.setText(projectInfo.getName());

		Label codeLable = new Label(composite, SWT.NONE);
		codeLable.setText(Messages.NAME);

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
}
