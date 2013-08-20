package com.photon.phresco.ui.phrescoexplorer.wizard;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import com.photon.phresco.commons.model.ArtifactGroup;

/**
 * Page to render the modules
 * @author syed
 *
 */
public class ModuleFeaturePage extends AbstractFeatureWizardPage {
	
	public static final String PAGE_NAME = "Modules";
	Table jsLibTable;
	private List<ArtifactGroup> features = null;
	private boolean isFirstPage;
	private boolean isRendered;
	
	public ModuleFeaturePage(List<ArtifactGroup> features, boolean isFirstPage) {
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
		renderFeatureTable(topLevel, PAGE_NAME, features);
		
		setControl(topLevel);
		
		if (isFirstPage) {
//			renderPage();
			isRendered = true;
		}
	}
	
	@Override
	public void renderPage() {
		
		if (isRendered) {
			return;
		}
		
		final Composite parentComposite = (Composite) getControl();
/*		showSelectedFeaturesCount(parentComposite);
		final ScrolledComposite scrolledComposite = new ScrolledComposite(parentComposite, SWT.V_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		scrolledComposite.setAlwaysShowScrollBars(false);
		scrolledComposite.setBounds(5, 5, 500, 350);*/
		
		renderFeatureTable(parentComposite, PAGE_NAME, features);
	}
}
