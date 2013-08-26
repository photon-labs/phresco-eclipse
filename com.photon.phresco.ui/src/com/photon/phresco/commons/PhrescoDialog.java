package com.photon.phresco.commons;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
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
		errorDialogWithStackTrace(e.getMessage(), e);
	}
	
	public static void errorDialogWithStackTrace(String msg, Throwable t) {

	    StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);
	    t.printStackTrace(pw);

	    final String trace = sw.toString(); // stack trace as a string

	    // Temp holder of child statuses
	    List<Status> childStatuses = new ArrayList<Status>();

	    // Split output by OS-independend new-line
	    for (String line : trace.split(System.getProperty("line.separator"))) {
	        // build & add status
	        childStatuses.add(new Status(IStatus.ERROR, PhrescoPlugin.PLUGIN_ID, line));
	    }

	    MultiStatus ms = new MultiStatus(PhrescoPlugin.PLUGIN_ID, IStatus.ERROR,
	            childStatuses.toArray(new Status[] {}), // convert to array of statuses
	            t.getLocalizedMessage(), t);

	    ErrorDialog.openError(null, Messages.ERROR, msg, ms);
	}
	
	public static void messageDialog(Shell shell, String message) {
		MessageDialog.openInformation(shell, Messages.INFORMATION, message);
	}
}
