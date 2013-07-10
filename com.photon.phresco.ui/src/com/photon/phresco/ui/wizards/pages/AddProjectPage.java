package com.photon.phresco.ui.wizards.pages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.photon.phresco.commons.model.ProjectInfo;
import com.photon.phresco.commons.model.Technology;

/**
 * App Info Page
 * @author suresh_ma
 *
 */
public class AddProjectPage extends WizardPage implements IWizardPage{

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
	public List<Technology> technologies;
	//project Technology version
	public Combo technologyVersionCombo;
	//project pilot
	public Combo pilotProjectCombo;
	//Layer Button
	public Button layerButton;

	// pilots
	public List<ProjectInfo> pilots = new ArrayList<ProjectInfo>();

	public Button webServiceBtn;

	private boolean nextPage = false;
	
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

	public boolean isNextPage() {
		return nextPage;
	}

	public void setNextPage(boolean nextPage) {
		this.nextPage = nextPage;
		setPageComplete(nextPage);
	}


	@Override
	public void createControl(Composite parent) {
		setPageComplete(false);
		Composite parentComposite = new Composite(parent, SWT.NULL);
		parentComposite.setLayout(new GridLayout(1,false));
		parentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		final Composite basicComposite = new Composite(parentComposite, SWT.NULL);
		basicComposite.setLayout(new GridLayout(2,false));
		basicComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label projectName = new Label(basicComposite, SWT.NONE);
		projectName.setText("Project name *");

		projectTxt = new Text(basicComposite,SWT.BORDER);
		projectTxt.setMessage("Name of the project");
		projectTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		projectTxt.setFocus();

		projectTxt.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) { 
				if(projectTxt.getText().trim().length()>0) {
					setNextPage(true);
				} else {
					setNextPage(false);
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		Label code = new Label(basicComposite, SWT.NONE);
		code.setText("Code");

		codeTxt = new Text(basicComposite, SWT.BORDER);
		codeTxt.setMessage("Project Code");

		Label description = new Label(basicComposite, SWT.NONE);
		description.setText("Description");

		descriptionTxt = new StyledText(basicComposite, SWT.BORDER);
		descriptionTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));

		Label version = new Label(basicComposite, SWT.NONE);
		version.setText("Version");

		versionTxt = new Text(basicComposite, SWT.BORDER);
		versionTxt.setText("1.0.0");

//		new Label(basicComposite, SWT.NONE);		

		Composite composite = new Composite(parentComposite, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		composite.setLayout(new GridLayout(6, false));
		
		layerButton = new Button(composite, SWT.CHECK);
		Label appLayerLbl = new Label(composite, SWT.BOLD);
		appLayerLbl.setText("Application Layer");
		
		layerButton = new Button(composite, SWT.CHECK);
		Label webLayerLbl = new Label(composite, SWT.BOLD);
		webLayerLbl.setText("Web Layer");
		
		layerButton = new Button(composite, SWT.CHECK);
		Label mobileLayerLbl = new Label(composite, SWT.BOLD);
		mobileLayerLbl.setText("Mobile Layer");
		
		new Label(composite, SWT.NONE);
		
		setControl(parentComposite);
		
	}

	@Override
	public IWizardPage getNextPage() {
		return super.getNextPage();
	}
}