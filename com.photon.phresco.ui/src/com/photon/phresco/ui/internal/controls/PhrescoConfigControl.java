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

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

/**
 * Control to handle the phresco configurations.
 * 
 * @author arunachalam.lakshmanan@photoninfotech.net
 *
 */
public class PhrescoConfigControl extends Composite {
	
	private Table table;
	private IPath configPath;

	public PhrescoConfigControl(Composite parent, int style, IPath configFilePath) {
		super(parent, style);
//		Composite configParent = new Composite(parent, SWT.NONE);
		
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
//		try {
//			ConfigReader reader = new ConfigReader(configPath.toFile());
//			comboViewer.setInput(reader.getEnvironments().toArray());
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		envManageBtn.setLayoutData(new GridData(SWT.DEFAULT, SWT.CENTER, false, false, 1, 1));
		
		
		Composite ConfigComposite = new Composite(this, SWT.NONE);
		ConfigComposite.setLayout(new GridLayout(1, false));
		ConfigComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		
		CheckboxTableViewer checkboxTableViewer = CheckboxTableViewer.newCheckList(ConfigComposite, SWT.BORDER | SWT.FULL_SELECTION);
		table = checkboxTableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		checkboxTableViewer.setColumnProperties(new String[]{"Name"});

	}

}
