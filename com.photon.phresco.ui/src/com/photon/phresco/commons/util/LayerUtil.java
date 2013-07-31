package com.photon.phresco.commons.util;

import java.util.List;

import com.photon.phresco.commons.PhrescoDialog;
import com.photon.phresco.commons.model.ApplicationType;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.ui.model.BaseAction;

public class LayerUtil {

	public static ApplicationType getApplicationType(String layerType) {
		BaseAction action = new BaseAction();
		String userId = action.getUserId();
		String customerId = action.getCustomerId();
		ServiceManager serviceManager = PhrescoUtil.getServiceManager(userId);
		if(serviceManager == null) {
			PhrescoDialog.errorDialog(null, "Error", "Please Login before making Request");
			return null;
		}
		try {
			List<ApplicationType> applicationTypes = serviceManager.getApplicationTypes(customerId);
			for (ApplicationType applicationType : applicationTypes) {
				if(applicationType.getName().equals(layerType)) {
					return applicationType;
				}
			}
		} catch (PhrescoException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void getTechNames(String techGroupName) {
		
	}
}
