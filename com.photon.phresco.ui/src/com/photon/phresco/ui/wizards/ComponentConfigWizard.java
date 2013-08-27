package com.photon.phresco.ui.wizards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.photon.phresco.api.ApplicationProcessor;
import com.photon.phresco.commons.PhrescoDialog;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.configuration.Configuration;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.ui.phrescoexplorer.wizard.AbstractFeatureWizardPage;

public class ComponentConfigWizard extends Wizard implements IWizard {

	public Map<String, Text> configMap = new HashMap<String, Text>();
	List<String> keyList = new ArrayList<String>();
	
	@Override
	public void addPages() {
		ComponenentConfigWizardPage page = new ComponenentConfigWizardPage("ComponenentConfigWizardPage");
		addPage(page);
		super.addPages();
	}
	
	@Override
	public boolean performFinish() {
		List<Configuration> configurations = new ArrayList<Configuration>();
		Configuration configuration = new Configuration();
		Properties properties = new Properties();
		if (CollectionUtils.isNotEmpty(keyList)) {
			for (String key : keyList) {
				Text text = configMap.get(key);
				String value = text.getText();
				if(StringUtils.isEmpty(value)) {
					value = "";
				}
				properties.setProperty(key, value);
			}
		}
		configuration.setProperties(properties);
		configurations.add(configuration);
		ApplicationProcessor applicationProcessor = AbstractFeatureWizardPage.getApplicationProcessor();
		try {
			ApplicationInfo applicationInfo = PhrescoUtil.getApplicationInfo();
			String featureName = AbstractFeatureWizardPage.getFeatureName();
			applicationProcessor.postFeatureConfiguration(applicationInfo, configurations, featureName);
		} catch (PhrescoException e) {
			PhrescoDialog.exceptionDialog(getShell(), e);
		}
		return true;
	}
	
	class ComponenentConfigWizardPage extends WizardPage implements IWizardPage {

		public Label label;
		public Text text;
		
		public ComponenentConfigWizardPage(String pageName) {
			super(pageName);
			setTitle("Component specific configuration");
		}

		@Override
		public void createControl(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout(2, false));
			composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			List<Configuration> configurations = AbstractFeatureWizardPage.getConfigurations();
			Configuration configuration = configurations.get(0);
			Properties properties = configuration.getProperties();
			Set<Object> keySets = properties.keySet();
			for (final Object keySet : keySets) {
				String value = properties.getProperty(keySet.toString());
				label = new Label(composite, SWT.NONE);
				label.setText(keySet.toString());
				keyList.add(label.getText());
				
				text = new Text(composite, SWT.BORDER);
				text.setText(value);
				
				configMap.put(label.getText(), text);
			}
			setControl(composite);
		}
		
	}

}
