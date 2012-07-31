package com.photon.phresco.ui.wizards.pages;

import java.util.List;

import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.m2e.core.ui.internal.wizards.MavenImportWizardPage;
import org.eclipse.ui.IWorkingSet;

@SuppressWarnings( "restriction" )
public class ProjectImportPage extends MavenImportWizardPage{
	
	 public ProjectImportPage(ProjectImportConfiguration importConfiguration,
			List<IWorkingSet> workingSets) {
		super(importConfiguration, workingSets);
		setTitle("Import Phresco Project to WorkSpace");
		setDescription("Phresco Import Page Description Appears here");
		// TODO Auto-generated constructor stub
	}

	 	
		    
}
