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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.photon.phresco.ui.wizards.pages.AppInfoPage;
import com.photon.phresco.ui.wizards.pages.ConfigurationsPage;
import com.photon.phresco.ui.wizards.pages.FeaturesPage;

/**
 * Phresco project wizard
 * 
 * @author arunachalam.lakshmanan@photoninfotech.net
 */
public class PhrescoProjectWizard extends Wizard implements INewWizard {

	private AppInfoPage appInfoPage;
	private FeaturesPage featuresPage;
	private ConfigurationsPage configurationsPage;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void addPages() {
		// TODO Auto-generated method stub
		super.addPages();
		appInfoPage = new AppInfoPage("AppInfoPage");
		addPage(appInfoPage);
		featuresPage = new FeaturesPage("FeaturesPage");
		addPage(featuresPage);
		configurationsPage = new ConfigurationsPage("ConfigurationsPage");
		addPage(configurationsPage);
	}

	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return false;
	}

}