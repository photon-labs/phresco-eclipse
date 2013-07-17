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

package com.photon.phresco.ui.wizards.pages;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.WizardResourceImportPage;

import com.photon.phresco.ui.wizards.PhrescoImportFromSCMWizard;

/**
 * Page to handle phresco import from scm
 * @author syed
 *
 */

public class ImportFromSCMPage extends WizardResourceImportPage implements IWizardPage {

	public ImportFromSCMPage(String name, IStructuredSelection selection) {
	super(name, selection);
		// TODO Auto-generated constructor stub
	}
	
	public ImportFromSCMPage(PhrescoImportFromSCMWizard importProjectFromScmWizard) {
		super("", null);
	}
	
	@Override
	protected void createSourceGroup(Composite parent) {
		// TODO Auto-generated method stub
	}
	
	@Override
	protected ITreeContentProvider getFileProvider() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected ITreeContentProvider getFolderProvider() {
		// TODO Auto-generated method stub
		return null;
	}
}
