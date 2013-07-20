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
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
