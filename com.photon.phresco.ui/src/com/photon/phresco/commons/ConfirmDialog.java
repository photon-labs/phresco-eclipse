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

package com.photon.phresco.commons;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.internal.OpenPreferencesAction;

import com.photon.phresco.ui.resource.Messages;

/**
 * confirm dialog to login
 * @author syed
 *
 */

public class ConfirmDialog implements PhrescoConstants {

	static ConfirmDialog confirmDialog = null;
	
	public ConfirmDialog() {		
		confirmDialog = this;
	}
	
	public static ConfirmDialog getConfirmDialog() {
		if (confirmDialog == null) {
			new ConfirmDialog();
		}
		return confirmDialog;
	}
	
	public void showConfirm(Shell s) {
		MessageBox messageBox = new MessageBox(s, SWT.ICON_QUESTION
	            | SWT.YES | SWT.NO);
			messageBox.setText(Messages.LOGIN_CONFIRM_DIALOG_TITLE);
	        messageBox.setMessage(Messages.LOGIN_CONFIRM_DIALOG_MSG);
	    int response = messageBox.open();
	    
	    if (response==SWT.YES) {
            PreferenceManager pm = PlatformUI.getWorkbench( ).getPreferenceManager();
            IPreferenceNode[] rootSubNodes = pm.getRootSubNodes();
            for (int i = 0; i < rootSubNodes.length; i++) {
            	IPreferenceNode iPreferenceNode = rootSubNodes[i];
            	if (PHRESCO_PREFERENCE_ID.equals(iPreferenceNode.getId())) {
            		PreferenceAction t = new PreferenceAction(s, PHRESCO_PREFERENCE_ID);
            		t.run();
            	}
			}
	    }
	}
}

class PreferenceAction extends OpenPreferencesAction {
	Shell shell = null;
	String PHRESCO_PREFERENCE_ID;
	
	PreferenceAction(Shell s, String id) {
		this.shell = s;
		this.PHRESCO_PREFERENCE_ID = id;
	}
	@Override
	public void run() {
		String displayLoginPage[] = new String[1];
		displayLoginPage[0] = PhrescoConstants.PHRESCO_LOGIN_PREFERENCE_ID;
		PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(shell, PHRESCO_PREFERENCE_ID, displayLoginPage, null);
		dialog.open();
		
	}
}
