package com.photon.phresco.commons.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;

import com.photon.phresco.api.ApplicationProcessor;
import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.model.ArtifactGroup;
import com.photon.phresco.commons.model.ArtifactInfo;
import com.photon.phresco.commons.model.Customer;
import com.photon.phresco.commons.model.RepoInfo;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.plugins.model.Mojos.ApplicationHandler;
import com.photon.phresco.plugins.util.MojoProcessor;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.util.Constants;
import com.photon.phresco.util.PhrescoDynamicLoader;

public class ProjectManager implements PhrescoConstants {

	public ApplicationProcessor getApplicationProcessor() throws PhrescoException {
		ApplicationProcessor applicationProcessor = null;
		BaseAction action = new BaseAction();
		try {
			ServiceManager serviceManager = PhrescoUtil.getServiceManager(action.getUserId());

			Customer customer = serviceManager.getCustomer(action.getCustomerId());
			RepoInfo repoInfo = customer.getRepoInfo();
			StringBuilder sb = new StringBuilder(getApplicationHome())
			.append(File.separator)
			.append(Constants.DOT_PHRESCO_FOLDER)
			.append(File.separator)
			.append(Constants.APPLICATION_HANDLER_INFO_FILE);
			MojoProcessor mojoProcessor = new MojoProcessor(new File(sb.toString()));
			ApplicationHandler applicationHandler = mojoProcessor.getApplicationHandler();
			if (applicationHandler != null) {
				List<ArtifactGroup> plugins = setArtifactGroup(applicationHandler);
				PhrescoDynamicLoader dynamicLoader = new PhrescoDynamicLoader(repoInfo, plugins);
				applicationProcessor = dynamicLoader.getApplicationProcessor(applicationHandler.getClazz());
			}
		} catch (PhrescoException e) {
			throw new PhrescoException(e);
		}

		return applicationProcessor;
	}

	protected List<ArtifactGroup> setArtifactGroup(ApplicationHandler applicationHandler) {
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

	private File getProjectHome() {
		File projectPath = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString()	+ File.separator + PROJECTS);
		return projectPath;
	}

	public String getApplicationHome() throws PhrescoException {
		StringBuilder builder = new StringBuilder(getProjectHome() + File.separator + "TestProject");
		return builder.toString();
	}

}
