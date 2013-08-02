package com.photon.phresco.ui.phrescoexplorer;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
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
		
		UpdateSCM scm = new UpdateSCM(buildDialog);
		scm.setTitle("Update");
		scm.open();
		return null;
	}
	
	public class UpdateSCM extends StatusDialog {

		private Combo typeCombo;
		private Text applicationRepoURLText;
		private Text usernameText;
		private Text passwordText;
		private Button headRevision;
		private Button revision;
		private Text revisionText;
		
		public UpdateSCM(Shell parent) {
			super(parent);
		}
		
		@Override
		protected Control createContents(Composite parent) {
			
			final Composite parentComposite = new Composite(parent, SWT.NONE);
			parentComposite.setLayout(new GridLayout(1, false));
			parentComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			final Composite composite = new Composite(parentComposite, SWT.NONE);
			composite.setLayout(new GridLayout(2, false));
			composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
			
			Label typeLabel = new Label(composite, SWT.NONE);
			typeLabel.setText(Messages.SCM_TYPE);
			
			typeCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
			String[] typeValue =  {Messages.TYPE_GIT, Messages.TYPE_SVN};
			typeCombo.setItems(typeValue);
			typeCombo.select(0);
			typeCombo.setLayoutData(gridData);
			
			Label applicationRepoURLLabel = new Label(composite, SWT.NONE);
			applicationRepoURLLabel.setText(Messages.REPO_URL);
			
			applicationRepoURLText = new Text(composite, SWT.BORDER);
			applicationRepoURLText.setLayoutData(gridData);
			
			Label usernameLabel = new Label(composite, SWT.NONE);
			usernameLabel.setText(Messages.USER_NAME);
			
			usernameText = new Text(composite, SWT.BORDER);
			usernameText.setLayoutData(gridData);
			
			Label passwordLabel = new Label(composite, SWT.NONE);
			passwordLabel.setText(Messages.USER_PWD);
			
			passwordText = new Text(composite, SWT.BORDER | SWT.PASSWORD);
			passwordText.setLayoutData(gridData);
			
			final Group revisionGroup = new Group(parentComposite, SWT.NONE);
			revisionGroup.setLayout(new GridLayout(3, false));
			revisionGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			revisionGroup.setText("Revision");
			
			headRevision = new Button(revisionGroup, SWT.RADIO);
			headRevision.setText("Head Revision");
			headRevision.setSelection(true);

			revision = new Button(revisionGroup, SWT.RADIO);
			revision.setText("Revision");
			
			revisionText = new Text(revisionGroup, SWT.BORDER);
			revisionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			revisionGroup.setVisible(false);
			
			typeCombo.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					if(SVN.equalsIgnoreCase(typeCombo.getText())) {
						revisionGroup.setVisible(true);
						revisionGroup.redraw();
					} else {
						revisionGroup.setVisible(false);
						revisionGroup.redraw();
					}
//					composite.pack();
					super.widgetSelected(e);
				}
			});
			
			parentComposite.pack();
			return super.createContents(parentComposite);
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
					PhrescoDialog.errorDialog(getShell(), Messages.WARNING, "Revision value shoul not be empty");
					return;
				}
				revisionValue = revisionTextValue;
			}
			try {
				ApplicationInfo appInfo = PhrescoUtil.getApplicationInfo();
				SCMManagerUtil managerUtil = new SCMManagerUtil();
				boolean updateProject = managerUtil.updateProject(typeLower, repoUrl, username, password, "", revisionValue, appInfo);
				if(updateProject) {
					PhrescoDialog.messageDialog(getShell(), "Project updated");
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
			} 
			return true;
		}
		
	}
}
