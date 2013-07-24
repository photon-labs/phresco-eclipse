package com.photon.phresco.ui.phrescoexplorer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.handlers.HandlerUtil;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.model.BuildInfo;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.exception.PhrescoException;

public class View extends AbstractHandler implements PhrescoConstants {

	private Button buildButton;
	private Button cancelButton;
	
	private Shell viewDialog = null; 
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);
		final Shell viewsDialog = new Shell(shell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		try {
			viewDialog = createViewDialog(viewsDialog);
		} catch (PhrescoException e) {
			e.printStackTrace();
		}
		
		viewDialog.open();
		
		Listener generateBuildListener = new Listener() {
			@Override
			public void handleEvent(Event events) {
				viewDialog.setVisible(false);
				Build  build = new Build();
				try {
					build.execute(event);
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		};
		
		Listener cancelButtonListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				viewDialog.setVisible(false);
			}
		};
		
		
		buildButton.addListener(SWT.Selection, generateBuildListener);
		cancelButton.addListener(SWT.Selection, cancelButtonListener);

		return null;
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	public Shell createViewDialog(Shell dialog) throws PhrescoException {
		viewDialog = new Shell(dialog, SWT.DIALOG_TRIM);
		GridLayout layout = new GridLayout(2, false);
		layout.verticalSpacing = 6;

		viewDialog.setText("View");
		viewDialog.setLocation(385, 130);
		viewDialog.setSize(477, 300);
		viewDialog.setLayout(layout);

		final Table table = new Table(viewDialog,  SWT.BORDER | SWT.MULTI);
		GridData data = new GridData(GridData.VERTICAL_ALIGN_FILL);
		data.widthHint = 436;
		GridLayout tableLayout = new GridLayout(4, true);
		data.horizontalSpan = 20;
		data.verticalSpan = 14;

		table.setLayoutData(data);
		table.setLayout(tableLayout);
		table.setSize(200,175);
		Color color = new Color(null, new RGB(235, 233, 216));
		table.setBackground(color);

		TableColumn no = new TableColumn(table, SWT.CENTER);
		TableColumn name = new TableColumn(table, SWT.CENTER);
		TableColumn download = new TableColumn(table, SWT.CENTER);
		TableColumn delete = new TableColumn(table, SWT.CENTER);

		no.setText(BUILD_NO);
		name.setText(BUILD_NAME);
		download.setText(DOWNLOAD);
		delete.setText(DEPLOY);

		no.setWidth(40);
		name.setWidth(183);
		download.setWidth(110);
		delete.setWidth(120);

		table.setHeaderVisible(true);

		List<BuildInfo> buildInfos = getBuildInfos(PhrescoUtil.getBuildInfoPath());
		if (CollectionUtils.isNotEmpty(buildInfos)) {
			for (BuildInfo buildInfo : buildInfos) {
				TableItem items = new TableItem(table, SWT.FILL);
				int buildNo = buildInfo.getBuildNo();
				String buildName = buildInfo.getBuildName();
				items.setText(new String[]{String.valueOf(buildNo), buildName});
			}
			
			buildButton = new Button(viewDialog, SWT.PUSH);
			buildButton.setText("Build");
			GridData buildsButton = new GridData(SWT.LEFT, SWT.BOTTOM, true, true, 0, 0);
			buildsButton.widthHint = 89;
			buildButton.setLayoutData(buildsButton);
			buildButton.setSize(70, 10);
			
			
			GridData gd_cancelButton = new GridData(SWT.RIGHT, SWT.BOTTOM, true, true, 0, 0);
			gd_cancelButton.widthHint = 82;
			cancelButton = new Button(viewDialog, SWT.PUSH);
			cancelButton.setText(CANCEL);
			gd_cancelButton.heightHint = 23;
			cancelButton.setLayoutData(gd_cancelButton);
			
		} else {
			table.setVisible(false);
			
			Composite composite = new Composite(viewDialog, SWT.None);
			GridLayout layouts = new GridLayout(1, false);
			layouts.verticalSpacing = 130;
			composite.setLayout(layouts);
			
			Label nobuildLabel = new Label(composite, SWT.NONE);
			nobuildLabel.setText("No Builds Available");
			nobuildLabel.setFont(new Font(null, STR_EMPTY, 9, SWT.BOLD));
			nobuildLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP,true, true, 0, 0));
			
			buildButton = new Button(composite, SWT.PUSH);
			buildButton.setText("Build");
			GridData buildsButton = new GridData(SWT.LEFT, SWT.BOTTOM, true, true, 0, 0);
			buildsButton.widthHint = 74;
			buildButton.setLayoutData(buildsButton);
			buildButton.setSize(56, 10);
			
			GridData gd_cancelButton = new GridData(SWT.RIGHT, SWT.BOTTOM, true, true, 0, 0);
			gd_cancelButton.widthHint = 82;
			cancelButton = new Button(viewDialog, SWT.PUSH);
			cancelButton.setText(CANCEL);
			gd_cancelButton.heightHint = 23;
			cancelButton.setLayoutData(gd_cancelButton);
		}
		return viewDialog;
	}

	public List<BuildInfo> getBuildInfos(File buildInfoFile) throws PhrescoException {
		return readBuildInfo(buildInfoFile);
	}

	private List<BuildInfo> readBuildInfo(File path) {
		try {
			if (!path.exists()) {
				return new ArrayList<BuildInfo>(1);
			}

			BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
			Gson gson = new Gson();
			Type type = new TypeToken<List<BuildInfo>>(){}.getType();
			List<BuildInfo> buildInfos = gson.fromJson(bufferedReader, type);
			bufferedReader.close();

			return buildInfos;
		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
