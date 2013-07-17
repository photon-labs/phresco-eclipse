package com.photon.phresco.ui.phrescoexplorer;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.handlers.HandlerUtil;

import com.photon.phresco.commons.PhrescoConstants;

public class ConfigurationPage extends AbstractHandler implements  PhrescoConstants {
	private Button addButton;
	private Button deleteButton;
	private Button envbutton;
	private Button envSaveButton;
	private Button envCancelButton;
	private Text descText;
	private Text envText;
	private Shell configDialog;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);

		final Shell dialog = new Shell(shell, SWT.APPLICATION_MODAL| SWT.DIALOG_TRIM);

		GridLayout layout = new GridLayout(3, false);
		layout.verticalSpacing = 6;

		dialog.setText("Environment");
		dialog.setLocation(385,130);
		dialog.setSize(430, 350);
		dialog.setLayout(layout);


		addButton = new Button(dialog, SWT.BUTTON1);
		addButton.setText("Add");
		addButton.setLayoutData(new GridData(50,25));

		deleteButton = new Button(dialog, SWT.BUTTON1);
		deleteButton.setText("Delete");
		deleteButton.setLayoutData(new GridData(50,25));

		envbutton = new Button(dialog, SWT.BUTTON1);
		envbutton.setText("Environment");
		envbutton.setLayoutData(new GridData(70,25));
		
		
		
		configDialog = createDialog(dialog);

		Listener buttonListener = new Listener() {
			public void handleEvent(Event event) {
				configDialog.open();
			}
		};


		Listener saveListener = new Listener() {
			public void handleEvent(Event event) {
				String environmentName = envText.getText();
				String description = descText.getText();	
				System.out.println("Environment = " + environmentName);
				System.out.println("description = " + description);
			}
		};

		Listener cancelListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				envText.setText("");
				descText.setText("");
				configDialog.setVisible(false);
			}
		};
		

		envbutton.addListener(SWT.Selection, buttonListener);
		envSaveButton.addListener(SWT.Selection, saveListener);
		envCancelButton.addListener(SWT.Selection, cancelListener);
		
		
		dialog.open();
		return null;

	}

	private Shell createDialog(Shell dialog) {

		final Shell configDialog = new Shell(dialog, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		configDialog.setText("Environment");
		configDialog.setLocation(385,130);
		configDialog.setSize(430, 350);

		GridLayout subLayout = new GridLayout(2, false);
		subLayout.verticalSpacing = 20;
		subLayout.horizontalSpacing = 60;
		configDialog.setLayout(subLayout);

		Label envLabel = new  Label(configDialog,  SWT.LEFT);
		envLabel.setText("Environment Name :");
		envLabel.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
		envLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

		envText = new Text(configDialog, SWT.BORDER); 
		envText.setToolTipText("Environment Name");
		envText.setMessage("Environment Name");
		envText.setLayoutData(new GridData(115,13));


		Label descLabel = new  Label(configDialog,  SWT.LEFT);
		descLabel.setText("Description :");
		descLabel.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
		envLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

		descText = new Text(configDialog, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL); 
		descText.setToolTipText("Description");
		descText.setLayoutData(new GridData(100,20));
		descText.setMessage("Description");

		Label aplliesToLabel = new  Label(configDialog,  SWT.LEFT);
		aplliesToLabel.setText("Applies To :");
		aplliesToLabel.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
		aplliesToLabel.setLayoutData(new GridData(100,20));


		final String[] ITEMS = { "All", "PHP", "DRUPAL", "JAVA" };
		org.eclipse.swt.widgets.List list = new org.eclipse.swt.widgets.List(configDialog, SWT.MULTI | SWT.SCROLLBAR_OVERLAY | SWT.CHECK);
		list.setLayoutData(new GridData(100,50));
		for (int i = 0; i < ITEMS.length; i++) {
			list.add(ITEMS[i]);
		}

		envSaveButton = new Button(configDialog, SWT.PUSH);
		envSaveButton.setText("Save");
		envSaveButton.setLayoutData(new GridData(75,20));
		envSaveButton.setLocation(500,505);


		envCancelButton = new Button(configDialog, SWT.PUSH);
		envCancelButton.setText("Cancel");
		envCancelButton.setLayoutData(new GridData(75,20));

		return configDialog;
	}
}
