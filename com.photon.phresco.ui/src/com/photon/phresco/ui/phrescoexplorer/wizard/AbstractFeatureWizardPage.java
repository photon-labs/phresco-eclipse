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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.model.ArtifactGroup;
import com.photon.phresco.commons.model.ArtifactInfo;

/**
 * Abstract class to handle feature page
 * @author syed
 *
 */

public abstract class AbstractFeatureWizardPage extends WizardPage implements PhrescoConstants {

	private Map<ArtifactGroup, String> selectedArtifactGroupWithComboVersion = new HashMap<ArtifactGroup, String>();
	private Map<ArtifactGroup, String> selectedArtifactGroup = new HashMap<ArtifactGroup, String>();
	
	protected AbstractFeatureWizardPage(String pageName, String title,
			ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		// TODO Auto-generated constructor stub
	}
	
	public Table getFeatureTable(final Composite composite, String featureName, List<ArtifactGroup> features) {
		
		Group jsLibGroups = new Group(composite, SWT.SHADOW_ETCHED_IN);
		jsLibGroups.setText(featureName);

		Table table = renderFeatureTable(jsLibGroups, features);
		
		jsLibGroups.setBounds(0, 5, 250, 240);
	    jsLibGroups.pack();
		
	    return table;
	    
		/*final Table jsLibTable = new Table(jsLibGroups, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
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
        
        for (ArtifactGroup artifactGroup : features) {
        	TableItem item = new TableItem(jsLibTable, SWT.NONE);
        	
        	item.setData(artifactGroup);
        	
        	item.setText(0, artifactGroup.getDisplayName());
        	String description = artifactGroup.getDescription();
        	if (description == null) {
        		description = "";
        	}
        	
        	item.setText(1, description);
        	
        	// Versions should be in combo
            ArtifactInfo artifactInfo = artifactGroup.getVersions().get(0);
            item.setText(2, artifactInfo.getVersion());
            
            TableEditor editor = new TableEditor(jsLibTable);
            final Combo combo = new Combo(jsLibTable, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
            
            List<ArtifactInfo> versions = artifactGroup.getVersions();
            for (ArtifactInfo artifactInfo : versions) {
            	combo.add(artifactInfo.getVersion()); 
			}
            
            combo.pack();
            
            editor.setEditor(combo, item, 2);
		}
        
        jsLibTable.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
            	String string = event.detail == SWT.CHECK ? "Checked" : "Selected";
            	
            	System.out.println(event.item + " " + string);
            	
            	if ("Checked".equals(string)) {
            		System.out.println("checked");
            	} else {
            		System.out.println("unchecked");
            	}
            	
            	TableItem tItem = (TableItem) event.item;
            	if ("Checked".equals(string) && !selectedTableItem.contains(tItem)) {
            		selectedTableItem.add(tItem);
            		tItem.setChecked(true);
            	} else if ("Selected".equals(string) ) {
            		tItem.setChecked(false);
            		if (selectedTableItem.contains(tItem)) {
            			selectedTableItem.remove(tItem);
            		}
            	}
            }
        });
        
        SelectionListener listener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	if (e.widget == jsLibTable) {
            		
            		TableColumn[] columns = jsLibTable.getColumns();
            		TableColumn ta = null;
            		TableItem[] selection = jsLibTable.getSelection();
            		jsLibTable.setSelection(selection);
            		
            		
            		TableItem ti = (TableItem) e.item;
            		ti.setChecked(true);
            	}
            	
            	System.out.println(" e.item : " + e.item);
            }
        };
        
        jsLibTable.addSelectionListener(listener);
        
        jsLibGroups.setLocation(0, 95);
        jsLibGroups.pack();
        
        return null;*/
	}
	
	private Table renderFeatureTable(Group jsLibGroups, List<ArtifactGroup> features) {
		Table table = new Table(jsLibGroups, SWT.BORDER | SWT.MULTI);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData());
		
		table.setBounds(12, 20, 450, 175);
		
        TableColumn checkBoxColumn = new TableColumn(table, SWT.LEFT, 0);
        checkBoxColumn.setText("");
        checkBoxColumn.setWidth(25);
        
	    TableColumn nameColumn = new TableColumn(table, SWT.LEFT, 1);
        nameColumn.setText("Name");
        nameColumn.setWidth(140);
        
        TableColumn descColumn = new TableColumn(table, SWT.LEFT, 2);
        descColumn.setText("Description");
        descColumn.setWidth(160);
        
        TableColumn versionColumn = new TableColumn(table, SWT.LEFT, 3);
        versionColumn.setText("Version");
        versionColumn.setWidth(120);
        
        for (int i = 0; i < features.size(); i++) {
            new TableItem(table, SWT.NONE);
        }
        
	    TableItem[] items = table.getItems();
	    
	    int i = 0;
	    
	    for (final ArtifactGroup artifactGroup : features) {
	    	
	    	final TableItem tableItem = items[i];
	    	TableEditor editor = new TableEditor(table);
			final Button checkButton = new Button(table, SWT.CHECK);
			checkButton.pack();
			editor.minimumWidth = checkButton.getSize().x;
			editor.horizontalAlignment = SWT.LEFT;
			editor.setEditor(checkButton, tableItem, 0);
			  
			editor = new TableEditor(table);
			Text text = new Text(table, SWT.NONE);
			text.setText(artifactGroup.getDisplayName());
			text.setToolTipText(artifactGroup.getDisplayName());
			editor.grabHorizontal = true;
			editor.setEditor(text, tableItem, 1);
			  
			editor = new TableEditor(table);
			text = new Text(table, SWT.NONE);
			if (artifactGroup.getDescription() != null) {
				text.setText(artifactGroup.getDescription());
				text.setToolTipText(artifactGroup.getDescription());
			} else {
				text.setText("");
			}
			
			editor.grabHorizontal = true;
			editor.setEditor(text, tableItem, 2);

			editor = new TableEditor(table);
	        List<ArtifactInfo> versions = artifactGroup.getVersions();
	        
	        final CCombo combo = new CCombo(table, SWT.NONE);
	        final Text versionText = new Text(table, SWT.NONE);
	        
	        boolean isFirst = true;
	        if (versions.size() > 1) {
		        for (ArtifactInfo artifactInfo : versions) {
		        	combo.add(artifactInfo.getVersion()); 
		        	
		        	if (isFirst) {
		        		combo.setItem(0, artifactInfo.getVersion()); 
		        		isFirst = false;
		        	}
				}
				editor.grabHorizontal = true;
				editor.setEditor(combo, tableItem, 3);
	        } else {
	        	versionText.setText(versions.get(0).getVersion());
	        	versionText.setToolTipText(versions.get(0).getVersion());
				editor.grabHorizontal = true;
				editor.setEditor(versionText, tableItem, 3);
	        }
	        
			combo.addSelectionListener(new SelectionAdapter() {
			    @Override
			    public void widgetSelected(SelectionEvent e) {
			    	String selectedComboVersion = combo.getItem(combo.getSelectionIndex());
			        if (checkButton.getSelection()) {
			        	selectedArtifactGroupWithComboVersion.put(artifactGroup, selectedComboVersion);
			        } else if (selectedArtifactGroupWithComboVersion.containsKey(artifactGroup)) {
			        	selectedArtifactGroupWithComboVersion.remove(artifactGroup);
			        }
			    }
			});
	        
			checkButton.addSelectionListener(new SelectionAdapter() {
			    @Override
			    public void widgetSelected(SelectionEvent e) {
			    	String selectedVersion = versionText.getText();
			    	
			    	if (selectedVersion.equals("")) {
			    		selectedVersion = KEY_EMPTY;
			    	}
			    	
			        if (checkButton.getSelection()) {
			            selectedArtifactGroup.put(artifactGroup, selectedVersion);
			        } else if (selectedArtifactGroup.containsKey(artifactGroup)) {
            			selectedArtifactGroup.remove(artifactGroup);
			        }
			    }
			});
			  
	    	i++;
	    }
	    
	    return table;
	}
	
	public abstract void renderPage();
	
	public Map<ArtifactGroup, String> getSelectedCheckBoxRows() {
		return selectedArtifactGroup;
	}
	
	public Map<ArtifactGroup, String> getSelectedComboBoxRows() {
		return selectedArtifactGroupWithComboVersion;
	}
}
