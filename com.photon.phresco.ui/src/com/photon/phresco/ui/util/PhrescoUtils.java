package com.photon.phresco.ui.util;

import com.photon.phresco.commons.model.User;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.framework.PhrescoFrameworkFactory;
import com.photon.phresco.framework.api.ProjectAdministrator;
import com.photon.phresco.util.Credentials;

public class PhrescoUtils {
	
	public static void main(String[] args) {
		User user = doLogin();
		System.out.println("user is working :: " + user);
	}
	
	public static User doLogin(){
		
		try {
			ProjectAdministrator admin = PhrescoFrameworkFactory.getProjectAdministrator();
			//IPreferenceStore store = Activator.getDefault().getPreferenceStore();
			String username = "hariharan_t";
			String password = "Unicast@3";
			Credentials credentials = new Credentials(username, password);
			User user = admin.doLogin(credentials);
			System.out.println("user is " + user.getDisplayName());
			return user;
		} catch (PhrescoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
		
	}

	public PhrescoUtils() {
		// TODO Auto-generated constructor stub
	}

}
