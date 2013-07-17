package com.photon.phresco.commons;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;


public class PhrescoDialog {

	public static void ErrorDialog(Shell shell, String title, String message) {
		MessageDialog.openError(shell, title, message);
	}
}
