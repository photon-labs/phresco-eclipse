package com.photon.phresco.ui.wizards.pages;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.INewWizard;

import com.photon.phresco.commons.util.DesignUtil;

/**
 * App Info Page
 * @author suresh_ma
 *
 */
public class AddProjectPage extends WizardPage implements IWizardPage {

	//project name;
	public Text projectTxt;
	//project code
	public Text codeTxt;
	//project description
	public StyledText descriptionTxt;
	//project version
	public Text versionTxt;
	//project Application Type	
	public String appTypeConstant;
	//project Technologies
//	public List<Technology> technologies;
	//project Technology version
	public Combo technologyVersionCombo;
	//project pilot
	public Combo pilotProjectCombo;
	//Layer Button
	public Button layerButton;

	public Button webServiceBtn;

	private boolean projectNameValidation = false;
	
	private boolean layerValidation = false;
	
	/**
	 * @wbp.parser.constructor
	 */
	public AddProjectPage(String pageName) {
		super(pageName);
		setTitle("{Phresco}");
		setDescription("Project Creation Page");
	}
	
	/**
	 *  Getter and Setters for Feature pages.
	 *
	 */
/*
	public boolean isNextPage() {
		return projectNameValidation;
	}

	public void setNextPage(boolean nextPage) {
		this.projectNameValidation = nextPage;
//		setPageComplete(nextPage);
	}*/
	
	private void checkStatus() {
		canFlipToNextPage();
		getWizard().getContainer().updateButtons();
	}


	@Override
	public void createControl(Composite parent) {
		Composite parentComposite = new Composite(parent, SWT.NULL);
		parentComposite.setLayout(new GridLayout(1,false));
		parentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		final Composite basicComposite = new Composite(parentComposite, SWT.NULL);
		basicComposite.setLayout(new GridLayout(2,false));
		basicComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label projectName = new Label(basicComposite, SWT.NONE);
		projectName.setText("Project name *");
		projectName.setFont(DesignUtil.getLabelFont());

		projectTxt = new Text(basicComposite,SWT.BORDER);
		projectTxt.setMessage("Name of the project");
		projectTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		projectTxt.setFocus();
		
		projectTxt.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) { 
				if(projectTxt.getText().trim().length()>0) {
					projectNameValidation = true;
				} else {
					projectNameValidation = false;
				}
				checkStatus();
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		Label code = new Label(basicComposite, SWT.NONE);
		code.setText("Code");
		code.setFont(DesignUtil.getLabelFont());
		
		codeTxt = new Text(basicComposite, SWT.BORDER);
		codeTxt.setMessage("Project Code");

		Label description = new Label(basicComposite, SWT.NONE);
		description.setText("Description");
		description.setFont(DesignUtil.getLabelFont());

		descriptionTxt = new StyledText(basicComposite, SWT.BORDER);
		descriptionTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));

		Label version = new Label(basicComposite, SWT.NONE);
		version.setText("Version");
		version.setFont(DesignUtil.getLabelFont());

		versionTxt = new Text(basicComposite, SWT.BORDER);
		versionTxt.setText("1.0.0");

//		new Label(basicComposite, SWT.NONE);		

		Composite composite = new Composite(parentComposite, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		composite.setLayout(new GridLayout(3, false));
		
		layerButton = new Button(composite, SWT.CHECK);
		layerButton.setText("Application Layer");
		layerButton.setFont(DesignUtil.getLabelFont());
		
		/*Label appLayerLbl = new Label(composite, SWT.BOLD);
		appLayerLbl.setText("Application Layer");
		appLayerLbl.setFont(DesignUtil.getLabelFont());*/
		
		layerButton = new Button(composite, SWT.CHECK);
		layerButton.setText("Web Layer");
		layerButton.setFont(DesignUtil.getLabelFont());
		
		/*Label webLayerLbl = new Label(composite, SWT.BOLD);
		webLayerLbl.setText("Web Layer");
		webLayerLbl.setFont(DesignUtil.getLabelFont());*/
		
		layerButton = new Button(composite, SWT.CHECK);
		layerButton.setText("Mobile Layer");
		layerButton.setFont(DesignUtil.getLabelFont());
		
		layerButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(layerButton.getSelection()) {
					layerValidation = true;
				} else {
					layerValidation = false;
				}
				checkStatus();
			}
		});
		/*Label mobileLayerLbl = new Label(composite, SWT.BOLD);
		mobileLayerLbl.setText("Mobile Layer");
		mobileLayerLbl.setFont(DesignUtil.getLabelFont());*/
		
		new Label(composite, SWT.NONE);
		
		setControl(parentComposite);
	}
	
	
	@Override
	public boolean canFlipToNextPage() {
		return projectNameValidation && layerValidation;
	}
	
	@Override
	public IWizardPage getNextPage() {
		return super.getNextPage();
	}
}