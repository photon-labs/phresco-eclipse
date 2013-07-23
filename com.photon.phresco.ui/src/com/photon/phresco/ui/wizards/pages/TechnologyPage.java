package com.photon.phresco.ui.wizards.pages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
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
		GridLayout layout = new GridLayout(1, false);
        parentComposite.setLayout(layout);
		parentComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		parentComposite.pack();
		setControl(parentComposite);
	}
	
	public void renderLayer(List<Button> selectedLayers) {
		Composite parentComposite = (Composite) getControl();
		for (final Button button : selectedLayers) {
			if("app-layer".equals(button.getData(button.getText()))) {
				final Group appLayerGroup = new Group(parentComposite, SWT.SHADOW_ETCHED_IN);
				appLayerGroup.setText(button.getText());
				GridLayout layout = new GridLayout(6, false);
		        appLayerGroup.setLayout(layout);
				appLayerGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				appLayerGroup.setFont(DesignUtil.getHeaderFont());
				
				AppLayerComponent appLayerComponent = new AppLayerComponent(appLayerGroup, SWT.NONE);
				appLayerComponent.getComponent(button);
				appLayerComponents.add(appLayerComponent);
				
				Button buttons = new Button(appLayerGroup, SWT.PUSH);
				buttons.setText("+");
				buttons.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						AppLayerComponent appLayerComponent = new AppLayerComponent(appLayerGroup, SWT.NONE);
						appLayerComponents.add(appLayerComponent);
						appLayerComponent.getComponent(button);
						super.widgetDefaultSelected(e);
					}
				});
			}
			if("web-layer".equals(button.getData(button.getText()))) {
				final Group webLayerGroup = new Group(parentComposite, SWT.SHADOW_ETCHED_IN);
				webLayerGroup.setText(button.getText());
				GridLayout layout = new GridLayout(2, false);
		        webLayerGroup.setLayout(layout);
				webLayerGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				webLayerGroup.setFont(DesignUtil.getHeaderFont());
				
				WebLayerComponent webLayerComponent = new WebLayerComponent(webLayerGroup, SWT.NONE);
				webLayerComponents.add(webLayerComponent);
				webLayerComponent.getComponent(button);
				
				Button buttons = new Button(webLayerGroup, SWT.PUSH);
				buttons.setText("+");
				
				buttons.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						WebLayerComponent webLayerComponent = new WebLayerComponent(webLayerGroup, SWT.NONE);
						webLayerComponents.add(webLayerComponent);
						webLayerComponent.getComponent(button);
						super.widgetDefaultSelected(e);
					}
				});
			}
			if("mob-layer".equals(button.getData(button.getText()))) {
				final Group mobLayerGroup = new Group(parentComposite, SWT.SHADOW_ETCHED_IN);
				mobLayerGroup.setText(button.getText());
				mobLayerGroup.setLayout(new GridLayout(2, true));
				mobLayerGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				mobLayerGroup.setFont(DesignUtil.getHeaderFont());
				
				MobLayerComponent mobLayerComponent = new MobLayerComponent(mobLayerGroup, SWT.NONE);
				mobLayerComponents.add(mobLayerComponent);
				mobLayerComponent.getComponent(button);
				
				Button buttons = new Button(mobLayerGroup, SWT.PUSH);
				buttons.setText("+");
				buttons.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						MobLayerComponent mobLayerComponent = new MobLayerComponent(mobLayerGroup, SWT.NONE);
						mobLayerComponents.add(mobLayerComponent);
						mobLayerComponent.getComponent(button);
						super.widgetDefaultSelected(e);
					}
				});
			}
		}
		parentComposite.pack();
		setControl(parentComposite);
	}
}
