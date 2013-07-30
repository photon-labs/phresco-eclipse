package com.photon.phresco.ui.phrescoexplorer.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

/**
 * Page to render the modules
 * @author syed
 *
 */
public class ModuleFeaturePage extends AbstractFeatureWizardPage {
	
	public static final String PAGE_NAME = "Module";
	Table jsLibTable;

	public ModuleFeaturePage() {
		super(PAGE_NAME, "Module", null);
	}

	public void createControl(Composite parent) {
        GridLayout layout = new GridLayout(2, false);
        layout.marginLeft = 5;
        layout.marginTop = 5;
        
		Composite topLevel = new Composite(parent, SWT.NONE);
		topLevel.setLayout(layout);

		setControl(topLevel);
		
		setPageComplete(true);
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
