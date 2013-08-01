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

package com.photon.phresco.commons.util;

import java.io.BufferedReader;
import java.io.IOException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Class to render information in the console
 * @author syed
 *
 */
public class ConsoleViewManager {
	
	private static ConsoleViewManager fDefault = null;
	private String fTitle = null;
	private MessageConsole fMessageConsole = null;
	
	public static final int MSG_INFORMATION = 1;
	public static final int MSG_ERROR = 2;
	public static final int MSG_WARNING = 3;
		
	public ConsoleViewManager(String messageTitle) {		
		fDefault = this;
		fTitle = messageTitle;
	}
	
	public static ConsoleViewManager getDefault(String msgTitle) {
		if (fDefault == null) {
			new ConsoleViewManager(msgTitle);
		}
		return fDefault;
	}	
		
	public void println(BufferedReader performAction) {		
		
		/* if console-view in Java-perspective is not active, then show it and
		 * then display the message in the console attached to it */		
		if( !displayConsoleView()) {
			/*If an exception occurs while displaying in the console, then just diplay atleast the same in a message-box */
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error", "Opening console view has problem");
			return;
		}
		
		/* display message on console */	
		MessageConsoleStream newMessageConsoleStream = getNewMessageConsoleStream();
//		newMessageConsoleStream.println();
		try {
			String line;
			while ((line = performAction.readLine())!= null) {
				newMessageConsoleStream.println(line);
				newMessageConsoleStream.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * To check the console can be viewable
	 */
	private boolean displayConsoleView() {
		
		try {
			
			IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if( activeWorkbenchWindow != null ) {
				IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
				
				if( activePage != null )
					activePage.showView(IConsoleConstants.ID_CONSOLE_VIEW, null, IWorkbenchPage.VIEW_VISIBLE);
			}
			
		} catch (PartInitException partEx) {			
			return false;
		}
		
		return true;
	}
	
	/*
	 * Get the console message stream to render the message in console
	 */
	private MessageConsoleStream getNewMessageConsoleStream() {		
		MessageConsoleStream msgConsoleStream = getMessageConsole().newMessageStream();		
		return msgConsoleStream;
	}
	
	private MessageConsole getMessageConsole() {
		if( fMessageConsole == null )
			createMessageConsoleStream(fTitle);	
		
		return fMessageConsole;
	}
	
	/*
	 * Creating instance of message console
	 */
	private void createMessageConsoleStream(String title) {
		fMessageConsole = new MessageConsole(title, null); 
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{ fMessageConsole });
	}	
}
