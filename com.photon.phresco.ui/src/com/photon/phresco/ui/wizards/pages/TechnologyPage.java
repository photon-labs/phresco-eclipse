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
import com.photon.phresco.ui.wizards.componets.AppLayerComponent;
import com.photon.phresco.ui.wizards.componets.MobLayerComponent;
import com.photon.phresco.ui.wizards.componets.WebLayerComponent;

public class TechnologyPage extends WizardPage implements IWizardPage, PhrescoConstants {

	public List<AppLayerComponent> appLayerComponents = new ArrayList<AppLayerComponent>();
	public List<WebLayerComponent> webLayerComponents = new ArrayList<WebLayerComponent>();
	public List<MobLayerComponent> mobLayerComponents = new ArrayList<MobLayerComponent>();

	public Text appCodeTxt;
	public Combo techGroupNameCombo;
	public Label techNameLabel;
	public Combo techNameCombo;
	public Label techVersionLabel;
	public Combo techVersionCombo;
	
	private int pageWidth = 0;
	
	public TechnologyPage(String pageName) {
		super(pageName);
		setTitle("{Phresco}");
		setDescription("Technology Selection Page");
	}

	@Override
	public void createControl(Composite parent) {
		Composite parentComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
        parentComposite.setLayout(layout);
		parentComposite.pack();
		parentComposite.redraw();
		setControl(parentComposite);
	}
	
	@Override
	public IWizardPage getPreviousPage() {
		
		return super.getPreviousPage();
	}
	int x = 20;
	public void renderLayer(List<Button> selectedLayers) {
		final Composite parentComposite = (Composite) getControl();
		clearExistingControls(parentComposite);
		final ScrolledComposite scrolledComposite = new ScrolledComposite(parentComposite, SWT.Resize | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		scrolledComposite.setAlwaysShowScrollBars(true);
		
		final Composite composite = new Composite(scrolledComposite, SWT.NONE);
		GridLayout CompositeLayout = new GridLayout(1, true);
		composite.setLayout(CompositeLayout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		for (final Button layerButton : selectedLayers) {
			if(APP_LAYER.equals(layerButton.getData(layerButton.getText()))) {
				
				final Group mainApplayerGroup = new Group(composite, SWT.NONE);
				mainApplayerGroup.setText(layerButton.getText());
				GridLayout mainLayout = new GridLayout(1, false);
				mainApplayerGroup.setLayout(mainLayout);
				mainApplayerGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL));
				mainApplayerGroup.setFont(DesignUtil.getHeaderFont());
				
				final Group appLayerGroup = new Group(mainApplayerGroup, SWT.NONE);
				GridLayout layout = new GridLayout(7, false);
		        appLayerGroup.setLayout(layout);
				appLayerGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				appLayerGroup.setFont(DesignUtil.getHeaderFont());
				
				AppLayerComponent appLayerComponent = new AppLayerComponent(appLayerGroup, SWT.NONE);
				appLayerComponent.getComponent(layerButton);
				appLayerComponents.add(appLayerComponent);
				
				Button appAddButton = new Button(appLayerGroup, SWT.PUSH);
				appAddButton.setText(PLUS_SYMBOL);
				
				appAddButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						final Group newGroup = new Group(mainApplayerGroup, SWT.NONE);
						GridLayout newLayout = new GridLayout(7, false);
						newGroup.setLayout(newLayout);
						newGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
						newGroup.setBounds(x, 5, 275, 100);
						x = x + 20;
						
						final AppLayerComponent appLayerComponent = new AppLayerComponent(newGroup, SWT.NONE);
						appLayerComponents.add(appLayerComponent);
						appLayerComponent.getComponent(layerButton);
						
						Button appDeleteButton = new Button(newGroup, SWT.PUSH);
						appDeleteButton.setText(MINUS_SYMBOL);
						
						appDeleteButton.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								appLayerComponents.remove(appLayerComponent);
								newGroup.dispose();
							}
						});
						
						reSize(composite, scrolledComposite);
						
						parentComposite.pack();
						parentComposite.redraw();

						composite.pack();
						composite.redraw();
						
						mainApplayerGroup.pack();
						mainApplayerGroup.redraw();
						
						newGroup.pack();
						newGroup.redraw();
						
						scrolledComposite.pack();
						scrolledComposite.redraw();
						
						super.widgetSelected(e);
					}
				});
				
				if(pageWidth < appLayerGroup.getSize().x) {
					pageWidth = appLayerGroup.getSize().x;
				}
				mainApplayerGroup.pack();
				mainApplayerGroup.redraw();
			}
			
			if(WEB_LAYER.equals(layerButton.getData(layerButton.getText()))) {
				
				final Group mainWebLayerGroup = new Group(composite, SWT.NONE);
				mainWebLayerGroup.setText(layerButton.getText());
				GridLayout mainLayout = new GridLayout(1, false);
				mainWebLayerGroup.setLayout(mainLayout);
				mainWebLayerGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL));
				mainWebLayerGroup.setFont(DesignUtil.getHeaderFont());
				
				final Group webLayerGroup = new Group(mainWebLayerGroup, SWT.NONE);
				GridLayout layout = new GridLayout(9, false);
		        webLayerGroup.setLayout(layout);
				webLayerGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				webLayerGroup.setFont(DesignUtil.getHeaderFont());
				
				WebLayerComponent webLayerComponent = new WebLayerComponent(webLayerGroup, SWT.NONE);
				webLayerComponents.add(webLayerComponent);
				webLayerComponent.getComponent(layerButton);
				
				Button webAddButton = new Button(webLayerGroup, SWT.PUSH);
				webAddButton.setText(PLUS_SYMBOL);
				
				webAddButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						
						final Group newGroup = new Group(mainWebLayerGroup, SWT.NONE);
						GridLayout newLayout = new GridLayout(7, false);
						newGroup.setLayout(newLayout);
						newGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
						newGroup.setBounds(x, 5, 250, 100);
						x = x + 20;
						
						final WebLayerComponent webLayerComponent = new WebLayerComponent(newGroup, SWT.NONE);
						webLayerComponents.add(webLayerComponent);
						webLayerComponent.getComponent(layerButton);
						
						Button webDeleteButton = new Button(newGroup, SWT.PUSH);
						webDeleteButton.setText(MINUS_SYMBOL);
						webDeleteButton.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								webLayerComponents.remove(webLayerComponent);
								newGroup.dispose();
							}
						});
						
						reSize(composite, scrolledComposite);
						
						parentComposite.pack();
						parentComposite.redraw();

						composite.pack();
						composite.redraw();
						
						mainWebLayerGroup.pack();
						mainWebLayerGroup.redraw();
						
						newGroup.pack();
						newGroup.redraw();
						
						scrolledComposite.pack();
						scrolledComposite.redraw();
						
						super.widgetSelected(e);
						
					}
				});
				if(pageWidth < webLayerGroup.getSize().x) {
					pageWidth = webLayerGroup.getSize().x;
				}
				mainWebLayerGroup.pack();
				mainWebLayerGroup.redraw();
			}
			if(MOBILE_LAYER.equals(layerButton.getData(layerButton.getText()))) {
				
				final Group mainMobileLayerGroup = new Group(composite, SWT.NONE);
				mainMobileLayerGroup.setText(layerButton.getText());
				GridLayout mainLayout = new GridLayout(1, false);
				mainMobileLayerGroup.setLayout(mainLayout);
				mainMobileLayerGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL));
				mainMobileLayerGroup.setFont(DesignUtil.getHeaderFont());
				
				final Group mobLayerGroup = new Group(mainMobileLayerGroup, SWT.NONE);
				mobLayerGroup.setLayout(new GridLayout(9, false));
				mobLayerGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				mobLayerGroup.setFont(DesignUtil.getHeaderFont());
				
				MobLayerComponent mobLayerComponent = new MobLayerComponent(mobLayerGroup, SWT.NONE);
				mobLayerComponents.add(mobLayerComponent);
				mobLayerComponent.getComponent(layerButton);
				
				Button mobAddButton = new Button(mobLayerGroup, SWT.PUSH);
				mobAddButton.setText(PLUS_SYMBOL);
				mobAddButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						
						final Group newGroup = new Group(mainMobileLayerGroup, SWT.NONE);
						GridLayout newLayout = new GridLayout(7, false);
						newGroup.setLayout(newLayout);
						newGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
						newGroup.setBounds(x, 5, 350, 100);
						x = x + 20;
						
						final MobLayerComponent mobLayerComponent = new MobLayerComponent(newGroup, SWT.NONE);
						mobLayerComponents.add(mobLayerComponent);
						mobLayerComponent.getComponent(layerButton);
						
						Button mobDeleteButton = new Button(newGroup, SWT.PUSH);
						mobDeleteButton.setText(MINUS_SYMBOL);
						
						mobDeleteButton.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								mobLayerComponents.remove(mobLayerComponent);
								newGroup.dispose();
							}
						});
						
						reSize(composite, scrolledComposite);
						
						parentComposite.pack();
						parentComposite.redraw();

						composite.pack();
						composite.redraw();
						
						mainMobileLayerGroup.pack();
						mainMobileLayerGroup.redraw();
						
						newGroup.pack();
						newGroup.redraw();
						
						scrolledComposite.pack();
						scrolledComposite.redraw();
						
						super.widgetSelected(e);
					}
				});
				if(pageWidth < mobLayerGroup.getSize().x) {
					pageWidth = mobLayerGroup.getSize().x;
				}
				mainMobileLayerGroup.pack();
				mainMobileLayerGroup.redraw();
			}
		}
		
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
	}
}
