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

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.PhrescoDialog;
import com.photon.phresco.commons.util.SCMManagerUtil;
import com.photon.phresco.ui.wizards.pages.ImportFromSCMPage;

/**
 * Wizard to handle phresco import from scm
 * @author syed
 *
 */
public class PhrescoImportFromSCMWizard extends Wizard implements IImportWizard, PhrescoConstants {

	public PhrescoImportFromSCMWizard() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean performFinish() {
		IWizardPage wizardPage = getContainer().getCurrentPage();
		if (wizardPage instanceof ImportFromSCMPage) {
			ImportFromSCMPage scmPage = (ImportFromSCMPage) wizardPage;
			if (validate(scmPage)) {
				try {
					
/*					  IWorkspace workspace = ResourcesPlugin.getWorkspace();
					  IWorkspaceRoot root = workspace.getRoot();
					  System.out.println(" full path : " + root.getFullPath());
					  System.out.println(" root location :" + root.getLocation());
					  System.out.println(" root :" + root.getProjectRelativePath());
					  System.out.println(" project full path : "+ root.getProject().getFullPath());
					  System.out.println(" root.getProject().getlocation " + root.getProject().getLocation());*/
					
					IWorkspace workspace = ResourcesPlugin.getWorkspace();  
					  
					//get location of workspace (java.io.File)  
					File workspaceDirectory = workspace.getRoot().getLocation().toFile();
					System.out.println(" workspaceDirectory : " + workspaceDirectory.getPath());		
					System.out.println(" Calling import project ");
					SCMManagerUtil.importProject(scmPage.gitRadio.getText(), scmPage.repoURLText.getText(), 
							scmPage.userName.getText(), scmPage.password.getText(), STR_EMPTY, scmPage.revisionText.getText(), workspaceDirectory.getPath());
//					updateProjectIntoWorkspace(importProject);
				} catch (Exception e) {
					e.printStackTrace();
					PhrescoDialog.errorDialog(getShell(), "ERROR", e.getLocalizedMessage());
				}
				return true;
			}
		}
		
		return false;
	}
	
	
	@Override
	public void addPages() {
		addPage(new ImportFromSCMPage("Import from SCM", "Provide SCM Details"));
	}
	
	private boolean validate(ImportFromSCMPage scmPage) {
		boolean canFinish = false;
		if (scmPage.gitRadio.getSelection() && isValueDefined(scmPage)) {
			canFinish = true;
		} else if (isValueDefined(scmPage) && (scmPage.headRevisionButton.getSelection() || !scmPage.revisionText.getText().equals(STR_EMPTY)) ){
			canFinish = true;
		} else {
			canFinish = false;
		}
		
		if (!canFinish) {
			scmPage.setErrorMessage("Provide Mandatory Values");
		}
		
		System.out.println(" canFinish inj validate  " + canFinish);
		return canFinish;
	}

	private boolean isValueDefined(ImportFromSCMPage scmPage) {
		boolean canFinish = false;
		if (!scmPage.repoURLText.getText().equals(STR_EMPTY) && !scmPage.userName.getText().equals(STR_EMPTY) && 
						!scmPage.password.getText().equals(STR_EMPTY)) {
			canFinish = true;
		} else {
			canFinish = false;
		}
		return canFinish;
	}
}