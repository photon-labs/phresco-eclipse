package com.photon.phresco.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.photon.phresco.configuration.Environment;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.framework.PhrescoFrameworkFactory;
import com.photon.phresco.framework.api.Project;
import com.photon.phresco.framework.api.ProjectAdministrator;
import com.photon.phresco.model.SettingsInfo;

public class ManageEnvironmentsDialog extends TitleAreaDialog {

	private Text nameTxt;
	private Text descriptionTxt;
	
	private Label label;
	
	private List<Environment> environmentList;
	
	private List<String> deletableEnvs;
	
	private Button removeButton;
	
	private String labelValue = "";
	
	private Table table;
	
	private String projectCode;
	
	
	public ManageEnvironmentsDialog(Shell parentShell, String projectCode) {
		super(parentShell);
		this.projectCode = projectCode;
	}
	@Override
	public void create() {
		super.create();
		setHelpAvailable(false);
		setDialogHelpAvailable(false);
		getShell().setText("Manage Environments");
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {

	    Composite composite = new Composite(parent, SWT.NONE);
	    composite.setLayout(new GridLayout(2, false));
	    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
	    
	    Composite ConfigComposite = new Composite(composite, SWT.NONE);
		ConfigComposite.setLayout(new GridLayout(1, false));
		ConfigComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		
		final CheckboxTableViewer checkboxTableViewer = CheckboxTableViewer.newCheckList(ConfigComposite, SWT.BORDER | SWT.FULL_SELECTION);
		table = checkboxTableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TableColumn tblNameColumn = new TableColumn(table, SWT.NONE);
		tblNameColumn.setWidth(100);
		tblNameColumn.setText("Name");
		
		TableColumn tblValueColumn = new TableColumn(table, SWT.NONE);
		tblValueColumn.setWidth(100);
		tblValueColumn.setText("Description");
		
		TableColumn tblDefaultEnvColumn = new TableColumn(table, SWT.NONE);
		tblDefaultEnvColumn.setWidth(100);
		tblDefaultEnvColumn.setText("Default Environment");
		
		Composite controlComposite = new Composite(composite, SWT.NONE);
		controlComposite.setLayout(new GridLayout(1, true));
		controlComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		
		Button addBtn = new Button(controlComposite, SWT.PUSH);
		addBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addBtn.setText("Add...");
		
		Button deleteBtn = new Button(controlComposite, SWT.PUSH);
		deleteBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		deleteBtn.setText("Delete");
		
		Button defaultEvnBtn = new Button(controlComposite, SWT.PUSH);
		defaultEvnBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		defaultEvnBtn.setText("Default Environment");
		
		defaultEvnBtn.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				Object[] checkedElements = checkboxTableViewer.getCheckedElements();
				for (Object object : checkedElements) {
					if (object instanceof Environment) {
						Environment environment = (Environment) object;
						if(!environment.isDefaultEnv() && environment.getName().equals("ss")) {
							
							environment.setDefaultEnv(true);
						}
					}
				}
			}
		});
		
		deletableEnvs = new ArrayList<String>();
		deleteBtn.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Object[] checkedElements = checkboxTableViewer.getCheckedElements();
				checkboxTableViewer.remove(checkedElements);
				for (Object object : checkedElements) {
					if (object instanceof Environment) {
						Environment environment = (Environment) object;
						String name =environment.getName();
						List<SettingsInfo> configuration = getConfiguration(name,projectCode);
						if(configuration.isEmpty() && !environment.isDefaultEnv()) {
							deletableEnvs.add(name);
						}
					}
				}
			}
		});
		
		
		final EnvironmentDialog dialog = new EnvironmentDialog(null);
		environmentList = new ArrayList<Environment>();
		addBtn.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				dialog.create();
				if(dialog.open() == Window.OK) {
					String name = dialog.getName();
					String description = dialog.getDescription();
					environmentList.add(new Environment(name, description, false));
					checkboxTableViewer.add(new Environment(name, description, false));
				}
			}
		});
		
		ConfigDialog configDialog = new ConfigDialog(null,projectCode);
		final List<Environment> environmentList = configDialog.getEnvironment(projectCode);
		
		checkboxTableViewer.setContentProvider(new ArrayContentProvider());
		checkboxTableViewer.setLabelProvider(new ITableLabelProvider() {
			
			@Override
			public void removeListener(ILabelProviderListener listener) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean isLabelProperty(Object element, String property) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void dispose() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void addListener(ILabelProviderListener listener) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public String getColumnText(Object element, int columnIndex) {
				Environment environment = (Environment) element;
				String name = environment.getName();
				String desc = environment.getDesc();
				switch (columnIndex){
				case 0:
					return name;
				case 1:
					return desc;
				case 2:
					if(environment.isDefaultEnv()) {
						return "true";
					}
				}
				return "";
			}
			
			@Override
			public Image getColumnImage(Object element, int columnIndex) {
				// TODO Auto-generated method stub
				return null;
			}
		});
		checkboxTableViewer.setInput(environmentList);
		
		return parent;
	}
//		GridLayout layout = new GridLayout();
//	    layout.numColumns = 2;
//	    parent.setLayout(layout);
//
//	    // The text fields will grow with the size of the dialog
//	    GridData gridData = new GridData();
//	    gridData.grabExcessHorizontalSpace = true;
//	    gridData.horizontalAlignment = GridData.FILL;
//	    
//	    Label nameLbl = new Label(parent, SWT.NONE);
//	    nameLbl.setText("Name");
//	    
//	    nameTxt = new Text(parent, SWT.BORDER);
//	    nameTxt.setLayoutData(new GridData(GridData.FILL_BOTH));    
//	    
//	    Label descriptionLbl = new Label(parent, SWT.NONE);
//	    descriptionLbl.setText("Description");
//	    
//	    descriptionTxt = new Text(parent, SWT.BORDER | SWT.WRAP | SWT.MULTI);
//	    descriptionTxt.setLayoutData(new GridData(GridData.FILL_BOTH));    
//	    
//	    Button addBtn = new Button(parent, SWT.PUSH);
//	    addBtn.setText("Add");
//	    
//	    Group group = new Group(parent, SWT.NONE);
//	    group.setLayout(new GridLayout(1,false));
//	    group.setLayoutData(new GridData(GridData.FILL_BOTH));
//	    group.setText("Added Environments");
//	    
//	    final Composite groupComposite = new Composite(group,SWT.NONE);
//	    groupComposite.setLayout(new GridLayout(2,false));
//	    
//	    final ConfigDialog configDialog = new ConfigDialog(null);
//	    
//	    environmentList = configDialog.getEnvironment("PHR_q");
//	    for (final Environment environment : environmentList) {
//	    	final String name = environment.getName();
//	    	String description = environment.getDesc();
//	    	label = new Label(groupComposite, SWT.Selection);
//	    	label.setText(name);
//	    	label.setData(name);
//	    	if(!environment.isDefaultEnv()) {
//	    		removeButton = new Button(groupComposite, SWT.PUSH);
//	    		removeButton.setText("X");
//	    		removeButton.setData(name);
//	    	} else {
//	    		new Label(groupComposite, SWT.NONE);
//	    	}
//	    
//	    	environmentList = new ArrayList<Environment>();
//	    	addBtn.addListener(SWT.Selection, new Listener() {
//	    		@Override
//	    		public void handleEvent(Event event) {
//	    			if(!isValidInput()) {
//	    				return;
//	    			}
//	    			environmentList.add(new Environment(nameTxt.getText(), descriptionTxt.getText(), false));
//	    			Label label = new Label(groupComposite, SWT.NONE);
//	    			label.setText(nameTxt.getText());
//	    			removeButton = new Button(groupComposite, SWT.PUSH);
//	    			removeButton.setText("X");
//	    			removeButton.setData(name);
//	    			
//	    			groupComposite.getShell().pack();
//	    			refresh();
//	    		}
//	    	});
//	    	
//	    }
//	    deletableEnvs = new ArrayList<String>();
//	    Control[] children = groupComposite.getChildren();
//	    
//	    for (Control control : children) {
//				if(control instanceof Button) {
//					removeButton = (Button) control;
//					labelValue = (String) removeButton.getData();
////					if(control instanceof Label) {
////					label = (Label) control;
////					labelValue = label.getText();
////					}
//					removeButton.addSelectionListener(new SelectionAdapter() {
//						@Override
//						public void widgetSelected(SelectionEvent e) {
//							System.out.println(labelValue);
//							List<SettingsInfo> configuration = getConfiguration(labelValue);
//							if(configuration.isEmpty()) {
//								System.out.println("kkkkkkkkkkk");
//								deletableEnvs.add(labelValue);
//							} else {
//								setErrorMessage("Environment "+ " ["+ labelValue +"] is already in use");
//							}
//						}
//					});
//				}
//		}
//	    return parent;
//	}
//	
	@Override
	protected boolean isResizable() {
		return true;
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
	    createOkButton(parent, OK, "Save", true);
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
	    button.addListener(SWT.Selection ,new Listener() {
	    	@Override
	    	public void handleEvent(Event event) {
	    			createEnvironment(environmentList,projectCode);
	    			deleteEnvironment(deletableEnvs,projectCode);
	    			okPressed();
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
//	  
//	  private boolean isValidInput() {
//		    boolean valid = true;
//		    if (nameTxt.getText().length() == 0) {
//		      setErrorMessage("Please Enter the Name");
//		      valid = false;
//		    }
//		    if (descriptionTxt.getText().length() == 0) {
//		      setErrorMessage("Please Enter the Description");
//		      valid = false;
//		    }
//		    return valid;
//		  }
	  
	  private void createEnvironment(List<Environment> environmentList,String projectCode) { 
		  try {
			  ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
			  Project project = administrator.getProject(projectCode);
			  administrator.createEnvironments(project, environmentList, false);
		  } catch (PhrescoException e) {
			  e.printStackTrace();
		  }
	  }
	  
	  private String deleteEnvironment(List<String> deletableEnvs,String projectCode) {
	    	try {
	    		ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
	            Project project = administrator.getProject(projectCode);
		    	administrator.deleteEnvironments(deletableEnvs, project);
			
	    	} catch(Exception e) {
	    	}
	    		return "";
	    }

	  private List<SettingsInfo> getConfiguration(String envName,String projectCode) {
		  try {
			  ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
			  Project project = administrator.getProject(projectCode);
			  List<SettingsInfo> configurations = administrator.configurationsByEnvName(envName, project);
			  return configurations;

		  } catch(Exception e) {
		  }
		  return null;
	  }

	  private void refresh() {
		  nameTxt.setText("");
		  descriptionTxt.setText("");
	  }
}
