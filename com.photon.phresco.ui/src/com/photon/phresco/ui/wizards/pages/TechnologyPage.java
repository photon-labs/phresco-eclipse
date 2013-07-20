package com.photon.phresco.ui.wizards.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.photon.phresco.commons.PhrescoDialog;
import com.photon.phresco.commons.model.ApplicationType;
import com.photon.phresco.commons.model.TechnologyGroup;
import com.photon.phresco.commons.model.TechnologyInfo;
import com.photon.phresco.commons.util.BaseAction;
import com.photon.phresco.commons.util.DesignUtil;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.service.client.api.ServiceManager;

public class TechnologyPage extends WizardPage implements IWizardPage {

	private Group layerGroup;

	private static Map<String, List<TechnologyInfo>> techMap = new HashMap<String, List<TechnologyInfo>>();
	private static Map<String, List<String>> techVersionMap = new HashMap<String, List<String>>();
	private static Map<String, String> appTypeIdMap = new HashMap<String, String>();
	private static Map<String, Group> layerMap = new HashMap<String, Group>();

	public Label techNameLabel;
	public Combo techNameCombo;
	public Label techVersionLabel;
	public Combo techVersionCombo;

	public TechnologyPage(String pageName) {
		super(pageName);
		setTitle("{Phresco}");
		setDescription("Technology Selection Page");
	}

	@Override
	public void createControl(Composite parent) {
		Composite parentComposite = new Composite(parent, SWT.NULL);
		parentComposite.setLayout(new GridLayout(1,true));
		parentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		setControl(parentComposite);
	}

	public void renderLayer(List<Button> selectedLayers) {
		Composite parentComposite = (Composite) getControl();
		BaseAction action = new BaseAction();
		String userId = action.getUserId();
		final String customerId = action.getCustomerId();
		final ServiceManager serviceManager = PhrescoUtil.getServiceManager(userId);
		if(serviceManager == null) {
			PhrescoDialog.errorDialog(getShell(), "Error", "Please Login before making Request");
			return;
		}

		for (final Button button : selectedLayers) {
			final String appTypeId = (String) button.getData(button.getText());

			if("app-layer".equals(appTypeId)) {
				Group appLayerGroup = new Group(parentComposite, SWT.SHADOW_ETCHED_IN);
				appLayerGroup.setText(button.getText());
				appLayerGroup.setLayout(new GridLayout(6, true));
				appLayerGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				appLayerGroup.setFont(DesignUtil.getHeaderFont());
				layerGroup = appLayerGroup;
				layerMap.put(appTypeId, appLayerGroup);
				appTypeIdMap.put(button.getText(), appTypeId);
			}
			if("web-layer".equals(appTypeId)) {
				Group webLayerGroup = new Group(parentComposite, SWT.SHADOW_ETCHED_IN);
				webLayerGroup.setText(button.getText());
				webLayerGroup.setLayout(new GridLayout(2, true));
				webLayerGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				webLayerGroup.setFont(DesignUtil.getHeaderFont());
				layerGroup = webLayerGroup;
				layerMap.put(appTypeId, webLayerGroup);
				appTypeIdMap.put(button.getText(), appTypeId);
			}
			if("mob-layer".equals(appTypeId)) {
				Group mobileLayerGroup = new Group(parentComposite, SWT.SHADOW_ETCHED_IN);
				mobileLayerGroup.setText(button.getText());
				mobileLayerGroup.setLayout(new GridLayout(2, true));
				mobileLayerGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				mobileLayerGroup.setFont(DesignUtil.getHeaderFont());
				layerGroup = mobileLayerGroup;
				layerMap.put(appTypeId, mobileLayerGroup);
				appTypeIdMap.put(button.getText(), appTypeId);
			}

			Label appCodeLabel = new Label(layerGroup, SWT.BOLD);
			appCodeLabel.setText("AppCode");
			appCodeLabel.setFont(DesignUtil.getLabelFont());
			appCodeLabel.pack();

			Text appCodeTxt = new Text(layerGroup, SWT.NONE);
			appCodeTxt.setMessage("Enter AppCode");

			try {
				ApplicationType applicationType = null;
				List<ApplicationType> applicationTypes = serviceManager.getApplicationTypes(customerId);
				for (ApplicationType appType : applicationTypes) {
					if(appType.getId().equals(appTypeId)) {
						applicationType = appType;
					}
				}
				List<TechnologyGroup> techGroups = applicationType.getTechGroups();
				List<String> technologyGroupNameList = new ArrayList<String>();
				for (TechnologyGroup technologyGroup : techGroups) {
					technologyGroupNameList.add(technologyGroup.getName());
					List<TechnologyInfo> techInfos = technologyGroup.getTechInfos();
					techMap.put(technologyGroup.getName(), techInfos);
					for (TechnologyInfo technologyInfo : techInfos) {
						System.out.println("name :: " + technologyInfo.getName() + " :::: Versions ::" + technologyInfo.getTechVersions());
						techVersionMap.put(technologyInfo.getName(), technologyInfo.getTechVersions());
					}
				}
				if(CollectionUtils.isNotEmpty(technologyGroupNameList)) {
					String[] techGroupNameArray = technologyGroupNameList.toArray(new String[technologyGroupNameList.size()]);
					Label techGroupNameLabel = new Label(layerGroup, SWT.BOLD);
					techGroupNameLabel.setText("type");
					techGroupNameLabel.setFont(DesignUtil.getLabelFont());

					final Combo techGroupNameCombo = new Combo(layerGroup, SWT.NONE | SWT.READ_ONLY | SWT.RESIZE);
					techGroupNameCombo.setItems(techGroupNameArray);
					techGroupNameCombo.select(0);
					techGroupNameCombo.setData(button.getText(), appTypeId);

					List<TechnologyInfo> techInfolist = techMap.get(techGroupNameCombo.getItem(0));
					List<String> technologyNameList = new ArrayList<String>();
					for (TechnologyInfo technologyInfo : techInfolist) {
						technologyNameList.add(technologyInfo.getName());
					}
					if (CollectionUtils.isNotEmpty(technologyNameList)) {
						String[] techNameArray = technologyNameList.toArray(new String[technologyNameList.size()]);
						techNameLabel = new Label(layerGroup, SWT.BOLD);
						techNameLabel.setText("Technology");
						techNameLabel.setFont(DesignUtil.getLabelFont());
						techNameLabel.pack();

						techNameCombo = new Combo(layerGroup, SWT.NONE | SWT.READ_ONLY | SWT.RESIZE);
						techNameCombo.setItems(techNameArray);
						techNameCombo.select(0);
					}
					List<String> techVersionList = techVersionMap.get(techNameCombo.getItem(0));
					techVersionLabel = new Label(layerGroup, SWT.BOLD);
					techVersionLabel.setText("Version");
					techVersionLabel.setFont(DesignUtil.getLabelFont());

					techVersionCombo = new Combo(layerGroup, SWT.NONE | SWT.READ_ONLY | SWT.RESIZE);
					if(CollectionUtils.isNotEmpty(techVersionList)) {
						for (int i = 0; i < techVersionList.size(); i++) {
							techVersionCombo.add(techVersionList.get(i), i);
						}
						techVersionCombo.select(0);
						techVersionCombo.pack();
					}

					techGroupNameCombo.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							layerGroup = layerMap.get(appTypeId);
							List<TechnologyInfo> techInfolist = techMap.get(techGroupNameCombo.getText());
							List<String> technologyNameList = new ArrayList<String>();
							for (TechnologyInfo technologyInfo : techInfolist) {
								technologyNameList.add(technologyInfo.getName());
							}
							String[] techNameArray = technologyNameList.toArray(new String[technologyNameList.size()]);
							if (CollectionUtils.isNotEmpty(technologyNameList)) {
								/*techNameLabel = new Label(layerGroup, SWT.BOLD);
								techNameLabel.setText("Technology");
								techNameLabel.setFont(DesignUtil.getLabelFont());
								techNameLabel.pack();*/
//								techNameCombo = new Combo(layerGroup, SWT.NONE | SWT.READ_ONLY | SWT.RESIZE);
								
								techNameCombo.setItems(techNameArray);
								techNameCombo.select(0);
							}
							super.widgetSelected(e);
						}
					});
					techNameCombo.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							List<String> techVersionList = techVersionMap.get(techNameCombo.getText());
							techVersionLabel = new Label(layerGroup, SWT.BOLD);
							techVersionLabel.setText("Version");
							techVersionLabel.setFont(DesignUtil.getLabelFont());

							techVersionCombo = new Combo(layerGroup, SWT.NONE | SWT.READ_ONLY | SWT.RESIZE);
							if(CollectionUtils.isNotEmpty(techVersionList)) {
								for (int i = 0; i < techVersionList.size(); i++) {
									techVersionCombo.add(techVersionList.get(i), i);
								}
								techVersionCombo.select(0);
								techVersionCombo.pack();
							}
							super.widgetSelected(e);
						}
					});
				}
				layerGroup.pack();
			} catch (PhrescoException e) {
				e.printStackTrace();
			}
		}
		parentComposite.pack();
		setControl(parentComposite);
	}
}
