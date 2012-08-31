package com.photon.phresco.ui.util;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.internal.events.BuildCommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import com.photon.phresco.commons.model.User;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.framework.PhrescoFrameworkFactory;
import com.photon.phresco.framework.api.ProjectAdministrator;
import com.photon.phresco.model.ProjectInfo;
import com.photon.phresco.ui.builder.PhrescoNature;
import com.photon.phresco.util.Credentials;

public class PhrescoUtils {
	
	public static void createProject(ProjectInfo projectInfo, User user, String path, IProgressMonitor monitor){
		if(projectInfo.getTechId().equalsIgnoreCase("tech-android-native")){
			CreateAndroidProject(projectInfo, user, path, monitor);
		}else{
			CreateGeneralProject(projectInfo, user, path, monitor);
		}
		
		
	}
	
	
	private static void CreateGeneralProject(ProjectInfo projectInfo, User user, String path, IProgressMonitor monitor){
		try {
			//Create Phresco Project
			ProjectAdministrator admin = PhrescoFrameworkFactory.getProjectAdministrator();
			File file = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
			admin.createProject(projectInfo, file, user);
			//Link the created Project to Eclipse
			IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(projectInfo.getCode());
			description.setLocation(new Path(path));
			String[] natures = {PhrescoNature.NATURE_ID, "org.maven.ide.eclipse.maven2Nature"};
			description.setNatureIds(natures);
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());
			
			project.create(description, monitor);
			project.open(monitor);
		} catch (PhrescoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private static void CreateAndroidProject(ProjectInfo projectInfo, User user, String path, IProgressMonitor monitor){
		try {
			//System.out.println("Android Path is :: " + path );
			path = path + "/source";
			//Create Phresco Project
			ProjectAdministrator admin = PhrescoFrameworkFactory.getProjectAdministrator();
			File file = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
			admin.createProject(projectInfo, file, user);
			
			//Link the created Project to Eclipse
			IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(projectInfo.getCode());
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
			
			String[] natures = {PhrescoNature.NATURE_ID, "org.maven.ide.eclipse.maven2Nature", "com.android.ide.eclipse.adt.AndroidNature", "org.eclipse.jdt.core.javanature" };
			description.setNatureIds(natures);
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());
			project.create(description, monitor);
			project.open(monitor);
		} catch (PhrescoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static void setSrcFolder(){
		
	}
	
	private static void setClassPathToNewLibs(String projectName,String[] jarPathList){
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        try {
            IJavaProject javaProject = (IJavaProject)project.getNature(JavaCore.NATURE_ID);
            IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();
            List list = new LinkedList(java.util.Arrays.asList(rawClasspath));
            for(String path:jarPathList){
                String jarPath = path.toString();
                boolean isAlreadyAdded=false;
                for(IClasspathEntry cpe:rawClasspath){
                    isAlreadyAdded=cpe.getPath().toOSString().equals(jarPath);
                    if (isAlreadyAdded) break;
                }
                if (!isAlreadyAdded){
                    IClasspathEntry jarEntry = JavaCore.newLibraryEntry(new Path(jarPath),null,null);
                    list.add(jarEntry);
                }
            }
            IClasspathEntry[] newClasspath = (IClasspathEntry[])list.toArray(new IClasspathEntry[0]);
            javaProject.setRawClasspath(newClasspath,null);
        } catch (CoreException e) {
            e.printStackTrace();
        }
	}
	
	
	
	
	


	public PhrescoUtils() {
		// TODO Auto-generated constructor stub
	}

}
