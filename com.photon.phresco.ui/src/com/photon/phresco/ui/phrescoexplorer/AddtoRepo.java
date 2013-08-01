package com.photon.phresco.ui.phrescoexplorer;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
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
import com.photon.phresco.ui.resource.Messages;

/**
 * @author suresh_ma
 *
 */
public class AddtoRepo extends AbstractHandler implements PhrescoConstants {

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
		
		AddRepo addRepo = new AddRepo(buildDialog);
		addRepo.setTitle("Add to repo");
		addRepo.open();
		return null;
	}
	
	/**
	 * @author suresh_ma
	 *
	 */
	public class AddRepo extends StatusDialog {

		private Combo typeCombo;
		private Text applicationRepoURLText;
		private Text usernameText;
		private Text passwordText;
		private Text messageText;
		
		private Label typeLabel;
		private Label applicationRepoURLLabel;
		private Label usernameLabel;
		private Label passwordLabel;
		private Label messageLabel;
		
		public AddRepo(Shell parent) {
			super(parent);
		}
		
		@Override
		protected Control createContents(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout(2, false));
			composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
			
			typeLabel = new Label(composite, SWT.NONE);
			typeLabel.setText(Messages.SCM_TYPE);
			
			typeCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
			String[] typeValue =  {Messages.TYPE_GIT, Messages.TYPE_SVN};
			typeCombo.setItems(typeValue);
			typeCombo.select(0);
			typeCombo.setLayoutData(gridData);
			
			applicationRepoURLLabel = new Label(composite, SWT.NONE);
			applicationRepoURLLabel.setText(Messages.REPO_URL);
			
			applicationRepoURLText = new Text(composite, SWT.BORDER);
			applicationRepoURLText.setLayoutData(gridData);
			
			usernameLabel = new Label(composite, SWT.NONE);
			usernameLabel.setText(Messages.USER_NAME);
			
			usernameText = new Text(composite, SWT.BORDER);
			usernameText.setLayoutData(gridData);
			
			passwordLabel = new Label(composite, SWT.NONE);
			passwordLabel.setText(Messages.USER_PWD);
			
			passwordText = new Text(composite, SWT.BORDER | SWT.PASSWORD);
			passwordText.setLayoutData(gridData);
			
			messageLabel = new Label(composite, SWT.NONE);
			messageLabel.setText(Messages.MESSAGE);
			
			messageText = new Text(composite, SWT.BORDER | SWT.MULTI);
			GridData messageGridData = new GridData();
			messageGridData.widthHint = 100;
			messageGridData.heightHint = 40;
			messageText.setLayoutData(messageGridData);
			
			composite.pack();
			return super.createContents(composite);
		}
		
		@Override
		protected void okPressed() {
			boolean validate = validate();
			if(!validate) {
				return;
			}
			String type = typeCombo.getText();
			String typeLower = type.toLowerCase();
			String repoUrl = applicationRepoURLText.getText();
			String userName = usernameText.getText();
			String password = passwordText.getText();
			try {
				ApplicationInfo appInfo = PhrescoUtil.getApplicationInfo();
				String message = messageText.getText();
				SCMManagerUtil managerUtil = new SCMManagerUtil();
				boolean importToRepo = managerUtil.importToRepo(typeLower, repoUrl, userName, password, "", "", appInfo, message);
				if(importToRepo) {
					PhrescoDialog.messageDialog(getShell(), "project " + appInfo.getAppDirName() + " added into " + repoUrl);
				}
			} catch (PhrescoException e) {
				PhrescoDialog.exceptionDialog(getShell(), e);
			} catch (Exception e) {
				PhrescoDialog.exceptionDialog(getShell(), e);
			}
			super.okPressed();
		}
		
		private boolean validate() {
			if(StringUtils.isEmpty(typeCombo.getText())) {
				PhrescoDialog.errorDialog(getShell(), Messages.WARNING, "Type" + STR_SPACE + Messages.EMPTY_STRING_WARNING);
				return false;
			} else if(StringUtils.isEmpty(applicationRepoURLText.getText())) {
				PhrescoDialog.errorDialog(getShell(), Messages.WARNING, "Repo url" + STR_SPACE + Messages.EMPTY_STRING_WARNING);
				return false;
			} else if(StringUtils.isEmpty(usernameText.getText())) {
				PhrescoDialog.errorDialog(getShell(), Messages.WARNING, "Username" + STR_SPACE + Messages.EMPTY_STRING_WARNING);
				return false;
			} else if(StringUtils.isEmpty(passwordText.getText())) {
				PhrescoDialog.errorDialog(getShell(), Messages.WARNING, "Password" + STR_SPACE + Messages.EMPTY_STRING_WARNING);
				return false;
			} else if(StringUtils.isEmpty(messageText.getText())) {
				PhrescoDialog.errorDialog(getShell(), Messages.WARNING, "Message" + STR_SPACE + Messages.EMPTY_STRING_WARNING);
				return false;
			}
			return true;
		}
	}
}
