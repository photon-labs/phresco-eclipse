package com.photon.phresco.ui.phrescoexplorer;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.handlers.HandlerUtil;

import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.PhrescoDialog;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.commons.util.SCMManagerUtil;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.ui.phrescoexplorer.wizard.UpdateWizardPage;
import com.photon.phresco.ui.phrescoexplorer.wizard.WizardComposite;
import com.photon.phresco.ui.resource.Messages;

public class Update extends AbstractHandler implements PhrescoConstants {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);
		
		// To check the user has logged in
		ServiceManager serviceManager = PhrescoUtil.getServiceManager(PhrescoUtil.getUserId());
		if(serviceManager == null) {
			PhrescoDialog.errorDialog(shell,Messages.WARNING, Messages.PHRESCO_LOGIN_WARNING);
			return null;
		}
		
		Shell buildDialog = new Shell(shell, SWT.APPLICATION_MODAL |  SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.TITLE | SWT.RESIZE);
		GridLayout layout = new GridLayout(1, false);
		buildDialog.setLocation(385, 130);
		buildDialog.setLayout(layout);
		buildDialog.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		final WizardComposite wizardComposite = new WizardComposite(buildDialog);
		BusyIndicator.showWhile(null, new Runnable() {
			public void run() {
				UpdateSCM updateSCM = new UpdateSCM();
				updateSCM.setWindowTitle(Messages.TITLE_UPDATE);
				WizardDialog wizardControl = wizardComposite.getWizardControl(updateSCM);
				wizardControl.open();
			}
		});
		
		return null;
	}
	
	public class UpdateSCM extends Wizard {

		private Combo typeCombo;
		private Text applicationRepoURLText;
		private Text usernameText;
		private Text passwordText;
		private Button headRevision;
		private Button revision;
		private Text revisionText;
		
		@Override
		public void addPages() {
			UpdateWizardPage wizardPage = new UpdateWizardPage(Messages.TITLE_UPDATE);
			addPage(wizardPage);
			super.addPages();
		}
		
		@Override
		public boolean performFinish() {
			IWizardPage[] pages = getPages();
			for (IWizardPage iWizardPage : pages) {
				if(iWizardPage instanceof UpdateWizardPage) {
					UpdateWizardPage wizardPage = (UpdateWizardPage) iWizardPage;
					typeCombo = wizardPage.typeCombo;
					applicationRepoURLText = wizardPage.applicationRepoURLText;
					usernameText = wizardPage.usernameText;
					passwordText = wizardPage.passwordText;
					headRevision  = wizardPage.headRevision;
					revision = wizardPage.revision;
					revisionText = wizardPage.revisionText;
					return Update(wizardPage);
				}
			}
			return false;
		}

		private boolean Update(WizardPage wizardPage) {
			boolean validate = validate(wizardPage);
			if(!validate) {
				return validate;
			}
			String type = typeCombo.getText();
			String typeLower = type.toLowerCase();
			String repoUrl = applicationRepoURLText.getText();
			String username = usernameText.getText();
			String password = passwordText.getText();
			String revisionValue = "";
			if(GIT.equalsIgnoreCase(type)) {
				revisionValue = "master";
			} else if(headRevision.getSelection() && SVN.equalsIgnoreCase(type)) {
				revisionValue = "HEAD";
			} else if(revision.getSelection() && SVN.equalsIgnoreCase(type)) {
				String revisionTextValue = revisionText.getText();
				if(StringUtils.isEmpty(revisionTextValue)) {
					wizardPage.setErrorMessage("Revision value should not be empty");
					return false;
				}
				revisionValue = revisionTextValue;
			}
			try {
				ApplicationInfo appInfo = PhrescoUtil.getApplicationInfo();
				SCMManagerUtil managerUtil = new SCMManagerUtil();
				boolean updateProject = managerUtil.updateProject(typeLower, repoUrl, username, password, "", revisionValue, appInfo);
				if(updateProject) {
					PhrescoDialog.messageDialog(getShell(), "Project updated");
					return updateProject;
				}
			
			} catch (PhrescoException e) {
				PhrescoDialog.exceptionDialog(getShell(), e);
			} catch (Exception e) {
				PhrescoDialog.exceptionDialog(getShell(), e);
			}
			return false;
		}
		
		private boolean validate(WizardPage wizardPage) {
			if(StringUtils.isEmpty(typeCombo.getText())) {
				wizardPage.setErrorMessage("Type" + STR_SPACE + Messages.EMPTY_STRING_WARNING);
				return false;
			} else if(StringUtils.isEmpty(applicationRepoURLText.getText())) {
				wizardPage.setErrorMessage("Repo url" + STR_SPACE + Messages.EMPTY_STRING_WARNING);
				return false;
			} else if(StringUtils.isEmpty(usernameText.getText())) {
				wizardPage.setErrorMessage("Username" + STR_SPACE + Messages.EMPTY_STRING_WARNING);
				return false;
			} else if(StringUtils.isEmpty(passwordText.getText())) {
				wizardPage.setErrorMessage("Password" + STR_SPACE + Messages.EMPTY_STRING_WARNING);
				return false;
			} 
			return true;
		}
	}
}
