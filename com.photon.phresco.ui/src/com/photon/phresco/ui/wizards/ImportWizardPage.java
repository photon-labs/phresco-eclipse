/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.photon.phresco.ui.wizards;

import java.util.List;

import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.m2e.core.ui.internal.wizards.MavenImportWizardPage;
import org.eclipse.ui.IWorkingSet;

import com.photon.phresco.ui.resource.Messages;


/**
 * @author suresh_ma
 *
 */
public class ImportWizardPage extends MavenImportWizardPage {
	
	@SuppressWarnings("restriction")
	public ImportWizardPage(ProjectImportConfiguration importConfiguration,
			List<IWorkingSet> workingSets) {
		super(importConfiguration, workingSets);
		setTitle(Messages.PHRESCO_IMPORT_PAGE_TITLE);
		setDescription(Messages.PHRESCO_IMPORT_PAGE_DESC);
		// TODO Auto-generated constructor stub
	}
}
