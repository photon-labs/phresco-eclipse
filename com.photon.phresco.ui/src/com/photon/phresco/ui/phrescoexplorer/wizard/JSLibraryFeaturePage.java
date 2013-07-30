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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * Page to render js libraries
 * @author syed
 *
 */

public class JSLibraryFeaturePage extends AbstractFeatureWizardPage {
	public static final String PAGE_NAME = "JS Library";
	Table jsLibTable;
	
	public JSLibraryFeaturePage() {
		super(PAGE_NAME, "JS Library", null);
	}

	public void createControl(Composite parent) {
		
        GridLayout layout = new GridLayout(2, false);
        layout.marginLeft = 5;
        layout.marginTop = 5;
        
		Composite topLevel = new Composite(parent, SWT.NONE);
		topLevel.setLayout(layout);
		setControl(topLevel);
		
	}

	public boolean useDefaultDirectory() {
		return false; 
	}
	
	public void getSelectedJSLib() {
		TableItem[] selection = jsLibTable.getSelection();
		System.out.println(" selection : " + selection);
		
		for (int i = 0; i < selection.length; i++) {
			TableItem tableItem = selection[i];
			String id = (String) tableItem.getData();
			System.out.println(" selected id : " + id);
		}
	}

	@Override
	public void renderPage() {
		final Composite parentComposite = (Composite) getControl();
		
		final ScrolledComposite scrolledComposite = new ScrolledComposite(parentComposite, SWT.V_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		scrolledComposite.setAlwaysShowScrollBars(false);
		
		scrolledComposite.setLocation(5, 5);
		scrolledComposite.setBounds(5, 5, 500, 200);
		
		jsLibTable = getFeatureTable(scrolledComposite, PAGE_NAME);
	}
	
}
