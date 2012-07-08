package com.photon.phresco.ui.wizards.pages;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.WizardResourceImportPage;

import com.photon.phresco.ui.wizards.ImportProjectFromScmWizard;

public class PhrescoProjectImportPage extends WizardResourceImportPage implements
		IWizardPage {

	public PhrescoProjectImportPage(String name, IStructuredSelection selection) {
		super(name, selection);
		// TODO Auto-generated constructor stub
	}

	public PhrescoProjectImportPage(
			ImportProjectFromScmWizard importProjectFromScmWizard) {
		super("", null);
	}

	@Override
	protected void createSourceGroup(Composite parent) {
		// TODO Auto-generated method stub

	}

	@Override
	protected ITreeContentProvider getFileProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ITreeContentProvider getFolderProvider() {
		// TODO Auto-generated method stub
		return null;
	}

}
