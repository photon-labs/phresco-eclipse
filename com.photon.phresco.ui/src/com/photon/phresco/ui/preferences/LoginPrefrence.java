package com.photon.phresco.ui.preferences;

import java.io.File;
import java.net.URISyntaxException;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.PhrescoDialog;
import com.photon.phresco.commons.SuccessDialog;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.ui.PhrescoPlugin;
import com.photon.phresco.ui.resource.Messages;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class LoginPrefrence
	extends PreferencePage
	implements IWorkbenchPreferencePage, PhrescoConstants {

    private Text userName;
    private Text password;
    private IPreferenceStore prefStore;
    private boolean isLoggedIn;
    private String jarName = "/com.photon.phresco.plugin.jar";
    
	public LoginPrefrence() {
		super();
	}
	
	public LoginPrefrence(String title) {
		super(title);
	}
	
    public LoginPrefrence(String title, ImageDescriptor image) {
        super(title, image);
    }
    
    protected Control createContents(Composite parent) {
    	//this.composite = parent;
        Label label = new Label(parent, SWT.CENTER);
        label.setForeground(new Color(null, new RGB(0, 0, 0)));
        label.setFont(new Font(null, STR_EMPTY, 10, SWT.BOLD));

        GridLayout layout = new GridLayout(2, false);
        layout.verticalSpacing = 6;

        Composite composite = new Composite(parent, 0);
        composite.setLayout(layout);

        Label userNameLabel = new Label(composite, SWT.LEFT);
        userNameLabel.setText(Messages.LOGIN_ID);
        userName = new Text(composite, SWT.BORDER);
        userNameLabel.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
        userName.setLayoutData(new GridData(140,13));

        Label passwordLabel = new Label(composite, SWT.LEFT);
        passwordLabel.setText(Messages.LOGIN_PWD);
        password = new Text(composite, SWT.BORDER);
        password.setEchoChar(CHAR_ASTERISK);
        passwordLabel.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
        password.setLayoutData(new GridData(140,13));

        setDefaultValues();
        return composite;
    }
    
    public void init(IWorkbench workbench) {

    }

    protected void performDefaults() {
        prefStore = getPreferenceStore();
        userName.setText(DEFAULT_USER_NAME);
        password.setText(DEFAULT_PASSWORD);

        PhrescoPlugin.getDefault().savePluginPreferences();
    }

    public boolean performOk() {
    	String jarPath="";
		try {
			File f = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			String jarDir = f.getParentFile().getPath();
			jarPath = jarDir+jarName;
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
    	final String serviceURL = PhrescoUtil.getServiceURL(jarPath);
    	
        boolean result = false;
        prefStore = getPreferenceStore();
        final String loginUserName = userName.getText();
        final String loginPassword = password.getText();
        
        BusyIndicator.showWhile(null, new Runnable() {
            public void run() {
            	try {
					isLoggedIn = PhrescoUtil.doLogin(loginUserName, loginPassword, serviceURL);
				} catch (PhrescoException e) {
					PhrescoDialog.exceptionDialog(getShell(), e);
				}
            }
        });

        if (isLoggedIn) {
            prefStore.setValue(USER_ID, userName.getText());
            prefStore.setValue(PASSWORD, password.getText());
            PhrescoPlugin.getDefault().savePluginPreferences();
            result = true;
            setMessage(Messages.LOGIN_SUCCESSFUL);
            
            PhrescoDialog.messageDialog(getShell(), Messages.LOGIN_SUCCESSFUL);
        } 
        return result;
    }

    private void setDefaultValues() {

    	prefStore = getPreferenceStore();
        String regUserName = prefStore.getString(USER_ID);
        String regPassword = prefStore.getString(PASSWORD);

        if(PhrescoConstants.STR_EMPTY.equals(regUserName)) {
        	userName.setText(DEFAULT_USER_NAME);
        } else {
        	userName.setText(regUserName);
        }

        if (PhrescoConstants.STR_EMPTY.equals(regPassword)) {
        	password.setText(DEFAULT_PASSWORD);
        } else {
        	password.setText(regPassword);
        }
    }
    
    protected IPreferenceStore doGetPreferenceStore() {
        return PhrescoPlugin.getDefault().getPreferenceStore();
    }
	
}