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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * Abstract class to handle feature page
 * @author syed
 *
 */

public abstract class AbstractFeatureWizardPage extends WizardPage {

	
	protected AbstractFeatureWizardPage(String pageName, String title,
			ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		// TODO Auto-generated constructor stub
	}
	
	
	public Table getFeatureTable(final Composite composite, String featureName) {
		
		Group jsLibGroups = new Group(composite, SWT.SHADOW_ETCHED_IN);
		jsLibGroups.setText(featureName);
		jsLibGroups.setLocation(0, 95);
		
		final Table jsLibTable = new Table(jsLibGroups, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		jsLibTable.setLinesVisible(true);
		jsLibTable.setHeaderVisible(true);
		jsLibTable.setLayoutData(new GridData());
		
		jsLibTable.setBounds(12, 20, 455, 150);

        TableColumn nameColumn = new TableColumn(jsLibTable, SWT.LEFT, 0);
        nameColumn.setText("Name");
        nameColumn.setWidth(160);
        
        TableColumn descColumn = new TableColumn(jsLibTable, SWT.LEFT, 1);
        descColumn.setText("Description");
        descColumn.setWidth(160);
        
        TableColumn versionColumn = new TableColumn(jsLibTable, SWT.LEFT, 2);
        versionColumn.setText("Version");
        versionColumn.setWidth(160);
        
        for (int i = 0; i < 5; i++) {
            TableItem item = new TableItem(jsLibTable, SWT.NONE);
            item.setData("id "+i);
            item.setText(0, "abc name"+i);
            item.setText(1, "abc desc"+i);
            item.setText(1, "abc version"+i);
        }
        
        SelectionListener listener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	if (e.widget == jsLibTable) {
            		TableItem[] selection = jsLibTable.getSelection();
            		jsLibTable.setSelection(selection);
            	}
            }
        };
        
        jsLibTable.addSelectionListener(listener);
        
        jsLibGroups.setLocation(0, 95);
        jsLibGroups.pack();
        
        return jsLibTable;
	}
	
	public abstract void renderPage();
}
