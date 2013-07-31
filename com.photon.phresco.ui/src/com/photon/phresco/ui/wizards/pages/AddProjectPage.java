package com.photon.phresco.ui.wizards.pages;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jface.wizard.IWizardPage;
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

import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.PhrescoDialog;
import com.photon.phresco.commons.model.ApplicationType;
import com.photon.phresco.commons.model.Customer;
import com.photon.phresco.commons.util.DesignUtil;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.ui.model.BaseAction;

/**
 * App Info Page
 * @author suresh_ma
 *
 */
public class AddProjectPage extends WizardPage implements IWizardPage, PhrescoConstants {

	//project name;
	public Text projectTxt;
	
	public Text getProjectTxt() {
		return projectTxt;
	}

	public void setProjectTxt(Text projectTxt) {
		this.projectTxt = projectTxt;
	}

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
	public Button layerButtons;

	public Button webServiceBtn;
	
	public Combo customerIds;

	private boolean projectNameValidation = false;
	
	private boolean layerValidation = false;
	
	List<Button> selectedLayersList = new ArrayList<Button>();
	
	/**
	 * @wbp.parser.constructor
	 */
	public AddProjectPage(String pageName) {
		super(pageName);
		selectedLayersList.clear();
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
		BaseAction baseAction = new BaseAction();
		ServiceManager serviceManager = PhrescoUtil.getServiceManager(baseAction.getUserId());
		if(serviceManager == null) {
			PhrescoDialog.errorDialog(getShell(),"Error", "Please Login before making Request");
			return;
		}
		Composite parentComposite = new Composite(parent, SWT.NULL);
		parentComposite.setLayout(new GridLayout(1,false));
		parentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		final Composite basicComposite = new Composite(parentComposite, SWT.NULL);
		basicComposite.setLayout(new GridLayout(2,false));
		basicComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label customerLabel = new Label(basicComposite, SWT.NONE);
		customerLabel.setText("Customers");
		
		customerIds = new Combo(basicComposite, SWT.BOLD | SWT.RIGHT | SWT.READ_ONLY);
		try {
			List<Customer> customers = serviceManager.getCustomers();
			List<String> customerIdList = new ArrayList<String>();
			for (Customer customer : customers) {
				customerIds.setData(customer.getName(), customer.getId());
				customerIdList.add(customer.getName());
			}
			String[] custIdArray = customerIdList.toArray(new String[customerIdList.size()]);
			customerIds.setItems(custIdArray);
			customerIds.select(0);
		} catch (PhrescoException e2) {
			e2.printStackTrace();
		}
		Label projectName = new Label(basicComposite, SWT.NONE);
		projectName.setText("Project name *");
		projectName.setFont(DesignUtil.getLabelFont());

		projectTxt = new Text(basicComposite,SWT.BORDER);
		projectTxt.setMessage("Name of the project");
		projectTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		projectTxt.setFocus();
		
		Label code = new Label(basicComposite, SWT.NONE);
		code.setText("Code");
		code.setFont(DesignUtil.getLabelFont());
		
		codeTxt = new Text(basicComposite, SWT.BORDER);
		codeTxt.setMessage("Project Code");
		codeTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		codeTxt.setEditable(false);
		
		projectTxt.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) { 
				codeTxt.setText(projectTxt.getText());
				if(projectTxt.getText().trim().length() > 0) {
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

		Composite composite = new Composite(parentComposite, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		composite.setLayout(new GridLayout(3, false));
		List<Button> layersList = new ArrayList<Button>();
		try {
			List<ApplicationType> applicationTypes = serviceManager.getApplicationTypes(baseAction.getCustomerId());
			for (ApplicationType applicationType : applicationTypes) {
				layerButtons = new Button(composite, SWT.CHECK);
				layerButtons.setText(applicationType.getName());
				layerButtons.setFont(DesignUtil.getLabelFont());
				layerButtons.setData(applicationType.getName(), applicationType.getId());
				layersList.add(layerButtons);
			}
		} catch (PhrescoException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		for (final Button button : layersList) {
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if(button.getSelection()) {
						selectedLayersList.add(button);
					} else {
						selectedLayersList.remove(button);
					}
					layerValidation = false;
					if(CollectionUtils.isNotEmpty(selectedLayersList)) {
						layerValidation = true;
					} 
					checkStatus();
				}
			});
		}
		
		new Label(composite, SWT.NONE);
		
		setControl(parentComposite);
	}
	
	
	public void saveData() {
		setProjectTxt(projectTxt);
	}
	
	
	@Override
	public boolean canFlipToNextPage() {
		return projectNameValidation && layerValidation;
	}
	
	@Override
	public IWizardPage getNextPage() {
		return super.getNextPage();
	}
	
	public List<Button> getLayersList() {
		return selectedLayersList;
	}
}