package com.photon.phresco.ui.phrescoexplorer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.handlers.HandlerUtil;

import com.photon.phresco.commons.ConfirmDialog;
import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.PhrescoDialog;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.model.ArtifactGroupInfo;
import com.photon.phresco.commons.model.ArtifactInfo;
import com.photon.phresco.commons.model.FunctionalFrameworkInfo;
import com.photon.phresco.commons.util.ApplicationManagerUtil;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.commons.util.ProjectManager;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.ui.model.BaseAction;
import com.photon.phresco.ui.phrescoexplorer.wizard.EditProjectPage;
import com.photon.phresco.ui.phrescoexplorer.wizard.WizardComposite;
import com.photon.phresco.ui.resource.Messages;
import com.photon.phresco.ui.wizards.componets.DatabaseComponent;
import com.photon.phresco.ui.wizards.componets.ServerComponent;
import com.phresco.pom.exception.PhrescoPomException;
import com.phresco.pom.util.PomProcessor;

/**
 * @author suresh_ma
 *
 */
public class EditProject extends AbstractHandler implements PhrescoConstants {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final Shell shell = HandlerUtil.getActiveShell(event);
		// To check the user has logged in
		BaseAction baseAction = new BaseAction();
		ServiceManager serviceManager = PhrescoUtil.getServiceManager(baseAction.getUserId());
		if(serviceManager == null) {
			ConfirmDialog.getConfirmDialog().showConfirm(shell);
			return null;
		}

		final WizardComposite wizardComposite = new WizardComposite(shell);

		BusyIndicator.showWhile(null, new Runnable() {
			public void run() {
				WizardDialog wizardControl = wizardComposite.getWizardControl(new EditWizard());
				wizardControl.open();
			}
		});
		return null;
	}

	class EditWizard extends Wizard {

		EditProjectPage editProjectPage = null;

		public Text nameText;
		public Text codeText;
		public Text descText;
		public Text appDirText;
		public Text versionText;
		public String projectName;

		public List<ServerComponent> serverComponents = new ArrayList<ServerComponent>();
		public List<DatabaseComponent> dbComponents = new ArrayList<DatabaseComponent>();

		public Button[] webServiceButtons;

		public EditWizard() {
			super();
			this.setWindowTitle(Messages.EDIT_PROJECT_DIALOG_TITLE);
		}


		@Override
		public void addPages() {
			try {
				projectName = PhrescoUtil.getApplicationInfo().getAppDirName();
				if (StringUtils.isNotEmpty(projectName)) {
					EditProjectPage editProjectPage = new EditProjectPage(projectName);
					addPage(editProjectPage);
				} else {
					EditProjectPage editProjectPage = new EditProjectPage(Messages.EDIT_PROJECT_DIALOG_TITLE);
					addPage(editProjectPage);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public boolean performFinish() {
			IWizardPage[] pages = getPages();
			for (IWizardPage iWizardPage : pages) {
				if(iWizardPage instanceof EditProjectPage) {
					EditProjectPage editProjectPage = (EditProjectPage) iWizardPage;
					appDirText = editProjectPage.appDirText;
					codeText = editProjectPage.codeText;
					nameText = editProjectPage.nameText;
					versionText = editProjectPage.versionText;
					descText = editProjectPage.descText;
					webServiceButtons = editProjectPage.webServiceButtons;
					serverComponents  =editProjectPage.serverComponents;
					dbComponents = editProjectPage.dbComponents;
				}
			}
			BusyIndicator.showWhile(null, new Runnable() {
				
				@Override
				public void run() {
					update();
				}
			});
			
			PhrescoDialog.messageDialog(getShell(), Messages.PROJECT_UPDATED_SUCCESS_MSG);
			return true;
		}

		private void update() {
			try {
				boolean validate = validate();
				updateFunctionalTestType();
				if(!validate) {
					return;
				}
				//To set the basic information into applicationInfo
				ApplicationManagerUtil managerUtil = new ApplicationManagerUtil();
				ApplicationInfo appInfo = PhrescoUtil.getApplicationInfo();
				appInfo.setAppDirName(appDirText.getText());
				appInfo.setCode(codeText.getText());
				appInfo.setName(nameText.getText());
				appInfo.setVersion(versionText.getText());
				appInfo.setCreated(true);
				
				FunctionalFrameworkInfo functionalFrameworkInfo = new FunctionalFrameworkInfo();
				functionalFrameworkInfo.setFrameworkGroupId("cac095c9-911a-4d8b-8613-65a994ed3a67");
				functionalFrameworkInfo.setFrameworkIds("bf0170f7-2f03-44be-8f68-95c562347816");
				functionalFrameworkInfo.setVersion("2.30");
				appInfo.setFunctionalFrameworkInfo(functionalFrameworkInfo);
				
				if(StringUtils.isNotEmpty(descText.getText())) {
					appInfo.setDescription(descText.getText());
				}
				String oldAppDirName = PhrescoUtil.getProjectName();

				//To set the selected server
				setSelectedServers(appInfo);
				setSelectedDatabase(appInfo);

				List<String> webServiceIds = new ArrayList<String>();
				if(webServiceButtons != null) {
					List<Button> buttons = Arrays.asList(webServiceButtons);
					int i = 0;
					for (Button button : buttons) {
						if(button.getSelection()) {
							String webServiceId = (String) button.getData(button.getText());
							webServiceIds.add(webServiceId);
						}
						i = i+ 1;
					}
				}
				if(CollectionUtils.isNotEmpty(webServiceIds)) {
					appInfo.setSelectedWebservices(webServiceIds);
				}

				managerUtil.updateApplication(oldAppDirName, appInfo);
				if(!oldAppDirName.equals(appInfo.getAppDirName())) {
					ProjectManager.deleteProjectIntoWorkspace(oldAppDirName);
					ProjectManager.updateProjectIntoWorkspace(appInfo.getAppDirName());
				}
			} catch (PhrescoException e) {
				PhrescoDialog.exceptionDialog(getShell(), e);
			}
		}

		private void updateFunctionalTestType() {
			try {
				PomProcessor pomProcessor = PhrescoUtil.getPomProcessor(PhrescoUtil.getApplicationInfo().getAppDirName());
				String property = pomProcessor.getProperty("phresco.functionalTest.selenium.tool");
				if (StringUtils.isNotEmpty(property)) {
					pomProcessor.setProperty("phresco.functionalTest.selenium.tool", "webdriver");
				}
				pomProcessor.save();
			} catch (PhrescoException e) {
				e.printStackTrace();
			} catch (PhrescoPomException e) {
				e.printStackTrace();
			}
		}


		private boolean validate() {
			if(StringUtils.isEmpty(nameText.getText())) {
				PhrescoDialog.errorDialog(getShell(), Messages.ERROR, "Name should no be empty");
				return false;
			} if(StringUtils.isEmpty(codeText.getText())) {
				PhrescoDialog.errorDialog(getShell(), Messages.ERROR, "Code should no be empty");
				return false;
			} if(StringUtils.isEmpty(appDirText.getText())) {
				PhrescoDialog.errorDialog(getShell(), Messages.ERROR, "App Directory should no be empty");
				return false;
			}  if(StringUtils.isEmpty(versionText.getText())) {
				PhrescoDialog.errorDialog(getShell(), Messages.ERROR, "Version should no be empty");
				return false;
			} 
			return true;
		}

		/**
		 * @param appInfo
		 * @param artifactGroupInfos
		 */
		private void setSelectedServers(final ApplicationInfo appInfo) {
			if(CollectionUtils.isNotEmpty(serverComponents)) {
				List<ArtifactGroupInfo> artifactGroupInfos = new ArrayList<ArtifactGroupInfo>();
				for (ServerComponent serverComponent : serverComponents) {
					String serverName = serverComponent.serverNameCombo.getText();
					String[] selectedVersions = serverComponent.serverVersionListBox.getSelection();
					String serverId = ServerComponent.serverIdMap.get(serverName);
					ArtifactGroupInfo artifactGroupInfo = new ArtifactGroupInfo();
					artifactGroupInfo.setArtifactGroupId(serverId);

					List<String> artifactInfoIds = new ArrayList<String>();
					List<ArtifactInfo> artifactInfos = ServerComponent.serverVersionMap.get(serverName);
					for (ArtifactInfo artifactInfo : artifactInfos) {
						List<String> selectedVersionsList = Arrays.asList(selectedVersions);
						if(selectedVersionsList.contains(artifactInfo.getVersion())) {
							artifactInfoIds.add(artifactInfo.getId());
						}
					}
					artifactGroupInfo.setArtifactInfoIds(artifactInfoIds);
					artifactGroupInfos.add(artifactGroupInfo);
					appInfo.setSelectedServers(artifactGroupInfos);
				}
			}
		}

		/**
		 * @param appInfo
		 * @param artifactGroupInfos
		 */
		private void setSelectedDatabase(final ApplicationInfo appInfo) {
			if(CollectionUtils.isNotEmpty(dbComponents)) {
				List<ArtifactGroupInfo> artifactGroupInfos = new ArrayList<ArtifactGroupInfo>();
				for (DatabaseComponent dbComponent : dbComponents) {
					String dataBaseName = dbComponent.dbNameCombo.getText();
					String[] selectedVersions = dbComponent.dbVersionListBox.getSelection();
					String serverId = DatabaseComponent.dbIdMap.get(dataBaseName);
					ArtifactGroupInfo artifactGroupInfo = new ArtifactGroupInfo();
					artifactGroupInfo.setArtifactGroupId(serverId);

					List<String> artifactInfoIds = new ArrayList<String>();
					List<ArtifactInfo> artifactInfos = DatabaseComponent.dbVersionMap.get(dataBaseName);
					for (ArtifactInfo artifactInfo : artifactInfos) {
						List<String> selectedVersionsList = Arrays.asList(selectedVersions);
						if(selectedVersionsList.contains(artifactInfo.getVersion())) {
							artifactInfoIds.add(artifactInfo.getId());
						}
					}
					artifactGroupInfo.setArtifactInfoIds(artifactInfoIds);
					artifactGroupInfos.add(artifactGroupInfo);
					appInfo.setSelectedDatabases(artifactGroupInfos);
				}
			}
		}
	}
}
