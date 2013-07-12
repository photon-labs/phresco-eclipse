package com.photon.phresco.ui.wizards.pages;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.photon.phresco.commons.util.DesignUtil;

public class TechnologyPage extends WizardPage implements IWizardPage {

	AddProjectPage addProjectPage;
	
	public TechnologyPage(String pageName) {
		super(pageName);
		setTitle("{Phresco}");
		setDescription("Technology Selection Page");
	}

	@Override
	public void createControl(Composite parent) {
		addProjectPage = (AddProjectPage) getWizard().getPreviousPage(this);
		Composite parentComposite = new Composite(parent, SWT.NULL);
		parentComposite.setLayout(new GridLayout(1,true));
		parentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		System.out.println("AppSelection =====> " + addProjectPage.layerButton.getSelection());
		Label appLayerLabel = new Label(parentComposite, SWT.BOLD);
		appLayerLabel.setFont(DesignUtil.getHeaderFont());
		appLayerLabel.setText("Application Layer");

		Composite appComposite = new Composite(parentComposite, SWT.BORDER);
		appComposite.setLayout(new GridLayout(6, true));
		appComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label appCodeLabel = new Label(appComposite, SWT.BOLD);
		appCodeLabel.setText("AppCode");
		appCodeLabel.setFont(DesignUtil.getLabelFont());

		Text appCodeTxt = new Text(appComposite, SWT.NONE);
		appCodeTxt.setMessage("Enter AppCode");

		Label techLabel = new Label(appComposite, SWT.BOLD);
		techLabel.setText("Technology");
		techLabel.setFont(DesignUtil.getLabelFont());

		Combo techCombo = new Combo(appComposite, SWT.NONE | SWT.READ_ONLY | SWT.RESIZE);
		String[] techItems = {"Java", "J2EE","Drupal"};
		techCombo.setItems(techItems);
		techCombo.select(0);

		Label versionLabel = new Label(appComposite, SWT.BOLD);
		versionLabel.setText("Version");
		versionLabel.setFont(DesignUtil.getLabelFont());

		Combo versionCombo = new Combo(appComposite, SWT.NONE | SWT.READ_ONLY);
		String[] versionItems = {"1.2","2.4"};
		versionCombo.setItems(versionItems);
		versionCombo.select(0);
		
		Label webLayerLabel = new Label(parentComposite, SWT.BOLD);
		webLayerLabel.setFont(DesignUtil.getHeaderFont());
		webLayerLabel.setText("Web Layer");

		Composite webComposite = new Composite(parentComposite, SWT.BORDER);
		webComposite.setLayout(new GridLayout(2, true));
		webComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label webAppCodeLabel = new Label(webComposite, SWT.BOLD);
		webAppCodeLabel.setText("AppCode");
		webAppCodeLabel.setFont(DesignUtil.getLabelFont());

		Text webAppCodeTxt = new Text(webComposite, SWT.NONE);
		webAppCodeTxt.setMessage("Enter AppCode");

		Label webWebLayerLabel = new Label(webComposite, SWT.BOLD);
		webWebLayerLabel.setText("Web Layer");
		webWebLayerLabel.setFont(DesignUtil.getLabelFont());

		Combo webLayerCombo = new Combo(webComposite, SWT.NONE | SWT.READ_ONLY);
		String[] webLayerItems = {"HTML5"};
		webLayerCombo.setItems(webLayerItems);
		webLayerCombo.select(0);

		Label webWidgetLabel = new Label(webComposite, SWT.BOLD);
		webWidgetLabel.setText("Widget");
		webWidgetLabel.setFont(DesignUtil.getLabelFont());

		Combo webWidgetCombo = new Combo(webComposite, SWT.NONE | SWT.READ_ONLY);
		String[] webLayerWidgetItems = {"Jquery Mobile Widget", "YUI Mobile Widget", "Multi Channel JqueryWidget", "Multi Channel YUI Widget"};
		webWidgetCombo.setItems(webLayerWidgetItems);
		webWidgetCombo.setLayoutData(new GridData(GridData.CENTER));;
		webWidgetCombo.select(0);

		Label webVersion = new Label(webComposite, SWT.BOLD);
		webVersion.setText("Version");
		webVersion.setFont(DesignUtil.getLabelFont());

		Combo webVersionCombo = new Combo(webComposite, SWT.NONE | SWT.READ_ONLY);
		String[] webLayerVersionItems = {"2.0.2","2.0.1"};
		webVersionCombo.setItems(webLayerVersionItems);
		webVersionCombo.select(0);

		parentComposite.pack();
		setControl(parentComposite);
	}

}
