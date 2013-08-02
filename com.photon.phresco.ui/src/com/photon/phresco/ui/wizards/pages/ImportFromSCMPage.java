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
    
	public ImportFromSCMPage(String pageName, String message) {
		super(pageName);
		setMessage(message);
	}

	@Override
	public void createControl(Composite parent) {
		
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
        userName.setText(ADMIN_USER);
        userName.setLayoutData(new GridData(160,15));

        Label passwordLabel = new Label(composite, SWT.LEFT);
        passwordLabel.setText(Messages.USER_PWD);
        password = new Text(composite, SWT.BORDER);
        password.setEchoChar(CHAR_ASTERISK);
        password.setText(ADMIN_PWD);
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
		final Button testCheckOutButton = new Button(composite, SWT.CHECK);
		testCheckOutButton.setLayoutData(new GridData(15,13));
		
		// To hide for default selection scm type Git
		
		lblHeadRevision.setVisible(false);
		headRevisionButton.setVisible(false);
		revisionLabel.setVisible(false);
		revisionText.setVisible(false);
		lblTestCheckout.setVisible(false);
		testCheckOutButton.setVisible(false);
		lblOtherCredentials.setVisible(false);
		otherCredentialButton.setVisible(false);
		userName.setEnabled(false);
		password.setEnabled(false);
		
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
		    		lblOtherCredentials.setVisible(false);
		    		otherCredentialButton.setVisible(false);
		    	} else {
		    		lblHeadRevision.setVisible(true);
		    		headRevisionButton.setVisible(true);
		    		revisionLabel.setVisible(true);
		    		revisionText.setVisible(true);
		    		lblTestCheckout.setVisible(true);
		    		testCheckOutButton.setVisible(true);
		    		lblOtherCredentials.setVisible(true);
		    		otherCredentialButton.setVisible(true);
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
					userName.setText(ADMIN_USER);
					password.setText(ADMIN_PWD);
				}
		    }
		}); 
		
	}
	
}
