package com.photon.phresco.commons;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.photon.phresco.ui.PhrescoPlugin;
import com.photon.phresco.ui.resource.Messages;


public class PhrescoDialog {

	public static void errorDialog(Shell shell, String title, String message) {
		MessageDialog.openError(shell, title, message);
	}
	
	public static void exceptionDialog(Shell shell, Exception e) {
		Status status = new Status(IStatus.ERROR, PhrescoPlugin.PLUGIN_ID,  e.getLocalizedMessage(), e);
		ErrorDialog.openError(shell, Messages.ERROR, e.getMessage(), status);
	}
}
