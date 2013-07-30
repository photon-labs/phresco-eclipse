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

import java.util.List;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.photon.phresco.commons.model.ArtifactGroup;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.service.client.api.ServiceManager;

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
		
		for (int i = 0; i < pages.length; i++) {
			IWizardPage wizardPage = pages[i];
			
			System.out.println(" wizardPage :"  + wizardPage);
			if (wizardPage instanceof JSLibraryFeaturePage) {
				JSLibraryFeaturePage jsLibPage = (JSLibraryFeaturePage) wizardPage;
				jsLibPage.getSelectedItems();
			} else if (wizardPage instanceof ModuleFeaturePage) {
				ModuleFeaturePage modulePage = (ModuleFeaturePage) wizardPage;
				modulePage.getSelectedItems();
			} else if (wizardPage instanceof ComponentFeaturePage) {
				ComponentFeaturePage componentPage = (ComponentFeaturePage) wizardPage;
				componentPage.getSelectedItems();
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
}
