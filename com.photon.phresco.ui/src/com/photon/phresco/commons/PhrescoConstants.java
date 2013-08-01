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
 
package com.photon.phresco.commons;

/**
 * 
 * @author syed
 *
 */
 
public interface PhrescoConstants {
	
	String DEFAULT_SERVICE_URL = "http://172.16.8.250:7070/service-testing/rest/api";
	String SONAR_REPORT_URL = "http://localhost:9000/dashboard/index/";
	
	char CHAR_ASTERISK = '*';
	
	String STR_EMPTY = "";
	
    String USER_ID = "userId";
    String PASSWORD = "Password";
    String CUSTOMER_ID = "customerId";
    
    String DEFAULT_USER_NAME = "admin";
    String DEFAULT_PASSWORD = "manage";
    
    String CONNECTION_TITLE = "Phresco Connection";
    String LOGIN_SUCCESSFUL = "Logged In successfully";
    String LOGIN_FAILED = "Login Failed \n\n\nInvalid credentials";
    
    String SERVICE_URL = "phresco.service.url";
	String SERVICE_USERNAME = "phresco.service.username";
	String SERVICE_PASSWORD = "phresco.service.password";
	String SERVICE_API_KEY = "phresco.service.api.key";
	
	String ADMIN_USER = "admin";
	String ADMIN_PWD = "manage";
	
	String ARCHIVE_FORMAT = ".zip";
	
	//FOR SCMManagerUtil
	
	String SVN = "svn";
    String GIT = "git";
    String BITKEEPER = "bitkeeper";
    String REPO_TYPE = "repoType";
    String REPO = "repo";
    String IS_NOT_WORKING_COPY = "is not a working copy";
    String UPDATE_SVN_PROJECT = "updateProject";
    String SVN_TYPE = "svnType";
    String SVN_CHECKOUT_TEMP = "svn-checkout-temp";
    String PHRESCO = "/.phresco";
    String GITHUB_SCM = "hudson.plugins.git.GitSCM";
    String SVN_SCM = "hudson.scm.SubversionSCM";
    String GITHUB_SCM_FILE_NAME = "gitHubScm.xml";
    String COLON = ":";
    String HTTP_PROTOCOL = "http";
    String PROTOCOL_POSTFIX = "://";
    String SPLIT_DOT = "\\.";
    String FORWARD_SLASH = "/";
    String BACK_SLASH = "\\";
    String GITIGNORE_FILE = "/.gitignore";
    String SUCCESSFUL = "Successful";
    String URL = "url";
    
    /*
     * Common constants
     */
    String HYPHEN = "-";
    String KEY_QUOTES = "\"";
    String WORKSPACE_DIR = "workspace";
    String SPACE = " ";
    String UNDERSCORE = "_";
    String PHRESCO_DOT = "phresco.";
    
    /**
     * BitKeeper Commands
     */
    String BK_CLONE = "bk clone";
    String BK_PARENT = "bk parent";
    String BK_PULL = "bk pull";
    String BK_PUSH = "bk push";
    String BK_COMMIT = "bk commit";
    String BK_ADD_FILES = "bk -x -r ci -i";
    String BK_CI = "bk ci";
    String BK_ADD_COMMENT = "-y\"";
    String HEAD_REVISION = "HEAD";
    String UNVERSIONED = "unversioned";
    String ALREADY_EXISTS = "exists and is not empty";
    String DO_NOT_CHECKIN_DIR = "do_not_checkin";
    String DOT = ".";
    String NONE = "None";
    String NEWLINE = "\n";
    String FOLDER_DOT_PHRESCO = ".phresco";
    String POM_FILENAME	="pom.xml";
    
    /*
     * Project Import keys
     */
    String IMPORT_PROJECT_FAIL = "import.project.fail";
    String UPDATE_PROJECT_FAIL = "update.project.fail";
    String INVALID_CREDENTIALS = "import.invalid.credential";
    String APPLN_INVALID_CREDENTIALS = "import.appln.invalid.credential";
    String INVALID_FOLDER = "import.invalid.folder";
    String PROJECT_ALREADY = "import.project.already";
    String SVN_FAILED = "failed";
    String SVN_INTERNAL = "Internal";
    String SVN_IS_NOT_WORKING_COPY = "is not a working copy";
    String INVALID_URL = "import.invalid.url";
    String INVALID_REVISION = "import.invalid.revision";
    String NOT_WORKING_COPY = "not.working.dir";
    String NO_POM_XML = "project.pom.not.exist";
    String POM_URL_FAIL = "project.pomurlupdate.fail";
    String PROJECT_INFO = "project.info";
    String GIT_IMPORT_TEMP_DIR = "gitImportTemp";
    String TEMP_FOLDER ="temp";
    
    String SCM = "scm";
	String ZIP_FILE = "zip";
    String MASTER = "master";
    String UPDATE = "update";
    String REMOTE = "remote";
    String ORIGIN = "origin";
    String FETCH = "fetch";
    String BRANCH = "branch";
    String MERGE = "merge";
    String REF_HEAD_MASTER = "refs/heads/master";
    String REFS_HEADS_REMOTE_ORIGIN = "+refs/heads/*:refs/remotes/origin/*";
    
    String PROJECTS = "projects";
	String DOT_PHRESCO_FOLDER = ".phresco";
	String ENVIRONMENT_CONFIG_FILE = "phresco-env-config.xml";
	String APPLICATION_HANDLER_INFO_FILE= "phresco-application-handler-info.xml";
	
	// Build Constants
	
	String PACKAGE_INFO_FILE = "phresco-package-info.xml";
	String PACKAGE_GOAL		 = "package";
	String ENVIROMENT        = "Environment";
	String ENVIRONMENT_NAME  = "Environment Name";
	String DELETE            = "Delete";
	String NAME		         = "Name"; 
	String DESCRITPTION      = "Description";
	String CONFIGURE         = "Configure";
	String CLONE		     = "Clone";
	String CANCEL 		     = "Cancel";
	String SAVE				 = "Save";
	String STRING		     = "String";
	String BOOLEAN		     = "Boolean";
	String NUMBER 		     = "Number";
	String LIST				 = "List";
	String MAP				 = "Map";
	String DEFAULT			 = "Default";
	String CONFIGURATION 	 = "Configuration";
	String FRAMEWORK		 = "framework";
	String STR_SPACE         = " ";
	String MAVEN_COMMAND	 = "mvn";
	String BUILD			 = "build";
	String BUILD_INFO        = "build.info";
	
	String BUILD_NO          = "No";
	String BUILD_NAME		 = "BuildName";
	String DOWNLOAD 		 = "Download";
	String DEPLOY 		     = "Deploy";
	String DYNAMIC_PARAMETER = "DynamicParameter";
	
	// Deploy constants
	
	String 	DEPLOY_INFO_FILE = "phresco-deploy-info.xml";
	
	// Code Validate
	
	String VALIDATECODE_INFO_FILE = "phresco-validate-code-info.xml";
	String VALIDATE_CODE_GOAL     = "validate-code";
	String VALIDATE				  = "Validate";
	String SONAR_BRANCH			  = "sonar.branch";
	String SONAR_STATUS 		  = "Sonar Status";
	String SONAR_DIALOG_NAME      = "Code Validation";
	String SONAR_STATUS_MESSAGE    = "Sonar is not Yet Started . Start the sonar to continue";
	String SONAR_REPORT_NOTAVAILABLE = "Reports Not Available";
	String SONAR_HOST_URL            = "http://localhost:9000";
	String SONAR_LOGS 				= "Sonar Logs ";
	
	// Unit Test
	
	String UNITTEST_INFO_FILE = "phresco-unit-test-info.xml";
	String UNIT_TEST_GOAL     = "unit-test";
	String TEST				  = "Test";
	String UNIT_LOGS 		  = "Unit logs";
	

	String FAILED = "FAILED";
	String OK = "OK";	
	
	//platform Constants
	String OS_NAME = "os.name";
	String OS_ARCH = "os.arch";
	String LINUX = "Linux";
	String MAC = "Mac";
	String SERVER = "Server";
	String WINDOWS_7 = "Windows 7";
	String BIT_64 = "64";
	String BIT_86 = "86";
	
	String PHRESCO_SQL_PATH = "phresco.sql.path";
	String KEY_EMPTY = "KEY_EMPTY";
	
	//Pdf constants 
	
	String STATIC_ANALYSIS_REPORT = "static-analysis-report";
	String INDEX_HTML = "index.html";
	String FILE_EXTENSION_XML = "xml";
	String FUNCTIONAL = "functional";
	String TYPE_LIST = "list";
	String REQ_TEST_AGAINST = "testAgainst";
	String TEST_DIRECTORY = "dir_type";
	String REQ_REPORT_TYPE = "reportType";
	String REQ_TEST_TYPE = "testType";
	String FROMPAGE_ALL = "All";
	String REQ_REPORT_NAME = "reportName";
	String HYPHEN_N = "-N";
	String PLUGIN_PARAMETER_FRAMEWORK = "framework";
	String LBL_PHRESCO = "phresco";
	String ARCHIVES = "archives";
	String CUMULATIVE = "cumulativeReports";
	String PDF = "pdf";
	
}
