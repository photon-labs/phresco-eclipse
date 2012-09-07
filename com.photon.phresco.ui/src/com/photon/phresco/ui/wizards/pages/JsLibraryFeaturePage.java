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

public class JsLibraryFeaturePage extends WizardPage implements IWizardPage{

	public ScrolledComposite scrolledCompositejsLibraries;
	public Group jsLibrariesComposite;
	private String tech;
	
	private List<ModuleGroup> features;
	
	
	public JsLibraryFeaturePage(String pageName) {
		super(pageName);
		setTitle("{ js Phresco}");
		setDescription("Project Creation Page");
	}

	@Override
	public void createControl(Composite parent) {
		Composite parentComposite = new Composite(parent, SWT.NULL);
		parentComposite.setLayout(new GridLayout(1,false));
		parentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		scrolledCompositejsLibraries = new ScrolledComposite(parentComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledCompositejsLibraries.setLayoutData(new GridData(GridData.FILL_BOTH));

		setControl(parentComposite);
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		populateJsLibraries();
	}
	
	public void populateJsLibraries() {
		if(features == null) {
			scrolledCompositejsLibraries.setVisible(false);
			return;
		}
		jsLibrariesComposite = new Group(scrolledCompositejsLibraries, SWT.NULL);
		jsLibrariesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		jsLibrariesComposite.setLayout(new GridLayout(1,false));
		jsLibrariesComposite.setBackground(new Color(null, 255, 255, 255));
		jsLibrariesComposite.setText("JsLibraries");
		scrolledCompositejsLibraries.setVisible(true);
		List<ModuleGroup> jsLiraries = features;
		int size = 15;
		for (ModuleGroup moduleGroup : jsLiraries) {
			Button featureButton = new Button(jsLibrariesComposite, SWT.CHECK);
			featureButton.setText(moduleGroup.getName());
			featureButton.setBackground(new Color(null, 255, 255, 255));
			size = size +21;
		}
		final int vertical_scroll_size = size-15;
		scrolledCompositejsLibraries.setContent(jsLibrariesComposite);
		scrolledCompositejsLibraries.setExpandHorizontal(true);
		scrolledCompositejsLibraries.setExpandVertical(true);
		Rectangle r = scrolledCompositejsLibraries.getClientArea();
		scrolledCompositejsLibraries.setMinSize(scrolledCompositejsLibraries.computeSize(r.width, vertical_scroll_size));
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

	public String getTech() {
		return tech;
	}

	public void setTech(String tech) {
		this.tech = tech;
	}
}
