package com.photon.phresco.ui.util;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.internal.events.BuildCommand;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.Document;


import com.photon.phresco.commons.model.User;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.framework.PhrescoFrameworkFactory;
import com.photon.phresco.framework.api.ProjectAdministrator;
import com.photon.phresco.model.ProjectInfo;
import com.photon.phresco.ui.builder.PhrescoNature;
import com.photon.phresco.util.Credentials;

public class PhrescoUtils {
	
	public static void createProject(ProjectInfo projectInfo, User user, String path, IProgressMonitor monitor){
		if(projectInfo.getTechId().contains("android")){
			//Create Phresco Project
			ProjectAdministrator admin;
			try {
				admin = PhrescoFrameworkFactory.getProjectAdministrator();
				File file = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
				admin.createProject(projectInfo, file, user);
			} catch (PhrescoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String projectCode = projectInfo.getCode();
			String mainProjectName =projectCode + ".main"; 
			projectInfo.setCode(mainProjectName);
			String sourcePath = path + "/source";
			CreateAndroidProject(projectInfo, user, sourcePath, monitor);
			projectInfo.setCode(projectCode + ".functional");
			String functionalPath = path + "/test/functional";
			CreateAndroidProject(projectInfo, user, functionalPath, monitor, mainProjectName);
			projectInfo.setCode(projectCode + ".performance");
			String performancePath = path + "/test/performance";
			CreateAndroidProject(projectInfo, user, performancePath, monitor, mainProjectName);
			projectInfo.setCode(projectCode + ".unit");
			String unitPath = path + "/test/unit";
			CreateAndroidProject(projectInfo, user, unitPath, monitor, mainProjectName);
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
	
	private static void CreateAndroidProject(ProjectInfo projectInfo, User user, String path, IProgressMonitor monitor, String mainProjectName){
		try {
			//System.out.println("Android Path is :: " + path );
			
			
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
