package com.photon.phresco.ui.wizards.pages;

import java.util.List;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.photon.phresco.model.ModuleGroup;

public class CustomModuleFeaturesPage extends WizardPage implements IWizardPage{

	public ScrolledComposite scrolledCompositeCustomModules;
	public Group customModuleComposite;
	private String tech;
	
	public CustomModuleFeaturesPage(String pageName) {
		super(pageName);
		setTitle("{custom Phresco}");
		setDescription("Project Creation Page");
	}

	public String getTech() {
		return tech;
	}

	public void setTech(String tech) {
		this.tech = tech;
	}

	@Override
	public void createControl(Composite parent) {
		Composite parentComposite = new Composite(parent, SWT.NULL);
		parentComposite.setLayout(new GridLayout(1,false));
		parentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		scrolledCompositeCustomModules = new ScrolledComposite(parentComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledCompositeCustomModules.setLayoutData(new GridData(GridData.FILL_BOTH));

		setControl(parentComposite);
	}
	
	public void populateCustomModules(List<ModuleGroup> features) {
		if(features == null) {
			scrolledCompositeCustomModules.setVisible(false);
			return;
		}
		customModuleComposite = new Group(scrolledCompositeCustomModules, SWT.BAR);
		customModuleComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		customModuleComposite.setLayout(new GridLayout(1,false));
		customModuleComposite.setBackground(new Color(null, 255, 255, 255));
		customModuleComposite.setText("CustomModules");
		scrolledCompositeCustomModules.setVisible(true);
		List<ModuleGroup> customModule = features;
		int size = 15;
		for (ModuleGroup moduleGroup : customModule) {
			Button featureButton = new Button(customModuleComposite, SWT.CHECK);
			featureButton.setText(moduleGroup.getName());
			featureButton.setBackground(new Color(null, 255, 255, 255));
			size = size +20;
		}

		final int vertical_scroll_size = size-15;
		scrolledCompositeCustomModules.setContent(customModuleComposite);
		scrolledCompositeCustomModules.setExpandHorizontal(true);
		scrolledCompositeCustomModules.setExpandVertical(true);
		Rectangle r = scrolledCompositeCustomModules.getClientArea();
		scrolledCompositeCustomModules.setMinSize(scrolledCompositeCustomModules.computeSize(r.width, vertical_scroll_size));
	}

	@Override
	public boolean canFlipToNextPage() {
		return true;
	}
}
