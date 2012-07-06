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

package com.photon.phresco.ui.preferences;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import com.photon.phresco.ui.Activator;


/**
 * Phresco primary preference page
 * 
 * @author arunachalam.lakshmanan@photoninfotech.net
 */
public class PhrescoPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	private Text userTxt;
	private Text pwdTxt;
	private Button testBtn;
	
	private String username, password;

	public PhrescoPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(Messages.PhrescoPreferencePage_description);
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		Composite serviceComposite = new Composite(getFieldEditorParent(), NONE);
		GridLayout layout = new GridLayout(1,false);
		serviceComposite.setLayout(layout);
		StringFieldEditor serviceURL = new StringFieldEditor(PreferenceConstants.SERVICE_URL, Messages.PhrescoPreferencePage_service_url, serviceComposite);
		addField(serviceURL);
		serviceURL.setEnabled(false, serviceComposite);
		new Label(serviceComposite, SWT.None).setText(Messages.PhrescoPreferencePage_username_label);
		userTxt = new Text(serviceComposite, SWT.BORDER);
		userTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		userTxt.setText(username);
		new Label(serviceComposite, SWT.None).setText(Messages.PhrescoPreferencePage_password_label);
		pwdTxt = new Text(serviceComposite, SWT.BORDER|SWT.PASSWORD);
		pwdTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		pwdTxt.setText(password);
		new Label(serviceComposite, SWT.None).setText("");
		testBtn = new Button(serviceComposite, SWT.PUSH);
		testBtn.setText(Messages.PhrescoPreferencePage_testbutton_name);
		testBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		serviceComposite.setLayoutData(data);
		StringFieldEditor jenkinsURL = new StringFieldEditor(PreferenceConstants.JENKINS_URL, Messages.PhrescoPreferencePage_ci_url, getFieldEditorParent());
		addField(jenkinsURL);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		ISecurePreferences pref = SecurePreferencesFactory.getDefault();
		ISecurePreferences phresco = pref.node(PreferenceConstants.CREDENTIAL_STORE_NAME);
		try {
			username = phresco.get(PreferenceConstants.USER_NAME, "");
			password = phresco.get(PreferenceConstants.PASSWORD, "");
		} catch (StorageException e) {
			//problem in reading secure storage
			username = password = "";
		}
		
	}
	
	@Override
	protected void performApply() {
		super.performApply();
		//store user name and password.
		storeCredentials();	
	}
	
	@Override
	public boolean performOk() {
		//store user name and password.
		storeCredentials();		
		return super.performOk();
	}

	private void storeCredentials() {
		ISecurePreferences pref = SecurePreferencesFactory.getDefault();
		ISecurePreferences phresco = pref.node(PreferenceConstants.CREDENTIAL_STORE_NAME);
		try {
			phresco.put(PreferenceConstants.USER_NAME, userTxt.getText(), true);
			phresco.put(PreferenceConstants.PASSWORD, pwdTxt.getText(), true);
		} catch (StorageException e) {
		}
	}
	
}