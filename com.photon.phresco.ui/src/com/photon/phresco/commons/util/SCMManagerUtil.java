/*
 * ###
 * 
 * Copyright (C) 1999 - 2012 Photon Infotech Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ###
 */

package com.photon.phresco.commons.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import com.google.gson.Gson;
import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.model.ProjectInfo;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.ui.resource.Messages;
import com.photon.phresco.util.FileUtil;
import com.photon.phresco.util.Utility;
import com.phresco.pom.util.PomProcessor;

/**
 * Util class to handle import project from git
 * @author syed
 *
 */
public class SCMManagerUtil implements PhrescoConstants {

	
	static SVNClientManager cm = null;
	static boolean dotphresco = false;
    
	public static ApplicationInfo importProject(String type, String url, String username,
			String password, String branch, String revision, String workspacePath) throws Exception {
		if (SVN.equals(type)) {

			SVNURL svnURL = SVNURL.parseURIEncoded(url);
			DAVRepositoryFactory.setup();
			DefaultSVNOptions options = new DefaultSVNOptions();
			cm = SVNClientManager.newInstance(options, username, password);
			boolean valid = checkOutFilter(url, username, password, revision, svnURL);

			if (valid) {
				ProjectInfo projectInfo = getSvnAppInfo(revision, svnURL);
				if (projectInfo != null) {
					return returnAppInfo(projectInfo);
				}
			} 
			return null;
		} else if (GIT.equals(type)) {
			String uuid = UUID.randomUUID().toString();
			File gitImportTemp = new File(workspacePath, uuid);
			if (gitImportTemp.exists()) {
				FileUtils.deleteDirectory(gitImportTemp);
			}
			importFromGit(url, gitImportTemp, username, password);
			ApplicationInfo applicationInfo = cloneFilter(gitImportTemp, url, true);
			if (gitImportTemp.exists()) {
				FileUtils.deleteDirectory(gitImportTemp);
			}
			if (applicationInfo != null) {
				PhrescoUtil.updateProjectIntoWorkspace(applicationInfo.getAppDirName());
				return applicationInfo;
			} 
		} else if (BITKEEPER.equals(type)) {
		    return importFromBitKeeper(url);
		}
		
		return null;
	}
	
	private static boolean checkOutFilter(String url, String name, String password,String revision, SVNURL svnURL) throws Exception {
		setupLibrary();
		SVNRepository repository = null;
		repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(name, password);
		repository.setAuthenticationManager(authManager);
			SVNNodeKind nodeKind = repository.checkPath("", -1);
			if (nodeKind == SVNNodeKind.NONE) {
			} else if (nodeKind == SVNNodeKind.FILE) {
			}
			boolean valid = validateDir(repository, STR_EMPTY, revision, svnURL, true);
			return valid;
	}
	
	private static ProjectInfo getSvnAppInfo(String revision, SVNURL svnURL) throws Exception {
		BufferedReader reader = null;
		File tempDir = new File(Utility.getSystemTemp(), SVN_CHECKOUT_TEMP);
		try {
			SVNUpdateClient uc = cm.getUpdateClient();
			uc.doCheckout(svnURL.appendPath(PHRESCO, true), tempDir,
					SVNRevision.UNDEFINED, SVNRevision.parse(revision),
					SVNDepth.UNKNOWN, false);
			File dotProjectFile = new File(tempDir, PROJECT_INFO);
			if (!dotProjectFile.exists()) {
				throw new PhrescoException(INVALID_FOLDER);
			}
			reader = new BufferedReader(new FileReader(dotProjectFile));
			return new Gson().fromJson(reader, ProjectInfo.class);
		} finally {
			Utility.closeStream(reader);

			if (tempDir.exists()) {
				FileUtil.delete(tempDir);
			}
		}
	}

	private static ApplicationInfo returnAppInfo(ProjectInfo projectInfo) {
		List<ApplicationInfo> appInfos = null;
		ApplicationInfo applicationInfo = null;
		if (projectInfo != null) {
		appInfos = projectInfo.getAppInfos();
		}
		if (appInfos != null) {
		applicationInfo = appInfos.get(0);
		}
		if (applicationInfo != null) {
			return applicationInfo;
		}
		return null;
	}
	
	private static void importFromGit(String url, File gitImportTemp, String username, String password)throws Exception {
		Git repo = null;
		CloneCommand cloneRepository = Git.cloneRepository();
		CloneCommand cloneCommand = null;
		if (username != null && password != null) {
			UsernamePasswordCredentialsProvider userCredential = new UsernamePasswordCredentialsProvider(username, password);
			cloneCommand = cloneRepository.setCredentialsProvider(userCredential);
		}
		CloneCommand callCommand = cloneRepository.setURI(url).setDirectory(gitImportTemp);
		repo = callCommand.call();
		for (Ref b : repo.branchList().setListMode(ListMode.ALL).call()) {
        }
        repo.getRepository().close();
	}	

	private static ApplicationInfo cloneFilter(File appDir, String url, boolean recursive)throws Exception {
		if (appDir.isDirectory()) {
			ProjectInfo projectInfo = getGitAppInfo(appDir);
			if (projectInfo == null) {
				throw new PhrescoException(INVALID_FOLDER);
			}
			List<ApplicationInfo> appInfos = projectInfo.getAppInfos();
			if (appInfos == null) {
				throw new PhrescoException(INVALID_FOLDER);
			}
			ApplicationInfo appInfo = appInfos.get(0);
			if (appInfo != null) {
				importToWorkspace(appDir, Utility.getProjectHome(),	appInfo.getAppDirName());
				// update connection in pom.xml
				updateSCMConnection(appInfo, url);
				return appInfo;
			}
		}
		return null;
	}

	private static void setupLibrary() {
		DAVRepositoryFactory.setup();
		SVNRepositoryFactoryImpl.setup();
		FSRepositoryFactory.setup();
	}
	
	private static boolean validateDir(SVNRepository repository, String path,
			String revision, SVNURL svnURL, boolean recursive)throws Exception {
		// first level check
			Collection entries = repository.getDir(path, -1, null, (Collection) null);
			Iterator iterator = entries.iterator();
			if (entries.size() != 0) {
				while (iterator.hasNext()) {
					SVNDirEntry entry = (SVNDirEntry) iterator.next();
					if ((entry.getName().equals(FOLDER_DOT_PHRESCO))
							&& (entry.getKind() == SVNNodeKind.DIR)) {
						ProjectInfo projectInfo = getSvnAppInfo(revision, svnURL);
						SVNUpdateClient uc = cm.getUpdateClient();
						if (projectInfo == null) {
							throw new PhrescoException(INVALID_FOLDER);
						}
						List<ApplicationInfo> appInfos = projectInfo.getAppInfos();
						if (appInfos == null) {
							throw new PhrescoException(INVALID_FOLDER);
						}
						ApplicationInfo appInfo = appInfos.get(0);
						File file = new File(Utility.getProjectHome(), appInfo.getAppDirName());
						if (file.exists()) {
							throw new PhrescoException(PROJECT_ALREADY);
			            }
						uc.doCheckout(svnURL, file, SVNRevision.UNDEFINED,
								SVNRevision.parse(revision), SVNDepth.UNKNOWN,
								false);
						// update connection url in pom.xml
						updateSCMConnection(appInfo,
								svnURL.toDecodedString());
						dotphresco = true;
						return dotphresco;
					} else if (entry.getKind() == SVNNodeKind.DIR && recursive) {
						// second level check (only one iteration)
						SVNURL svnnewURL = svnURL.appendPath(FORWARD_SLASH + entry.getName(), true);
						validateDir(repository,(path.equals("")) ? entry.getName() : path
										+ FORWARD_SLASH + entry.getName(), revision,svnnewURL, false);
					}
				}
			}
		return dotphresco;
	}
	
	private static ProjectInfo getGitAppInfo(File directory)throws PhrescoException {
		BufferedReader reader = null;
		try {
			File dotProjectFile = new File(directory, FOLDER_DOT_PHRESCO+ File.separator + PROJECT_INFO);
			if (!dotProjectFile.exists()) {
				return null;
			}
			reader = new BufferedReader(new FileReader(dotProjectFile));
			return new Gson().fromJson(reader, ProjectInfo.class);
		} catch (FileNotFoundException e) {
			throw new PhrescoException(INVALID_FOLDER);
		} finally {
			Utility.closeStream(reader);
		}
	}
	
	private static void importToWorkspace(File gitImportTemp, String projectHome,String code) throws Exception {
		try {
			File workspaceProjectDir = new File(projectHome + code);
			if (workspaceProjectDir.exists()) {
				throw new PhrescoException(PROJECT_ALREADY);
			}
			FileUtils.copyDirectory(gitImportTemp, workspaceProjectDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void updateSCMConnection(ApplicationInfo appInfo, String repoUrl)throws Exception {
		try {
			PomProcessor processor = getPomProcessor(appInfo);
				processor.setSCM(repoUrl, STR_EMPTY, STR_EMPTY, STR_EMPTY);
				processor.save();
		} catch (Exception e) {
			throw new PhrescoException(POM_URL_FAIL);
		}
	}
	
	private static PomProcessor getPomProcessor(ApplicationInfo appInfo)throws Exception {
		try {
			StringBuilder builder = new StringBuilder(Utility.getProjectHome());
			builder.append(appInfo.getAppDirName());
			builder.append(File.separatorChar);
			builder.append(Utility.getPomFileName(appInfo));
			File pomPath = new File(builder.toString());
			return new PomProcessor(pomPath);
		} catch (Exception e) {
			throw new PhrescoException(NO_POM_XML);
		}
	}

	/*
	 * To import application from bitkeeper
	 */
	private static ApplicationInfo importFromBitKeeper(String repoUrl) throws PhrescoException {
	    BufferedReader reader = null;
	    File file = new File(Utility.getPhrescoTemp() + "bitkeeper.info");
	    boolean isImported = false;
	    try {
	        String command = BK_CLONE + SPACE + repoUrl;
	        String uuid = UUID.randomUUID().toString();
			File bkImportTemp = new File(Utility.getPhrescoTemp(), uuid);
			if (bkImportTemp.exists()) {
				FileUtils.deleteDirectory(bkImportTemp);
			}
	        Utility.executeStreamconsumer(bkImportTemp.getPath(), command, new FileOutputStream(file));
	        reader = new BufferedReader(new FileReader(file));
	        String strLine;
	        while ((strLine = reader.readLine()) != null) {
	            if (strLine.contains(OK)) {
	                isImported = true;
	                break;
	            } else if (strLine.contains(ALREADY_EXISTS)) {
	                throw new PhrescoException(Messages.PROJ_ALREADY_IMPORTED);
	            } else if (strLine.contains(FAILED)) {
	                throw new PhrescoException(Messages.PROJ_IMPORT_FAILED);
	            }
	        }
	        if (isImported) {
	        	ProjectInfo projectInfo = getGitAppInfo(bkImportTemp);
	        	if (projectInfo != null) {
	        		ApplicationInfo appInfo = returnAppInfo(projectInfo);
	        		if (appInfo != null) {
	        			importToWorkspace(bkImportTemp, Utility.getProjectHome(), appInfo.getAppDirName());
	        			return appInfo;
	        		}
	        	} 
	        } 
	        return null;
	    } catch (IOException e) {
	        throw new PhrescoException(e);
	    } catch (Exception e) {
			throw new PhrescoException(e);
		} finally {
	        if (reader != null) {
	            try {
	                reader.close();
	            } catch (IOException e) {
	                throw new PhrescoException(e);
	            }
	        }
	        if (file.exists()) {
	            file.delete();
	        }
	    }
	}	
	
	/*
	 * Update the imported project into workspace
	 */
	private static void updateProjectIntoWorkspace(String projectName) {
		IProgressMonitor progressMonitor = new NullProgressMonitor();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(projectName);

		try {
			project.create(progressMonitor);
			project.open(progressMonitor);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
}
