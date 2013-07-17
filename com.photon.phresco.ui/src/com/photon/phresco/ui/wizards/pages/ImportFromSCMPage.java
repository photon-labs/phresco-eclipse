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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.photon.phresco.commons.PhrescoConstants;

/**
 * Page to handle phresco import from scm
 * @author syed
 *
 */
public class ImportFromSCMPage extends WizardPage implements PhrescoConstants {

	private Text repoURLText;
    private Text userName;
    private Text password;
    
	public ImportFromSCMPage(String pageName, String message) {
		super(pageName);
		setMessage(message);
	}

	@Override
	public void createControl(Composite parent) {
        Composite composite = new Composite(parent, 0);
        setControl(composite);
        GridLayout layout = new GridLayout(2, false);
        layout.marginLeft = 25;
        layout.horizontalSpacing = 5;
        layout.verticalSpacing = 10;
        composite.setLayout(layout);
        
		Label lblScmUrl = new Label(composite, SWT.NONE);
		lblScmUrl.setText("Type");
		Label empty = new Label(composite, SWT.NONE);
		
		Button gitRadio = new Button(composite, SWT.RADIO);
		gitRadio.setText("Git");
		gitRadio.setLayoutData(new GridData(50,13));
		
		Button svnRadio = new Button(composite, SWT.RADIO);
		svnRadio.setText("SVN");
		svnRadio.setLayoutData(new GridData(50,13));
		
		Label repoURL = new Label(composite, SWT.NONE);
		repoURL.setText("Repo URL");
		repoURLText = new Text(composite, SWT.BORDER);
		repoURLText.setLayoutData(new GridData(230,15));
		
        Label userNameLabel = new Label(composite, SWT.LEFT);
        userNameLabel.setText("Username");
        userName = new Text(composite, SWT.BORDER);
        userName.setLayoutData(new GridData(160,15));

        Label passwordLabel = new Label(composite, SWT.LEFT);
        passwordLabel.setText(PASSWORD);
        password = new Text(composite, SWT.BORDER);
        password.setEchoChar(CHAR_ASTERISK);
        password.setLayoutData(new GridData(160,15));
        
		Label lblHeadRevision = new Label(composite, SWT.NONE);
		lblHeadRevision.setText("Head Revision");
		final Button headRevisionButton = new Button(composite, SWT.CHECK);
		headRevisionButton.setLayoutData(new GridData(15,13));
		
		final Label revisionLabel = new Label(composite, SWT.NONE);
		revisionLabel.setText("Revision");
		final Text revisionText = new Text(composite, SWT.BORDER);
		revisionText.setLayoutData(new GridData(160,13));
		
		
		headRevisionButton.addSelectionListener(new SelectionAdapter() {
		    @Override
		    public void widgetSelected(SelectionEvent e) {
		        // Handle the selection event
				boolean selection = !headRevisionButton.getSelection();
				revisionText.setEnabled(selection);
		    }
		}); 
		
		Label lblTestCheckout = new Label(composite, SWT.NONE);
		lblTestCheckout.setText("Test Checkout");
		Button btnCheckButton = new Button(composite, SWT.CHECK);
		btnCheckButton.setLayoutData(new GridData(15,13));
	}
}
