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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.photon.phresco.commons.model.CertificateInfo;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.framework.model.AddCertificateInfo;
import com.photon.phresco.framework.model.RemoteCertificateInfo;
import com.photon.phresco.ui.phrescoexplorer.ConfigurationCreation;
import com.photon.phresco.ui.resource.Messages;
import com.photon.phresco.util.Utility;

/**
 * confirm dialog to login
 * @author syed
 *
 */

public class ConfirmDialog implements PhrescoConstants {

	static ConfirmDialog confirmDialog = null;
	
	public ConfirmDialog() {		
		confirmDialog = this;
	}
	
	public static ConfirmDialog getConfirmDialog() {
		if (confirmDialog == null) {
			new ConfirmDialog();
		}
		return confirmDialog;
	}
	
	public void showConfirm(Shell s) {
		MessageBox messageBox = new MessageBox(s, SWT.ICON_QUESTION
	            | SWT.YES | SWT.NO);
			messageBox.setText(Messages.LOGIN_CONFIRM_DIALOG_TITLE);
	        messageBox.setMessage(Messages.LOGIN_CONFIRM_DIALOG_MSG);
	    int response = messageBox.open();
	    
	    if (response==SWT.YES) {
            PreferenceManager pm = PlatformUI.getWorkbench( ).getPreferenceManager();
            IPreferenceNode[] rootSubNodes = pm.getRootSubNodes();
            for (int i = 0; i < rootSubNodes.length; i++) {
            	IPreferenceNode iPreferenceNode = rootSubNodes[i];
            	if (PHRESCO_PREFERENCE_ID.equals(iPreferenceNode.getId())) {
            		String displayLoginPage[] = new String[1];
            		displayLoginPage[0] = PhrescoConstants.PHRESCO_LOGIN_PREFERENCE_ID;
            		PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(s, PHRESCO_PREFERENCE_ID, displayLoginPage, null);
            		dialog.open();
            	}
			}
	    }
	}
	
	public String addCerficate(Shell s, RemoteCertificateInfo authenticateServerInfo, String value, String env, String host, String port) throws PhrescoException {
		MessageBox messageBox = new MessageBox(s, SWT.ICON_QUESTION
	            | SWT.YES | SWT.NO);
			messageBox.setText("Add Certificate");
	        messageBox.setMessage("Do you want to add Certificate");
	    int response = messageBox.open();
	    if (response==SWT.YES) {
	    	try {
				AddCertificateInfo info = new AddCertificateInfo();
				info.setPropValue(value);
				info.setAppDirName(PhrescoUtil.getApplicationInfo().getAppDirName());
				info.setConfigName(SERVER);
				List<CertificateInfo> certificates = authenticateServerInfo.getCertificates();
				if (CollectionUtils.isNotEmpty(certificates)) {
					for (CertificateInfo certificateInfo : certificates) {
						info.setCertificateName(certificateInfo.getDisplayName());
					}
				}
				info.setEnvironmentName(env);
				info.setHost(host);
				info.setPort(port);
				String certificatePath = addCertificate(info);
				return certificatePath;
			} catch (PhrescoException e) {
				throw new PhrescoException(e);
			}
	    }
		return "";
	}
	
	public String addCertificate(AddCertificateInfo addCertificateInfo) {
		String certificatePath = "";
		try {
			String propValue = addCertificateInfo.getPropValue();
			String appDirName = addCertificateInfo.getAppDirName();
			if (StringUtils.isNotEmpty(propValue)) {
				File file = new File(propValue);
				certificatePath = configCertificateSave(propValue, file, appDirName, addCertificateInfo);
				return certificatePath;
			}
		} catch (PhrescoException e) {
		}
		return null;
	}
	
	private String configCertificateSave(String value, File file, String appDirName,
			AddCertificateInfo addCertificateInfo) throws PhrescoException {
		if (file.exists()) {
			String path = Utility.getProjectHome().replace("\\", "/");
			value = value.replace(path + appDirName + "/", "");
		} else {
			StringBuilder sb = new StringBuilder(FOLDER_DOT_PHRESCO).append(File.separator).append(CERTIFICATES)
					.append(File.separator).append(addCertificateInfo.getEnvironmentName()).append(HYPHEN).append(
							addCertificateInfo.getConfigName()).append(FrameworkConstants.DOT)
							.append(FILE_TYPE_CRT);
			value = sb.toString();
			saveCertificateFile(value, addCertificateInfo.getHost(), Integer
					.parseInt(addCertificateInfo.getPort()), addCertificateInfo.getCertificateName(), appDirName);
		}
		return value;
	}


	private void saveCertificateFile(String certificatePath, String host, int port,
			String certificateName, String appDirName) throws PhrescoException {
		List<CertificateInfo> certificates = ConfigurationCreation.getCertificate(host, port);
		if (CollectionUtils.isNotEmpty(certificates)) {
			for (CertificateInfo certificate : certificates) {
				if (certificate.getDisplayName().equals(certificateName)) {
					File file = new File(Utility.getProjectHome() + appDirName + "/" + certificatePath);
					addCertificate(certificate, file);
				}
			}
		}
	}

	public static void addCertificate(CertificateInfo info, File file) throws PhrescoException {
		char[] passphrase = "changeit".toCharArray();
		InputStream inputKeyStore = null;
		OutputStream outputKeyStore = null;
		try {
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(null);
			keyStore.setCertificateEntry(info.getDisplayName(), info.getCertificate());
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			outputKeyStore = new FileOutputStream(file);
			keyStore.store(outputKeyStore, passphrase);
		} catch (Exception e) {
			throw new PhrescoException(e);
		} finally {
			Utility.closeStream(inputKeyStore);
			Utility.closeStream(outputKeyStore);
		}
	}
	
}
