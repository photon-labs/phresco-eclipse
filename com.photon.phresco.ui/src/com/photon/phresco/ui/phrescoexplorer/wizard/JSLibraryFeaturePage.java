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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import com.photon.phresco.commons.model.ArtifactGroup;

/**
 * Page to render js libraries
 * @author syed
 *
 */

public class JSLibraryFeaturePage extends AbstractFeatureWizardPage {
	public static final String PAGE_NAME = "JS Libraries";
	private Table jsLibTable;
	private List<ArtifactGroup> features = null;
	private boolean isFirstPage;
	private boolean isRendered;
	
	public JSLibraryFeaturePage(List<ArtifactGroup> features, boolean isFirstPage) {
		super(PAGE_NAME, PAGE_NAME, null);
		this.features = features;
		this.isFirstPage = isFirstPage;
	}

	public void createControl(Composite parent) {
		
        GridLayout layout = new GridLayout(2, false);
        layout.marginLeft = 5;
        layout.marginTop = 5;
        
		Composite topLevel = new Composite(parent, SWT.NONE);
		topLevel.setLayout(layout);
		setControl(topLevel);
		
		if (isFirstPage) {
			renderPage();
			isRendered = true;
		}
		
	}
	
	@Override
	public void renderPage() {
		
		if (isRendered) {
			return;
		}
		
		final Composite parentComposite = (Composite) getControl();
		
		final ScrolledComposite scrolledComposite = new ScrolledComposite(parentComposite, SWT.V_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		scrolledComposite.setAlwaysShowScrollBars(false);
		scrolledComposite.setBounds(5, 5, 500, 350);
		
		jsLibTable = getFeatureTable(scrolledComposite, PAGE_NAME, features);
	}
	
	public void getSelectedItems() {
		System.out.println(" getSelectedCheckBoxRows : " + getSelectedCheckBoxRows());
		Map<ArtifactGroup, String> selectedComboBoxRows = getSelectedComboBoxRows();
		Map<ArtifactGroup, String> selectedCheckBoxRows = getSelectedCheckBoxRows();
		
		Iterator entries = selectedCheckBoxRows.entrySet().iterator();
		while (entries.hasNext()) {
		    Map.Entry entry = (Map.Entry) entries.next();
		    ArtifactGroup key = (ArtifactGroup)entry.getKey();
		    
		    String value = (String)entry.getValue();
		    System.out.println("Key = " + key.getDisplayName() + " = " + value);
		    
		    if (value != null && KEY_EMPTY.equals(value)) {
		    	String comboValue = selectedComboBoxRows.get(key);
		    	System.out.println(" comboValue :" + comboValue);
		    }
		}
		
	}
}
