package com.photon.phresco.commons.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.model.ArtifactGroup;
import com.photon.phresco.commons.model.ArtifactGroupInfo;
import com.photon.phresco.commons.model.ArtifactInfo;
import com.photon.phresco.commons.model.DownloadInfo;
import com.photon.phresco.commons.model.ProjectInfo;
import com.photon.phresco.commons.model.WebService;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.plugins.model.Mojos.ApplicationHandler;
import com.photon.phresco.plugins.util.MojoProcessor;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.util.Constants;
import com.phresco.pom.exception.PhrescoPomException;

public class ApplicationManagerUtil {

	public void updateApplication(String oldAppDirName, ApplicationInfo appInfo) throws PhrescoException {
		BufferedReader bufferedReader = null;
		File filePath = null;
		try {
			validateAppInfo(oldAppDirName,appInfo);
			String user_Id = PhrescoUtil.getUserId();
			ServiceManager serviceManager = PhrescoUtil.getServiceManager(user_Id);
			List<DownloadInfo> selectedServerGroup = new ArrayList<DownloadInfo>();
			List<DownloadInfo> selectedDatabaseGroup = new ArrayList<DownloadInfo>();

			Gson gson = new Gson();

			String applicationHome = PhrescoUtil.getApplicationHome();

			StringBuilder sb = new StringBuilder(applicationHome).append(File.separator)
					.append(Constants.DOT_PHRESCO_FOLDER).append(File.separator).append(
							Constants.APPLICATION_HANDLER_INFO_FILE);
			filePath = new File(sb.toString());
			MojoProcessor mojo = new MojoProcessor(filePath);
			ApplicationHandler applicationHandler = mojo.getApplicationHandler();
			// To write selected Database into phresco-application-Handler-info.xml
			List<ArtifactGroupInfo> selectedDatabases = appInfo.getSelectedDatabases();
			if (CollectionUtils.isNotEmpty(selectedDatabases)) {
				for (ArtifactGroupInfo selectedDatabase : selectedDatabases) {
					DownloadInfo downloadInfo = serviceManager.getDownloadInfo(selectedDatabase.getArtifactGroupId());
					String id = downloadInfo.getArtifactGroup().getId();
					ArtifactGroup artifactGroupInfo = serviceManager.getArtifactGroupInfo(id);
					List<ArtifactInfo> dbVersionInfos = artifactGroupInfo.getVersions();
					// for selected version infos from ui
					List<ArtifactInfo> selectedDBVersionInfos = new ArrayList<ArtifactInfo>();
					for (ArtifactInfo versionInfo : dbVersionInfos) {
						String versionId = versionInfo.getId();
						if (selectedDatabase.getArtifactInfoIds().contains(versionId)) {
							// Add selected version infos to list
							selectedDBVersionInfos.add(versionInfo);
						}
					}
					downloadInfo.getArtifactGroup().setVersions(selectedDBVersionInfos);
					selectedDatabaseGroup.add(downloadInfo);
				}
				if (CollectionUtils.isNotEmpty(selectedDatabaseGroup)) {
					String databaseGroup = gson.toJson(selectedDatabaseGroup);
					applicationHandler.setSelectedDatabase(databaseGroup);
				}
			} else {
				applicationHandler.setSelectedDatabase(null);
			}

			// To write selected Servers into phresco-application-Handler-info.xml
			List<ArtifactGroupInfo> selectedServers = appInfo.getSelectedServers();
			if (CollectionUtils.isNotEmpty(selectedServers)) {
				for (ArtifactGroupInfo selectedServer : selectedServers) {
					DownloadInfo downloadInfo = serviceManager.getDownloadInfo(selectedServer.getArtifactGroupId());
					String id = downloadInfo.getArtifactGroup().getId();
					ArtifactGroup artifactGroupInfo = serviceManager.getArtifactGroupInfo(id);
					List<ArtifactInfo> serverVersionInfos = artifactGroupInfo.getVersions();
					List<ArtifactInfo> selectedServerVersionInfos = new ArrayList<ArtifactInfo>();
					for (ArtifactInfo versionInfo : serverVersionInfos) {
						String versionId = versionInfo.getId();
						if (selectedServer.getArtifactInfoIds().contains(versionId)) {
							selectedServerVersionInfos.add(versionInfo);
						}
					}
					downloadInfo.getArtifactGroup().setVersions(selectedServerVersionInfos);
					selectedServerGroup.add(downloadInfo);
				}
				if (CollectionUtils.isNotEmpty(selectedServerGroup)) {
					String serverGroup = gson.toJson(selectedServerGroup);
					applicationHandler.setSelectedServer(serverGroup);
				}
			} else {
				applicationHandler.setSelectedServer(null);
			}

			// To write selected WebServices info to phresco-plugin-info.xml
			List<String> selectedWebservices = appInfo.getSelectedWebservices();
			List<WebService> webServiceList = new ArrayList<WebService>();
			if (CollectionUtils.isNotEmpty(selectedWebservices)) {
				List<WebService> webServices = serviceManager.getWebServices();
				for (String selectedWebService : selectedWebservices) {
					for (WebService webService : webServices) {
						if(selectedWebService.equals(webService.getId())) {
							webServiceList.add(webService);
						}
					}
				}
				if (CollectionUtils.isNotEmpty(webServiceList)) {
					String serverGroup = gson.toJson(webServiceList);
					applicationHandler.setSelectedWebService(serverGroup);
				}
			} else {
				applicationHandler.setSelectedWebService(null);
			}

			mojo.save();
			System.out.println("Mojo saved......");
			StringBuilder sbs = null;
			sbs = new StringBuilder(applicationHome).append(File.separator).append(
					Constants.DOT_PHRESCO_FOLDER).append(File.separator).append(Constants.PROJECT_INFO_FILE);
			bufferedReader = new BufferedReader(new FileReader(sbs.toString()));
			Type type = new TypeToken<ProjectInfo>() {
			}.getType();
			ProjectInfo projectInfo = gson.fromJson(bufferedReader, type);
			ApplicationInfo applicationInfo = projectInfo.getAppInfos().get(0);

			bufferedReader.close();
			deleteSqlFolder(applicationInfo, selectedDatabases, serviceManager, oldAppDirName);

			projectInfo.setAppInfos(Collections.singletonList(appInfo));
			/*ProjectManager projectManager = PhrescoFrameworkFactory.getProjectManager();
			projectManager.update(projectInfo, serviceManager, oldAppDirName);
			List<ProjectInfo> projects = projectManager.discover(customerId);
			if (CollectionUtils.isNotEmpty(projects)) {
				Collections.sort(projects, sortByDateToLatest());
			}*/
		} catch (PhrescoException e) {
			throw new PhrescoException(e);
		} catch (FileNotFoundException e) {
			throw new PhrescoException(e);
		} catch (IOException e) {
			throw new PhrescoException(e);
		}
	}

	private void validateAppInfo(String oldAppDirName, ApplicationInfo appInfo) throws PhrescoException {
		/*ProjectManager projectManager = PhrescoFrameworkFactory.getProjectManager();
		List<ProjectInfo> discoveredProjectInfos = projectManager.discover();
		for (ProjectInfo projectInfo : discoveredProjectInfos) {
			List<ApplicationInfo> appInfos = projectInfo.getAppInfos();
			for (int i = 0; i < appInfos.size(); i++) {
				if(appInfo.getAppDirName().equals(oldAppDirName)) {
					continue;
				} else if(appInfo.getAppDirName().equals(appInfos.get(i).getAppDirName())) {
					throw new PhrescoException("App directory already exists");
				}
			}
		}*/
	}

	public void deleteSqlFolder(ApplicationInfo applicationInfo, List<ArtifactGroupInfo> selectedDatabases,
			ServiceManager serviceManager, String oldAppDirName) throws PhrescoException {
		try {
			List<String> dbListToDelete = new ArrayList<String>();
			List<ArtifactGroupInfo> existingDBList = applicationInfo.getSelectedDatabases();
			System.out.println("existingDBList====> " + existingDBList);
			if (CollectionUtils.isEmpty(existingDBList)) {
				return;
			}
			for (ArtifactGroupInfo artifactGroupInfo : existingDBList) {
				String oldArtifactGroupId = artifactGroupInfo.getArtifactGroupId();
				for (ArtifactGroupInfo newArtifactGroupInfo : selectedDatabases) {
					String newArtifactid = newArtifactGroupInfo.getArtifactGroupId();
					if (newArtifactid.equals(oldArtifactGroupId)) {
						checkForVersions(newArtifactid, oldArtifactGroupId, oldAppDirName, serviceManager);
						break;
					} else {
						DownloadInfo downloadInfo = serviceManager.getDownloadInfo(oldArtifactGroupId);
						dbListToDelete.add(downloadInfo.getName());
					}
				}
			}
			File sqlPath = null;
			System.out.println("old appDirName " + oldAppDirName);
			if (StringUtils.isNotEmpty(oldAppDirName)) {
				System.out.println("sqlFilepath===> " + PhrescoUtil.getSqlFilePath(oldAppDirName));
				sqlPath = new File(PhrescoUtil.getSqlFilePath(oldAppDirName));
			} else {
				sqlPath = new File(PhrescoUtil.getSqlFilePath(applicationInfo.getAppDirName()));
			}
			for (String dbVersion : dbListToDelete) {
				File dbVersionFolder = new File(sqlPath, dbVersion.toLowerCase());
				FileUtils.deleteDirectory(dbVersionFolder.getParentFile());
			}
		} catch (Exception e) {
			throw new PhrescoException(e);
		}
	}

	private void checkForVersions(String newArtifactid, String oldArtifactGroupId, String oldAppDirName,
			ServiceManager serviceManager) throws PhrescoException {
		try {
			File sqlPath = new File(PhrescoUtil.getSqlFilePath(oldAppDirName));
			DownloadInfo oldDownloadInfo = serviceManager.getDownloadInfo(oldArtifactGroupId);
			DownloadInfo newDownloadInfo = serviceManager.getDownloadInfo(newArtifactid);
			List<ArtifactInfo> oldVersions = oldDownloadInfo.getArtifactGroup().getVersions();
			List<ArtifactInfo> newVersions = newDownloadInfo.getArtifactGroup().getVersions();
			for (ArtifactInfo artifactInfo : oldVersions) {
				for (ArtifactInfo newartifactInfo : newVersions) {
					if (!newartifactInfo.getVersion().equals(artifactInfo.getVersion())) {
						String deleteVersion = "/" + oldDownloadInfo.getName() + "/" + artifactInfo.getVersion();
						FileUtils.deleteDirectory(new File(sqlPath, deleteVersion));
					}
				}
			}
		} catch (PhrescoException e) {
			throw new PhrescoException(e);
		} catch (IOException e) {
			throw new PhrescoException(e);
		} catch (PhrescoPomException e) {
			throw new PhrescoException(e);
		}
	}
}
