package com.photon.phresco.commons.util;

import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.model.User;
import com.photon.phresco.exception.PhrescoWebServiceException;
import com.photon.phresco.service.client.api.ServiceContext;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.service.client.factory.ServiceClientFactory;


public class PhrescoUtil implements PhrescoConstants {

	static boolean isLoggedIn = false;
	
	public static boolean doLogin(String userName, String password) throws PhrescoWebServiceException {
		
		ServiceContext context = new ServiceContext();
        context.put(SERVICE_URL, "http://172.16.8.250:7070/service-testing/rest/api");
		System.out.println(" user name in phresco util : " + userName);
		System.out.println(" password in phresco util : " + password);
        context.put(SERVICE_USERNAME, userName);
        context.put(SERVICE_PASSWORD, password);
        System.out.println("ServiceUrl--------> " + context.get(SERVICE_URL));
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
}
