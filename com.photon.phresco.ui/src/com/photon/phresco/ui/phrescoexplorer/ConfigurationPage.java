package com.photon.phresco.ui.phrescoexplorer;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.handlers.HandlerUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.model.ProjectInfo;
import com.photon.phresco.commons.model.PropertyTemplate;
import com.photon.phresco.commons.model.SettingsTemplate;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.configuration.Configuration;
import com.photon.phresco.configuration.Environment;
import com.photon.phresco.exception.ConfigurationException;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.impl.ConfigManagerImpl;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.ui.model.BaseAction;

public class ConfigurationPage extends AbstractHandler implements  PhrescoConstants {
	private Button addButton;
	private Button deleteButton;
	private Button envSaveButton;
	private Button envCancelButton;
	private Button defaultCheckBoxButton;
	private Button CancelButton;
	private Button configureButton;
	private Button configCancelButton;
	private Button configenvSaveButton;
	private Text descText;
	private Text envText;
	private Shell configDialog;
	private Shell configureDialog;
	private Shell configurationDialog;
	private Listener configureLister;
	private Listener configurationLister;

	private Text nameText;
	private Text configDescText;
	private Combo environmentList;
	private Combo typeList;
	private Text hostText;
	private Text portText;
	private Text contextText;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);

		final Shell dialog = new Shell(shell, SWT.APPLICATION_MODAL| SWT.DIALOG_TRIM);

		GridLayout layout = new GridLayout(3, false);
		layout.verticalSpacing = 6;

		dialog.setText(ENVIROMENT);
		dialog.setLocation(385,130);
		dialog.setSize(400, 230);
		dialog.setLayout(layout);


		addButton = new Button(dialog, SWT.BUTTON1);
		addButton.setText(ENVIROMENT);
		addButton.setLayoutData(new GridData(100,25));

		deleteButton = new Button(dialog, SWT.BUTTON1);
		deleteButton.setText(DELETE);
		deleteButton.setLayoutData(new GridData(50,25));
		deleteButton.setVisible(false);


		final Table table = new Table(dialog,  SWT.BORDER | SWT.MULTI);
		GridData data = new GridData(GridData.VERTICAL_ALIGN_FILL);
		GridLayout tableLayout = new GridLayout(5, true);
		data.horizontalSpan = 20;
		data.verticalSpan = 20;
		table.setLayoutData(data);
		table.setLayout(tableLayout);
		table.setSize(200,175);
		Color color = new Color(null, new RGB(235, 233, 216));
		table.setBackground(color);

		TableColumn name = new TableColumn(table, SWT.CENTER);
		TableColumn desc = new TableColumn(table, SWT.CENTER);
		TableColumn configure = new TableColumn(table, SWT.CENTER);
		TableColumn clone = new TableColumn(table, SWT.CENTER);
		TableColumn delete = new TableColumn(table, SWT.CENTER);

		name.setText(NAME);
		desc.setText(DESCRITPTION);
		configure.setText(CONFIGURE);
		clone.setText(CLONE);
		delete.setText(DELETE);

		name.setWidth(80);
		desc.setWidth(80);
		configure.setWidth(80);
		clone.setWidth(80);
		delete.setWidth(80);

		table.setHeaderVisible(true);


		CancelButton = new Button(dialog, SWT.PUSH);
		CancelButton.setText(CANCEL);
		CancelButton.setLayoutData(new GridData(75,20));
		CancelButton.setLocation(75, 80);

		configDialog = createEnvironmentDialog(dialog);

		deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if ( table.getItemCount() > 0)
				{
					int[] row = table.getSelectionIndices();
					table.remove(row); 
					deleteButton.setVisible(false);
				}
			}
		});

		Listener buttonListener = new Listener() {
			public void handleEvent(Event event) {
				configDialog.open();
			}
		};

		table.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				deleteButton.setVisible(true);
			}
		});


		Listener envCancelListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				envText.setText("");
				descText.setText("");
				configDialog.setVisible(false);
				configCancelButton.setVisible(false);
			}
		};


		Listener cancelListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				dialog.close();
			}
		};


		Listener envSaveListener = new Listener() {
			public void handleEvent(Event event) {
				String environmentName = envText.getText();
				String description = descText.getText();
				boolean selection = defaultCheckBoxButton.getSelection();


				List<Environment> environments = new ArrayList<Environment>();

				Environment environment = new Environment();
				environment.setDefaultEnv(selection);
				environment.setName(environmentName);
				environment.setDesc(description);

				environments.add(environment);

				File projectFilePath = new File(getProjectHome() + File.separator + "TestProject" + File.separator + DOT_PHRESCO_FOLDER + File.separator + ENVIRONMENT_CONFIG_FILE);
				try {
					ConfigManagerImpl impl = new ConfigManagerImpl(projectFilePath);
					impl.addEnvironments(environments);
				} catch (ConfigurationException e) {
					e.printStackTrace();
				}

				TableItem items = new TableItem(table, SWT.FILL);

				items.setText(new String[]{environmentName,description});

				Composite configureButtonPan = new Composite(table, SWT.NONE);
				configureButtonPan.setLayout(new FillLayout());


				configureButton = new Button(configureButtonPan, SWT.NONE);
				configureButton.setText(CONFIGURE);


				TableEditor editor = new TableEditor(table);
				editor.grabHorizontal  = true;
				editor.grabVertical = true;
				editor.setEditor(configureButtonPan, items, 2);
				editor.layout();

				envText.setText("");
				descText.setText("");
				configDialog.setVisible(false);
				configureButton.addListener(SWT.Selection, configureLister);
			}
		};

		configurationLister = new Listener() {

			@Override
			public void handleEvent(Event event) {
				String name = nameText.getText();
				String desc = configDescText.getText();
				String host = hostText.getText();
				String port = portText.getText();
				String context = contextText.getText();
				String type = typeList.getText();

				String environmentName = environmentList.getText();
				Properties properties = new Properties();

				Configuration configuration = new Configuration();
				configuration.setEnvName(environmentName);
				configuration.setDesc(desc);
				configuration.setName(name);
				configuration.setType(type);

				properties.setProperty("host", host);
				properties.setProperty("port", port);
				properties.setProperty("context",context);

				configuration.setProperties(properties);

				List<Configuration> configurations = new ArrayList<Configuration>();
				configurations.add(configuration);

				File ProjectFilePath = new File(getProjectHome() + File.separator + "TestProject" + File.separator + DOT_PHRESCO_FOLDER + File.separator + ENVIRONMENT_CONFIG_FILE);
				try {
					ConfigManagerImpl impl = new ConfigManagerImpl(ProjectFilePath);
					impl.createConfiguration(environmentName, configurations);
				} catch (ConfigurationException e) {
					e.printStackTrace();
				}
			}
		};



		configureLister = new Listener() {
			@Override
			public void handleEvent(Event event) {
				SettingsTemplate serverTemplate = null;
				try {
					BaseAction baseAction = new BaseAction();
					ProjectInfo info = readProjectInfo();
					String techId = info.getAppInfos().get(0).getTechInfo().getId();
					ServiceManager serviceManager = PhrescoUtil.getServiceManager(baseAction.getUserId());
					serverTemplate = serviceManager.getConfigTemplateByTechId(techId, "Server");
					//					createTemplate(serverTemplate);

				} catch (PhrescoException e) {
					e.printStackTrace();
				}

				configureDialog = createServerDialog(dialog, serverTemplate);
				configureDialog.open();
			}

		};

		addButton.addListener(SWT.Selection, buttonListener);
		envSaveButton.addListener(SWT.Selection, envSaveListener);
		envCancelButton.addListener(SWT.Selection, envCancelListener);
		CancelButton.addListener(SWT.Selection, cancelListener);

		dialog.open();
		return null;

	}


	private Shell createEnvironmentDialog(Shell dialog) {
		final Shell configDialog = new Shell(dialog, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		configDialog.setText("Environment");
		configDialog.setLocation(385,130);
		configDialog.setSize(400, 230);

		GridLayout subLayout = new GridLayout(2, false);
		subLayout.verticalSpacing = 20;
		subLayout.horizontalSpacing = 60;
		configDialog.setLayout(subLayout);

		Label envLabel = new  Label(configDialog,  SWT.LEFT);
		envLabel.setText(ENVIRONMENT_NAME);
		envLabel.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
		envLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

		envText = new Text(configDialog, SWT.BORDER); 
		envText.setToolTipText(ENVIRONMENT_NAME);
		envText.setMessage(ENVIRONMENT_NAME);
		envText.setLayoutData(new GridData(80,13));


		Label descLabel = new  Label(configDialog,  SWT.LEFT);
		descLabel.setText(DESCRITPTION);
		descLabel.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
		envLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

		descText = new Text(configDialog, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL); 
		descText.setToolTipText(DESCRITPTION);
		descText.setLayoutData(new GridData(80,13));
		descText.setMessage(DESCRITPTION);

		Label defaults = new  Label(configDialog,  SWT.LEFT);
		defaults.setText(DEFAULT);
		defaults.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
		defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

		defaultCheckBoxButton = new Button(configDialog, SWT.CHECK);
		defaultCheckBoxButton.setLayoutData(new GridData(75,20));


		envSaveButton = new Button(configDialog, SWT.PUSH);
		envSaveButton.setText(SAVE);
		envSaveButton.setLayoutData(new GridData(75,20));
		envSaveButton.setLocation(500,505);


		envCancelButton = new Button(configDialog, SWT.PUSH);
		envCancelButton.setText(CANCEL);
		envCancelButton.setLayoutData(new GridData(75,20));

		return configDialog;
	}


	private Shell createServerDialog(Shell dialog, SettingsTemplate serverTemplate) {
		configurationDialog = new Shell(dialog, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		configurationDialog.setText(CONFIGURATION);
		configurationDialog.setLocation(385,130);
		configurationDialog.setSize(400, 600);

		GridLayout subLayout = new GridLayout(2, false);
		subLayout.verticalSpacing = 20;
		subLayout.horizontalSpacing = 60;
		configurationDialog.setLayout(subLayout);

		List<PropertyTemplate> propertyTemplates  = serverTemplate.getProperties();
		for (PropertyTemplate propertyTemplate : propertyTemplates) {

			String type = propertyTemplate.getType();
			if (type.equalsIgnoreCase(STRING) || type.equalsIgnoreCase(NUMBER)) {
				Label envLabel = new Label(configurationDialog, SWT.NONE);
				envLabel.setText(propertyTemplate.getName());
				envLabel.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
				envLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));


				nameText = new Text(configurationDialog, SWT.BORDER); 
				nameText.setToolTipText(ENVIRONMENT_NAME);
				nameText.setMessage(NAME);
				nameText.setLayoutData(new GridData(80,13));
				nameText.setText(propertyTemplate.getKey());

			} else if (type.equalsIgnoreCase(BOOLEAN)) {

				Label defaults = new  Label(configurationDialog,  SWT.LEFT);
				defaults.setText(propertyTemplate.getName());
				defaults.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
				defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

				defaultCheckBoxButton = new Button(configurationDialog, SWT.CHECK);
				defaultCheckBoxButton.setLayoutData(new GridData(75,20));
				defaultCheckBoxButton.setText(propertyTemplate.getKey());

			} else if (type.equalsIgnoreCase(PASSWORD)) {

				Label defaults = new  Label(configurationDialog,  SWT.LEFT);
				defaults.setText(propertyTemplate.getName());
				defaults.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
				defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			
				defaultCheckBoxButton = new Button(configurationDialog, SWT.PASSWORD);
				defaultCheckBoxButton.setLayoutData(new GridData(75,20));
			}
			
		}

		configenvSaveButton = new Button(configurationDialog, SWT.PUSH);
		configenvSaveButton.setText(SAVE);
		configenvSaveButton.setLayoutData(new GridData(75,20));
		configenvSaveButton.setLocation(500,505);

		
		configCancelButton = new Button(configurationDialog, SWT.PUSH);
		configCancelButton.setText(CANCEL);
		configCancelButton.setLayoutData(new GridData(75,20));
		return configurationDialog;
	}


	private File getProjectHome() {
		File projectPath = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString() + File.separator + PROJECTS);
		return projectPath;
	}

	private ProjectInfo readProjectInfo() throws PhrescoException {
		try {
			File projectFilePath = new File(getProjectHome() + File.separator + "TestProject" + File.separator + DOT_PHRESCO_FOLDER + File.separator + PROJECT_INFO);
			FileReader reader = new FileReader(projectFilePath);
			Gson  gson = new Gson();
			Type type = new TypeToken<ProjectInfo>() {}.getType();
			ProjectInfo info = gson.fromJson(reader, type);
			return info;
		} catch (FileNotFoundException e) {
			throw new PhrescoException(e);
		}
	}


}

