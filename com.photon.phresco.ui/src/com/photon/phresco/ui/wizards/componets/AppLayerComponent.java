package com.photon.phresco.ui.wizards.componets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.photon.phresco.commons.model.ApplicationType;
import com.photon.phresco.commons.model.TechnologyGroup;
import com.photon.phresco.commons.model.TechnologyInfo;
import com.photon.phresco.commons.util.DesignUtil;
import com.photon.phresco.commons.util.LayerUtil;

public class AppLayerComponent extends Composite {
	
	public Text appCodeText;
	public Combo techNameCombo;
	public Combo techVersionCombo;
	private static  Map<String, List<TechnologyInfo>> techInfoMap = new HashMap<String, List<TechnologyInfo>>();
	private static Map<String, List<String>> techVersionMap = new HashMap<String, List<String>>();
	
	private Composite composite;
	
	private String appTypeId;
	private Map<String, String> techIdMap = new HashMap<String, String>();

	public AppLayerComponent(Composite composite, int style) {
		super(composite, style);
		this.composite = composite;
	}
	
	public Composite getComponent(Button button) {
		
		Label appCodeLabel = new Label(composite, SWT.BOLD);
		appCodeLabel.setText("AppCode");
		appCodeLabel.setFont(DesignUtil.getLabelFont());
		appCodeLabel.pack();

		appCodeText = new Text(composite, SWT.NONE);
		appCodeText.setMessage("Enter AppCode");
		
		String appTypeId = (String) button.getData(button.getText());
		setAppTypeId(appTypeId);
		
		ApplicationType applicationType = LayerUtil.getApplicationType(button.getText());
		List<TechnologyGroup> techGroups = applicationType.getTechGroups();
		List<String> techInfoList = new ArrayList<String>();
		for (TechnologyGroup technologyGroup : techGroups) {
			techInfoMap.put(technologyGroup.getName(), technologyGroup.getTechInfos());
			for (TechnologyInfo techInfo : technologyGroup.getTechInfos()) {
				techInfoList.add(techInfo.getName());
				techIdMap.put(techInfo.getName(), techInfo.getId());
				setTechIdMap(techIdMap);
				techVersionMap.put(techInfo.getName(), techInfo.getTechVersions());
			}
		}

		if(CollectionUtils.isNotEmpty(techInfoList)) {
			Label techNameLabel = new Label(composite, SWT.BOLD);
			techNameLabel.setText("Technology");
			techNameLabel.setFont(DesignUtil.getLabelFont());
			
			String[] techNameArray = (String[]) techInfoList.toArray(new String[techInfoList.size()]);
			techNameCombo = new Combo(composite, SWT.NONE | SWT.READ_ONLY | SWT.RESIZE);
			techNameCombo.setItems(techNameArray);
			techNameCombo.select(0);
		}
		
		List<String> techVersionList = techVersionMap.get(techInfoList.get(0));
		Label techVersionLabel = new Label(composite, SWT.BOLD);
		techVersionLabel.setText("Version");
		techVersionLabel.setFont(DesignUtil.getLabelFont());

		techVersionCombo = new Combo(composite, SWT.NONE | SWT.READ_ONLY | SWT.RESIZE);
		if(CollectionUtils.isNotEmpty(techVersionList)) {
			String[] techVersionArray = (String[]) techVersionList.toArray(new String[techVersionList.size()]);
			techVersionCombo.setItems(techVersionArray);
			techVersionCombo.select(0);
		}
		
		techNameCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				List<String> techVersionList = techVersionMap.get(techNameCombo.getText());
				techVersionCombo.removeAll();
				if(CollectionUtils.isNotEmpty(techVersionList)) {
					for (int i = 0; i < techVersionList.size(); i++) {
						techVersionCombo.add(techVersionList.get(i), i);
					}
					techVersionCombo.select(0);
					techVersionCombo.pack();
				}
			}
		});
		techNameCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				List<String> techVersionList = techVersionMap.get(techNameCombo.getText());
				techVersionCombo.removeAll();
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
		composite.pack();
		
		return composite;
	}
	
	public String getAppTypeId() {
		return appTypeId;
	}

	public void setAppTypeId(String appTypeId) {
		this.appTypeId = appTypeId;
	}
	
	
	public Map<String, String> getTechIdMap() {
		return techIdMap;
	}

	public void setTechIdMap(Map<String, String> techIdMap) {
		this.techIdMap = techIdMap;
	}
}
