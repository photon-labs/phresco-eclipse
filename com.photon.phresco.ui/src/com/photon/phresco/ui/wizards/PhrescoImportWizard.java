package com.photon.phresco.ui.wizards;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.maven.model.Model;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.core.internal.lifecyclemapping.discovery.IMavenDiscovery;
import org.eclipse.m2e.core.internal.lifecyclemapping.discovery.IMavenDiscoveryProposal;
import org.eclipse.m2e.core.project.IMavenProjectImportResult;
import org.eclipse.m2e.core.project.MavenProjectInfo;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.m2e.core.ui.internal.actions.SelectionUtil;
import org.eclipse.m2e.core.ui.internal.wizards.AbstactCreateMavenProjectJob;
import org.eclipse.m2e.core.ui.internal.wizards.LifecycleMappingPage;
import org.eclipse.m2e.core.ui.internal.wizards.MavenImportWizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;

import com.photon.phresco.commons.ConfirmDialog;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.ui.PhrescoNature;

/**
 * @author suresh_ma
 *
 */
public class PhrescoImportWizard extends MavenImportWizard {
	
	private ImportWizardPage page;
	private LifecycleMappingPage lifecycleMappingPage;
	private List<String> locations;
	private boolean showLocation = true;
	//private LifecycleMappingConfiguration mappingConfiguration;

	  public PhrescoImportWizard() {
	    setNeedsProgressMonitor(true);
	    setWindowTitle("Phresco Import Wizard");
	  }

	  public PhrescoImportWizard(ProjectImportConfiguration importConfiguration, List<String> locations) {
	    this.locations = locations;
	    this.showLocation = false;
	    setNeedsProgressMonitor(true);
	  }

	  public void init(IWorkbench workbench, IStructuredSelection selection) {
	    super.init(workbench, selection);
	    if(locations == null || locations.isEmpty()) {
	      IPath location = SelectionUtil.getSelectedLocation(selection);
	      if(location != null) {
	        locations = Collections.singletonList(location.toOSString());
	      }
	    }
	  }

	  public void addPages() {
		ServiceManager serviceManager = PhrescoUtil.getServiceManager(PhrescoUtil.getUserId());
		if(serviceManager == null) {
			Shell shell = new Shell();
			ConfirmDialog.getConfirmDialog().showConfirm(shell);
			return;
		}
	    page = new ImportWizardPage(importConfiguration, workingSets);
	    page.setLocations(locations);
	    page.setShowLocation(showLocation);
	    addPage(page);  

	    lifecycleMappingPage = new LifecycleMappingPage();
	    addPage(lifecycleMappingPage);
	  }

	  @SuppressWarnings("deprecation")
	  public boolean performFinish() {
		  
		  //mkleint: this sounds wrong.
		  if(!page.isPageComplete()) {
			  return false;
		  }

		  final MavenPlugin plugin = MavenPlugin.getDefault();
		  final List<IMavenDiscoveryProposal> proposals = getMavenDiscoveryProposals();
		  final Collection<MavenProjectInfo> projects = getProjects();
		  try {
			  getContainer().run(true, true, new IRunnableWithProgress() {
				  @SuppressWarnings("static-access")
				  public void run(final IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException {
					  // Use the monitor from run() in order to provide progress to the wizard 
					  Job job = new AbstactCreateMavenProjectJob("import Phresco project job created", workingSets) {

						  @Override
						  protected List<IProject> doCreateMavenProjects(IProgressMonitor pm) throws CoreException {
							  SubMonitor monitor = SubMonitor.convert(progressMonitor, 101);
							  try {
								  IMavenDiscovery discovery = getDiscovery();
								  boolean restartRequired = false;
								  if(discovery != null && !proposals.isEmpty()) {
									  //restartRequired = discovery.isRestartRequired(proposals, monitor);
									  // No restart required, install prior to importing
									  if(!restartRequired) {
										  //discovery.implement(proposals, monitor.newChild(50));
									  }
								  }
								  // Import projects
								  monitor.beginTask("Phresco project import in progress", proposals.isEmpty() ? 100 : 50);

								  for(MavenProjectInfo inf : projects){
									  IProject project = create(inf, importConfiguration, monitor);
									  addNature(project,monitor);
								  }
								  List<IMavenProjectImportResult> results = plugin.getProjectConfigurationManager().importProjects(
										  projects, importConfiguration, monitor.newChild(proposals.isEmpty() ? 100 : 50));

								  // Restart required, schedule job
								  if(restartRequired && !proposals.isEmpty()) {
									  //discovery.implement(proposals, monitor.newChild(1));
								  }
            
								  return toProjects(results);
							  } finally {
								  monitor.done();
							  }
						  }
					  };
					  job.setRule(plugin.getProjectConfigurationManager().getRule());
					  job.schedule();
					  job.join();
				  }
			  });
			  return true;
		  } catch(InvocationTargetException e) {
			  // TODO This doesn't seem like it should occur
		  } catch(InterruptedException e) {
			  // User cancelled operation, we don't return the 
		  }
		  return false;
	  }

	  /* (non-Javadoc)
	   * @see org.eclipse.jface.wizard.Wizard#canFinish()
	   */
	  @Override
	  public boolean canFinish() {
		  if(isCurrentPageKnown()) {
			  // Discovery pages aren't added to the wizard in case they need to go away
			  IWizardPage cPage = getContainer().getCurrentPage();
			  while(cPage != null && cPage.isPageComplete()) {
				  cPage = cPage.getNextPage();
			  }
			  return cPage == null || cPage.isPageComplete();
		  }

		  //in here make sure that the lifecycle page is hidden from view when the mappings are fine
		  //but disable finish when there are some problems (thus force people to at least look at the other page)
		  boolean complete = page.isPageComplete();
		  if (complete && getContainer().getCurrentPage() == page) { //only apply this logic on the first page
			  /**LifecycleMappingConfiguration mapping = getMappingConfiguration();
			   * if (mapping == null || !mapping.isMappingComplete()) {
			   * return false;
	       }*/
		  }
		  return super.canFinish();
	  }

	  /*
	   * Is the current page known by the wizard (ie, has it been passed to addPage())
	   */
	  private boolean isCurrentPageKnown() {
	    for(IWizardPage p : getPages()) {
	      if(p == getContainer().getCurrentPage()) {
	        return false;
	      }
	    }
	    return true;
	  }

	  /**
	   * @return
	   */
	 private List<IMavenDiscoveryProposal> getMavenDiscoveryProposals() {
	    return lifecycleMappingPage.getSelectedDiscoveryProposals();
	  }

	  public Collection<MavenProjectInfo> getProjects() {
	    return page.getProjects();
	  }

	  
	  
	  private static void addNature(IProject project,IProgressMonitor monitor) throws CoreException {
		  if (!project.hasNature(PhrescoNature.NATURE_ID)) {
			  IProjectDescription description = project.getDescription();
			  String[] prevNatures = description.getNatureIds();
			  String[] newNatures = new String[prevNatures.length + 1];
			  System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
			  newNatures[prevNatures.length] = PhrescoNature.NATURE_ID;
			  description.setNatureIds(newNatures);
			  project.setDescription(description, monitor);
		  }
	  }
  
	  @SuppressWarnings("deprecation")
	private IProject create(MavenProjectInfo projectInfo, ProjectImportConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		  IWorkspace workspace = ResourcesPlugin.getWorkspace();
		  IWorkspaceRoot root = workspace.getRoot();
	   
		  File pomFile = projectInfo.getPomFile(); 
		  Model model = projectInfo.getModel();
//		  Model model = projectInfo.getModel();
	    
		  String projectName = configuration.getProjectName(model);

		  File projectDir = pomFile.getParentFile();
		  String projectParent = projectDir.getParentFile().getAbsolutePath();

		  if (projectInfo.getBasedirRename() == MavenProjectInfo.RENAME_REQUIRED) {
			  File newProject = new File(projectDir.getParent(), projectName);
			  if(!projectDir.equals(newProject)) {
				  boolean renamed = projectDir.renameTo(newProject);
				  if(!renamed) {
					  StringBuilder msg = new StringBuilder();
					  msg.append(NLS.bind("error", projectDir.getAbsolutePath())).append('.');
					  if (newProject.exists()) {
						  msg.append(NLS.bind("error", newProject.getAbsolutePath()));
					  }
					  //throw new CoreException(new Status(IStatus.ERROR, IMavenConstants.PLUGIN_ID, -1, msg.toString(), null));
				  }
				  projectInfo.setPomFile(getCanonicalPomFile(newProject));
				  projectDir = newProject;
			  }
		  } else {
			  if(projectParent.equals(root.getLocation().toFile().getAbsolutePath())) {
				  // immediately under workspace root, project name must match filesystem directory name
				  projectName = projectDir.getName();
			  }
		  }

		  monitor.subTask(NLS.bind("project", projectName));

		  IProject project = root.getProject(projectName);
		  if(project.exists()) {
			  //console.logError("Project " + projectName + " already exists");
			  return null;
		  }

		  if(projectDir.equals(root.getLocation().toFile())) {
			  //console.logError("Can't create project " + projectName + " at Workspace folder");
			  return null;
		  }

		  if(projectParent.equals(root.getLocation().toFile().getAbsolutePath())) {
			  project.create(monitor);
		  } else {
			  IProjectDescription description = workspace.newProjectDescription(projectName);
			  description.setLocation(new Path(projectDir.getAbsolutePath()));
			  project.create(description, monitor);
		  }

		  if(!project.isOpen()) {
			  project.open(monitor);
		  }

		  //ResolverConfiguration resolverConfiguration = configuration.getResolverConfiguration();
		  //enableBasicMavenNature(project, resolverConfiguration, monitor);

		  return project;
	  }
  
	  private File getCanonicalPomFile(File projectDir) throws CoreException {
		  try {
			  return new File(projectDir.getCanonicalFile(), IMavenConstants.POM_FILE_NAME);
		  } catch(IOException ex) {
			  ex.printStackTrace();
		  }
		  return null;
	  }

}
