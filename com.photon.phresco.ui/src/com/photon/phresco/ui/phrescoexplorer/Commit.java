package com.photon.phresco.ui.phrescoexplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.handlers.HandlerUtil;

import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.PhrescoDialog;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.commons.util.SCMManagerUtil;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.ui.phrescoexplorer.wizard.CommitScmPage;
import com.photon.phresco.ui.phrescoexplorer.wizard.WizardComposite;
import com.photon.phresco.ui.resource.Messages;

/**
 * @author suresh_ma
 *
 */
public class Commit extends AbstractHandler implements PhrescoConstants {

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
				WizardDialog wizardControl = wizardComposite.getWizardControl(new CommitScm());
				wizardControl.open();
			}
		});
		
		return null;
	}
	
	/**
	 * @author suresh_ma
	 *
	 */
	public class CommitScm extends Wizard {

		private Combo typeCombo;
		private Text applicationRepoURLText;
		private Text usernameText;
		private Text passwordText;
		private Text messageText;
		
		private String connectionUrl;
		private List<File> filesTobeCommit = new ArrayList<File>();
		
		@Override
		public void addPages() {
			this.setWindowTitle("Commit");
			CommitScmPage scmPage = new CommitScmPage("Commit");
			addPage(scmPage);
			super.addPages();
		}
		
		@Override
		public boolean performFinish() {
			IWizardPage[] pages = getPages();
			for (IWizardPage iWizardPage : pages) {
				if(iWizardPage instanceof CommitScmPage) {
					CommitScmPage scmPage =  (CommitScmPage) iWizardPage;
					typeCombo = scmPage.typeCombo;
					applicationRepoURLText = scmPage.applicationRepoURLText;
					usernameText = scmPage.usernameText;
					passwordText = scmPage.passwordText;
					messageText = scmPage.messageText;
					connectionUrl = scmPage.connectionUrl;
					filesTobeCommit = scmPage.filesTobeCommit;
					return commit(scmPage);
				}
			}
			return false;
		}

		private boolean commit(WizardPage wizardPage) {
			boolean validate = validate(wizardPage);
			if(!validate) {
				return validate;
			}
			String username = usernameText.getText();
			String password = passwordText.getText();
			String applicationHome = PhrescoUtil.getApplicationHome();
			File commitHome = new File(applicationHome);
			String message = messageText.getText();
			SCMManagerUtil util = new SCMManagerUtil();
			boolean commitToRepo = false;
			if(connectionUrl.contains(GIT)) {
				try {
					commitToRepo = util.commitToRepo(GIT, connectionUrl, username, password, "", "", commitHome, message);
				} catch (Exception e) {
					PhrescoDialog.exceptionDialog(getShell(), e);
				}
			} else if(connectionUrl.contains(SVN)) {
				try {
					util.commitSpecifiedFiles(filesTobeCommit, username, password, message);
					commitToRepo = true;
				} catch (Exception e) {
					PhrescoDialog.exceptionDialog(getShell(), e);
				}
			}
			if(commitToRepo) {
				PhrescoDialog.messageDialog(getShell(), "Files commited");
				return commitToRepo;
			}
			return commitToRepo;
		}
		
		/**
		 * @return
		 */
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
			} else if(StringUtils.isEmpty(messageText.getText())) {
				wizardPage.setErrorMessage("Message" + STR_SPACE + Messages.EMPTY_STRING_WARNING);
				return false;
			}
			return true;
		}

	}
}
