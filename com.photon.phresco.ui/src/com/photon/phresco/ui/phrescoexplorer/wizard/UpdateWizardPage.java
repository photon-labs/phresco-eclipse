package com.photon.phresco.ui.phrescoexplorer.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.ui.resource.Messages;

public class UpdateWizardPage extends WizardPage implements PhrescoConstants {

	public Combo typeCombo;
	public Text applicationRepoURLText;
	public Text usernameText;
	public Text passwordText;
	public Button headRevision;
	public Button revision;
	public Text revisionText;

	public UpdateWizardPage(String pageName) {
		super(pageName);
		setTitle(Messages.TITLE_UPDATE);
	}

	@Override
	public void createControl(Composite parent) {

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
		setControl(composite);
		parentComposite.pack();
	}
	
	/*private boolean validate(final Text text) {
		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(text.getText().length() > 0) {
					setErrorMessage(null);
				}
				super.keyPressed(e);
			}
		});
		return false;
	}*/
}
