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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.photon.phresco.ui.resource.Messages;

/**
 * Class to render information in the console
 * @author syed
 *
 */
public class ConsoleViewManager {
	
	private static ConsoleViewManager fDefault = null;
	
	public static final int MSG_INFORMATION = 1;
	public static final int MSG_ERROR = 2;
	public static final int MSG_WARNING = 3;
	
	String line;
		
	public ConsoleViewManager(String messageTitle) {		
		fDefault = this;
	}
	
	public static ConsoleViewManager getDefault(String msgTitle) {
		if (fDefault == null) {
			new ConsoleViewManager(msgTitle);
		}
		return fDefault;
	}	
		
	public void println(BufferedReader performAction) {		
		showConsolePopup(performAction);
	}
	
	public void showConsolePopup(BufferedReader performAction) {
		
		Display display = Display.getDefault();
		final Shell shell = new Shell(display, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.MAX);

		shell.setText(Messages.CONSOLE_DIALOG_TITLE);
		shell.setLayout(new GridLayout(1, false));
		shell.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		
	    final Text text = new Text(shell, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
	    text.setEditable(false);
	    text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	    text.redraw();
	    text.pack(true);
	    text.setSize(700, 540);

	    Composite buttonComposite = new Composite(shell, SWT.RIGHT | SWT.END);
	    buttonComposite.setSize(700, 50);
		GridLayout buttonLayout = new GridLayout(1, false);
		buttonComposite.setLayout(buttonLayout);
		buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.END, false, false, 1, 1)); 
		
		Button ok = new Button(buttonComposite, SWT.PUSH | SWT.RIGHT);
		ok.setText(Messages.CLOSE);
		
		Listener closeListener = new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				shell.close();
			}
		};
		
		ok.addListener(SWT.Selection, closeListener);
		
		buttonComposite.setSize(700, 50);
		buttonComposite.pack();
		
		shell.setSize(720, 550);
		shell.open();
	    
		try {
			while ((line = performAction.readLine())!= null) {
				text.append(line + "\n");
			} 
		} catch (Exception e) {
		}	    
	}
}
