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

public class DatabaseComponent {
	
	public org.eclipse.swt.widgets.List dbVersionListBox;
	public Combo dbNameCombo;
	
	public static Map<String, List<ArtifactInfo>> dbVersionMap = new HashMap<String, List<ArtifactInfo>>();
	public static Map<String, String> dbIdMap = new HashMap<String, String>();
	private static Map<String, String> dbVersionIdMap = new HashMap<String, String>();
	
	/**
	 * @param dbGroup
	 * @param dataBases
	 * @param customerId
	 * @param techId
	 * @param platform
	 * @throws PhrescoException
	 */
	public void getDataBases(final Composite dbGroup,
			List<DownloadInfo> dataBases, String customerId, String techId, String platform, ArtifactGroupInfo artifactGroupInfo) throws PhrescoException {
		List<String> dbNames = new ArrayList<String>();
		for (DownloadInfo downloadInfo : dataBases) {
			if(artifactGroupInfo != null && artifactGroupInfo.getArtifactGroupId().equals(downloadInfo.getId())) {
				dbNames.add(0, downloadInfo.getName());
			} else {
				dbNames.add(downloadInfo.getName());
			}
			dbIdMap.put(downloadInfo.getName(), downloadInfo.getId());
			List<ArtifactInfo> versions = downloadInfo.getArtifactGroup().getVersions();
			dbVersionMap.put(downloadInfo.getName(), versions);
		}
		if(CollectionUtils.isNotEmpty(dbNames)) {
			Label serverLabel = new Label(dbGroup, SWT.NONE);
			serverLabel.setText(Messages.DATABASE);
			
			String[] serverNamesArray = dbNames.toArray(new String[dbNames.size()]);
			dbNameCombo = new Combo(dbGroup, SWT.BORDER | SWT.READ_ONLY);
			dbNameCombo.setItems(serverNamesArray);
			dbNameCombo.select(0);
		}
		List<String> dbVersions = new ArrayList<String>();
		List<ArtifactInfo> artifactInfos = dbVersionMap.get(dbNames.get(0));
		for (ArtifactInfo artifactInfo : artifactInfos) {
			dbVersionIdMap.put(artifactInfo.getId(), artifactInfo.getVersion());
			dbVersions.add(artifactInfo.getVersion());
		}
		if(CollectionUtils.isNotEmpty(dbVersions)) {
			Label versionLanel = new Label(dbGroup, SWT.NONE);
			versionLanel.setText(Messages.VERSIONS);
			String[] serverVersionsArray = dbVersions.toArray(new String[dbVersions.size()]);
			dbVersionListBox = new org.eclipse.swt.widgets.List(dbGroup, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL);
			dbVersionListBox.setItems(serverVersionsArray);
			GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.heightHint = 40;
			dbVersionListBox.setLayoutData(gridData);
			if(artifactGroupInfo != null) {
				setSelectedDbVersion(artifactGroupInfo);
			} else {
				dbVersionListBox.select(0);
			}
		}
		
		dbNameCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final List<String> dbVersions = new ArrayList<String>();
				List<ArtifactInfo> artifactInfos = dbVersionMap.get(dbNameCombo.getText());
				for (ArtifactInfo artifactInfo : artifactInfos) {
					dbVersions.add(artifactInfo.getVersion());
				}
				String[] versionsArray = dbVersions.toArray(new String[dbVersions.size()]);
				dbVersionListBox.removeAll();
				dbVersionListBox.setItems(versionsArray);
				dbVersionListBox.select(0);
				dbGroup.redraw();
				super.widgetSelected(e);
			}
		});
	}
	
	private void setSelectedDbVersion(ArtifactGroupInfo artifactGroupInfo) {
		List<Integer> list = new ArrayList<Integer>();
		List<String> artifactInfoIds = artifactGroupInfo.getArtifactInfoIds();
		String[] items = dbVersionListBox.getItems();
		for (String artifactInfoId : artifactInfoIds) {
			String version = dbVersionIdMap.get(artifactInfoId);
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
			dbVersionListBox.select(indices);
		}
	}

}
