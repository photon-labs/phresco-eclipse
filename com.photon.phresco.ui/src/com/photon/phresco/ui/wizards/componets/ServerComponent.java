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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.photon.phresco.commons.model.ArtifactGroupInfo;
import com.photon.phresco.commons.model.ArtifactInfo;
import com.photon.phresco.commons.model.DownloadInfo;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.ui.resource.Messages;

public class ServerComponent {
	
	public Combo serverNameCombo;
	public org.eclipse.swt.widgets.List serverVersionListBox;

	public static Map<String, List<ArtifactInfo>> serverVersionMap = new HashMap<String, List<ArtifactInfo>>();
	public static Map<String, String> serverIdMap = new HashMap<String, String>();
	private static Map<String, String> serverVersionIdMap = new HashMap<String, String>();
	
	/**
	 * @param serverGroup
	 * @param serviceManager
	 * @param customerId
	 * @param techId
	 * @param platform
	 * @throws PhrescoException
	 */
	public void getServers(final Composite serverGroup,
			List<DownloadInfo> servers, String customerId, String techId, String platform, ArtifactGroupInfo artifactGroupInfo) throws PhrescoException {
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
			serverNameCombo.add(Messages.SELECT_SERVER, 0);
			if (artifactGroupInfo != null) {
				setSelectedServer(artifactGroupInfo);
			} else {
				serverNameCombo.select(0);
			}
		}
		
		List<String> serverVersions = new ArrayList<String>();
		List<ArtifactInfo> artifactInfos = serverVersionMap.get(serverNameCombo.getText());
		Label versionLanel = new Label(serverGroup, SWT.NONE);
		versionLanel.setText(Messages.VERSIONS);
		
		serverVersionListBox = new org.eclipse.swt.widgets.List(serverGroup, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.heightHint = 40;
		serverVersionListBox.setLayoutData(gridData);
		
		if (CollectionUtils.isNotEmpty(artifactInfos)) {
			for (ArtifactInfo artifactInfo : artifactInfos) {
				serverVersionIdMap.put(artifactInfo.getId(), artifactInfo.getVersion());
				serverVersions.add(artifactInfo.getVersion());
			}
			String[] serverVersionsArray = serverVersions.toArray(new String[serverVersions.size()]);
			serverVersionListBox.setItems(serverVersionsArray);
			setSelectedSeverVersion(artifactGroupInfo);
		} else {
			serverVersionListBox.add(Messages.SELECT_VERSION, 0);
			serverVersionListBox.select(0);
		}
		
		serverNameCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				serverVersionListBox.removeAll();
				if (Messages.SELECT_SERVER.equals(serverNameCombo.getText())) {
					serverVersionListBox.add(Messages.SELECT_VERSION, 0);
				} else {
					List<String> serverVersions = new ArrayList<String>();
					List<ArtifactInfo> artifactInfos = serverVersionMap.get(serverNameCombo.getText());
					for (ArtifactInfo artifactInfo : artifactInfos) {
						serverVersions.add(artifactInfo.getVersion());
					}
					String[] versionsArray = serverVersions.toArray(new String[serverVersions.size()]);
					serverVersionListBox.setItems(versionsArray);
				}
				serverVersionListBox.select(0);
				serverGroup.redraw();
				super.widgetSelected(e);
			}
		});
	}
	
	private void setSelectedServer(ArtifactGroupInfo artifactGroupInfo) {
		String artifactGroupId = artifactGroupInfo.getArtifactGroupId();
		String[] items = serverNameCombo.getItems();
		for (int i = 0; i < items.length; i++) {
			String id = serverIdMap.get(items[i]);
			if (artifactGroupId.equals(id)) {
				serverNameCombo.select(i);
			}
		}
	}

	private void setSelectedSeverVersion(ArtifactGroupInfo artifactGroupInfo) {
		List<Integer> list = new ArrayList<Integer>();
		List<String> artifactInfoIds = artifactGroupInfo.getArtifactInfoIds();
		String[] items = serverVersionListBox.getItems();
		for (String artifactInfoId : artifactInfoIds) {
			String version = serverVersionIdMap.get(artifactInfoId);
			for (int i = 0; i < items.length; i++) {
				if(items[i].equals(version)) {
					list.add(i);
				}
			}
		} 
		if (!list.isEmpty()) {
			int[] indices = new int[list.size()];
			for (int i = 0; i < list.size(); i++) {
				indices[i] = list.get(i);
			}
			serverVersionListBox.select(indices);
		}
	}
}
