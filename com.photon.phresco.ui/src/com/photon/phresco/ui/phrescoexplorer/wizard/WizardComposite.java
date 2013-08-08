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

package com.photon.phresco.ui.phrescoexplorer.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Class to create the wizard page to render features
 * @author syed_ah
 *
 */
public class WizardComposite extends Composite {
	Composite parent;
	public WizardComposite(Composite parent) {
		super(parent, SWT.NONE);
	}

	public WizardDialog getWizardControl(Wizard wizard) {
		parent = this;
		FillLayout layout = new FillLayout();
		parent.setLayout(layout);
    
		WizardDialog dialog = new WizardDialog(parent.getShell(), wizard);
        return dialog;
	}
}

