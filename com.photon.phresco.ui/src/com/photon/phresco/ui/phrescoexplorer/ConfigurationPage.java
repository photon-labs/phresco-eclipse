package com.photon.phresco.ui.phrescoexplorer;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.handlers.HandlerUtil;

import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.configuration.Configuration;
import com.photon.phresco.configuration.Environment;
import com.photon.phresco.exception.ConfigurationException;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.impl.ConfigManagerImpl;

public class ConfigurationPage extends AbstractHandler implements  PhrescoConstants {
	private Button envSaveButton;
	private Button envCancelButton;
	private Button defaultCheckBoxButton;
	private Text descText;
	private Text envText;

	private Tree tree;
	private Shell envDialog;
	private Shell configureDialogs;
	private Button addEnvironmentButton;

	private Listener envSaveListener;
	TreeItem itemTemplate;
	private Shell shell;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		shell = HandlerUtil.getActiveShell(event);

		final Shell dialog = new Shell(shell, SWT.APPLICATION_MODAL| SWT.DIALOG_TRIM | SWT.TITLE | SWT.MAX | SWT.MIN | SWT.RESIZE);

		GridLayout layout = new GridLayout(2,false);
		layout.verticalSpacing = 6;

		Shell createConfigurationDialog = createConfigurationDialog(dialog);
		try {
			File configurationFile = PhrescoUtil.getConfigurationFile();
			ConfigManagerImpl impl = new ConfigManagerImpl(configurationFile);
			List<Environment> environments = impl.getEnvironments();
			for (Environment environment : environments) {
				itemTemplate = new TreeItem(tree, SWT.FILL);
				String env = environment.getName();
				String desc = environment.getDesc();
				itemTemplate.setText(new String [] {env,desc,"status"});
				List<Configuration> configurations = environment.getConfigurations();
				for (Configuration configuration : configurations) {
					TreeItem item = new TreeItem(itemTemplate, SWT.FILL);
					String name = configuration.getName();
					String description = configuration.getDesc();
					item.setText(new String [] {name,description,"status"});
				}
			}

		} catch (PhrescoException e1) {
			e1.printErrorStack();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}

		createConfigurationDialog.open();

		addEnvironmentButton.addListener(SWT.Selection , new Listener() {
			@Override
			public void handleEvent(Event event) {
				configureDialogs.setVisible(false);
				Shell createEnvironmentDialog = createEnvironmentDialog(dialog);
				envSaveButton.addListener(SWT.Selection, envSaveListener);
				createEnvironmentDialog.open();
			}
		});


		envSaveListener = new Listener() {
			public void handleEvent(Event event) {
				envDialog.setVisible(false);
				configureDialogs.setVisible(true);
				String environmentName = envText.getText();
				String description = descText.getText();
				boolean selection = defaultCheckBoxButton.getSelection();
				List<Environment> environments = new ArrayList<Environment>();

				Environment environment = new Environment();
				environment.setDefaultEnv(selection);
				environment.setName(environmentName);
				environment.setDesc(description);

				environments.add(environment);
				try {
					ConfigManagerImpl impl = new ConfigManagerImpl(PhrescoUtil.getConfigurationFile());
					List<Environment> environmentList = impl.getEnvironments();
					environmentList.add(environment);
					impl.addEnvironments(environmentList);
				} catch (com.photon.phresco.exception.ConfigurationException e) {
					e.printStackTrace();
				} catch (PhrescoException e) {
					e.printStackTrace();
				}

				itemTemplate = new TreeItem(tree, SWT.FILL);
				itemTemplate.setText(new String [] {environmentName,description,"status"});
			}

		};

		return null;

	}

	private Shell createConfigurationDialog(final Shell shell) {
		configureDialogs = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
		configureDialogs.setText("Environment");
		configureDialogs.setLocation(385,130);
		configureDialogs.setSize(400, 230);

		GridLayout subLayout = new GridLayout(1, false);
		configureDialogs.setLayout(subLayout);

		Composite composite = new Composite(configureDialogs, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		tree = new Tree(composite, SWT.BORDER);
		tree.setHeaderVisible(true);
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		TreeColumn name = new TreeColumn(tree, SWT.LEFT);
		name.setText("Name");
		name.setWidth(100);
		final TreeColumn desc = new TreeColumn(tree, SWT.CENTER);
		desc.setText("Description");
		desc.setWidth(100);
		TreeColumn configure = new TreeColumn(tree, SWT.RIGHT);
		configure.setText("configure");
		configure.setWidth(100);

		GridLayout tableLayout = new GridLayout(3,false);

		Composite composites = new Composite(configureDialogs, SWT.NONE);
		composites.setLayout(tableLayout);
		GridData tablecolumndata = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		composites.setLayoutData(tablecolumndata);

		GridData data = new GridData();
		data.horizontalAlignment = SWT.RIGHT;

		addEnvironmentButton = new Button(composites, SWT.PUSH);
		addEnvironmentButton.setLocation(279, 121);
		addEnvironmentButton.setText("Environments");
		addEnvironmentButton.setSize(74, 23);
		addEnvironmentButton.setLayoutData(data);
		
		Button addButton = new Button(composites, SWT.PUSH);
		addButton.setLocation(279, 121);
		addButton.setText("Add");
		addButton.setSize(74, 23);
		addButton.setLayoutData(data);
		
		Button deleteButton = new Button(composites, SWT.PUSH);
		deleteButton.setLocation(279, 121);
		deleteButton.setText("Delete");
		deleteButton.setSize(74, 23);
		deleteButton.setLayoutData(data);
		
		addButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				configureDialogs.setVisible(false);
				ConfigurationCreation creation = new ConfigurationCreation();
				creation.createTemplateByType(tree);
			}

		});
		

//		tree.addListener(SWT.MouseDown, new Listener() {
//			public void handleEvent(Event event) {
//				Point point = new Point(event.x, event.y);
//				TreeItem item = tree.getItem(point);
//				if (item != null) {
//					TreeItem parentItem = item.getParentItem();
//					ConfigurationCreation.createConfigurationDialog(shell,parentItem ,item); 
//				} 
//			}
//		});

		return configureDialogs;
	}

	public Shell createEnvironmentDialog(Shell dialog) {
		envDialog = new Shell(dialog, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		envDialog.setText("Environment");
		envDialog.setLocation(385,130);
		envDialog.setSize(400, 230);

		GridLayout subLayout = new GridLayout(2, false);
		subLayout.verticalSpacing = 20;
		subLayout.horizontalSpacing = 60;
		envDialog.setLayout(subLayout);

		Label envLabel = new  Label(envDialog,  SWT.LEFT);
		envLabel.setText(ENVIRONMENT_NAME);
		envLabel.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
		envLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

		envText = new Text(envDialog, SWT.BORDER); 
		envText.setToolTipText(ENVIRONMENT_NAME);
		envText.setMessage(ENVIRONMENT_NAME);
		envText.setLayoutData(new GridData(80,13));


		Label descLabel = new  Label(envDialog,  SWT.LEFT);
		descLabel.setText(DESCRITPTION);
		descLabel.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
		envLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

		descText = new Text(envDialog, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL); 
		descText.setToolTipText(DESCRITPTION);
		descText.setLayoutData(new GridData(80,13));
		descText.setMessage(DESCRITPTION);

		Label defaults = new  Label(envDialog,  SWT.LEFT);
		defaults.setText(DEFAULT);
		defaults.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
		defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

		defaultCheckBoxButton = new Button(envDialog, SWT.CHECK);
		defaultCheckBoxButton.setLayoutData(new GridData(75,20));


		envSaveButton = new Button(envDialog, SWT.PUSH);
		envSaveButton.setText(SAVE);
		envSaveButton.setLayoutData(new GridData(75,20));
		envSaveButton.setLocation(500,505);


		envCancelButton = new Button(envDialog, SWT.PUSH);
		envCancelButton.setText(CANCEL);
		envCancelButton.setLayoutData(new GridData(75,20));

		return envDialog;
	}
}

