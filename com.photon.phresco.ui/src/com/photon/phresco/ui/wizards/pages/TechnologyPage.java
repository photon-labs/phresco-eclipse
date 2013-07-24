package com.photon.phresco.ui.wizards.pages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.photon.phresco.commons.util.DesignUtil;
import com.photon.phresco.ui.wizards.componets.AppLayerComponent;
import com.photon.phresco.ui.wizards.componets.MobLayerComponent;
import com.photon.phresco.ui.wizards.componets.WebLayerComponent;

public class TechnologyPage extends WizardPage implements IWizardPage {

	public List<AppLayerComponent> appLayerComponents = new ArrayList<AppLayerComponent>();
	public List<WebLayerComponent> webLayerComponents = new ArrayList<WebLayerComponent>();
	public List<MobLayerComponent> mobLayerComponents = new ArrayList<MobLayerComponent>();

	public Text appCodeTxt;
	public Combo techGroupNameCombo;
	public Label techNameLabel;
	public Combo techNameCombo;
	public Label techVersionLabel;
	public Combo techVersionCombo;
	
	public TechnologyPage(String pageName) {
		super(pageName);
		setTitle("{Phresco}");
		setDescription("Technology Selection Page");
	}

	@Override
	public void createControl(Composite parent) {
		Composite parentComposite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout(1, true);
        parentComposite.setLayout(layout);
		parentComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		parentComposite.pack();
		parentComposite.redraw();
		setControl(parentComposite);
	}
	
	@Override
	public IWizardPage getPreviousPage() {
		
		return super.getPreviousPage();
	}
	
	public void renderLayer(List<Button> selectedLayers) {
		
		final Composite parentComposite = (Composite) getControl();
		final ScrolledComposite scrolledComposite = new ScrolledComposite(parentComposite, SWT.Resize | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		scrolledComposite.setAlwaysShowScrollBars(true);
		
		final Composite composite = new Composite(scrolledComposite, SWT.NONE);
		GridLayout CompositeLayout = new GridLayout(1, true);
		composite.setLayout(CompositeLayout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		for (final Button layerButton : selectedLayers) {
			if("app-layer".equals(layerButton.getData(layerButton.getText()))) {
				final Group appLayerGroup = new Group(composite, SWT.NONE);
				appLayerGroup.setText(layerButton.getText());
				GridLayout layout = new GridLayout(7, false);
		        appLayerGroup.setLayout(layout);
				appLayerGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				appLayerGroup.setFont(DesignUtil.getHeaderFont());
				
				AppLayerComponent appLayerComponent = new AppLayerComponent(appLayerGroup, SWT.NONE);
				appLayerComponent.getComponent(layerButton);
				appLayerComponents.add(appLayerComponent);
				
				Button appAddButton = new Button(appLayerGroup, SWT.PUSH);
				appAddButton.setText("+");
				
				appAddButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						AppLayerComponent appLayerComponent = new AppLayerComponent(appLayerGroup, SWT.NONE);
						appLayerComponents.add(appLayerComponent);
						appLayerComponent.getComponent(layerButton);
						
						Button appDeleteButton = new Button(appLayerGroup, SWT.PUSH);
						appDeleteButton.setText("-");
						reSize(composite, scrolledComposite);
						parentComposite.pack();
						parentComposite.redraw();
						composite.pack();
						composite.redraw();
						appLayerGroup.pack();
						appLayerGroup.redraw();
						scrolledComposite.pack();
						scrolledComposite.redraw();
						super.widgetSelected(e);
					}
				});
				appLayerGroup.pack();
				appLayerGroup.redraw();
			}
			if("web-layer".equals(layerButton.getData(layerButton.getText()))) {
				final Group webLayerGroup = new Group(composite, SWT.NONE);
				webLayerGroup.setText(layerButton.getText());
				GridLayout layout = new GridLayout(4, false);
		        webLayerGroup.setLayout(layout);
				webLayerGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				webLayerGroup.setFont(DesignUtil.getHeaderFont());
				
				WebLayerComponent webLayerComponent = new WebLayerComponent(webLayerGroup, SWT.NONE);
				webLayerComponents.add(webLayerComponent);
				webLayerComponent.getComponent(layerButton);
				
				Button webAddButton = new Button(webLayerGroup, SWT.PUSH);
				webAddButton.setText("+");
				
				webAddButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						WebLayerComponent webLayerComponent = new WebLayerComponent(webLayerGroup, SWT.NONE);
						webLayerComponents.add(webLayerComponent);
						webLayerComponent.getComponent(layerButton);
						
						Button webDeleteButton = new Button(webLayerGroup, SWT.PUSH);
						webDeleteButton.setText("-");
						
						parentComposite.pack();
						parentComposite.redraw();
						super.widgetDefaultSelected(e);
					}
				});
				webLayerGroup.redraw();
			}
			if("mob-layer".equals(layerButton.getData(layerButton.getText()))) {
				final Group mobLayerGroup = new Group(composite, SWT.NONE);
				mobLayerGroup.setText(layerButton.getText());
				mobLayerGroup.setLayout(new GridLayout(2, true));
				mobLayerGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				mobLayerGroup.setFont(DesignUtil.getHeaderFont());
				
				MobLayerComponent mobLayerComponent = new MobLayerComponent(mobLayerGroup, SWT.NONE);
				mobLayerComponents.add(mobLayerComponent);
				mobLayerComponent.getComponent(layerButton);
				
				Button mobAddButton = new Button(mobLayerGroup, SWT.PUSH);
				mobAddButton.setText("+");
				mobAddButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						MobLayerComponent mobLayerComponent = new MobLayerComponent(mobLayerGroup, SWT.NONE);
						mobLayerComponents.add(mobLayerComponent);
						mobLayerComponent.getComponent(layerButton);
						parentComposite.pack();
						parentComposite.redraw();
						super.widgetDefaultSelected(e);
					}
				});
				mobLayerGroup.redraw();
			}
		}
		Point size = parentComposite.getSize();
		int y = size.x;
		final int vertical_scroll_size = y -10;
		scrolledComposite.setContent(composite);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		/*Rectangle r = scrolledComposite.getClientArea();
		scrolledComposite.setMinSize(scrolledComposite.computeSize(r.width, vertical_scroll_size));*/
		reSize(composite, scrolledComposite);
		scrolledComposite.pack();
		scrolledComposite.redraw();
		parentComposite.pack();
		parentComposite.redraw();
		setControl(parentComposite);
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
