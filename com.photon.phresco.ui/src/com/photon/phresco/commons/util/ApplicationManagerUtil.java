package com.photon.phresco.commons.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.photon.phresco.api.ApplicationProcessor;
import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.model.ArtifactGroup;
import com.photon.phresco.commons.model.ArtifactGroupInfo;
import com.photon.phresco.commons.model.ArtifactInfo;
import com.photon.phresco.commons.model.Customer;
import com.photon.phresco.commons.model.DownloadInfo;
import com.photon.phresco.commons.model.ProjectInfo;
import com.photon.phresco.commons.model.RepoInfo;
import com.photon.phresco.commons.model.WebService;
import com.photon.phresco.configuration.Environment;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.framework.api.DocumentGenerator;
import com.photon.phresco.framework.impl.ConfigurationReader;
import com.photon.phresco.framework.impl.ConfigurationWriter;
import com.photon.phresco.framework.impl.DocumentGeneratorImpl;
import com.photon.phresco.plugins.model.Mojos.ApplicationHandler;
import com.photon.phresco.plugins.util.MojoProcessor;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.ui.model.ActionType;
import com.photon.phresco.util.ArchiveUtil;
import com.photon.phresco.util.ArchiveUtil.ArchiveType;
import com.photon.phresco.util.Constants;
import com.photon.phresco.util.FileUtil;
import com.photon.phresco.util.PhrescoDynamicLoader;
import com.photon.phresco.util.ProjectUtils;
import com.photon.phresco.util.Utility;
import com.phresco.pom.exception.PhrescoPomException;
import com.phresco.pom.util.PomProcessor;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;

public class ApplicationManagerUtil implements Constants {

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
			
			update(projectInfo, serviceManager, oldAppDirName);
			/*List<ProjectInfo> projects = projectManager.discover(customerId);
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
		List<ProjectInfo> discoveredProjectInfos = discover();
		for (ProjectInfo projectInfo : discoveredProjectInfos) {
			List<ApplicationInfo> appInfos = projectInfo.getAppInfos();
			for (int i = 0; i < appInfos.size(); i++) {
				if(appInfo.getAppDirName().equals(oldAppDirName)) {
					continue;
				} else if(appInfo.getAppDirName().equals(appInfos.get(i).getAppDirName())) {
					throw new PhrescoException("App directory already exists");
				}
			}
		}
	}

	public void deleteSqlFolder(ApplicationInfo applicationInfo, List<ArtifactGroupInfo> selectedDatabases,
			ServiceManager serviceManager, String oldAppDirName) throws PhrescoException {
		try {
			List<String> dbListToDelete = new ArrayList<String>();
			List<ArtifactGroupInfo> existingDBList = applicationInfo.getSelectedDatabases();
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
			if (StringUtils.isNotEmpty(oldAppDirName)) {
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
	
	public List<ProjectInfo> discover(String customerId) throws PhrescoException {
		try {
			File projectsHome = new File(PhrescoUtil.getProjectHome());
			if (!projectsHome.exists()) {
				return null;
			}
			Map<String, ProjectInfo> projectInfosMap = new HashMap<String, ProjectInfo>();
			List<ProjectInfo> projectInfos = new ArrayList<ProjectInfo>();
			File[] appDirs = projectsHome.listFiles();
			for (File appDir : appDirs) {
			    if (appDir.isDirectory()) { 
			        File[] dotPhrescoFolders = appDir.listFiles(new PhrescoFileNameFilter(DOT_PHRESCO_FOLDER));
			        if (ArrayUtils.isEmpty(dotPhrescoFolders)) {
			        	continue;
			        }
			        File[] dotProjectFiles = dotPhrescoFolders[0].listFiles(new PhrescoFileNameFilter(PROJECT_INFO_FILE));
			        if (ArrayUtils.isEmpty(dotProjectFiles)) {
			            throw new PhrescoException("project.info file not found in .phresco of project " + dotPhrescoFolders[0].getParent());
			        }
			        projectInfosMap = fillProjects(dotProjectFiles[0], projectInfos, customerId, projectInfosMap);
			    }
			}

			Iterator<Entry<String, ProjectInfo>> iterator = projectInfosMap.entrySet().iterator();
			while (iterator.hasNext()) {
			    projectInfos.add(iterator.next().getValue());
			}
			return projectInfos;
		} catch (Exception e) {
			throw new PhrescoException(e);
		}
	}
	
	private Map<String, ProjectInfo> fillProjects(File dotProjectFile, List<ProjectInfo> projectInfos, String customerId, Map<String, ProjectInfo> projectInfosMap) throws PhrescoException {

        Gson gson = new Gson();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(dotProjectFile));
            ProjectInfo projectInfo = gson.fromJson(reader, ProjectInfo.class);
            if (projectInfo.getCustomerIds().get(0).equalsIgnoreCase(customerId)) {
                ProjectInfo projectInfoInMap = projectInfosMap.get(projectInfo.getId());
                if (projectInfoInMap != null) {
                    projectInfoInMap.getAppInfos().add(projectInfo.getAppInfos().get(0));
                    projectInfosMap.put(projectInfo.getId(), projectInfoInMap);
                } else {
                    projectInfosMap.put(projectInfo.getId(), projectInfo);
                }
            }
        } catch (FileNotFoundException e) {
            throw new PhrescoException(e);
        } finally {
            Utility.closeStream(reader);
        }
        return projectInfosMap;
    }
	
	public List<ProjectInfo> discover() throws PhrescoException {

		File projectsHome = new File(PhrescoUtil.getProjectHome());
		if (!projectsHome.exists()) {
			return null;
		}
		List<ProjectInfo> projectInfos = new ArrayList<ProjectInfo>();
	    File[] appDirs = projectsHome.listFiles();
	    for (File appDir : appDirs) {
	        if (appDir.isDirectory()) { 
	            File[] dotPhrescoFolders = appDir.listFiles(new PhrescoFileNameFilter(Constants.DOT_PHRESCO_FOLDER));
	            if (ArrayUtils.isEmpty(dotPhrescoFolders)) {
	            	continue;
	            }
	            File[] dotProjectFiles = dotPhrescoFolders[0].listFiles(new PhrescoFileNameFilter(Constants.PROJECT_INFO_FILE));
	            if (ArrayUtils.isEmpty(dotProjectFiles)) {
	                throw new PhrescoException("project.info file not found in .phresco of project " + dotPhrescoFolders[0].getParent());
	            }
	            fillProjects(dotProjectFiles[0], projectInfos);
	        }
	    }

        return projectInfos;
	}
	
	private void fillProjects(File dotProjectFile, List<ProjectInfo> projectInfos) throws PhrescoException {
        Gson gson = new Gson();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(dotProjectFile));
            ProjectInfo projectInfo = gson.fromJson(reader, ProjectInfo.class);
            projectInfos.add(projectInfo);
        } catch (FileNotFoundException e) {
            throw new PhrescoException(e);
        } finally {
            Utility.closeStream(reader);
        }
    }
	
	public ProjectInfo update(ProjectInfo projectInfo, ServiceManager serviceManager, String oldAppDirName) throws PhrescoException {
		if (projectInfo.getNoOfApps() == 0 && CollectionUtils.isEmpty(projectInfo.getAppInfos())) {
			try {
				ProjectInfo availableProjectInfo = getProject(projectInfo.getId(), projectInfo.getCustomerIds().get(0));
				List<ApplicationInfo> appInfos = availableProjectInfo.getAppInfos();
				for (ApplicationInfo applicationInfo : appInfos) {
					projectInfo.setAppInfos(Collections.singletonList(applicationInfo));
					StringBuilder sb = new StringBuilder(PhrescoUtil.getProjectHome())
					.append(applicationInfo.getAppDirName())
					.append(File.separator)
					.append(Constants.DOT_PHRESCO_FOLDER)
					.append(File.separator)
					.append(Constants.PROJECT_INFO_FILE);
					ProjectUtils.updateProjectInfo(projectInfo, new File(sb.toString()));
				}
			} catch (Exception e) {
				throw new PhrescoException(e);
			}
		} else {
			ClientResponse response = serviceManager.updateProject(projectInfo);
			if (response.getStatus() == 200) {
				File backUpProjectInfoFile = null;
				try {
					//application path with old app dir
					StringBuilder oldAppDirSb = new StringBuilder(PhrescoUtil.getProjectHome());
					oldAppDirSb.append(oldAppDirName);
					File oldDir = new File(oldAppDirSb.toString());
					backUpProjectInfoFile = backUpProjectInfoFile(oldDir.getPath());
					//application path with new app dir
					StringBuilder newAppDirSb = new StringBuilder(PhrescoUtil.getProjectHome());
					newAppDirSb.append(projectInfo.getAppInfos().get(0).getAppDirName());
					File projectInfoFile = new File(newAppDirSb.toString());
					
					//rename to application app dir
					oldDir.renameTo(projectInfoFile);
					extractArchive(response, projectInfo);
					updateProjectPom(projectInfo);
					StringBuilder dotPhrescoPathSb = new StringBuilder(projectInfoFile.getPath());
					dotPhrescoPathSb.append(File.separator);
					dotPhrescoPathSb.append(Constants.DOT_PHRESCO_FOLDER);
					dotPhrescoPathSb.append(File.separator);
	
					String customerId = projectInfo.getCustomerIds().get(0);
					Customer customer = serviceManager.getCustomer(customerId);
					RepoInfo repoInfo = customer.getRepoInfo();
					ApplicationInfo appInfo = projectInfo.getAppInfos().get(0);
					
					String pluginInfoFile = dotPhrescoPathSb.toString() + Constants.APPLICATION_HANDLER_INFO_FILE;
					MojoProcessor mojoProcessor = new MojoProcessor(new File(pluginInfoFile));
					ApplicationHandler applicationHandler = mojoProcessor.getApplicationHandler();
					
					createSqlFolder(appInfo, projectInfoFile, serviceManager);
					if (applicationHandler != null) {
						String selectedFeatures = applicationHandler.getSelectedFeatures();
						String deletedFeatures = applicationHandler.getDeletedFeatures();
						Gson gson = new Gson();
						Type jsonType = new TypeToken<Collection<ArtifactGroup>>(){}.getType();
						List<ArtifactGroup> artifactGroups = gson.fromJson(selectedFeatures, jsonType);
						List<ArtifactGroup> deletedArtifacts = gson.fromJson(deletedFeatures, jsonType);
						
						List<ArtifactGroup> plugins = setArtifactGroup(applicationHandler);
						//For Pdf Document Creation In Docs Folder
						DocumentGenerator documentGenerator = new DocumentGeneratorImpl();
						documentGenerator.generate(appInfo, projectInfoFile, artifactGroups, serviceManager);
						if(! appInfo.getAppDirName().equals(oldAppDirName)) {
							documentGenerator.deleteOldDocument(projectInfoFile, oldAppDirName);
						}
						// Dynamic Class Loading
						PhrescoDynamicLoader dynamicLoader = new PhrescoDynamicLoader(repoInfo, plugins);
						ApplicationProcessor applicationProcessor = dynamicLoader
								.getApplicationProcessor(applicationHandler.getClazz());
						
						applicationProcessor.postUpdate(appInfo, artifactGroups, deletedArtifacts);
	
						File projectInfoPath = new File(dotPhrescoPathSb.toString() + PROJECT_INFO_FILE);
						ProjectUtils.updateProjectInfo(projectInfo, projectInfoPath);
					}
					String baseDir = PhrescoUtil.getProjectHome() + appInfo.getAppDirName();
					Utility.executeCommand("mvn " +ActionType.ECLIPSE.getActionType(), baseDir);
				} catch (FileNotFoundException e) {
					throw new PhrescoException(e);
				} catch (IOException e) {
					throw new PhrescoException(e);
				} finally {
					if(backUpProjectInfoFile!= null && backUpProjectInfoFile.exists()) {
						FileUtil.delete(backUpProjectInfoFile);
					}
				}
			} else if (response.getStatus() == 401) {
				throw new PhrescoException("Session expired");
			} else {
				throw new PhrescoException("Project updation failed");
			}
			createEnvConfigXml(projectInfo, serviceManager);
		}
		return projectInfo;
	}
	
	private List<ArtifactGroup> setArtifactGroup(ApplicationHandler applicationHandler) {
		List<ArtifactGroup> plugins = new ArrayList<ArtifactGroup>();
		ArtifactGroup artifactGroup = new ArtifactGroup();
		artifactGroup.setGroupId(applicationHandler.getGroupId());
		artifactGroup.setArtifactId(applicationHandler.getArtifactId());
		List<ArtifactInfo> artifactInfos = new ArrayList<ArtifactInfo>();
		ArtifactInfo artifactInfo = new ArtifactInfo();
		artifactInfo.setVersion(applicationHandler.getVersion());
		artifactInfos.add(artifactInfo);
		artifactGroup.setVersions(artifactInfos);
		plugins.add(artifactGroup);
		return plugins;
	}
	
	public ProjectInfo getProject(String projectId, String customerId) throws PhrescoException {
		List<ProjectInfo> discover = discover(customerId);
		for (ProjectInfo projectInfo : discover) {
			if (projectInfo.getId().equals(projectId)) {
				return projectInfo;
			}
		}
		return null;
	}
	
	private File backUpProjectInfoFile(String oldDirPath) throws PhrescoException {
		if(StringUtils.isNotEmpty(oldDirPath)) {
			return null;
		}
		StringBuilder oldDotPhrescoPathSb = new StringBuilder(oldDirPath);
		oldDotPhrescoPathSb.append(File.separator);
		oldDotPhrescoPathSb.append(Constants.DOT_PHRESCO_FOLDER);
		oldDotPhrescoPathSb.append(File.separator);
		File projectInfoFile = new File(oldDotPhrescoPathSb.toString() + Constants.PROJECT_INFO_FILE);
		if(!projectInfoFile.exists()) {
			return null;
		}
		File backUpInfoFile = new File(oldDotPhrescoPathSb.toString() + Constants.PROJECT_INFO_BACKUP_FILE);
		if(!backUpInfoFile.exists()) {
			return null;
		}
		try {
			FileUtils.copyFile(projectInfoFile, backUpInfoFile);
			return backUpInfoFile;
		} catch (IOException e) {
			throw new PhrescoException(e);
		}
	}
	
	private void extractArchive(ClientResponse response, ProjectInfo info) throws IOException, PhrescoException {
		InputStream inputStream = response.getEntityInputStream();
		FileOutputStream fileOutputStream = null;
		String archiveHome = PhrescoUtil.getArchiveHome();
		File archiveFile = new File(archiveHome + info.getProjectCode() + PhrescoConstants.ARCHIVE_FORMAT);
		fileOutputStream = new FileOutputStream(archiveFile);
		try {
			byte[] data = new byte[1024];
			int i = 0;
			while ((i = inputStream.read(data)) != -1) {
				fileOutputStream.write(data, 0, i);
			}
			fileOutputStream.flush();
			ArchiveUtil.extractArchive(archiveFile.getPath(), PhrescoUtil.getProjectHome(), ArchiveType.ZIP);
		} finally {
			Utility.closeStream(inputStream);
			Utility.closeStream(fileOutputStream);
		}
	}
	
	private void updateProjectPom(ProjectInfo projectInfo) throws PhrescoException {
		ApplicationInfo applicationInfo = projectInfo.getAppInfos().get(0);
		File pomFile = new File(PhrescoUtil.getProjectHome() + applicationInfo.getAppDirName() + File.separatorChar + PhrescoUtil.getPomFileName(applicationInfo));
		if(!pomFile.exists()) {
			return;
		}
		PomProcessor pomProcessor;
		try {
			pomProcessor = new PomProcessor(pomFile);
			pomProcessor.setArtifactId(applicationInfo.getCode());
			pomProcessor.setName(applicationInfo.getName());
			pomProcessor.setVersion(applicationInfo.getVersion());
			pomProcessor.save();
		} catch (PhrescoPomException e) {
			throw new PhrescoException(e);
		}
	}
	
	private void createSqlFolder(ApplicationInfo appInfo, File path, ServiceManager serviceManager)
			throws PhrescoException {
		String dbName = "";
		try {
			File pomPath = new File(PhrescoUtil.getProjectHome() + appInfo.getAppDirName() + File.separator + PhrescoUtil.getPomFileName(appInfo));
			PomProcessor pompro = new PomProcessor(pomPath);
			String sqlFolderPath = pompro.getProperty(POM_PROP_KEY_SQL_FILE_DIR);
			File mysqlFolder = new File(path, sqlFolderPath + Constants.DB_MYSQL);
			File mysqlVersionFolder = getMysqlVersionFolder(mysqlFolder);
			File pluginInfoFile = new File(PhrescoUtil.getProjectHome() + appInfo.getAppDirName() + File.separator
					+ DOT_PHRESCO_FOLDER + File.separator + APPLICATION_HANDLER_INFO_FILE);
			MojoProcessor mojoProcessor = new MojoProcessor(pluginInfoFile);
			ApplicationHandler applicationHandler = mojoProcessor.getApplicationHandler();
			String selectedDatabases = applicationHandler.getSelectedDatabase();
			if (StringUtils.isNotEmpty(selectedDatabases) && StringUtils.isNotEmpty(sqlFolderPath)) {
				Gson gson = new Gson();
				java.lang.reflect.Type jsonType = new TypeToken<Collection<DownloadInfo>>() {
				}.getType();
				List<DownloadInfo> dbInfos = gson.fromJson(selectedDatabases, jsonType);
				List<ArtifactGroupInfo> newSelectedDatabases = appInfo.getSelectedDatabases();
				if(CollectionUtils.isNotEmpty(newSelectedDatabases)) {
					for (ArtifactGroupInfo artifactGroupInfo : newSelectedDatabases) {
						List<String> artifactInfoIds = artifactGroupInfo.getArtifactInfoIds();
						for (String artifactId : artifactInfoIds) {
							ArtifactInfo artifactInfo = serviceManager.getArtifactInfo(artifactId);
							String selectedVersion = artifactInfo.getVersion();
							for (DownloadInfo dbInfo : dbInfos) {
								dbName = dbInfo.getName().toLowerCase();
								ArtifactGroup artifactGroup = dbInfo.getArtifactGroup();
								mySqlFolderCreation(path, dbName, sqlFolderPath, mysqlVersionFolder,selectedVersion, artifactGroup);
							}
						}
					}
				}
			}
		} catch (PhrescoPomException e) {
			throw new PhrescoException(e);
		}
	}

	private void mySqlFolderCreation(File path, String dbName, String sqlFolderPath, File mysqlVersionFolder, String selectedVersion,
			ArtifactGroup artifactGroup) throws PhrescoException {
		try {
			List<ArtifactInfo> versions = artifactGroup.getVersions();
			for (ArtifactInfo version : versions) {
				if (selectedVersion.equals(version.getVersion())) {
					String dbversion = version.getVersion();
					String sqlPath = dbName + File.separator + dbversion.trim();
					File sqlFolder = new File(path, sqlFolderPath + sqlPath);
					sqlFolder.mkdirs();
					if (dbName.equals(Constants.DB_MYSQL) && mysqlVersionFolder != null
							&& !(mysqlVersionFolder.getPath().equals(sqlFolder.getPath()))) {
						FileUtils.copyDirectory(mysqlVersionFolder, sqlFolder);
					} else {
						File sqlFile = new File(sqlFolder, Constants.SITE_SQL);
						if (!sqlFile.exists()) {
							sqlFile.createNewFile();
						}
					}
				}
			}
		} catch (IOException e) {
			throw new PhrescoException(e);
		}
	}
	 
	private File getMysqlVersionFolder(File mysqlFolder) {
		File[] mysqlFolderFiles = mysqlFolder.listFiles();
		if (mysqlFolderFiles != null && mysqlFolderFiles.length > 0) {
			return mysqlFolderFiles[0];
		}
		return null;
	}
	
	private void createEnvConfigXml(ProjectInfo projectInfo, ServiceManager serviceManager) throws PhrescoException {
		try {
			List<ApplicationInfo> appInfos = projectInfo.getAppInfos();
			Environment defaultEnv = getEnvFromService(serviceManager);
			for (ApplicationInfo applicationInfo : appInfos) {
				createConfigurationXml(applicationInfo.getAppDirName(), serviceManager, defaultEnv);	
			}
		} catch (PhrescoException e) {
			throw new PhrescoException("Configuration creation failed"+e);
		}
	}
	
	private Environment getEnvFromService(ServiceManager serviceManager) throws PhrescoException {
		 try {
			 return serviceManager.getDefaultEnvFromServer();
		 } catch (ClientHandlerException ex) {
			 throw new PhrescoException(ex);
		 }
	 }
	
	private File createConfigurationXml(String appDirName, ServiceManager serviceManager, Environment defaultEnv) throws PhrescoException {
		File configFile = new File(getConfigurationPath(appDirName).toString());
		if (!configFile.exists()) {
			createEnvironments(configFile, defaultEnv, true);
		}
		return configFile;
	}
	
	private StringBuilder getConfigurationPath(String projectCode) {

		 StringBuilder builder = new StringBuilder(PhrescoUtil.getProjectHome());
		 builder.append(projectCode);
		 builder.append(File.separator);
		 builder.append(DOT_PHRESCO_FOLDER);
		 builder.append(File.separator);
		 builder.append(CONFIGURATION_INFO_FILE);

		 return builder;
	 }
	
	private void createEnvironments(File configPath, Environment defaultEnv, boolean isNewFile) throws PhrescoException {
		 try {
			 ConfigurationReader reader = new ConfigurationReader(configPath);
			 ConfigurationWriter writer = new ConfigurationWriter(reader, isNewFile);
			 writer.createEnvironment(Collections.singletonList(defaultEnv));
			 writer.saveXml(configPath);
		 } catch (Exception e) {
			 throw new PhrescoException(e);
		 }
	 }
	
	private class PhrescoFileNameFilter implements FilenameFilter {
		 private String filter_;
		 public PhrescoFileNameFilter(String filter) {
			 filter_ = filter;
		 }
		 public boolean accept(File dir, String name) {
			 return name.endsWith(filter_);
		 }
	 }
}
