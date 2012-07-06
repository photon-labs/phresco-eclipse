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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.photon.phresco.ui.wizards.PhrescoProjectWizard;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

/**
 * App info page
 * 
 * @author arunachalam.lakshmanan@photoninfotech.net
 *
 */
public class AppInfoPage extends WizardPage implements IWizardPage {

	/**
	 * @wbp.parser.constructor
	 */
	public AppInfoPage(String pageName) {
		super(pageName);
	}

	public AppInfoPage(String pageName, PhrescoProjectWizard phrescoProjectWizard) {
		super(pageName);
	}

	@Override
	public void createControl(Composite parent) {
		Composite parentComposite = new Composite(parent, SWT.NULL);
		parentComposite.setLayout(new GridLayout(2, false));
		parentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label projectName = new Label(parentComposite, SWT.NONE);
		projectName.setText("&Project name");
		
		Text projectTxt = new Text(parentComposite,SWT.BORDER);
		projectTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		new Label(parentComposite, SWT.NONE);
		
		Composite composite = new Composite(parentComposite, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		composite.setLayout(new GridLayout(3, false));
		
		Button btnWeb = new Button(composite, SWT.RADIO);
		btnWeb.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MessageDialog.openInformation(getShell(), "Alert", "Web Application radio selected");
			}
		});
		btnWeb.setText("Web Application");
		
		Button btnMobileApp = new Button(composite, SWT.RADIO);
		btnMobileApp.setBounds(0, 0, 91, 18);
		btnMobileApp.setText("Mobile Application");
		
		Button btnWebServices = new Button(composite, SWT.RADIO);
		btnWebServices.setBounds(0, 0, 91, 18);
		btnWebServices.setText("Web Services");
		setControl(parentComposite);
	}
	
	@Override
	public boolean canFlipToNextPage() {
		return false;
	}
}