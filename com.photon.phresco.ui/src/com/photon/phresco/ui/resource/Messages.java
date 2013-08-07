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
	public static String LOGIN_CONFIRM_DIALOG_TITLE;
	public static String LOGIN_CONFIRM_DIALOG_MSG;
	
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
	public static String UPDATE;
	
	//Add Project
	public static String TECHNOLOGY_WIZARD_DESC;
	public static String TECHNOLOGY_WIZARD_NAME;
	public staitc String PROJECT_BUTTON_NAME;
	public static String PROJECT_LABEL_CODE;
	public static String PROJECT_CODE_MSG_TEXT;
	public static String PROJECT_NAME_MSG_TEXT;
	public static String PROJECT_LABEL_DESC;
	public static String PROJECT_LABEL_VERSION;
	public static String PROJECT_VERSION_TEXT;
	
	//Edit Project
	public static String EDIT_PROJECT_DIALOG_TITLE;
	
	//Import
	public static String PHRESCO_IMPORT_WINDOW_TITLE;
	public static String PHRESCO_IMPORT_PAGE_TITLE;
	public static String PHRESCO_IMPORT_PAGE_DESC;
	public static String IMPORT_ADD_PROJECT_PAGE_NAME;
	public static String IMPORT_TECHNOLOGY_PAGE_NAME;
	
	//Deploy
	public static String DEPLOY_DIALOG_TITLE;
	public static String DEPLOY_BTN;
	public static String DEPLOY_TITLE;
	
	//Quality
	public static String UNIT_TEST_LABEL;
	public static String UNIT_TEST_TITLE;
	
	//PDF Report
	public static String PDF_REPORT_MSG;
	public static String GENERATE;
	public static String PDF_REPORT_NAME;
	public static String PDF_REPORT_DETAILED;
	public static String PDF_REPORT_OVERALL;
	public static String PDF_REPORT_TYPE_LBL;
	public static String PDF_REPORT_DIALOG_TITLE;
	public static String PDF_REPORT_DOWNLOAD_DIALOG_TITLE;
	
	*/
	
	// Needs to be localization
	// Login
	
    public static String CONNECTION_TITLE = "Phresco Connection";
    public static String LOGIN_SUCCESSFUL = "Logged In successfully";
    public static String LOGIN_CONFIRM_DIALOG_TITLE = "Login into Phresco";
    public static String LOGIN_CONFIRM_DIALOG_MSG = "This requires Login. Do you want to configure login now";
    
	public static String PROVIDE_SCM_DETAILS="Provide SCM Details";
	public static String IMPORT_FROM_SCM="Import from SCM";
	public static String PROVIDE_MANDATORY_VALUES="Provide Mandatory Values";
	public static String PHRESCO_LOGIN_WARNING="Please Login before making Request";
	public static String SCM_TYPE="*Type";
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
	public static String REPO_URL = "*Application Repo URL";
	public static String MESSAGE = "*Message";
	public static String EMPTY_STRING_WARNING = "should not be empty";
	public static String INFORMATION = "information";
	public static String ENVIRONMENT = "Environment";
	public static String COLAN = ":";
	
	//Add Project
	public static String PROJECT_CREATION_NAME="Phresco Project";
	public static String PROJECT_CREATION_DESC="Project Creation Page";
	public static String TECHNOLOGY_WIZARD_NAME="Technology";
	public static String TECHNOLOGY_WIZARD_DESC="Technology Selection Page";
	public static String PROJECT_BUTTON_NAME="Project name *";
	public static String PROJECT_NAME_MSG_TEXT="Name of the project";
	public static String PROJECT_LABEL_CODE="Code";
	public static String PROJECT_CODE_MSG_TEXT="Project Code";
	public static String PROJECT_LABEL_DESC="Description";
	public static String PROJECT_LABEL_VERSION="Version";
	public static String PROJECT_VERSION_TEXT="1.0.0";
	
	//Edit Project
	public static String EDIT_PROJECT_DIALOG_TITLE = "Edit Project"; 
	
	//Import
	public static String PHRESCO_IMPORT_WINDOW_TITLE="Phresco";
	public static String PHRESCO_IMPORT_PAGE_TITLE="Import Phresco Project to WorkSpace";
	public static String PHRESCO_IMPORT_PAGE_DESC="Phresco Import Page Description Appears here";
	public static String IMPORT_ADD_PROJECT_PAGE_NAME="AddProjectPage";
	public static String IMPORT_TECHNOLOGY_PAGE_NAME="Technology Page";
	
	//Deploy
	public static String DEPLOY_DIALOG_TITLE = "Deploy";
	public static String DEPLOY_BTN = "Deploy";
	public static String DEPLOY_TITLE = "Deploy";
	
	//Quality
	public static String UNIT_TEST_TITLE = "Unit";
	public static String UNIT_TEST_LABEL = "Unit";
	public static String REPORT_NOT_AVAILABLE = "Report Not Available";
	
	//testsuite 
	public static String TEST_SUITE = "Test Suite";
	public static String TOTAL  = "Total";
	public static String SUCCESS = "Success";
	public static String FAILURE = "Failure";
	public static String TEST = "Test";
	
	//PDF Report
	public static String PDF_REPORT_MSG = "Atleast one Test Report should be available Or Sonar report should be available";
	public static String GENERATE = "Generate";
	public static String PDF_REPORT_NAME = "Pdf Report Name";
	public static String PDF_REPORT_DETAILED = "Detailed";
	public static String PDF_REPORT_OVERALL =  "Overall";
	public static String PDF_REPORT_TYPE_LBL = "Report Type";
	public static String PDF_REPORT_DIALOG_TITLE = "Generate PDF";
	public static String PDF_REPORT_DOWNLOAD_DIALOG_TITLE = "Download PDF";
	
/*	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}*/
}
