package com.photon.phresco.ui.phrescoexplorer;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.PhrescoDialog;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.ui.resource.Messages;

public class Update extends AbstractHandler implements PhrescoConstants {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
Shell shell = HandlerUtil.getActiveShell(event);
		
		// To check the user has logged in
		ServiceManager serviceManager = PhrescoUtil.getServiceManager(PhrescoUtil.getUserId());
		if(serviceManager == null) {
			PhrescoDialog.errorDialog(shell,Messages.WARNING, Messages.PHRESCO_LOGIN_WARNING);
			return null;
		}
		
		Shell buildDialog = new Shell(shell, SWT.APPLICATION_MODAL |  SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.TITLE | SWT.RESIZE);
		GridLayout layout = new GridLayout(1, false);
		buildDialog.setLocation(385, 130);
		buildDialog.setLayout(layout);
		buildDialog.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		return null;
	}
	
	public class UpdateSCM extends StatusDialog {

		public UpdateSCM(Shell parent) {
			super(parent);
		}
		
	}

}
