package com.photon.phresco.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.framework.PhrescoFrameworkFactory;
import com.photon.phresco.framework.api.ProjectAdministrator;
import com.photon.phresco.framework.api.ServiceManager;
import com.photon.phresco.model.Database;

public class DataBaseDialog extends TitleAreaDialog {

	//TODO Comboviewer needs to be used instead of combo
	private Combo dataBaseCombo;
	private Combo versionCombo;
	
	  //private Text versionText;
	  private String dataBase;
	  private String version;
	  private String techId;


	public DataBaseDialog(Shell parentShell) {
	    super(parentShell);
	  }

	  @Override
	  public void create() {
	    super.create();
	    // Set the title
	    setTitle("Select DataBase");
//	    File file = new File("icons/phresco.jpeg");
//	    System.out.println("Path========>" + file.getAbsolutePath());
//	    Image image = new Image(null, file.getAbsolutePath());
//	    setDefaultImage(image);
	    // Set the message
	    //setMessage("This is a TitleAreaDialog", IMessageProvider.INFORMATION);

	  }

	  public String getTechId() {
			return techId;
		}

		public void setTechId(String techId) {
			this.techId = techId;
		}
		
	  @Override
	  protected Control createDialogArea(Composite parent) {
	    GridLayout layout = new GridLayout();
	    layout.numColumns = 2;
	    // layout.horizontalAlignment = GridData.FILL;
	    parent.setLayout(layout);

	    // The text fields will grow with the size of the dialog
	    GridData gridData = new GridData();
	    gridData.grabExcessHorizontalSpace = true;
	    gridData.horizontalAlignment = GridData.FILL;

	    Label label1 = new Label(parent, SWT.NONE);
	    label1.setText("DataBase");
	    
	    dataBaseCombo = new Combo(parent, SWT.BORDER | SWT.READ_ONLY);
	    dataBaseCombo.setText("SELECT");
	    dataBaseCombo.setLayoutData(gridData);
	    List<String> dataBaseNames = getDataBaseNames();
	    String[] dataBaseNameArray = new String[dataBaseNames.size()];
	    dataBaseNameArray = dataBaseNames.toArray(dataBaseNameArray);
	    dataBaseCombo.setItems(dataBaseNameArray);
	    dataBaseCombo.select(0);
	    
	    
	    Label label2 = new Label(parent, SWT.NONE);
	    label2.setText("Version");
	    // You should not re-use GridData
	    gridData = new GridData();
	    gridData.grabExcessHorizontalSpace = true;
	    gridData.horizontalAlignment = GridData.FILL;
	    
	    versionCombo = new Combo(parent, SWT.BORDER | SWT.READ_ONLY);
	    versionCombo.setText("SELECT");
	    versionCombo.setLayoutData(gridData);
	    versionCombo.select(0);
	    
	    dataBaseCombo.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					onDataBaseSelect(e);
				}
			});
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
	  
	  private List<Database> getDataBase(){
		  try{
			  ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
			  List<Database> dataBase = administrator.getDatabases(techId, "photon");
			  for (Database data : dataBase) {
				  System.out.println(data.getName());
			}
			  return dataBase;
		  }catch(PhrescoException ex){
			  ex.printStackTrace();  
		  }
		  return null; 
	  }
	  
	  private List<String> getDataBaseNames(){
		  List<Database> dataBases = this.getDataBase();
		  List<String> DataBaseNames = new ArrayList<String>();
		  for(Database dataBase : dataBases){
			  DataBaseNames.add(dataBase.getName());
		  }
		  return DataBaseNames; 
	  }
	  
	  private List<String> getVersions(Database dataBase){
		  return dataBase.getVersions();		  
	  }
	  
	  private void onDataBaseSelect(SelectionEvent e){
		  int index = dataBaseCombo.getSelectionIndex();
		  List<Database> dataBases = getDataBase();
		  Database dataBase = dataBases.get(index);
		  List<String> versions = getVersions(dataBase);
		  String[] versionStrings = new String[versions.size()];
		  versionStrings =  versions.toArray(versionStrings);
		  versionCombo.setItems(versionStrings);
	  }

	  private boolean isValidInput() {
	    boolean valid = true;
	    if (versionCombo.getText().length() == 0) {
	      setErrorMessage("Please select the version to add");
	      valid = false;
	    }
	    if (dataBaseCombo.getText().length() == 0) {
	      setErrorMessage("Please select the DataBase to add");
	      valid = false;
	    }
	    
	    return valid;
	  }
	  
	  @Override
	  protected boolean isResizable() {
	    return true;
	  }

	  // Copy textFields because the UI gets disposed
	  // and the Text Fields are not accessible any more.
	  private void saveInput() {
		dataBase = dataBaseCombo.getText();
	    version = versionCombo.getText();
	  }

	  @Override
	  protected void okPressed() {
	    saveInput();
	    super.okPressed();
	  }

	  public String getdataBase() {
	    return dataBase;
	  }

	  public String getVersion() {
	    return version;
	  }

}
