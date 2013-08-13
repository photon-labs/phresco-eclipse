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

package com.photon.phresco.ui.phrescoexplorer.wizard;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.photon.phresco.commons.FrameworkConstants;
import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.PhrescoDialog;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.model.ArtifactGroup;
import com.photon.phresco.commons.model.ArtifactInfo;
import com.photon.phresco.commons.model.CoreOption;
import com.photon.phresco.commons.model.RequiredOption;
import com.photon.phresco.commons.model.SelectedFeature;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.plugins.model.Mojos.ApplicationHandler;
import com.photon.phresco.plugins.util.MojoProcessor;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.util.Constants;
import com.photon.phresco.util.Utility;

/**
 * Abstract class to handle feature page
 * @author syed
 *
 */

public abstract class AbstractFeatureWizardPage extends WizardPage implements PhrescoConstants {

	private Map<ArtifactGroup, String> selectedArtifactGroupWithComboVersion = new HashMap<ArtifactGroup, String>();
	private Map<ArtifactGroup, String> selectedArtifactGroup = new HashMap<ArtifactGroup, String>();
	
	protected AbstractFeatureWizardPage(String pageName, String title,
			ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		// TODO Auto-generated constructor stub
	}
	
	public void renderFeatureTable(final Composite composite, String featureName, List<ArtifactGroup> features) {
		
		Group jsLibGroups = new Group(composite, SWT.SHADOW_ETCHED_IN);
		jsLibGroups.setText(featureName);

		renderTable(jsLibGroups, features);
		
		jsLibGroups.setBounds(0, 5, 250, 240);
	    jsLibGroups.pack();
		
	}
	
	private Table renderTable(Group jsLibGroups, List<ArtifactGroup> features) {
		Table table = new Table(jsLibGroups, SWT.BORDER | SWT.MULTI);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData());
		
		table.setBounds(12, 20, 450, 175);
		
        TableColumn checkBoxColumn = new TableColumn(table, SWT.LEFT, 0);
        checkBoxColumn.setText("");
        checkBoxColumn.setWidth(25);
        
	    TableColumn nameColumn = new TableColumn(table, SWT.LEFT, 1);
        nameColumn.setText("Name");
        nameColumn.setWidth(140);
        
        TableColumn descColumn = new TableColumn(table, SWT.LEFT, 2);
        descColumn.setText("Description");
        descColumn.setWidth(160);
        
        TableColumn versionColumn = new TableColumn(table, SWT.LEFT, 3);
        versionColumn.setText("Version");
        versionColumn.setWidth(120);
        
        for (int i = 0; i < features.size(); i++) {
            new TableItem(table, SWT.NONE);
        }
        
	    TableItem[] items = table.getItems();
	    
	    int i = 0;
	    List<SelectedFeature> selectedFeatures = null;
		try {
			selectedFeatures = getSelectedFeatures();
		} catch (PhrescoException e1) {
			PhrescoDialog.exceptionDialog(getShell(), e1);
		}
	    
	    for (final ArtifactGroup artifactGroup : features) {
	    	final TableItem tableItem = items[i];
	    	TableEditor editor = new TableEditor(table);
			final Button checkButton = new Button(table, SWT.CHECK);
			checkButton.pack();
			editor.minimumWidth = checkButton.getSize().x;
			editor.horizontalAlignment = SWT.LEFT;
			editor.setEditor(checkButton, tableItem, 0);
			
			if(CollectionUtils.isNotEmpty(selectedFeatures)) {
				for (SelectedFeature selectedFeature : selectedFeatures) {
					String artifactGroupId = selectedFeature.getArtifactGroupId();
					if(artifactGroupId.equals(artifactGroup.getId())) {
						checkButton.setSelection(true);
						selectedFeatures.remove(selectedFeature);
						break;
					}
				}
			}
			  
			editor = new TableEditor(table);
			Text text = new Text(table, SWT.NONE | SWT.BORDER);
			text.setEditable(false);
			text.setText(artifactGroup.getDisplayName());
			text.setToolTipText(artifactGroup.getDisplayName());
			editor.grabHorizontal = true;
			editor.setEditor(text, tableItem, 1);
			  
			editor = new TableEditor(table);
			text = new Text(table, SWT.NONE | SWT.BORDER);
			text.setEditable(false);
			if (artifactGroup.getDescription() != null) {
				text.setText(artifactGroup.getDescription());
				text.setToolTipText(artifactGroup.getDescription());
			} else {
				text.setText("");
			}
			
			editor.grabHorizontal = true;
			editor.setEditor(text, tableItem, 2);

			editor = new TableEditor(table);
	        List<ArtifactInfo> versions = artifactGroup.getVersions();
	        
	        final CCombo combo = new CCombo(table, SWT.NONE | SWT.BORDER);
	        combo.setEditable(false);
	        final Text versionText = new Text(table, SWT.NONE | SWT.BORDER);
	        versionText.setEditable(false);
	        if (versions.size() > 1) {
		        for (ArtifactInfo artifactInfo : versions) {
		        	combo.add(artifactInfo.getVersion()); 
				}
		        combo.select(0);
				editor.grabHorizontal = true;
				editor.setEditor(combo, tableItem, 3);
	        } else {
	        	versionText.setText(versions.get(0).getVersion());
	        	versionText.setToolTipText(versions.get(0).getVersion());
				editor.grabHorizontal = true;
				editor.setEditor(versionText, tableItem, 3);
	        }
	        
			combo.addSelectionListener(new SelectionAdapter() {
			    @Override
			    public void widgetSelected(SelectionEvent e) {
			    	String selectedComboVersion = combo.getItem(combo.getSelectionIndex());
			        if (checkButton.getSelection()) {
			        	selectedArtifactGroupWithComboVersion.put(artifactGroup, selectedComboVersion);
			        } else if (selectedArtifactGroupWithComboVersion.containsKey(artifactGroup)) {
			        	selectedArtifactGroupWithComboVersion.remove(artifactGroup);
			        }
			    }
			});
			
			String selectedVersion = versionText.getText();
	    	
	    	if (selectedVersion.equals("")) {
	    		selectedVersion = KEY_EMPTY;
	    	}
	    	
			if (checkButton.getSelection()) {
	            selectedArtifactGroup.put(artifactGroup, selectedVersion);
	        } else if (selectedArtifactGroup.containsKey(artifactGroup)) {
    			selectedArtifactGroup.remove(artifactGroup);
	        }
	        
			checkButton.addSelectionListener(new SelectionAdapter() {
			    @Override
			    public void widgetSelected(SelectionEvent e) {
			    	String selectedVersion = versionText.getText();
			    	
			    	if (selectedVersion.equals("")) {
			    		selectedVersion = KEY_EMPTY;
			    	}
			    	
			        if (checkButton.getSelection()) {
			            selectedArtifactGroup.put(artifactGroup, selectedVersion);
			        } else if (selectedArtifactGroup.containsKey(artifactGroup)) {
            			selectedArtifactGroup.remove(artifactGroup);
			        }
			    }
			});
			  
	    	i++;
	    }
	    
	    return table;
	}
	
	public abstract void renderPage();
	
	public Map<ArtifactGroup, String> getSelectedCheckBoxRows() {
		return selectedArtifactGroup;
	}
	
	public Map<ArtifactGroup, String> getSelectedComboBoxRows() {
		return selectedArtifactGroupWithComboVersion;
	}
	
	public List<SelectedFeature> getSelectedItems() {
		Map<ArtifactGroup, String> selectedComboBoxRows = getSelectedComboBoxRows();
		Map<ArtifactGroup, String> selectedCheckBoxRows = getSelectedCheckBoxRows();
		
		List<SelectedFeature> selectedFeatures = new ArrayList<SelectedFeature>();
		Iterator entries = selectedCheckBoxRows.entrySet().iterator();
		while (entries.hasNext()) {
			SelectedFeature selectedFeature = new SelectedFeature();
		    Map.Entry entry = (Map.Entry) entries.next();
		    ArtifactGroup key = (ArtifactGroup)entry.getKey();
		    
		    String value = (String)entry.getValue();
		    selectedFeature.setModuleId(key.getId());
		    selectedFeature.setType(key.getType().name());
		    List<ArtifactInfo> versions = key.getVersions();
		    for (ArtifactInfo artifactInfo : versions) {
				if(artifactInfo.getVersion().equals(value)) {
					selectedFeature.setVersionID(artifactInfo.getId());
				}
			}
		    
		    if (value != null && KEY_EMPTY.equals(value)) {
		    	value = selectedComboBoxRows.get(key);
		    }
		    selectedFeatures.add(selectedFeature);
		}
		return selectedFeatures;
	}
	
	private List<SelectedFeature> getSelectedFeatures() throws PhrescoException {
		
		List<SelectedFeature> listFeatures = new ArrayList<SelectedFeature>();
		try {
			ServiceManager serviceManager = PhrescoUtil.getServiceManager(PhrescoUtil.getUserId());
			ApplicationInfo appInfo = PhrescoUtil.getApplicationInfo();
			String selectedTechId = appInfo.getTechInfo().getId();
			List<String> selectedModules = appInfo.getSelectedModules();
			if (CollectionUtils.isNotEmpty(selectedModules)) {
				for (String selectedModule : selectedModules) {
					SelectedFeature selectFeature = createArtifactInformation(selectedModule, selectedTechId, appInfo,
							serviceManager);
					listFeatures.add(selectFeature);
				}
			}

			List<String> selectedJSLibs = appInfo.getSelectedJSLibs();
			if (CollectionUtils.isNotEmpty(selectedJSLibs)) {
				for (String selectedJSLib : selectedJSLibs) {
					SelectedFeature selectFeature = createArtifactInformation(selectedJSLib, selectedTechId, appInfo,
							serviceManager);
					listFeatures.add(selectFeature);
				}
			}

			List<String> selectedComponents = appInfo.getSelectedComponents();
			if (CollectionUtils.isNotEmpty(selectedComponents)) {
				for (String selectedComponent : selectedComponents) {
					SelectedFeature selectFeature = createArtifactInformation(selectedComponent, selectedTechId,
							appInfo, serviceManager);
					listFeatures.add(selectFeature);
				}
			}
		} catch (PhrescoException e) {
			throw new PhrescoException(e);
		}
		
		return listFeatures;
	}
	
	/**
	 * Creates the artifact information.
	 *
	 * @param selectedModule the selected module
	 * @param techId the tech id
	 * @param appInfo the app info
	 * @param serviceManager the service manager
	 * @return the selected feature
	 * @throws PhrescoException the phresco exception
	 */
	private SelectedFeature createArtifactInformation(String selectedModule, String techId, ApplicationInfo appInfo,
			ServiceManager serviceManager) throws PhrescoException {
		SelectedFeature slctFeature = new SelectedFeature();
		ArtifactInfo artifactInfo = serviceManager.getArtifactInfo(selectedModule);

		slctFeature.setDispValue(artifactInfo.getVersion());
		slctFeature.setVersionID(artifactInfo.getId());
		slctFeature.setModuleId(artifactInfo.getArtifactGroupId());

		String artifactGroupId = artifactInfo.getArtifactGroupId();
		ArtifactGroup artifactGroupInfo = serviceManager.getArtifactGroupInfo(artifactGroupId);
		slctFeature.setName(artifactGroupInfo.getName());
		slctFeature.setDispName(artifactGroupInfo.getDisplayName());
		slctFeature.setType(artifactGroupInfo.getType().name());
		slctFeature.setArtifactGroupId(artifactGroupInfo.getId());
		slctFeature.setPackaging(artifactGroupInfo.getPackaging());
		getScope(appInfo, artifactInfo.getId(), slctFeature);
		List<CoreOption> appliesTo = artifactGroupInfo.getAppliesTo();
		for (CoreOption coreOption : appliesTo) {
			if (coreOption.getTechId().equals(techId) && !coreOption.isCore()
					&& !slctFeature.getType().equals(FrameworkConstants.REQ_JAVASCRIPT_TYPE_MODULE)
					&& artifactGroupInfo.getPackaging().equalsIgnoreCase(ZIP_FILE)) {
				slctFeature.setCanConfigure(true);
			} else {
				slctFeature.setCanConfigure(false);
			}
		}
		List<RequiredOption> appliesToReqird = artifactInfo.getAppliesTo();
		if (CollectionUtils.isNotEmpty(appliesToReqird)) {
			for (RequiredOption requiredOption : appliesToReqird) {
				if (requiredOption.isRequired() && requiredOption.getTechId().equals(techId)) {
					slctFeature.setDefaultModule(true);
				}
			}
		}

		return slctFeature;
	}
	
	/**
	 * Gets the scope.
	 *
	 * @param appInfo the app info
	 * @param id the id
	 * @param selectFeature the select feature
	 * @return the scope
	 * @throws PhrescoException the phresco exception
	 */
	private void getScope(ApplicationInfo appInfo, String id, SelectedFeature selectFeature) throws PhrescoException {
		StringBuilder dotPhrescoPathSb = new StringBuilder(Utility.getProjectHome());
		dotPhrescoPathSb.append(appInfo.getAppDirName());
		dotPhrescoPathSb.append(File.separator);
		dotPhrescoPathSb.append(Constants.DOT_PHRESCO_FOLDER);
		dotPhrescoPathSb.append(File.separator);
		String pluginInfoFile = dotPhrescoPathSb.toString() + APPLICATION_HANDLER_INFO_FILE;
		MojoProcessor mojoProcessor = new MojoProcessor(new File(pluginInfoFile));
		ApplicationHandler applicationHandler = mojoProcessor.getApplicationHandler();
		String selectedFeatures = applicationHandler.getSelectedFeatures();
		if (StringUtils.isNotEmpty(selectedFeatures)) {
			Gson gson = new Gson();
			Type jsonType = new TypeToken<Collection<ArtifactGroup>>() {
			}.getType();
			List<ArtifactGroup> artifactGroups = gson.fromJson(selectedFeatures, jsonType);
			for (ArtifactGroup artifactGroup : artifactGroups) {
				for (ArtifactInfo artifactInfo : artifactGroup.getVersions()) {
					if (artifactInfo.getId().equals(id)) {
						selectFeature.setScope(artifactInfo.getScope());
					}
				}
			}
		}
	}
}
