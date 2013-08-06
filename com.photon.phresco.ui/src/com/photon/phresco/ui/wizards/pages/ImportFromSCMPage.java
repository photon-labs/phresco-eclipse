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

package com.photon.phresco.ui.wizards.pages;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.photon.phresco.commons.ConfirmDialog;
import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.ui.resource.Messages;

/**
 * Page to handle phresco import from scm
 * @author syed
 *
 */
public class ImportFromSCMPage extends WizardPage implements PhrescoConstants {

	public Text repoURLText;
    public Text userName;
    public Text password;
    public Button gitRadio;
    public Button svnRadio;
    public Button headRevisionButton;
    public Text revisionText;
    public Button testCheckOutButton;
    
    public Label testRepoUrlLabel;
    public Text testRepoUrlText;
    public Label otherCredentialLbl;
    public Button testOtherCredentialButton;
    public Label testRepoUsernameLabel;
    public Text testRepoUsernameText;
    public Text testRepoPasswordText;
    public Button repoUrlHeadRevisionButton;
    public Text testRepoRevisionText;
    
	public ImportFromSCMPage(String pageName, String message) {
		super(pageName);
		setMessage(message);
	}

	@Override
	public void createControl(final Composite parent) {
		
		if(!PhrescoUtil.isLoggedIn()) {
			ConfirmDialog.getConfirmDialog().showConfirm(getShell());
			return;
		}
		
        Composite composite = new Composite(parent, 0);
        setControl(composite);
        GridLayout layout = new GridLayout(2, false);
        layout.marginLeft = 25;
        layout.horizontalSpacing = 5;
        layout.verticalSpacing = 10;
        composite.setLayout(layout);
        
		Label lblScmUrl = new Label(composite, SWT.NONE);
		lblScmUrl.setText(Messages.SCM_TYPE);
		Label empty = new Label(composite, SWT.NONE); // For the second column grid
		
		gitRadio = new Button(composite, SWT.RADIO);
		gitRadio.setSelection(true);
		gitRadio.setText(Messages.TYPE_GIT);
		gitRadio.setLayoutData(new GridData(50,13));
		
		svnRadio = new Button(composite, SWT.RADIO);
		svnRadio.setText(Messages.TYPE_SVN);
		svnRadio.setLayoutData(new GridData(50,13));
		
		Label repoURL = new Label(composite, SWT.NONE);
		repoURL.setText(Messages.REPO_RUL);
		repoURLText = new Text(composite, SWT.BORDER);
		repoURLText.setLayoutData(new GridData(230,15));
		
		final Label lblOtherCredentials = new Label(composite, SWT.NONE);
		lblOtherCredentials.setText(Messages.OTHER_CREDENTIALS);
		final Button otherCredentialButton = new Button(composite, SWT.CHECK);
		otherCredentialButton.setLayoutData(new GridData(15,13));
		
        Label userNameLabel = new Label(composite, SWT.LEFT);
        userNameLabel.setText(Messages.USER_NAME);
        userName = new Text(composite, SWT.BORDER);
        userName.setText(DEFAULT_USER_NAME);
        userName.setLayoutData(new GridData(160,15));

        Label passwordLabel = new Label(composite, SWT.LEFT);
        passwordLabel.setText(Messages.USER_PWD);
        password = new Text(composite, SWT.BORDER);
        password.setEchoChar(CHAR_ASTERISK);
        password.setText(DEFAULT_PASSWORD);
        password.setLayoutData(new GridData(160,15));
        
		final Label lblHeadRevision = new Label(composite, SWT.NONE);
		lblHeadRevision.setText(Messages.REVISION);
		lblHeadRevision.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
		
		headRevisionButton = new Button(composite, SWT.CHECK);
		headRevisionButton.setText(Messages.HEAD_REVISION);
		headRevisionButton.setLayoutData(new GridData(80,13));
		
		final Label revisionLabel = new Label(composite, SWT.NONE);
		revisionText = new Text(composite, SWT.BORDER);
		revisionText.setLayoutData(new GridData(160,13));
		
		final Label lblTestCheckout = new Label(composite, SWT.NONE);
		lblTestCheckout.setText(Messages.TEST_CHECKOUT);
		testCheckOutButton = new Button(composite, SWT.CHECK);
		testCheckOutButton.setLayoutData(new GridData(15,13));
		
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayoutData(gridData);
		
		testRepoUrlLabel = new Label(composite, SWT.NONE);
		testRepoUrlLabel.setText(Messages.REPO_RUL);
		
		testRepoUrlText = new Text(composite, SWT.BORDER);
		testRepoUrlText.setLayoutData(new GridData(160,15));
		
		otherCredentialLbl = new Label(composite, SWT.NONE);
		otherCredentialLbl.setText(Messages.OTHER_CREDENTIALS);
		
		testOtherCredentialButton = new Button(composite, SWT.CHECK);
		testOtherCredentialButton.setLayoutData(new GridData(15, 13));
		
		final Label testRepouserNameLabel = new Label(composite, SWT.LEFT);
        testRepouserNameLabel.setText(Messages.USER_NAME);
        testRepoUsernameText = new Text(composite, SWT.BORDER);
        testRepoUsernameText.setText(DEFAULT_USER_NAME);
        testRepoUsernameText.setEnabled(false);
        testRepoUsernameText.setLayoutData(new GridData(160,15));
		
        final Label testRepopasswordLabel = new Label(composite, SWT.LEFT);
        testRepopasswordLabel.setText(Messages.USER_PWD);
        testRepoPasswordText = new Text(composite, SWT.BORDER);
        testRepoPasswordText.setEchoChar(CHAR_ASTERISK);
        testRepoPasswordText.setText(DEFAULT_PASSWORD);
        testRepoPasswordText.setEnabled(false);
        testRepoPasswordText.setLayoutData(new GridData(160,15));
		
        final Label testRepoLblHeadRevision = new Label(composite, SWT.NONE);
		testRepoLblHeadRevision.setText(Messages.REVISION);
		testRepoLblHeadRevision.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
		
		repoUrlHeadRevisionButton = new Button(composite, SWT.CHECK);
		repoUrlHeadRevisionButton.setText(Messages.HEAD_REVISION);
		repoUrlHeadRevisionButton.setLayoutData(new GridData(80,13));
		
		new Label(composite, SWT.NONE);
		testRepoRevisionText = new Text(composite, SWT.BORDER);
		testRepoRevisionText.setLayoutData(new GridData(160,13));
		
		testCheckOutButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(testCheckOutButton.getSelection()) {
					testRepoUrlLabel.setVisible(true);
					testRepoUrlText.setVisible(true);
					otherCredentialLbl.setVisible(true);
					testOtherCredentialButton.setVisible(true);
					testRepouserNameLabel.setVisible(true);
					testRepoUsernameText.setVisible(true);
					testRepopasswordLabel.setVisible(true);
					testRepoPasswordText.setVisible(true);
					testRepoLblHeadRevision.setVisible(true);
					repoUrlHeadRevisionButton.setVisible(true);
					testRepoRevisionText.setVisible(true);
				} else {
					testRepoUrlLabel.setVisible(false);
					testRepoUrlText.setVisible(false);
					otherCredentialLbl.setVisible(false);
					testOtherCredentialButton.setVisible(false);
					testRepouserNameLabel.setVisible(false);
					testRepoUsernameText.setVisible(false);
					testRepopasswordLabel.setVisible(false);
					testRepoPasswordText.setVisible(false);
					testRepoLblHeadRevision.setVisible(false);
					repoUrlHeadRevisionButton.setVisible(false);
					testRepoRevisionText.setVisible(false);
				}
				super.widgetSelected(e);
			}
		});
		// To hide for default selection scm type Git
		
		lblHeadRevision.setVisible(false);
		headRevisionButton.setVisible(false);
		revisionLabel.setVisible(false);
		revisionText.setVisible(false);
		lblTestCheckout.setVisible(false);
		testCheckOutButton.setVisible(false);
		lblOtherCredentials.setVisible(true);
		otherCredentialButton.setVisible(true);
		userName.setEnabled(false);
		password.setEnabled(false);
		testRepoUrlLabel.setVisible(false);
		testRepoUrlText.setVisible(false);
		otherCredentialLbl.setVisible(false);
		testOtherCredentialButton.setVisible(false);
		testRepouserNameLabel.setVisible(false);
		testRepoUsernameText.setVisible(false);
		testRepopasswordLabel.setVisible(false);
		testRepoPasswordText.setVisible(false);
		testRepoLblHeadRevision.setVisible(false);
		repoUrlHeadRevisionButton.setVisible(false);
		testRepoRevisionText.setVisible(false);
		
		// To handle based on SCM type selection
		gitRadio.addSelectionListener(new SelectionAdapter() {
		    @Override
		    public void widgetSelected(SelectionEvent e) {
		        // Handle the selection event
		    	boolean selection = gitRadio.getSelection();
		    	if (selection) {
		    		lblHeadRevision.setVisible(false);
		    		headRevisionButton.setVisible(false);
		    		revisionLabel.setVisible(false);
		    		revisionText.setVisible(false);
		    		lblTestCheckout.setVisible(false);
		    		testCheckOutButton.setVisible(false);
		    		lblOtherCredentials.setVisible(true);
		    		otherCredentialButton.setVisible(true);
		    		testRepoUrlLabel.setVisible(false);
		    		testRepoUrlText.setVisible(false);
		    		otherCredentialLbl.setVisible(false);
		    		testOtherCredentialButton.setVisible(false);
		    		testRepouserNameLabel.setVisible(false);
		    		testRepoUsernameText.setVisible(false);
		    		testRepopasswordLabel.setVisible(false);
		    		testRepoPasswordText.setVisible(false);
		    		testRepoLblHeadRevision.setVisible(false);
		    		repoUrlHeadRevisionButton.setVisible(false);
		    		testRepoRevisionText.setVisible(false);
		    	} else {
		    		lblHeadRevision.setVisible(true);
		    		headRevisionButton.setVisible(true);
		    		revisionLabel.setVisible(true);
		    		revisionText.setVisible(true);
		    		lblTestCheckout.setVisible(true);
		    		testCheckOutButton.setVisible(true);
		    		lblOtherCredentials.setVisible(true);
		    		otherCredentialButton.setVisible(true);
		    		if(testCheckOutButton.getSelection()) {
		    			testRepoUrlLabel.setVisible(true);
						testRepoUrlText.setVisible(true);
						otherCredentialLbl.setVisible(true);
						testOtherCredentialButton.setVisible(true);
						testRepouserNameLabel.setVisible(true);
						testRepoUsernameText.setVisible(true);
						testRepopasswordLabel.setVisible(true);
						testRepoPasswordText.setVisible(true);
						testRepoLblHeadRevision.setVisible(true);
						repoUrlHeadRevisionButton.setVisible(true);
						testRepoRevisionText.setVisible(true);
		    		}
		    	}
		    }
		});
		
		//To enable/disable the revision option
		headRevisionButton.addSelectionListener(new SelectionAdapter() {
		    @Override
		    public void widgetSelected(SelectionEvent e) {
		        // Handle the selection event
				boolean selection = !headRevisionButton.getSelection();
				revisionText.setEnabled(selection);
		    }
		}); 
		
		repoUrlHeadRevisionButton.addSelectionListener(new SelectionAdapter() {
		    @Override
		    public void widgetSelected(SelectionEvent e) {
		        // Handle the selection event
				boolean selection = !repoUrlHeadRevisionButton.getSelection();
				testRepoRevisionText.setEnabled(selection);
		    }
		}); 
		
		otherCredentialButton.addSelectionListener(new SelectionAdapter() {
		    @Override
		    public void widgetSelected(SelectionEvent e) {
		        // Handle the selection event
				boolean selection = otherCredentialButton.getSelection();
				userName.setEnabled(selection);
				password.setEnabled(selection);
				if (selection) {
					userName.setText(STR_EMPTY);
					password.setText(STR_EMPTY);
				} else {
					userName.setText(DEFAULT_USER_NAME);
					password.setText(DEFAULT_PASSWORD);
				}
		    }
		}); 
		
		testOtherCredentialButton.addSelectionListener(new SelectionAdapter() {
		    @Override
		    public void widgetSelected(SelectionEvent e) {
		        // Handle the selection event
				boolean selection = testOtherCredentialButton.getSelection();
				testRepoUsernameText.setEnabled(selection);
				testRepoPasswordText.setEnabled(selection);
				if (selection) {
					testRepoUsernameText.setText(STR_EMPTY);
					testRepoPasswordText.setText(STR_EMPTY);
				} else {
					testRepoUsernameText.setText(DEFAULT_USER_NAME);
					testRepoPasswordText.setText(DEFAULT_PASSWORD);
				}
		    }
		}); 
		
	}
}
