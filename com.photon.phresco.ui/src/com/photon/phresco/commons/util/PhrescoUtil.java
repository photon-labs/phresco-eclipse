package com.photon.phresco.commons.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.internal.events.BuildCommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.model.ProjectInfo;
import com.photon.phresco.commons.model.User;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.exception.PhrescoWebServiceException;
import com.photon.phresco.service.client.api.ServiceContext;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.service.client.factory.ServiceClientFactory;
import com.photon.phresco.service.client.impl.ServiceManagerImpl;
import com.photon.phresco.ui.PhrescoNature;
import com.photon.phresco.util.ArchiveUtil;
import com.photon.phresco.util.ArchiveUtil.ArchiveType;
import com.photon.phresco.util.Utility;
import com.sun.jersey.api.client.ClientResponse;


public class PhrescoUtil implements PhrescoConstants {

	static boolean isLoggedIn = false;
	static String loggedInUserId;
	public static final Map<String, ServiceManager> CONTEXT_MANAGER_MAP = new HashMap<String, ServiceManager>();
	
	public static boolean doLogin(String userName, String password) throws PhrescoWebServiceException {
		
		ServiceContext context = new ServiceContext();
        context.put(SERVICE_URL, "http://localhost:3030/service/rest/api");
		System.out.println(" user name in phresco util : " + userName);
		System.out.println(" password in phresco util : " + password);
        context.put(SERVICE_USERNAME, userName);
        context.put(SERVICE_PASSWORD, password);
        try {
        	ServiceManager serviceManager = new ServiceManagerImpl(context);
        	CONTEXT_MANAGER_MAP.put(userName, serviceManager);
        	isLoggedIn = true;
        } catch(Exception e) {
        	isLoggedIn = false;
        	e.printStackTrace();
        }
        try {
        	ServiceManager serviceManager = ServiceClientFactory.getServiceManager(context);
        	User userInfo = serviceManager.getUserInfo();
        	loggedInUserId = userInfo.getId();
        	isLoggedIn = true;
        } catch(Exception e) {
        	isLoggedIn = false;
        }
        return isLoggedIn;
	}
	
	public static boolean isLoggedIn() {
		return isLoggedIn;
	}
	
	public static ServiceManager getServiceManager(String userId) {
		return CONTEXT_MANAGER_MAP.get(userId);
	}
	
	public static ServiceManager getServiceManager() {
		return CONTEXT_MANAGER_MAP.get(loggedInUserId);
	}
	
	public static void extractArchive(ClientResponse response, ProjectInfo info, String path) throws IOException, PhrescoException {
        InputStream inputStream = response.getEntityInputStream();
        FileOutputStream fileOutputStream = null;
        String archiveHome = Utility.getArchiveHome();
        File archiveFile = new File(archiveHome + info.getProjectCode() + PhrescoConstants.ARCHIVE_FORMAT);
        fileOutputStream = new FileOutputStream(archiveFile);
        try {
            byte[] data = new byte[1024];
            int i = 0;
            while ((i = inputStream.read(data)) != -1) {
                fileOutputStream.write(data, 0, i);
            }
            fileOutputStream.flush();
            ArchiveUtil.extractArchive(archiveFile.getPath(), path, ArchiveType.ZIP);
        } finally {
            Utility.closeStream(inputStream);
            Utility.closeStream(fileOutputStream);
        }
    }
	
	public static void updateProjectIntoWorkspace(String projectName) {
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
	
	public static void createProject(ProjectInfo projectInfo, IProgressMonitor monitor){
		//Create Phresco Project
		ServiceManager serviceManager = PhrescoUtil.getServiceManager();
		try {
			File file = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().getAbsolutePath() + File.separatorChar + "projects");
			ClientResponse response = serviceManager.createProject(projectInfo);
			if(response.getStatus() == 200) {
				PhrescoUtil.extractArchive(response, projectInfo, file.getPath());
				String path = "";
				List<ApplicationInfo> appInfos = projectInfo.getAppInfos();
				for (ApplicationInfo applicationInfo : appInfos) {
					String appDirName = applicationInfo.getAppDirName();
					path = file.getPath() + File.separator + appDirName;
					if("tech-android-native".equals(applicationInfo.getTechInfo().getId())) {
						String mainProjectName =appDirName + ".main"; 
						applicationInfo.setAppDirName(mainProjectName);
						String sourcePath = path + "/source";
						CreateAndroidProject(applicationInfo, sourcePath, monitor);
						applicationInfo.setAppDirName(appDirName + ".functional");
						String functionalPath = path + "/test/functional";
						CreateAndroidProject(applicationInfo, functionalPath, monitor, mainProjectName);
						applicationInfo.setAppDirName(appDirName + ".performance");
						String performancePath = path + "/test/performance";
						CreateAndroidProject(applicationInfo, performancePath, monitor, mainProjectName);
						applicationInfo.setAppDirName(appDirName + ".unit");
						String unitPath = path + "/test/unit";
						CreateAndroidProject(applicationInfo, unitPath, monitor, mainProjectName);
					} else {
						CreateGeneralProject(applicationInfo, path, monitor);
					}
				}
			}
		} catch (PhrescoException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void CreateGeneralProject(ApplicationInfo appInfo, String path, IProgressMonitor monitor){
		try {
			//Link the created Project to Eclipse
			IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(appInfo.getAppDirName());
			description.setLocation(new Path(path));
			String[] natures = {PhrescoNature.NATURE_ID, "org.maven.ide.eclipse.maven2Nature"};
			description.setNatureIds(natures);
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());
			project.create(description, monitor);
			project.open(monitor);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	private static void CreateAndroidProject(ApplicationInfo appInfo, String path, IProgressMonitor monitor){
		try {
			//System.out.println("Android Path is :: " + path );
			//Link the created Project to Eclipse
			IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(appInfo.getAppDirName());
			BuildCommand buildCommand1 = new BuildCommand();
			buildCommand1.setName("com.android.ide.eclipse.adt.ResourceManagerBuilder");
			BuildCommand buildCommand2 = new BuildCommand();
			buildCommand2.setName("com.android.ide.eclipse.adt.PreCompilerBuilder");
			BuildCommand buildCommand3 = new BuildCommand();
			buildCommand3.setName("org.eclipse.jdt.core.javabuilder");
			BuildCommand buildCommand4 = new BuildCommand();
			buildCommand4.setName("com.android.ide.eclipse.adt.ApkBuilder");
			BuildCommand[] buildSpec = {buildCommand1,buildCommand2,buildCommand3,buildCommand4};
			description.setBuildSpec(buildSpec);
			//BuildCommand buildCommand2 = new BuildCommand();
			//buildCommand2.setName("org.maven.ide.eclipse.maven2Builder");
			//[] buildSpec = {buildCommand2};
			//description.setBuildSpec(buildSpec);
			description.setLocation(new Path(path));
			String[] natures = {"com.android.ide.eclipse.adt.AndroidNature", JavaCore.NATURE_ID };
			description.setNatureIds(natures);
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());

			project.create( description, monitor);
			project.open(monitor);

			IJavaProject javaProject = JavaCore.create(project);
			IClasspathEntry[] newEntries = new IClasspathEntry[4];

			//src path
			IPath srcPath= javaProject.getPath().append("src");
			IClasspathEntry srcEntry= JavaCore.newSourceEntry(srcPath, null);
			newEntries[0]= srcEntry;

			//gen path
			IPath genPath= javaProject.getPath().append("gen");
			IClasspathEntry genEntry= JavaCore.newSourceEntry(genPath, null);
			newEntries[1]= genEntry;

			//frm container path
			IPath frmPath= new Path("com.android.ide.eclipse.adt.ANDROID_FRAMEWORK");
			IClasspathEntry frmEntry= JavaCore.newContainerEntry(frmPath, false);
			newEntries[2]= frmEntry;

			//frm container path
			IPath libPath= new Path("com.android.ide.eclipse.adt.LIBRARIES");
			IClasspathEntry libEntry= JavaCore.newContainerEntry(libPath, false);
			newEntries[3]= libEntry;


			IPath outPath= javaProject.getPath().append("bin/classes");
			javaProject.setRawClasspath(newEntries, outPath, monitor);

		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void CreateAndroidProject(ApplicationInfo appInfo, String path, IProgressMonitor monitor, String mainProjectName){
		try {
			//System.out.println("Android Path is :: " + path );


			//Link the created Project to Eclipse
			IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(appInfo.getAppDirName());
			BuildCommand buildCommand1 = new BuildCommand();
			buildCommand1.setName("com.android.ide.eclipse.adt.ResourceManagerBuilder");
			BuildCommand buildCommand2 = new BuildCommand();
			buildCommand2.setName("com.android.ide.eclipse.adt.PreCompilerBuilder");
			BuildCommand buildCommand3 = new BuildCommand();
			buildCommand3.setName("org.eclipse.jdt.core.javabuilder");
			BuildCommand buildCommand4 = new BuildCommand();
			buildCommand4.setName("com.android.ide.eclipse.adt.ApkBuilder");
			BuildCommand[] buildSpec = {buildCommand1,buildCommand2,buildCommand3,buildCommand4};
			description.setBuildSpec(buildSpec);
			//BuildCommand buildCommand2 = new BuildCommand();
			//buildCommand2.setName("org.maven.ide.eclipse.maven2Builder");
			//[] buildSpec = {buildCommand2};
			//description.setBuildSpec(buildSpec);
			description.setLocation(new Path(path));
			String[] natures = {"com.android.ide.eclipse.adt.AndroidNature", JavaCore.NATURE_ID };
			description.setNatureIds(natures);
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());


			project.create( description, monitor);
			project.open(monitor);

			IJavaProject javaProject = JavaCore.create(project);
			IClasspathEntry[] newEntries = new IClasspathEntry[5];

			//src path
			IPath srcPath= javaProject.getPath().append("src");
			IClasspathEntry srcEntry= JavaCore.newSourceEntry(srcPath, null);
			newEntries[0]= srcEntry;

			//gen path
			IPath genPath= javaProject.getPath().append("gen");
			IClasspathEntry genEntry= JavaCore.newSourceEntry(genPath, null);
			newEntries[1]= genEntry;

			//frm container path
			IPath frmPath= new Path("com.android.ide.eclipse.adt.ANDROID_FRAMEWORK");
			IClasspathEntry frmEntry= JavaCore.newContainerEntry(frmPath, false);
			newEntries[2]= frmEntry;

			//frm container path
			IPath libPath= new Path("com.android.ide.eclipse.adt.LIBRARIES");
			IClasspathEntry libEntry= JavaCore.newContainerEntry(libPath, false);
			newEntries[3]= libEntry;

			//Dependency Project Entry
			IPath mainPath= javaProject.getPath().append("../").append(mainProjectName);
			IClasspathEntry mainEntry= JavaCore.newProjectEntry(mainPath, null, false, null, false);
			newEntries[4]= mainEntry;


			IPath outPath= javaProject.getPath().append("bin/classes");
			javaProject.setRawClasspath(newEntries, outPath, monitor);

		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
