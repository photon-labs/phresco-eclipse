package com.photon.phresco.ui.phrescoexplorer.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.photon.phresco.ui.resource.Messages;

/**
 * @author suresh_ma
 *
 */
public class AddToRepoWizardPage extends WizardPage {

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

	public AddToRepoWizardPage(String pageName) {
		super(pageName);
		setTitle("Add To Repo");
	}

	@Override
	public void createControl(Composite parent) {
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
		setControl(composite);
	}
}
