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
			dbNames.add(downloadInfo.getName());
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
			dbNameCombo.add(Messages.SELECT_DATABASE, 0);
			if (artifactGroupInfo != null) {
				setSelectedDatabase(artifactGroupInfo);
			} else {
				dbNameCombo.select(0);
			}
		}
		
		List<String> dbVersions = new ArrayList<String>();
		List<ArtifactInfo> artifactInfos = dbVersionMap.get(dbNameCombo.getText());
		Label versionLanel = new Label(dbGroup, SWT.NONE);
		versionLanel.setText(Messages.VERSIONS);
		
		dbVersionListBox = new org.eclipse.swt.widgets.List(dbGroup, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.heightHint = 40;
		dbVersionListBox.setLayoutData(gridData);
		
		if (CollectionUtils.isNotEmpty(artifactInfos)) {
			for (ArtifactInfo artifactInfo : artifactInfos) {
				dbVersionIdMap.put(artifactInfo.getId(), artifactInfo.getVersion());
				dbVersions.add(artifactInfo.getVersion());
			}
			String[] serverVersionsArray = dbVersions.toArray(new String[dbVersions.size()]);
			dbVersionListBox.setItems(serverVersionsArray);
			setSelectedDbVersion(artifactGroupInfo);
		} else {
			dbVersionListBox.add(Messages.SELECT_VERSION, 0);
			dbVersionListBox.select(0);
		}
		
		dbNameCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				dbVersionListBox.removeAll();
				if (Messages.SELECT_DATABASE.equals(dbNameCombo.getText())) {
					dbVersionListBox.add(Messages.SELECT_VERSION, 0);
				} else {
					List<String> dbVersions = new ArrayList<String>();
					List<ArtifactInfo> artifactInfos = dbVersionMap.get(dbNameCombo.getText());
					for (ArtifactInfo artifactInfo : artifactInfos) {
						dbVersions.add(artifactInfo.getVersion());
					}
					String[] versionsArray = dbVersions.toArray(new String[dbVersions.size()]);
					dbVersionListBox.setItems(versionsArray);
				}
				dbVersionListBox.select(0);
				dbGroup.redraw();
				super.widgetSelected(e);
			}
		});
	}
	
	private void setSelectedDatabase(ArtifactGroupInfo artifactGroupInfo) {
		String artifactGroupId = artifactGroupInfo.getArtifactGroupId();
		String[] items = dbNameCombo.getItems();
		for (int i = 0; i < items.length; i++) {
			String id = dbIdMap.get(items[i]);
			if (artifactGroupId.equals(id)) {
				dbNameCombo.select(i);
			}
		}
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
