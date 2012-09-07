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
package com.photon.phresco.ui.internal.controls;

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.framework.PhrescoFrameworkFactory;
import com.photon.phresco.framework.api.Project;
import com.photon.phresco.framework.api.ProjectAdministrator;
import com.photon.phresco.model.PropertyInfo;
import com.photon.phresco.model.SettingsInfo;
import com.photon.phresco.ui.dialog.ConfigDialog;
import com.photon.phresco.ui.dialog.ManageEnvironmentsDialog;

/**
 * Control to handle the phresco configurations.
 * 
 * @author arunachalam.lakshmanan@photoninfotech.net
 *
 */
public class PhrescoConfigControl extends Composite {
	
	/**
	 * Configuration table
	 */
	private Table table;
	
	/**
	 * Config file path
	 */
	private IPath configPath;
	
    private List<PropertyInfo> propertyInfoList;
    
    private List<SettingsInfo> settingsInfoList;
    
    private String projectCode;

	public PhrescoConfigControl(Composite parent, int style, IPath configFilePath,String projectCode) {
		super(parent, style);

		GridLayout layout = new GridLayout(1, false);
		setLayout(layout);
		
		GridData data = new GridData(GridData.FILL_BOTH);
		setLayoutData(data);
		
		Composite envComposite = new Composite(this, SWT.NONE);
		envComposite.setLayout(new GridLayout(3, false));
		envComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblEnviroments = new Label(envComposite, SWT.NONE);
		lblEnviroments.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblEnviroments.setText("Enviroments");
		
		configPath = configFilePath;
		ComboViewer comboViewer = new ComboViewer(envComposite, SWT.NONE|SWT.READ_ONLY);
		Combo combo = comboViewer.getCombo();
		combo.setLayoutData(new GridData(SWT.DEFAULT, SWT.CENTER, false, false, 1, 1));
		comboViewer.setContentProvider(new ArrayContentProvider());
		comboViewer.setLabelProvider(new LabelProvider());
		comboViewer.setInput(new String[]{configPath.toOSString(),"production"});
		
		Button envManageBtn = new Button(envComposite, SWT.PUSH);
		envManageBtn.setText("Manage Environments");
		envManageBtn.setLayoutData(new GridData(SWT.DEFAULT, SWT.CENTER, false, false, 1, 1));
		
		final ManageEnvironmentsDialog environmentsDialog = new ManageEnvironmentsDialog(null, projectCode);
		envManageBtn.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				environmentsDialog.create();
				environmentsDialog.open();
			}
		});
		
		Button addConfigBtn = new Button(envComposite, SWT.PUSH);
		addConfigBtn.setText("Add...");
		
		final Composite ConfigComposite = new Composite(this, SWT.NONE);
		ConfigComposite.setLayout(new GridLayout(1, false));
		ConfigComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		
		final CheckboxTableViewer checkboxTableViewer = CheckboxTableViewer.newCheckList(ConfigComposite, SWT.BORDER | SWT.FULL_SELECTION);
		table = checkboxTableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TableColumn tblNameColumn = new TableColumn(table, SWT.NONE);
		tblNameColumn.setWidth(100);
		tblNameColumn.setText("Name");
		
		TableColumn tblValueColumn = new TableColumn(table, SWT.NONE);
		tblValueColumn.setWidth(100);
		tblValueColumn.setText("Description");
		
		TableColumn tblDescColumn = new TableColumn(table, SWT.NONE);
		tblDescColumn.setWidth(200);
		tblDescColumn.setText("Environment");
		
		TableColumn tblStatusColumn = new TableColumn(table, SWT.NONE);
		tblStatusColumn.setWidth(100);
		tblStatusColumn.setText("Status");
		
		settingsInfoList = getConfigValues(projectCode);
		checkboxTableViewer.setContentProvider(new ArrayContentProvider());
		checkboxTableViewer.setLabelProvider(new ITableLabelProvider() {
			
			@Override
			public void removeListener(ILabelProviderListener listener) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean isLabelProperty(Object element, String property) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void dispose() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void addListener(ILabelProviderListener listener) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public String getColumnText(Object element, int columnIndex) {
				SettingsInfo settingsInfo = (SettingsInfo) element;
				switch (columnIndex){
				case 0:
					return settingsInfo.getName();
				case 1:
					return settingsInfo.getDescription();
				case 2:
					return settingsInfo.getType() +" ["+ settingsInfo.getEnvName()+"]";
				
				}
				return "";
			}
			@Override
			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}
		});
			
		checkboxTableViewer.setInput(settingsInfoList);
		
		final ConfigDialog dialog = new ConfigDialog(null,projectCode);
		
		addConfigBtn.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				dialog.create();
				if(dialog.open() == Window.OK) {
//					dialog.addSave();
					checkboxTableViewer.add(dialog.getSettingsInfo());
				}
			}
		});

	}
	
	private List<SettingsInfo> getConfigValues(String projectCode) {
		try {
			ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();		
			Project project = administrator.getProject(projectCode);
			List<SettingsInfo> configurations = administrator.configurations(project);
			return configurations;
		} catch (PhrescoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
