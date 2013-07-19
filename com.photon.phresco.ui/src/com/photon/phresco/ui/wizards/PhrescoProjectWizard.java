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
package com.photon.phresco.ui.wizards;

import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.photon.phresco.ui.PhrescoPlugin;
import com.photon.phresco.ui.wizards.pages.AddProjectPage;
import com.photon.phresco.ui.wizards.pages.TechnologyPage;

/**
 * Phresco project wizard
 * 
 * @author suresh_ma
 *
 */
public class PhrescoProjectWizard extends Wizard implements INewWizard {

	private AddProjectPage appInfoPage;
	private TechnologyPage technologyPage;
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {

		ImageDescriptor myImage = ImageDescriptor.createFromURL(FileLocator.find(PhrescoPlugin.getDefault().getBundle(),
				new Path("icons/sample.gif"),null));
		super.setDefaultPageImageDescriptor(myImage);
		super.setNeedsProgressMonitor(true);
		super.setWindowTitle("Phresco");

	}

	@Override
	public void addPages() {
		super.addPages();
		technologyPage = new TechnologyPage("Technology Page");
		appInfoPage = new AddProjectPage("AddProjectPage");
		addPage(appInfoPage);
		addPage(technologyPage);
	}
	
	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		IWizardPage wizardPage = getContainer().getCurrentPage();

		if (wizardPage instanceof TechnologyPage) {
			
			TechnologyPage technologyPage = (TechnologyPage) wizardPage;
			
			IWizardPage firstPage = getContainer().getCurrentPage().getPreviousPage();

			AddProjectPage addProjectPage = (AddProjectPage) firstPage;
			List<Button> layersList = addProjectPage.getLayersList();
			System.out.println(" layersList :" + layersList.size());
			technologyPage.renderLayer(layersList);
			/*boolean isRendered = false;
			for (Object object : layersList) {
				Button layerButton = (Button) object;
				System.out.println(" layerButton.getSelection() :" + layerButton.getSelection());
				System.out.println(" layerButton.getText() :" + layerButton.getText());
				if (!isRendered && layerButton.getSelection()) {
					technologyPage.renderLayer(layerButton.getText());
					isRendered = true;
				}
			}*/
		}
		return super.getNextPage(page);
	}
	
	@Override
	public boolean performFinish() {
		return false;
	}
}