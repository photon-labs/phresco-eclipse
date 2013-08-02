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
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.PhrescoDialog;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.framework.api.SCMManager;
import com.photon.phresco.framework.impl.SCMManagerImpl;
import com.photon.phresco.ui.resource.Messages;
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
					
					//get location of workspace (java.io.File)
					String scmType = GIT;
					if (scmPage.svnRadio.getSelection()) {
						scmType = SVN;
					}
					String scmUrl = scmPage.repoURLText.getText();
					String username = scmPage.userName.getText();
					String password = scmPage.password.getText();
					String revision = scmPage.revisionText.getText();
					
					SCMManager scmManager = new SCMManagerImpl();
					ApplicationInfo appInfo = scmManager.importProject(scmType, scmUrl, username, password, STR_EMPTY, revision);
					PhrescoUtil.updateProjectIntoWorkspace(appInfo.getAppDirName());
				} catch (Exception e) {
					e.printStackTrace();
					PhrescoDialog.errorDialog(getShell(), "ERROR", e.getLocalizedMessage()); //$NON-NLS-1$
				}
				return true;
			}
		}
		
		return false;
	}
	
	
	@Override
	public void addPages() {
		addPage(new ImportFromSCMPage(Messages.IMPORT_FROM_SCM, Messages.PROVIDE_SCM_DETAILS));
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
			scmPage.setErrorMessage(Messages.PROVIDE_MANDATORY_VALUES);
		}
		
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