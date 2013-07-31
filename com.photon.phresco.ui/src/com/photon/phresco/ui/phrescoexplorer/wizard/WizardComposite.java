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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.photon.phresco.commons.PhrescoDialog;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.model.ArtifactGroup;
import com.photon.phresco.commons.model.ArtifactInfo;
import com.photon.phresco.commons.model.CoreOption;
import com.photon.phresco.commons.model.ProjectInfo;
import com.photon.phresco.commons.model.SelectedFeature;
import com.photon.phresco.commons.util.ApplicationManagerUtil;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.framework.impl.ApplicationManagerImpl;
import com.photon.phresco.plugins.model.Mojos.ApplicationHandler;
import com.photon.phresco.plugins.util.MojoProcessor;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.ui.resource.Messages;
import com.photon.phresco.util.Constants;

/**
 * Class to create the wizard page to render featues
 * @author syed_ah
 *
 */
public class WizardComposite extends Composite {
	Composite parent;
	public WizardComposite(Composite parent) {
		super(parent, SWT.NONE);
	}

	public WizardDialog getWizardControl() {
		parent = this;
		FillLayout layout = new FillLayout();
		parent.setLayout(layout);
    
		WizardDialog dialog = new WizardDialog(parent.getShell(), new FeatureWizard());
        return dialog;
	}
}

class FeatureWizard extends Wizard {

	public FeatureWizard() {
		super();
	}

	public void addPages() {
		
		try {
			boolean isFirstPage = true;
			ServiceManager serviceManager = PhrescoUtil.getServiceManager();
			String custId = PhrescoUtil.getCustomerId();
			String techId = PhrescoUtil.getTechId();
			List<ArtifactGroup> jsLibs = serviceManager.getFeatures(custId, techId, "JAVASCRIPT");
			List<ArtifactGroup> modules = serviceManager.getFeatures(custId, techId, "FEATURE");
			List<ArtifactGroup> components = serviceManager.getFeatures(custId, techId, "COMPONENT");
			
			if (jsLibs != null && jsLibs.size() != 0) {
				addPage(new JSLibraryFeaturePage(jsLibs, isFirstPage));
				isFirstPage = false;
			}
			
			if (modules != null && modules.size() != 0) {
				ModuleFeaturePage moduleFeaturePage = new ModuleFeaturePage(modules, isFirstPage);
				addPage(moduleFeaturePage);
				isFirstPage = false;
			}
			
			if (components != null && components.size() != 0) {
				ComponentFeaturePage componentFeaturePage = new ComponentFeaturePage(components, isFirstPage);
				addPage(componentFeaturePage);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean performFinish() {
		
		IWizardPage[] pages = getPages();
		List<SelectedFeature> selectedFeatures = null;
		for (int i = 0; i < pages.length; i++) {
			IWizardPage wizardPage = pages[i];
			
			if (wizardPage instanceof JSLibraryFeaturePage) {
				JSLibraryFeaturePage jsLibPage = (JSLibraryFeaturePage) wizardPage;
				selectedFeatures = jsLibPage.getSelectedItems();
				if(CollectionUtils.isNotEmpty(selectedFeatures)) {
					updateFeatures(selectedFeatures);
				}
			} else if (wizardPage instanceof ModuleFeaturePage) {
				ModuleFeaturePage modulePage = (ModuleFeaturePage) wizardPage;
				selectedFeatures = modulePage.getSelectedItems();
				if(CollectionUtils.isNotEmpty(selectedFeatures)) {
					updateFeatures(selectedFeatures);
				}
			} else if (wizardPage instanceof ComponentFeaturePage) {
				ComponentFeaturePage componentPage = (ComponentFeaturePage) wizardPage;
				selectedFeatures = componentPage.getSelectedItems();
				if(CollectionUtils.isNotEmpty(selectedFeatures)) {
					updateFeatures(selectedFeatures);
				}
			}
		}
		
		return true;
	}

	public boolean performCancel() {
		return true;
	}

	public IWizardPage getNextPage(IWizardPage page) {
		
		if (page instanceof JSLibraryFeaturePage) {
			JSLibraryFeaturePage libraryPage = (JSLibraryFeaturePage) page;
			libraryPage.renderPage();
		}
		
		if (page instanceof ComponentFeaturePage) {
			ComponentFeaturePage componentPage = (ComponentFeaturePage) page;
			componentPage.renderPage();
		}
		
		if (page instanceof ModuleFeaturePage) {
			ModuleFeaturePage modulePage = (ModuleFeaturePage) page;
			modulePage.renderPage();
		}
			
		return super.getNextPage(page);
	}
	
	private void updateFeatures(List<SelectedFeature> selectedFeaturesFromUI) {
		File filePath = null;
		BufferedReader bufferedReader = null;
		Gson gson = new Gson();
		List<String> selectedFeatures = new ArrayList<String>();
		List<String> selectedJsLibs = new ArrayList<String>();
		List<String> selectedComponents = new ArrayList<String>();
		List<ArtifactGroup> listArtifactGroup = new ArrayList<ArtifactGroup>();
		String userId = PhrescoUtil.getUserId();
		try {
			ServiceManager serviceManager = PhrescoUtil.getServiceManager(userId);
			if (serviceManager == null) {
				PhrescoDialog.errorDialog(getShell(), Messages.ERROR, Messages.PHRESCO_LOGIN_WARNING);
			}
			StringBuilder sbs = null;
			sbs = new StringBuilder(PhrescoUtil.getApplicationHome()).append(File.separator).append(
				Constants.DOT_PHRESCO_FOLDER).append(File.separator).append(Constants.PROJECT_INFO_FILE);
			bufferedReader = new BufferedReader(new FileReader(sbs.toString()));
			Type type = new TypeToken<ProjectInfo>() {
			}.getType();
			ProjectInfo projectinfo = gson.fromJson(bufferedReader, type);
			ApplicationInfo applicationInfo = projectinfo.getAppInfos().get(0);
			if (CollectionUtils.isNotEmpty(selectedFeaturesFromUI)) {
				for (SelectedFeature selectedFeatureFromUI : selectedFeaturesFromUI) {
					String artifactGroupId = selectedFeatureFromUI.getModuleId();
					ArtifactGroup artifactGroup = serviceManager.getArtifactGroupInfo(artifactGroupId);
					ArtifactInfo artifactInfo = serviceManager.getArtifactInfo(selectedFeatureFromUI.getVersionID());
					artifactInfo.setScope(selectedFeatureFromUI.getScope());
					if (artifactInfo != null) {
						artifactGroup.setVersions(Collections.singletonList(artifactInfo));
					}
					List<CoreOption> appliesTo = artifactGroup.getAppliesTo();
					if (CollectionUtils.isNotEmpty(appliesTo)) {
						for (CoreOption coreOption : appliesTo) {
							if (coreOption.getTechId().equals(applicationInfo.getTechInfo().getId())) {
								artifactGroup.setAppliesTo(Collections.singletonList(coreOption));
								listArtifactGroup.add(artifactGroup);
								break;
							}
						}
					}
					if (selectedFeatureFromUI.getType().equals(ArtifactGroup.Type.FEATURE.name())) {
						selectedFeatures.add(selectedFeatureFromUI.getVersionID());
					}
					if (selectedFeatureFromUI.getType().equals(ArtifactGroup.Type.JAVASCRIPT.name())) {
						selectedJsLibs.add(selectedFeatureFromUI.getVersionID());
					}
					if (selectedFeatureFromUI.getType().equals(ArtifactGroup.Type.COMPONENT.name())) {
						selectedComponents.add(selectedFeatureFromUI.getVersionID());
					}
				}
			}
			StringBuilder sb = new StringBuilder(PhrescoUtil.getApplicationHome())
					.append(File.separator).append(Constants.DOT_PHRESCO_FOLDER).append(File.separator).append(
							Constants.APPLICATION_HANDLER_INFO_FILE);
			filePath = new File(sb.toString());
			MojoProcessor mojo = new MojoProcessor(filePath);
			ApplicationHandler applicationHandler = mojo.getApplicationHandler();
			// To write selected Features into
			// phresco-application-Handler-info.xml
			String artifactGroup = gson.toJson(listArtifactGroup);
			applicationHandler.setSelectedFeatures(artifactGroup);

			// To write Deleted Features into
			// phresco-application-Handler-info.xml
			List<ArtifactGroup> removedModules = getRemovedModules(applicationInfo, selectedFeaturesFromUI,	serviceManager);
			Type jsonType = new TypeToken<Collection<ArtifactGroup>>() {}.getType();
			String deletedFeatures = gson.toJson(removedModules, jsonType);
			applicationHandler.setDeletedFeatures(deletedFeatures);

			mojo.save();

			applicationInfo.setSelectedModules(selectedFeatures);
			applicationInfo.setSelectedJSLibs(selectedJsLibs);
			applicationInfo.setSelectedComponents(selectedComponents);

			projectinfo.setAppInfos(Collections.singletonList(applicationInfo));
			ApplicationManagerUtil util = new ApplicationManagerUtil();
			util.updateApplication(applicationInfo.getAppDirName(), applicationInfo);
			
		} catch (FileNotFoundException e) {
			PhrescoDialog.exceptionDialog(getShell(), e);
		} catch (PhrescoException e) {
			PhrescoDialog.exceptionDialog(getShell(), e);
		}
	}
	
	private List<ArtifactGroup> getRemovedModules(ApplicationInfo appInfo, List<SelectedFeature> jsonData,
			ServiceManager serviceManager) throws PhrescoException {
		List<String> selectedFeaturesId = appInfo.getSelectedModules();
		List<String> selectedJSLibsId = appInfo.getSelectedJSLibs();
		List<String> selectedComponentsId = appInfo.getSelectedComponents();
		List<String> newlySelectedModuleGrpIds = new ArrayList<String>();
		if (CollectionUtils.isNotEmpty(jsonData)) {
			for (SelectedFeature obj : jsonData) {
				newlySelectedModuleGrpIds.add(obj.getModuleId());
			}
		}
		List<ArtifactGroup> artifactGroups = new ArrayList<ArtifactGroup>();
		if (CollectionUtils.isNotEmpty(selectedFeaturesId)) {
			addArtifactGroups(selectedFeaturesId, newlySelectedModuleGrpIds, artifactGroups, serviceManager);
		}
		if (CollectionUtils.isNotEmpty(selectedJSLibsId)) {
			addArtifactGroups(selectedJSLibsId, newlySelectedModuleGrpIds, artifactGroups, serviceManager);
		}
		if (CollectionUtils.isNotEmpty(selectedComponentsId)) {
			addArtifactGroups(selectedComponentsId, newlySelectedModuleGrpIds, artifactGroups, serviceManager);
		}
		return artifactGroups;
	}
	
	private void addArtifactGroups(List<String> selectedFeaturesIds, List<String> newlySelectedModuleGrpIds,
			List<ArtifactGroup> artifactGroups, ServiceManager serviceManager) throws PhrescoException {
		for (String selectedfeatures : selectedFeaturesIds) {
			ArtifactInfo artifactInfo = serviceManager.getArtifactInfo(selectedfeatures);
			if (!newlySelectedModuleGrpIds.contains(artifactInfo.getArtifactGroupId())) {
				ArtifactGroup artifactGroupInfo = serviceManager
						.getArtifactGroupInfo(artifactInfo.getArtifactGroupId());
				artifactGroups.add(artifactGroupInfo);
			}
		}
	}
	
}
