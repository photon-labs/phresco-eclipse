package com.photon.phresco.ui.wizards.componets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.photon.phresco.commons.model.ArtifactInfo;
import com.photon.phresco.commons.model.DownloadInfo;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.ui.resource.Messages;

public class ServerComponent {
	
	public Combo serverNameCombo;
	public org.eclipse.swt.widgets.List serverVersionListBox;

	public static Map<String, List<ArtifactInfo>> serverVersionMap = new HashMap<String, List<ArtifactInfo>>();
	public static Map<String, String> serverIdMap = new HashMap<String, String>();
	
	/**
	 * @param serverGroup
	 * @param serviceManager
	 * @param customerId
	 * @param techId
	 * @param platform
	 * @throws PhrescoException
	 */
	public void getServers(final Group serverGroup,
			List<DownloadInfo> servers, String customerId, String techId, String platform) throws PhrescoException {
		List<String> serverNames = new ArrayList<String>();
		for (DownloadInfo downloadInfo : servers) {
			serverNames.add(downloadInfo.getName());
			serverIdMap.put(downloadInfo.getName(), downloadInfo.getId());
			List<ArtifactInfo> versions = downloadInfo.getArtifactGroup().getVersions();
			serverVersionMap.put(downloadInfo.getName(), versions);
		}
		if(CollectionUtils.isNotEmpty(serverNames)) {
			Label serverLabel = new Label(serverGroup, SWT.NONE);
			serverLabel.setText(Messages.SERVER);
			
			String[] serverNamesArray = serverNames.toArray(new String[serverNames.size()]);
			serverNameCombo = new Combo(serverGroup, SWT.BORDER | SWT.READ_ONLY);
			serverNameCombo.setItems(serverNamesArray);
			serverNameCombo.select(0);
		}
		
		List<String> serverVersions = new ArrayList<String>();
		List<ArtifactInfo> artifactInfos = serverVersionMap.get(serverNames.get(0));
		for (ArtifactInfo artifactInfo : artifactInfos) {
			serverVersions.add(artifactInfo.getVersion());
		}
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
				List<String> serverVersions = new ArrayList<String>();
				List<ArtifactInfo> artifactInfos = serverVersionMap.get(serverNameCombo.getText());
				for (ArtifactInfo artifactInfo : artifactInfos) {
					serverVersions.add(artifactInfo.getVersion());
				}
				String[] versionsArray = serverVersions.toArray(new String[serverVersions.size()]);
				serverVersionListBox.removeAll();
				serverVersionListBox.setItems(versionsArray);
				serverVersionListBox.select(0);
				serverGroup.redraw();
				super.widgetSelected(e);
			}
		});
	}
}
