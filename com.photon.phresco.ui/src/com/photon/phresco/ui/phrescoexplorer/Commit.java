package com.photon.phresco.ui.phrescoexplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.handlers.HandlerUtil;
import org.tmatesoft.svn.core.SVNCommitInfo;

import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.PhrescoDialog;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.commons.util.SCMManagerUtil;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.framework.model.RepoFileInfo;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.ui.resource.Messages;
import com.phresco.pom.model.Scm;
import com.phresco.pom.util.PomProcessor;

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
		
		CommitScm commitScm = new CommitScm(buildDialog);
		commitScm.setTitle("Commit");
		commitScm.open();
		return null;
	}
	
	/**
	 * @author suresh_ma
	 *
	 */
	public class CommitScm extends StatusDialog {

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
		private String connectionUrl;
		private List<File> filesTobeCommit = new ArrayList<File>();
		
		public CommitScm(Shell parent) {
			super(parent);
		}
		
		@Override
		protected Control createContents(Composite parent) {
			
			Composite parentComposite = new Composite(parent, SWT.NONE);
			parentComposite.setLayout(new GridLayout(1, false));
			parentComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			Composite composite = new Composite(parentComposite, SWT.NONE);
			composite.setLayout(new GridLayout(2, false));
			composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			try {
				ApplicationInfo appInfo = PhrescoUtil.getApplicationInfo();
				connectionUrl = getConnectionUrl(appInfo);
			} catch (PhrescoException e1) {
				PhrescoDialog.exceptionDialog(getShell(), e1);
			}
			
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
			applicationRepoURLText.setText(connectionUrl);
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
			messageGridData.widthHint = 130;
			messageGridData.heightHint = 40;
			messageText.setLayoutData(messageGridData);
			
			Composite tableComposite = new Composite(parentComposite, SWT.NONE);
			tableComposite.setLayout(new GridLayout(1, false));
			tableComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			Table table = new Table(tableComposite, SWT.BORDER | SWT.MULTI);
			table.setLinesVisible(true);
			table.setHeaderVisible(true);
			table.setLayoutData(new GridData());
			
			table.setBounds(12, 20, 450, 175);
			
	        TableColumn checkBoxColumn = new TableColumn(table, SWT.LEFT, 0);
	        checkBoxColumn.setText("");
	        checkBoxColumn.setWidth(25);
	        
		    TableColumn fileColumn = new TableColumn(table, SWT.LEFT, 1);
	        fileColumn.setText("File");
	        fileColumn.setWidth(160);
	        
	        TableColumn statusColumn = new TableColumn(table, SWT.LEFT, 2);
	        statusColumn.setText("Status");
	        statusColumn.setWidth(140);
	        
	        List<RepoFileInfo> commitableFiles = null;
			try {
				if(connectionUrl.contains(GIT)) {
					commitableFiles = gitCommitableFiles();
				} else {
					commitableFiles = svnCommitableFiles();
				}
				 for (int i = 0; i < commitableFiles.size(); i++) {
			            new TableItem(table, SWT.NONE);
			        }
			} catch (PhrescoException e) {
				PhrescoDialog.exceptionDialog(getShell(), e);
				parentComposite.dispose();
			}
			TableItem[] tableItems = table.getItems();
			if(CollectionUtils.isNotEmpty(commitableFiles)) {
				int i = 0;
				for (final RepoFileInfo repoFileInfo : commitableFiles) {
					TableItem tableItem = tableItems[i];
					TableEditor editor = new TableEditor(table);
					final Button checkButton = new Button(table, SWT.CHECK);
					checkButton.pack();
					editor.minimumWidth = checkButton.getSize().x;
					editor.horizontalAlignment = SWT.LEFT;
					editor.setEditor(checkButton, tableItem, 0);
					
					if(connectionUrl.contains(GIT)) {
						checkButton.setEnabled(false);
					}
					checkButton.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							File commitFile = new File(repoFileInfo.getCommitFilePath());
							if(checkButton.getSelection()) {
								filesTobeCommit.add(commitFile);
							} else {
								filesTobeCommit.remove(commitFile);
							}
							super.widgetSelected(e);
						}
					});
					editor = new TableEditor(table);
					Label fileLabel = new Label(table, SWT.NONE);
					fileLabel.setText(repoFileInfo.getCommitFilePath());
					editor.grabHorizontal = true;
					editor.setEditor(fileLabel, tableItem, 1);
					
					editor = new TableEditor(table);
					Label statusLabel = new Label(table, SWT.CENTER);
					statusLabel.setText(repoFileInfo.getStatus());
					editor.grabHorizontal = true;
					editor.setEditor(statusLabel, tableItem, 2);
					
					i = i + 1;
				}
			}
			parentComposite.pack();
			return super.createContents(parentComposite);
		}
		
		@Override
		protected void okPressed() {
			boolean validate = validate();
			if(!validate) {
				return;
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
			}
			super.okPressed();
		}
		
		/**
		 * @return
		 */
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
	
	/**
	 * @param applicationInfo
	 * @return
	 * @throws PhrescoException
	 */
	private String getConnectionUrl(ApplicationInfo applicationInfo) throws PhrescoException {
		try {
			PomProcessor processor = PhrescoUtil.getPomProcessor(applicationInfo.getAppDirName());
			Scm scm = processor.getSCM();
			if (scm != null && !scm.getConnection().isEmpty()) {
				return scm.getConnection();
			}
		} catch (PhrescoException e) {
			throw new PhrescoException(e);
		}

		return "";
	}
	
	/**
	 * @return
	 * @throws PhrescoException
	 */
	private List<RepoFileInfo> svnCommitableFiles() throws PhrescoException {
		List<RepoFileInfo> commitableFiles = null;
		String revision = "";
		try {
			SCMManagerUtil util = new SCMManagerUtil();
			String applicationHome = PhrescoUtil.getApplicationHome();
			File appDir = new File(applicationHome);
			revision = HEAD_REVISION;
			commitableFiles = util.getCommitableFiles(appDir, revision);
		} catch (Exception e) {
			throw new PhrescoException(e);
		}
		return commitableFiles;
	}
	
	/**
	 * @return
	 * @throws PhrescoException
	 */
	private List<RepoFileInfo> gitCommitableFiles() throws PhrescoException {
		List<RepoFileInfo> gitCommitableFiles = null;
		try {
			SCMManagerUtil util = new SCMManagerUtil();
			String applicationHome = PhrescoUtil.getApplicationHome();
			File appDir = new File(applicationHome);
			gitCommitableFiles = util.getGITCommitableFiles(appDir);
		} catch (Exception e) {
			throw new PhrescoException(e);
		}
		return gitCommitableFiles;
	}
}
