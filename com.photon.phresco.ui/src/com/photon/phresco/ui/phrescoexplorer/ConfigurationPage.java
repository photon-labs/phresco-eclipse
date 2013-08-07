package com.photon.phresco.ui.phrescoexplorer;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
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

import com.photon.phresco.commons.ConfirmDialog;
import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.configuration.Configuration;
import com.photon.phresco.configuration.Environment;
import com.photon.phresco.exception.ConfigurationException;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.impl.ConfigManagerImpl;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.ui.model.BaseAction;
import com.photon.phresco.ui.resource.Messages;

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
	private Button cancelButton;
	private Shell createConfigurationDialog;
	private Map<String, Object> treeMap = new HashMap<String, Object>();
	

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		shell = HandlerUtil.getActiveShell(event);

		BaseAction baseAction = new BaseAction();
		ServiceManager serviceManager = PhrescoUtil.getServiceManager(baseAction.getUserId());
		if(serviceManager == null) {
			ConfirmDialog.getConfirmDialog().showConfirm(shell);
			return null;
		}
		
		final Shell dialog = new Shell(shell, SWT.APPLICATION_MODAL| SWT.DIALOG_TRIM | SWT.TITLE | SWT.MAX | SWT.MIN | SWT.RESIZE);

		GridLayout layout = new GridLayout(2,false);
		layout.verticalSpacing = 6;

		CreateEnvironmentConfigureDialog(dialog);

		return null;

	}

	private Shell CreateEnvironmentConfigureDialog(final Shell dialog) {
		createConfigurationDialog = createConfigurationDialog(dialog);
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
				try {
					envDialog.setVisible(false);
					configureDialogs.setVisible(true);
					String environmentName = envText.getText();
					String description = descText.getText();
					boolean selection = defaultCheckBoxButton.getSelection();
					ConfigManagerImpl impl = new ConfigManagerImpl(PhrescoUtil.getConfigurationFile());
					List<Environment> environments = new ArrayList<Environment>();
					List<Environment> envList = impl.getEnvironments();
					for (Environment environment : envList) {
						boolean defaultEnv = environment.isDefaultEnv();
						if (defaultEnv) {
							environment.setDefaultEnv(false);
							impl.updateEnvironment(environment);
						}
					}
					Environment environment = new Environment();
					environment.setDefaultEnv(selection);
					environment.setName(environmentName);
					environment.setDesc(description);

					environments.add(environment);
					List<Environment> environmentList = impl.getEnvironments();
					environmentList.add(environment);
					impl.addEnvironments(environmentList);
					itemTemplate = new TreeItem(tree, SWT.FILL);
					itemTemplate.setText(new String [] {environmentName,description,"status"});
				} catch (PhrescoException e) {
					e.printStackTrace();
				} catch (ConfigurationException e) {
					e.printStackTrace();
				}
			}

		};
		return createConfigurationDialog;
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	private Shell createConfigurationDialog(final Shell shell) {
		configureDialogs = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
		configureDialogs.setText("Environment");
		configureDialogs.setLocation(385,130);
		configureDialogs.setSize(416, 230);

		GridLayout subLayout = new GridLayout(1, false);
		configureDialogs.setLayout(subLayout);

		Composite composite = new Composite(configureDialogs, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		tree = new Tree(composite,  SWT.BORDER);
		tree.setHeaderVisible(true);
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		TreeColumn name = new TreeColumn(tree, SWT.LEFT);
		name.setText("Name");
		name.setWidth(100);
		final TreeColumn desc = new TreeColumn(tree, SWT.CENTER);
		desc.setText("Description");
		desc.setWidth(100);
		TreeColumn status = new TreeColumn(tree, SWT.RIGHT);
		status.setText("Status");
		status.setWidth(100);

		GridLayout tableLayout = new GridLayout(5,false);

		Composite composites = new Composite(configureDialogs, SWT.NONE);
		composites.setLayout(tableLayout);
		composites.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		addEnvironmentButton = new Button(composites, SWT.PUSH);
		addEnvironmentButton.setText("Add Environment");
		
		Button addButton = new Button(composites, SWT.PUSH);
		addButton.setText("Add Configuration");
		
		final Button configureButton = new Button(composites, SWT.PUSH);
		configureButton.setText("Configure");
		configureButton.setEnabled(false);
		
		final Button deleteButton = new Button(composites, SWT.PUSH);
		deleteButton.setText("Delete");
		deleteButton.setEnabled(false);
		
		cancelButton = new Button(composites, SWT.PUSH);
		cancelButton.setText("Cancel");
		
		addButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				ConfigurationCreation creation = new ConfigurationCreation();
				creation.createTemplateByType(configureDialogs, tree);
			}
		});

		final ConfigurationCreation creation = new ConfigurationCreation();
		
		
		tree.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				configureButton.setEnabled(true);
				deleteButton.setEnabled(true);
				treeMap.clear();
				Point point = new Point(event.x, event.y);
				final TreeItem child = tree.getItem(point);
				if (child != null) {
				TreeItem parent = child.getParentItem();
					if (parent != null) {
						treeMap.put("parent", parent);
						treeMap.put("child", child);
					} else {
						treeMap.put("parentTree", child);
					}
				}
			}
		});	
		
		configureButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				TreeItem parentTree = (TreeItem) treeMap.get("parentTree");
				if (parentTree == null) {
					TreeItem parent = (TreeItem) treeMap.get("parent");
					TreeItem child =  (TreeItem) treeMap.get("child");
					System.out.println("Parent Text = " + parent.getText());
					System.out.println("child Text = " + child.getText());
					creation.configure(parent, child);
				} else {
//					envDialog.close();
//					configureEnvironment(parentTree);
				}
			}
		});
		
		
		deleteButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				TreeItem parentTree = (TreeItem) treeMap.get("parentTree");
				if (parentTree != null) {
					creation.deleteParent(parentTree);
					refreshconfigureDialog(shell);
				} else {
					TreeItem parent = (TreeItem) treeMap.get("parent");
					TreeItem child =  (TreeItem) treeMap.get("child");
					creation.delete(parent, child);
					refreshconfigureDialog(shell);
				}
			}

		});
		
		
		cancelButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				shell.close();
			}
		});
		
		return configureDialogs;
	}

	private void refreshconfigureDialog(final Shell shell) {
		configureDialogs.close();
		final Shell dialog = new Shell(shell, SWT.APPLICATION_MODAL| SWT.DIALOG_TRIM | SWT.TITLE | SWT.MAX | SWT.MIN | SWT.RESIZE);
		GridLayout layout = new GridLayout(2,false);
		layout.verticalSpacing = 6;
		Shell configDialog = CreateEnvironmentConfigureDialog(dialog);
		configDialog.open();
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
		envCancelButton.setText(Messages.CANCEL);
		envCancelButton.setLayoutData(new GridData(75,20));

		return envDialog;
	}
	
	public Shell configureEnvironment(TreeItem parentTree) {

		final Shell envDialog = new Shell(new Shell(), SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		try {
			File envConfig = PhrescoUtil.getConfigurationFile();
			ConfigManagerImpl impl = new ConfigManagerImpl(envConfig);
			List<Environment> environments = impl.getEnvironments();
			if (CollectionUtils.isNotEmpty(environments)) {
				for (Environment environment : environments) {
					if (environment.getName().equalsIgnoreCase(parentTree.getText())) {
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

						Text envText = new Text(envDialog, SWT.BORDER); 
						envText.setToolTipText(ENVIRONMENT_NAME);
						envText.setLayoutData(new GridData(80,13));
						envText.setText(environment.getName());

						Label descLabel = new  Label(envDialog,  SWT.LEFT);
						descLabel.setText(DESCRITPTION);
						descLabel.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
						envLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

						Text descText = new Text(envDialog, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL); 
						descText.setToolTipText(DESCRITPTION);
						descText.setLayoutData(new GridData(80,13));
						descText.setText(environment.getDesc());

						Label defaults = new  Label(envDialog,  SWT.LEFT);
						defaults.setText(DEFAULT);
						defaults.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
						defaults.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

						defaultCheckBoxButton = new Button(envDialog, SWT.CHECK);
						defaultCheckBoxButton.setLayoutData(new GridData(75,20));
						defaultCheckBoxButton.setText(String.valueOf(environment.isDefaultEnv()));					}
				}
			}

			envSaveButton = new Button(envDialog, SWT.PUSH);
			envSaveButton.setText(SAVE);
			envSaveButton.setLayoutData(new GridData(75,20));
			envSaveButton.setLocation(500,505);

			envCancelButton = new Button(envDialog, SWT.PUSH);
			envCancelButton.setText(Messages.CANCEL);
			envCancelButton.setLayoutData(new GridData(75,20));

		} catch (Exception e) {
			e.printStackTrace();
		}

		return envDialog;
	}


	public void push() {
		final Shell dialog = new Shell(shell, SWT.APPLICATION_MODAL| SWT.DIALOG_TRIM | SWT.TITLE | SWT.MAX | SWT.MIN | SWT.RESIZE);
		GridLayout layout = new GridLayout(2,false);
		layout.verticalSpacing = 6;
		Shell configDialog = CreateEnvironmentConfigureDialog(dialog);
		configDialog.open();
		
	}
}

