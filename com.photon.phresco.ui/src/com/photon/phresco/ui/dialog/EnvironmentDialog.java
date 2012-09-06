package com.photon.phresco.ui.dialog;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class EnvironmentDialog extends TrayDialog {

	private Text nameTxt;
	private Text descriptionTxt;
	
	private String name;
	
	private String description;
	
	public EnvironmentDialog(Shell parent) {
		super(parent);
		setHelpAvailable(false);
	}
	
	@Override
	public void create() {
		super.create();
		getShell().setText("Add Environment");
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		GridLayout layout = new GridLayout(2, true);
	    
	    // layout.horizontalAlignment = GridData.FILL;
	    parent.setLayout(layout);

	    // The text fields will grow with the size of the dialog
	    GridData gridData = new GridData();
	    gridData.grabExcessHorizontalSpace = true;
	    gridData.horizontalAlignment = GridData.FILL;
	    
		Label nameLbl = new Label(parent, SWT.NONE);
	    nameLbl.setText("Name");
	    
	    nameTxt = new Text(parent, SWT.BORDER);
	    nameTxt.setLayoutData(new GridData(GridData.FILL_BOTH));    
	    
	    Label descriptionLbl = new Label(parent, SWT.NONE);
	    descriptionLbl.setText("Description");
	    
	    descriptionTxt = new Text(parent, SWT.BORDER | SWT.WRAP | SWT.MULTI);
	    descriptionTxt.setLayoutData(new GridData(GridData.FILL_BOTH));
	    
		return parent;
	}
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 3;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.CENTER;

		parent.setLayoutData(gridData);
		// Create Add button
		// Own method as we need to overview the SelectionAdapter
		createOkButton(parent, OK, "Add", true);
		// Add a SelectionListener

		// Create Cancel button
		Button cancelButton = createButton(parent, CANCEL, "Cancel", false);
		// Add a SelectionListener
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setReturnCode(CANCEL);
				close();
			}
		});
	}

	protected Button createOkButton(Composite parent, int id, 
			String label,
			boolean defaultButton) {
		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns++;
		Button button = new Button(parent, SWT.PUSH);
		button.setText(label);
		button.setFont(JFaceResources.getDialogFont());
		button.setData(new Integer(id));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (isValidInput()) {
					okPressed();
				}
			}
		});
		if (defaultButton) {
			Shell shell = parent.getShell();
			if (shell != null) {
				shell.setDefaultButton(button);
			}
		}
		setButtonLayoutData(button);
		return button;
	}
	private boolean isValidInput() {
		boolean valid = true;
		if (nameTxt.getText().length() == 0) {
//			setErrorMessage("Please Enter the Name");
			valid = false;
		}
		if (descriptionTxt.getText().length() == 0) {
//			setErrorMessage("Please Enter the Description");
			valid = false;
		}

		return valid;
	}
	
	@Override
	protected void okPressed() {
		saveInput();
		super.okPressed();
	}
	
	private void saveInput() {
		name = nameTxt.getText();
		description = descriptionTxt.getText();
	}
	//Getters for Name and Description
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
}
