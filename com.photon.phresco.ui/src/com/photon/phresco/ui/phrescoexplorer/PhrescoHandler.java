package com.photon.phresco.ui.phrescoexplorer;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

public class PhrescoHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		System.out.println("*************************");
		Shell shell = HandlerUtil.getActiveShell(event);
		MessageDialog.openInformation(shell, "Info", "Please select a Java source file");
		return null;
	}

}
