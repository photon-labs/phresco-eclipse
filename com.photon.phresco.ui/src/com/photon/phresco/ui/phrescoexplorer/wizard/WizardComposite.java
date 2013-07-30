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

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

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
		addPage(new JSLibraryFeaturePage());
		addPage(new ModuleFeaturePage());
		addPage(new ComponentFeaturePage());
	}

	public boolean performFinish() {
		JSLibraryFeaturePage dirPage = getDirectoryPage();
		if (dirPage.useDefaultDirectory()) {
			System.out.println("Using default directory");
		} else {
			ModuleFeaturePage choosePage = getChoosePage();
//			System.out.println("Using directory: " + choosePage.getDirectory());
		}
		
		IWizardPage wizardPage = getContainer().getCurrentPage();
		System.out.println(" wizardPage :" + wizardPage);
		if (wizardPage instanceof JSLibraryFeaturePage) {
			JSLibraryFeaturePage jsLibPage = (JSLibraryFeaturePage) wizardPage;
			jsLibPage.getSelectedJSLib();
		}
		
		return true;
	}

	private ModuleFeaturePage getChoosePage() {
		return (ModuleFeaturePage) getPage(ModuleFeaturePage.PAGE_NAME);
	}

	private JSLibraryFeaturePage getDirectoryPage() {
		return (JSLibraryFeaturePage) getPage(JSLibraryFeaturePage.PAGE_NAME);
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
