package com.photon.phresco.ui.phrescoexplorer.wizard;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.photon.phresco.commons.ConfirmDialog;
import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.PhrescoDialog;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.model.Category;
import com.photon.phresco.commons.model.DownloadInfo;
import com.photon.phresco.commons.model.ProjectInfo;
import com.photon.phresco.commons.model.WebService;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.ui.model.BaseAction;
import com.photon.phresco.ui.resource.Messages;
import com.photon.phresco.ui.wizards.componets.DatabaseComponent;
import com.photon.phresco.ui.wizards.componets.ServerComponent;

public class EditProjectPage extends WizardPage implements PhrescoConstants {

	public Text nameText;
	public Text codeText;
	public Text descText;
	public Text appDirText;
	public Text versionText;
	
	public List<ServerComponent> serverComponents = new ArrayList<ServerComponent>();
	public List<DatabaseComponent> dbComponents = new ArrayList<DatabaseComponent>();
	
	public Button[] webServiceButtons;
	
	public EditProjectPage(String pageName) {
		super(pageName);
		setTitle(pageName);
	}
	
	@Override
	public void createControl(Composite parent) {
		final Composite buildDialog = new Composite(parent, SWT.APPLICATION_MODAL |  SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.TITLE | SWT.RESIZE);
		GridLayout layout = new GridLayout(1, false);
		buildDialog.setLocation(385, 130);
		buildDialog.setLayout(layout);
		buildDialog.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		final ScrolledComposite scrolledComposite = new ScrolledComposite(buildDialog, SWT.V_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		final Composite composite = new Composite(scrolledComposite, SWT.NONE);
		GridLayout CompositeLayout = new GridLayout(1, true);
		composite.setLayout(CompositeLayout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		try {
			
			BaseAction baseAction = new BaseAction();
			ServiceManager serviceManager = PhrescoUtil.getServiceManager(baseAction.getUserId());
			if(serviceManager == null) {
				ConfirmDialog.getConfirmDialog().showConfirm(buildDialog.getShell());
				return;
			}
			
			ProjectInfo projectInfo = PhrescoUtil.getProjectInfo();
			final ApplicationInfo appInfo = PhrescoUtil.getApplicationInfo();
			final String customerId = PhrescoUtil.getCustomerId();
			final String techId = PhrescoUtil.getTechId();
			final String platform = PhrescoUtil.findPlatform();
			createAppInfoPage(composite, projectInfo, appInfo);
			
			final List<DownloadInfo> servers = serviceManager.getDownloads(customerId, techId, Category.SERVER.name(), platform);
			if(CollectionUtils.isNotEmpty(servers)) {
				final Group serverGroup = new Group(composite, SWT.NONE);
				serverGroup.setText(Messages.SERVERS);
				GridLayout serverLayout = new GridLayout(1, false);
				serverGroup.setLayout(serverLayout);
				serverGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				
				Composite serverComposite = new Composite(serverGroup, SWT.NONE);
				GridLayout gridLayout = new GridLayout(5, false);
				serverComposite.setLayout(gridLayout);
				serverComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				
				ServerComponent serverComponent = new ServerComponent();
				serverComponent.getServers(serverComposite, servers, customerId, techId, platform);
				serverComponents.add(serverComponent);
				
				Button serverAddButton = new Button(serverComposite, SWT.PUSH);
				serverAddButton.setText(PLUS_SYMBOL);
				
				serverAddButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						try {
							final ServerComponent serverComponent = new ServerComponent();
							final Composite serverComposite = new Composite(serverGroup, SWT.NONE);
							GridLayout gridLayout = new GridLayout(5, false);
							serverComposite.setLayout(gridLayout);
							serverComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
							
							serverComponent.getServers(serverComposite, servers, customerId, techId, platform);
							serverComponents.add(serverComponent);
							Button serverDeleteButton = new Button(serverComposite, SWT.PUSH);
							serverDeleteButton.setText(MINUS_SYMBOL);
							
							serverDeleteButton.addSelectionListener(new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent e) {
									serverComponents.remove(serverComponent);
									serverComposite.dispose();
									composite.pack();
									super.widgetSelected(e);
								}
							});
						} catch (PhrescoException e1) {
							PhrescoDialog.errorDialog(buildDialog.getShell(), Messages.ERROR, e1.getLocalizedMessage());
						}
						serverGroup.redraw();
						serverGroup.pack();
						composite.redraw();
						composite.pack();
						reSize(composite, scrolledComposite);
						super.widgetSelected(e);
					}
				});
			}
			final List<DownloadInfo> dataBases = serviceManager.getDownloads(customerId, techId, Category.DATABASE.name(), platform);
			if(CollectionUtils.isNotEmpty(dataBases)) {
				final Group dbGroup = new Group(composite, SWT.NONE);
				dbGroup.setText(Messages.DATABASES);
				GridLayout dbLlayout = new GridLayout(1, false);
				dbGroup.setLayout(dbLlayout);
				dbGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				
				Composite dbComposite = new Composite(dbGroup, SWT.NONE);
				GridLayout gridLayout = new GridLayout(5, false);
				dbComposite.setLayout(gridLayout);
				dbComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				
				DatabaseComponent dbComponent = new DatabaseComponent();
				dbComponent.getDataBases(dbComposite, dataBases, customerId, techId, platform);
				dbComponents.add(dbComponent);
				
				Button dbAddButton = new Button(dbComposite, SWT.PUSH);
				dbAddButton.setText(PLUS_SYMBOL);
				
				dbAddButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						try {
							final Composite dbComposite = new Composite(dbGroup, SWT.NONE);
							GridLayout gridLayout = new GridLayout(5, false);
							dbComposite.setLayout(gridLayout);
							dbComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
							
							final DatabaseComponent dbComponent = new DatabaseComponent();
							dbComponent.getDataBases(dbComposite, dataBases, customerId, techId, platform);
							dbComponents.add(dbComponent);
							
							Button dbDeleteButton = new Button(dbComposite, SWT.PUSH);
							dbDeleteButton.setText(MINUS_SYMBOL);
							
							dbDeleteButton.addSelectionListener(new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent e) {
									dbComponents.remove(dbComponent);
									dbComposite.dispose();
									composite.pack();
									super.widgetSelected(e);
								}
							});
							
						} catch (PhrescoException e1) {
							PhrescoDialog.errorDialog(buildDialog.getShell(), Messages.ERROR, e1.getLocalizedMessage());
						}
						dbGroup.redraw();
						composite.redraw();
						composite.pack();
						reSize(composite, scrolledComposite);
						super.widgetSelected(e);
					}
				});
			}
			getWebServices(serviceManager, composite);
			
		} catch (PhrescoException e) {
			PhrescoDialog.errorDialog(buildDialog.getShell(), Messages.ERROR, e.getLocalizedMessage());
		}
		
		scrolledComposite.setContent(composite);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		reSize(composite, scrolledComposite);
		scrolledComposite.pack();
		scrolledComposite.redraw();
		Point size = scrolledComposite.getSize();
		int x = size.x;
		int y = size.y;
		buildDialog.setSize(x + 100, y + 100);
		setControl(buildDialog);
		
	}
	

	/**
	 * @param buildDialog
	 */
	private void createAppInfoPage(Composite comp, ProjectInfo projectInfo, ApplicationInfo appInfo) {
		
		if(projectInfo == null || appInfo == null) {
			return;
		}
		Composite composite = new Composite(comp, SWT.BORDER);
		GridLayout gridlayout = new GridLayout(6, false);
		composite.setLayout(gridlayout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label nameLable = new Label(composite, SWT.NONE);
		nameLable.setText(Messages.NAME);

		nameText = new Text(composite, SWT.BORDER);
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nameText.setText(appInfo.getName());

		Label codeLable = new Label(composite, SWT.NONE);
		codeLable.setText(Messages.CODE);

		codeText = new Text(composite, SWT.BORDER);
		codeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		codeText.setText(appInfo.getCode());

		Label descLable = new Label(composite, SWT.NONE);
		descLable.setText(Messages.DESCRIPTION);

		descText = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData descGridData = new GridData();
		descGridData.widthHint = 100;
		descGridData.heightHint = 40;
		descText.setLayoutData(descGridData);
		if(StringUtils.isNotEmpty(appInfo.getDescription())) { 
			descText.setText(appInfo.getDescription());
		}

		Label appDirLabel = new Label(composite, SWT.NONE);
		appDirLabel.setText(Messages.APP_DIRECTORY);

		appDirText = new Text(composite, SWT.BORDER);
		appDirText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		appDirText.setText(appInfo.getAppDirName());

		Label versionLabel = new Label(composite, SWT.NONE);
		versionLabel.setText(Messages.VERSION);

		versionText = new Text(composite, SWT.BORDER);
		versionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		versionText.setText(appInfo.getVersion());

		Label techLabel = new Label(composite, SWT.NONE);
		techLabel.setText(Messages.TECHNOLOGY);

		Text techText = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		techText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		techText.setText(appInfo.getTechInfo().getName() + Messages.HYPHEN + appInfo.getTechInfo().getVersion());
	}

	
	private void getWebServices(ServiceManager serviceManager, Composite composite) {
		try {
			List<WebService> webServices = serviceManager.getWebServices();
			if(CollectionUtils.isEmpty(webServices)) {
				return;
			}
			Group webserviceGroup = new Group(composite, SWT.NONE);
			webserviceGroup.setText(Messages.WEBSERVICES);
			GridLayout webServiceLayout = new GridLayout(5, false);
			webserviceGroup.setLayout(webServiceLayout);
			webserviceGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			webServiceButtons = new Button[webServices.size()];
			int i =0;
			for (WebService webService : webServices) {
				webServiceButtons[i] = new Button(webserviceGroup, SWT.CHECK);
				webServiceButtons[i].setText(webService.getName());
				webServiceButtons[i].setData(webService.getName(), webService.getId());
				i = i + 1;
			}
		} catch (PhrescoException e) {
			PhrescoDialog.errorDialog(composite.getShell(), Messages.ERROR, e.getLocalizedMessage());
		}
	}

	private void reSize(final Composite composite, final ScrolledComposite scrolledComposite) {
		composite.addListener(SWT.Resize, new Listener() {
			int width = -1;
			@Override
			public void handleEvent(Event event) {
				int newWidth = composite.getSize().x;
				if (newWidth != width) {
			        scrolledComposite.setMinHeight(composite.computeSize(newWidth, SWT.DEFAULT).y);
			        width = newWidth;
			    }
			}
		});
	}
}
