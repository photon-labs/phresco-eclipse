package com.photon.phresco.commons.util;

import java.util.HashMap;
import java.util.Map;

import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.model.User;
import com.photon.phresco.exception.PhrescoWebServiceException;
import com.photon.phresco.service.client.api.ServiceContext;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.service.client.factory.ServiceClientFactory;
import com.photon.phresco.service.client.impl.ServiceManagerImpl;


public class PhrescoUtil implements PhrescoConstants {

	static boolean isLoggedIn = false;
	public static final Map<String, ServiceManager> CONTEXT_MANAGER_MAP = new HashMap<String, ServiceManager>();
	
	public static boolean doLogin(String userName, String password) throws PhrescoWebServiceException {
		
		ServiceContext context = new ServiceContext();
        context.put(SERVICE_URL, "http://172.16.8.250:7070/service-testing/rest/api");
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
        	System.out.println(" user name : " + userInfo.getDisplayName());
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
}
