package com.photon.phresco.ui.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.photon.phresco.commons.PhrescoConstants;

public class ConfigurationPage extends PreferencePage implements
IWorkbenchPreferencePage, PhrescoConstants {
	
	public ConfigurationPage() {
		super();
	}
	
	public ConfigurationPage(String title) {
		super(title);
	}
	
    public ConfigurationPage(String title, ImageDescriptor image) {
        super(title, image);
    }
    
	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
	}

	@Override
	protected Control createContents(Composite parent) {
        return null;
	}

}
