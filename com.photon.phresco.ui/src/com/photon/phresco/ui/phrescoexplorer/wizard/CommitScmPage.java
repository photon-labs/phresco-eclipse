package com.photon.phresco.ui.phrescoexplorer.wizard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.PhrescoDialog;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.commons.util.SCMManagerUtil;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.framework.model.RepoFileInfo;
import com.photon.phresco.ui.resource.Messages;
import com.phresco.pom.model.Scm;
import com.phresco.pom.util.PomProcessor;

/**
 * @author suresh_ma
 *
 */
public class CommitScmPage extends WizardPage implements PhrescoConstants {

	public CommitScmPage(String pageName) {
		super(pageName);
		setTitle(Messages.TITLE_COMMIT_TO_REPO);
	}

	public Combo typeCombo;
	public Text applicationRepoURLText;
	public Text usernameText;
	public Text passwordText;
	public Text messageText;

	private Label typeLabel;
	private Label applicationRepoURLLabel;
	private Label usernameLabel;
	private Label passwordLabel;
	private Label messageLabel;
	public String connectionUrl;
	public List<File> filesTobeCommit = new ArrayList<File>();

	@Override
	public void createControl(Composite parent) {

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
		table.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		TableColumn checkBoxColumn = new TableColumn(table, SWT.LEFT, 0);
		checkBoxColumn.setText("");
		checkBoxColumn.setWidth(25);

		TableColumn fileColumn = new TableColumn(table, SWT.LEFT, 1);
		fileColumn.setText("File");
		fileColumn.setWidth(260);

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
		setControl(composite);
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
