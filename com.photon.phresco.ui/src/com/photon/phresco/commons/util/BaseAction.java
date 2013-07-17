package com.photon.phresco.commons.util;

import org.eclipse.jface.preference.IPreferenceStore;

import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.model.Customer;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.ui.PhrescoPlugin;

public class BaseAction implements PhrescoConstants {

	public IPreferenceStore getPreferenceStore() {
		return PhrescoPlugin.getDefault().getPreferenceStore();
	}
	
	public String getUserId() {
		return getPreferenceStore().getString(USER_ID);
	}
	
	public String getCustomerId() {
		ServiceManager serviceManager = PhrescoUtil.getServiceManager(getUserId());
		if(serviceManager != null) {
			try {
				Customer customer = serviceManager.getCustomers().get(0);
				return customer.getId();
			} catch (PhrescoException e) {
				e.printStackTrace();
			}
		}
		return "";
	}
}
