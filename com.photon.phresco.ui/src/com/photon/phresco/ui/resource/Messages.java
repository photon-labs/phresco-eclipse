/*
 * ###
 * 
 * Copyright (C) 1999 - 2012 Photon Infotech Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ###
 */

package com.photon.phresco.ui.resource;

import org.eclipse.osgi.util.NLS;

/**
 * To handle for localization
 * @author syed
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.photon.phresco.ui.resource.messages"; //$NON-NLS-1$
	/*
	// Login page related keys
	public static String LOGIN_ID;
	public static String LOGIN_PWD;
	public static String LOGIN_FAILED_MSG;
	
	// Import From SCM related Keys
	public static String PROVIDE_SCM_DETAILS;
	public static String IMPORT_FROM_SCM;
	public static String PROVIDE_MANDATORY_VALUES;
	public static String PHRESCO_LOGIN_WARNING;
	
	public static String SCM_TYPE;
	public static String TYPE_GIT;
	public static String TYPE_SVN;
	public static String REPO_RUL;
	public static String OTHER_CREDENTIALS;
	public static String USER_NAME;
	public static String USER_PWD;
	public static String REVISION;
	public static String HEAD_REVISION;
	public static String TEST_CHECKOUT;
	public static String PROJ_ALREADY_IMPORTED;
	public static String FAILED;
	public static String PROJ_IMPORT_FAILED;
	
	//Build related Keys
	public static String  BUILD;
	public static String  OK;
	public static String  CANCEL;
	public static String ERROR;
	
	//Create page related keys
	public static String WARNING;
	public static String WARN_APPCODE_EMPTY;
	
	//Design related keys
	public static String NAME;
	public static String CODE;
	public static String DESCRIPTION;
	public static String APP_DIRECTORY;
	public static String VERSION;
	public static String TECHNOLOGY;
	public static String HYPHEN;
	public static String SERVER;
	public static String SERVERS;
	public static String VERSIONS;
	public static String DATABASE;
	public static String DATABASES;
	public static String WEBSERVICES;
	public static String UPDATE;*/
	
	// Needs to be localization
	
	public static String PROVIDE_SCM_DETAILS="Provide SCM Details";
	public static String IMPORT_FROM_SCM="Import from SCM";
	public static String PROVIDE_MANDATORY_VALUES="Provide Mandatory Values";
	public static String PHRESCO_LOGIN_WARNING="Please Login before making Request";
	public static String SCM_TYPE="Type";
	public static String TYPE_GIT="Git";
	public static String TYPE_SVN="Svn";
	public static String REPO_RUL="*Repo URL";
	public static String OTHER_CREDENTIALS="Other Credentials";
	public static String USER_NAME="*Username";
	public static String USER_PWD="*Password";
	public static String REVISION="*Revision";
	public static String HEAD_REVISION="HeadRevision";
	public static String TEST_CHECKOUT="Test Checkout";
	public static String LOGIN_ID="User Id";
	public static String LOGIN_PWD="Password";
	public static String LOGIN_FAILED_MSG="Login Failed \n\n\nInvalid credentials";
	public static String PROJ_ALREADY_IMPORTED="Project already imported";
	public static String PROJ_IMPORT_FAILED="Failed to import project";
	public static String BUILD="Build";
	public static String OK="Ok";
	public static String CANCEL="Cancel";
	public static String ERROR="Error";
	public static String WARNING="Warning";
	public static String WARN_APPCODE_EMPTY="Appcode should not be empty";
	public static String NAME="*Name";
	public static String CODE="*Code";
	public static String DESCRIPTION="Description";
	public static String APP_DIRECTORY="*App Directory";
	public static String VERSION="Version";
	public static String TECHNOLOGY="Technology";
	public static String HYPHEN="-";
	public static String SERVER="Server";
	public static String SERVERS="Servers";
	public static String VERSIONS="Versions";
	public static String DATABASE="Database";
	public static String DATABASES="Databases";
	public static String WEBSERVICES="Web Services";
	public static String UPDATE="Update";	
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
