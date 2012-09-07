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

import java.util.List;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.photon.phresco.model.ModuleGroup;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;

/**
 * @author arunachalam.lakshmanan@photoninfotech.net
 *
 */
public class CoreModuleFeaturesPage extends WizardPage implements IWizardPage {
	
	private String tech;
	private String pilotProject;
	private Text pilotProjectTxt;
	private List<ModuleGroup> features;

	
	public ScrolledComposite scrolledCompositeCoreModules;
	public Group coreModuleComposite;
	
	
	public CoreModuleFeaturesPage(String pageName) {
		super(pageName);
		setTitle("{Core Phresco}");
		setDescription("Project Creation Page");
	}


	public String getTech() {
		return tech;
	}

	public void setTech(String tech) {
		this.tech = tech;
	}
	
	public String getPilotProject() {
		return pilotProject;
	}

	public void setPilotProject(String pilotProject) {
		this.pilotProject = pilotProject;
	}

	@Override
	public void createControl(Composite parent) {
		final Composite parentComposite = new Composite(parent, SWT.NULL);
		parentComposite.setLayout(new GridLayout(1,false));
		parentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite pilotProjectComposite = new Composite(parentComposite, SWT.NULL);
		pilotProjectComposite.setLayout(new GridLayout(2,false));
		pilotProjectComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label pilotProjectlbl = new Label(pilotProjectComposite, SWT.NONE);
		pilotProjectlbl.setText("selected pilot Project");

		pilotProjectTxt = new Text(pilotProjectComposite, SWT.NONE);
		pilotProjectTxt.setLayoutData(new GridData(GridData.FILL_BOTH));
		pilotProjectTxt.setBackground(new Color(null, 255, 255, 255));
		pilotProjectTxt.setEditable(false);
		
		scrolledCompositeCoreModules = new ScrolledComposite(parentComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledCompositeCoreModules.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		setControl(parentComposite);
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if(visible) {
			populateCoreModules();
		}
	}
	public void populateCoreModules() {
		if(features == null) {
			scrolledCompositeCoreModules.setVisible(false);
			return;
		}
			for (ModuleGroup moduleGroup : features) {
				System.out.println(moduleGroup.getName());
			}
			coreModuleComposite = new Group(scrolledCompositeCoreModules, SWT.BAR);
			coreModuleComposite.setLayout(new GridLayout(1,false));
			coreModuleComposite.setBackground(new Color(null, 255, 255, 255));
			coreModuleComposite.setText("CoreModules");
			scrolledCompositeCoreModules.setVisible(true);
			List<ModuleGroup> coreModule = features;
			int size = 5;
			for (ModuleGroup moduleGroup : coreModule) {
				Button featureButton = new Button(coreModuleComposite, SWT.CHECK);
				featureButton.setText(moduleGroup.getName());
				featureButton.setBackground(new Color(null, 255, 255, 255));
				size = size +21;
			}
			final int vertical_scroll_size = size;
			scrolledCompositeCoreModules.setContent(coreModuleComposite);
			scrolledCompositeCoreModules.setExpandHorizontal(true);
			scrolledCompositeCoreModules.setExpandVertical(true);
			Rectangle r = scrolledCompositeCoreModules.getClientArea();
			scrolledCompositeCoreModules.setMinSize(scrolledCompositeCoreModules.computeSize(SWT.H_SCROLL, vertical_scroll_size));
		}
	@Override
	public boolean canFlipToNextPage() {
		return true;
	}
	
	public void getPilotProjectName() { 
		pilotProjectTxt.setText(getPilotProject());
	}

	@Override
	public IWizardPage getNextPage() {
			
		return super.getNextPage();
	}
	/**
	 * @return the features
	 */
	public List<ModuleGroup> getFeatures() {
		return features;
	}


	/**
	 * @param features the features to set
	 */
	public void setFeatures(List<ModuleGroup> features) {
		this.features = features;
	}

}
