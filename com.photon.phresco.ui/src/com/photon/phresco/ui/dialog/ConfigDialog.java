package com.photon.phresco.ui.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.photon.phresco.commons.FrameworkConstants;
import com.photon.phresco.configuration.Environment;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.framework.PhrescoFrameworkFactory;
import com.photon.phresco.framework.api.Project;
import com.photon.phresco.framework.api.ProjectAdministrator;
import com.photon.phresco.model.I18NString;
import com.photon.phresco.model.PropertyInfo;
import com.photon.phresco.model.PropertyTemplate;
import com.photon.phresco.model.SettingsInfo;
import com.photon.phresco.model.SettingsTemplate;

public class ConfigDialog extends TitleAreaDialog {

private Composite configComposite;
	
	private Combo possibleValueCombo;
	
	private Text nameTxt;
	
	private Text descriptionTxt;
	
	private Combo typeCombo;
	
	private Combo environmentCombo;
	
	private ProjectAdministrator administrator;
	
	private SettingsInfo settingsInfo;
	
	private List<PropertyInfo> propertyInfoList;
	
	private String projectCode;
	
	public ConfigDialog(Shell parentShell,String projectCode) {
		super(parentShell);
		setTitle("Configurations");
		this.projectCode = projectCode;
	}
	
	@Override
		public void create() {
			super.create();
			setTitle("Configurations");
		}
	
	@Override
	protected Control createDialogArea(final Composite parent) {
		GridLayout layout = new GridLayout();
	    layout.numColumns = 1;
	    parent.setLayout(layout);
		
		Composite envComposite = new Composite(parent, SWT.NONE);
		envComposite.setLayout(new GridLayout(2, false));
		envComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label nameLbl = new Label(envComposite, SWT.NONE);
		nameLbl.setText("Name");
		
		nameTxt = new Text(envComposite, SWT.BORDER);
		nameTxt.setMessage("Name of the configuration");
		
		Label descriptionLbl = new Label(envComposite, SWT.NONE);
		descriptionLbl.setText("Description");
		
		descriptionTxt = new Text(envComposite, SWT.BORDER | SWT.WRAP | SWT.MULTI);
		descriptionTxt.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label lblEnviroments = new Label(envComposite, SWT.NONE);
		lblEnviroments.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		lblEnviroments.setText("Enviroments");
		
		ComboViewer comboViewer = new ComboViewer(envComposite, SWT.NONE|SWT.READ_ONLY);
		environmentCombo = comboViewer.getCombo();
		
		List<Environment> environments = getEnvironment(projectCode);
		
		comboViewer.setContentProvider(new ArrayContentProvider());
		comboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if(element instanceof Environment) {
					return ((Environment)element).getName();
				}
				return super.getText(element);
			}
		});
		comboViewer.setInput(environments);
		comboViewer.getCombo().select(0);
		Label typeLbl = new Label(envComposite, SWT.NONE);
		typeLbl.setText("Type");
		
		typeCombo = new Combo(envComposite, SWT.READ_ONLY);
		ComboViewer typeComboViewer = new ComboViewer(typeCombo);
		List<SettingsTemplate> types = getSettingsTemplates("photon");
		typeComboViewer.setContentProvider(new ArrayContentProvider());
		typeComboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if(element instanceof SettingsTemplate) {
					return ((SettingsTemplate)element).getType();
				}
				return super.getText(element);
			}
		});
		typeComboViewer.setInput(types);

		configComposite = new Composite(parent, SWT.NONE);
		configComposite.setLayout(new GridLayout(2,false));
		configComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		typeCombo.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				
				SettingsTemplate settingsTemplate = getSettingsTemplate(typeCombo.getText(),"photon");
				List<PropertyTemplate> properties = settingsTemplate.getProperties();
				List<String> possibleValues = new ArrayList<String>();
				Label label;
				Text text = null;
				
				Control[] configchildren = configComposite.getChildren();
			
				if(configchildren != null ) {
					for (Control control : configchildren) {
						control.dispose();
					}
				}
				
				for (PropertyTemplate propertyTemplate : properties) {
					if(propertyTemplate == null) {
						return;
					}
					possibleValues = propertyTemplate.getPossibleValues();
					String key = propertyTemplate.getKey();
					label = new Label(configComposite, SWT.NONE);
					I18NString I18NStringName = propertyTemplate.getName();
					String localeString = Locale.getDefault().getLanguage()+"-"+Locale.getDefault().getCountry();
					String name = I18NStringName.get(localeString).getValue();
					label.setText(name);
					I18NString I18NStringDescription = propertyTemplate.getDescription();
					
					String description = I18NStringDescription.get(localeString).getValue();
					if (key.equals(FrameworkConstants.ADMIN_FIELD_PASSWORD) || key.equals(FrameworkConstants.PASSWORD)) {
						text = new Text(configComposite, SWT.BORDER | SWT.PASSWORD);
						text.setMessage(description);
						text.setData(key);
					} else if (possibleValues != null && !possibleValues.isEmpty() && possibleValues.size() > 0) {
						String[] possibleValueArray = new String[possibleValues.size()];
						possibleValueArray = possibleValues.toArray(possibleValueArray);
						possibleValueCombo = new Combo(configComposite, SWT.READ_ONLY);
						possibleValueCombo.setItems(possibleValueArray);
						possibleValueCombo.select(0);
						possibleValueCombo.setData(key);
					} else {
						text = new Text(configComposite, SWT.BORDER);
						text.setMessage(description);
						text.setData(key);
					}
				}
				
				Button saveBtn = new Button(configComposite, SWT.PUSH);
				saveBtn.setText("Save");
				
				saveBtn.addListener (SWT.Selection, new Listener() {
					public void handleEvent (Event e) {
						addSave();
//						Control[] children = configComposite.getChildren();
//						String key = "";
//						String value = "";
//						propertyInfoList = new ArrayList<PropertyInfo>();
//						for (Control control : children) {
//							if(control instanceof Text) {
//								Text text = (Text) control;
//								String propertyTemplate = (String) control.getData();
//								 key = propertyTemplate;
//								 value = text.getText().trim();
//								 System.out.println(key + value);
//								propertyInfoList.add(new PropertyInfo(key, value));
//							}
//							else if(control instanceof Combo) {
//								Combo combo = (Combo) control;
//								String propertyTemplate = (String) control.getData();
//								 key = propertyTemplate;
//								value = combo.getText().trim();
//								System.out.println(key + value);
//								propertyInfoList.add(new PropertyInfo(key, value));
//							}
//						}	
//						save(propertyInfoList,"PHR_q");
					}
				});
				parent.getShell().pack();
			
				
			}
		});

		typeCombo.select(0);
		this.getShell().pack();
		return parent;
	}

	public List<Environment> getEnvironment(String environmentName) { 
		try {
			administrator = PhrescoFrameworkFactory.getProjectAdministrator();
			Project project = administrator.getProject(environmentName);
			List<Environment> environments = administrator.getEnvironments(project);
			return environments;
	} catch(Exception e) {
		
	}
		return null;
	}
	public List<SettingsTemplate> getSettingsTemplates(String custometId) { 
		try {
			administrator = PhrescoFrameworkFactory.getProjectAdministrator();
			List<SettingsTemplate> settingsTemplates = administrator.getSettingsTemplates(custometId);
			return settingsTemplates;
	} catch(Exception e) {
		
	}
		return null;
	}
	
	private SettingsTemplate getSettingsTemplate(String type,String customerId) { 
		try {
			administrator = PhrescoFrameworkFactory.getProjectAdministrator();
			SettingsTemplate settingsTemplate = administrator.getSettingsTemplate(type,customerId);
			return settingsTemplate;
		} catch (PhrescoException e) {
		}
		return null;
	}
	
	public void addSave() { 

		Control[] children = configComposite.getChildren();
		String key = "";
		String value = "";
		propertyInfoList = new ArrayList<PropertyInfo>();
		for (Control control : children) {
			if(control instanceof Text) {
				Text text = (Text) control;
				String propertyTemplate = (String) control.getData();
				key = propertyTemplate;
				value = text.getText().trim();
				propertyInfoList.add(new PropertyInfo(key, value));
			}
			else if(control instanceof Combo) {
				Combo combo = (Combo) control;
				String propertyTemplate = (String) control.getData();
				key = propertyTemplate;
				value = combo.getText().trim();
				propertyInfoList.add(new PropertyInfo(key, value));
			}
		}	
		save(propertyInfoList,projectCode);

	}
	
	private void save(List<PropertyInfo> propertyInfoList,String projectCode) {
		String name = nameTxt.getText();
		String description = descriptionTxt.getText();
		String type = typeCombo.getText();
		String env = environmentCombo.getText();
		settingsInfo = new SettingsInfo(name, description, type);
		settingsInfo.setEnvName(env);
		settingsInfo.setPropertyInfo(propertyInfoList);
		try {
			administrator = PhrescoFrameworkFactory.getProjectAdministrator();
			Project project = administrator.getProject(projectCode);
			if(project != null && settingsInfo != null) {
			administrator.createConfiguration(settingsInfo, env, project); 
			}
		} catch (PhrescoException e) {
		}
	}

	public SettingsInfo getSettingsInfo() {
		return settingsInfo;
	}
}
