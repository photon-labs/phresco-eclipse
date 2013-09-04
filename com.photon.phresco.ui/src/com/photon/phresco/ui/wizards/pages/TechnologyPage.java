package com.photon.phresco.ui.wizards.pages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.util.DesignUtil;
import com.photon.phresco.ui.resource.Messages;
import com.photon.phresco.ui.wizards.componets.LayerComponent;

public class TechnologyPage extends WizardPage implements IWizardPage, PhrescoConstants {

	public List<LayerComponent> layerComponents = new ArrayList<LayerComponent>();

	public Text appCodeTxt;
	public Combo techGroupNameCombo;
	public Label techNameLabel;
	public Combo techNameCombo;
	public Label techVersionLabel;
	public Combo techVersionCombo;
	
	private int pageWidth = 0;
	
	public TechnologyPage(String pageName) {
		super(pageName);
		setTitle(Messages.TECHNOLOGY_WIZARD_NAME);
		setDescription(Messages.TECHNOLOGY_WIZARD_DESC);
	}

	@Override
	public void createControl(Composite parent) {
		Composite parentComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
        parentComposite.setLayout(layout);
		parentComposite.pack();
		setControl(parentComposite);
	}
	
	@Override
	public IWizardPage getPreviousPage() {
		layerComponents.clear();
		return super.getPreviousPage();
	}
	int x = 20;
	public void renderLayer(List<Button> selectedLayers) {
		final Composite parentComposite = (Composite) getControl();
		clearExistingControls(parentComposite);
		final ScrolledComposite scrolledComposite = new ScrolledComposite(parentComposite, SWT.V_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		final Composite composite = new Composite(scrolledComposite, SWT.NONE);
		GridLayout CompositeLayout = new GridLayout(1, true);
		composite.setLayout(CompositeLayout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		for (final Button layerButton : selectedLayers) {
			
			final Group mainLayerGroup = new Group(composite, SWT.NONE);
			mainLayerGroup.setText(layerButton.getText());
			GridLayout mainLayout = new GridLayout(1, false);
			mainLayerGroup.setLayout(mainLayout);
			mainLayerGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL));
			mainLayerGroup.setFont(DesignUtil.getHeaderFont());
			
			final Group layerGroup = new Group(mainLayerGroup, SWT.NONE);
			GridLayout layout = new GridLayout(9, false);
	        layerGroup.setLayout(layout);
			layerGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			layerGroup.setFont(DesignUtil.getHeaderFont());
			
			LayerComponent layerComponent = new LayerComponent(layerGroup, SWT.NONE);
			layerComponents.add(layerComponent);
			layerComponent.getComponent(layerButton);
			
			Button webAddButton = new Button(layerGroup, SWT.PUSH);
			webAddButton.setText(PLUS_SYMBOL);
			
			webAddButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					
					final Group newGroup = new Group(mainLayerGroup, SWT.NONE);
					GridLayout newLayout = new GridLayout(9, false);
					newGroup.setLayout(newLayout);
					newGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					
					final LayerComponent webLayerComponent = new LayerComponent(newGroup, SWT.NONE);
					layerComponents.add(webLayerComponent);
					webLayerComponent.getComponent(layerButton);
					
					Button webDeleteButton = new Button(newGroup, SWT.PUSH);
					webDeleteButton.setText(MINUS_SYMBOL);
					webDeleteButton.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							layerComponents.remove(webLayerComponent);
							newGroup.dispose();
							reSize(composite, scrolledComposite);
							
							parentComposite.pack();
							composite.pack();
							mainLayerGroup.pack();
							super.widgetSelected(e);
						}
					});
					
					reSize(composite, scrolledComposite);
					
					parentComposite.pack();
					composite.pack();
					mainLayerGroup.pack();
					newGroup.pack();
					super.widgetSelected(e);
					
				}
			});
			if(pageWidth < layerGroup.getSize().x) {
				pageWidth = layerGroup.getSize().x;
			}
			mainLayerGroup.pack();
			mainLayerGroup.redraw();
			
			scrolledComposite.setContent(composite);
			scrolledComposite.setExpandHorizontal(true);
			scrolledComposite.setExpandVertical(true);
			reSize(composite, scrolledComposite);
			scrolledComposite.pack();
			scrolledComposite.redraw();
			getContainer().getShell().setSize(pageWidth + 80, 500);
			
			parentComposite.pack();
			parentComposite.redraw();
			setControl(parentComposite);
		}
	}

	// This will clear the previously added controls
	private void clearExistingControls(final Composite parentComposite) {
		Control[] children = parentComposite.getChildren();
		for (Control control : children) {
			control.dispose();
		}
	}
	
	private void reSize(final Composite composite, final ScrolledComposite scrolledComposite) {
		composite.addListener(SWT.Resize, new Listener() {
			int width = -1;
			@Override
			public void handleEvent(Event event) {
				int newWidth = composite.getSize().x;
				if (newWidth != width) {
			        scrolledComposite.setMinHeight(composite.computeSize(newWidth, SWT.DEFAULT).y);
			        width = newWidth;
			    }
			}
		});
		/*scrolledComposite.addControlListener(new ControlListener() {
			
			@Override
			public void controlResized(ControlEvent e) {
				Rectangle r = scrolledComposite.getClientArea();
				scrolledComposite.setMinSize(composite.computeSize(r.width, SWT.DEFAULT));
			}
			
			@Override
			public void controlMoved(ControlEvent e) {
				// TODO Auto-generated method stub
				
			}
		});*/
	}
}
