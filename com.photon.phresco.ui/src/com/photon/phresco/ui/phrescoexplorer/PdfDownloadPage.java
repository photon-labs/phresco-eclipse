package com.photon.phresco.ui.phrescoexplorer;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.handlers.HandlerUtil;

import com.photon.phresco.commons.ConfirmDialog;
import com.photon.phresco.commons.FrameworkConstants;
import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.PhrescoDialog;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.commons.util.QualityUtil;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.ui.model.BaseAction;
import com.photon.phresco.ui.resource.Messages;
import com.photon.phresco.util.Utility;

public class PdfDownloadPage extends AbstractHandler implements PhrescoConstants {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);
		
		BaseAction baseAction = new BaseAction();
		ServiceManager serviceManager = PhrescoUtil.getServiceManager(baseAction.getUserId());
		if(serviceManager == null) {
			ConfirmDialog.getConfirmDialog().showConfirm(shell);
			return null;
		}
		
		final Shell downloadDialog = new Shell(shell, SWT.APPLICATION_MODAL |  SWT.DIALOG_TRIM | SWT.MIN | SWT.TITLE);
		downloadDialog.setText(Messages.PDF_REPORT_DOWNLOAD_DIALOG_TITLE);
		GridLayout layout = new GridLayout(1, false);
		downloadDialog.setLocation(downloadDialog.getLocation());
		downloadDialog.setLayout(layout);
		downloadDialog.setSize(450, 300);
		downloadDialog.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Composite composite = new Composite(downloadDialog, SWT.NONE);
		GridLayout compLayout = new GridLayout(2, false);
		composite.setLayout(compLayout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		try {
			ApplicationInfo appInfo = PhrescoUtil.getApplicationInfo();
			final String fromPage = "All";

			List<Map<String,String>> existingPDFs = getExistingPDFs(fromPage, appInfo);

			if(CollectionUtils.isEmpty(existingPDFs)) {
				PhrescoDialog.errorDialog(downloadDialog, Messages.ERROR, "Pdf report Not Available");
				return null;
			}
			final Table table = new Table(downloadDialog, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
			table.setLayoutData(new GridData(GridData.FILL_BOTH));
			table.setHeaderVisible(true);
			table.setLinesVisible(true);
			
			String[] columnValues = {"Existing Reports", "Type", "Download"};
			for (int i = 0; i < columnValues.length; i++) {
				TableColumn column = new TableColumn(table, SWT.FILL);
				column.setText(columnValues[i]);
				column.setWidth(120);
			}

			for (Map<String,String> pdfMap : existingPDFs) {
				TableItem item = new TableItem(table, SWT.FILL);
				final String time = pdfMap.get("time");
				String type = pdfMap.get("type");
				String reportFileName = pdfMap.get("fileName");
				item.setText(0, time);
				item.setText(1, type);

				int item_height = 18;
				Image fake = new Image(table.getDisplay(), 1, item_height);
				item.setImage(0, fake); 
				
				Composite configureButtonPan = new Composite(table, SWT.NONE);
				configureButtonPan.setLayout(new FillLayout());
				final Button downloadButton = new Button(configureButtonPan, SWT.PUSH);
				downloadButton.setText("download");
				downloadButton.setData(time, reportFileName);
				
				downloadButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						String reportFileName = (String) downloadButton.getData(time);
						if(StringUtils.isNotEmpty(reportFileName)) {
							try {
								download(fromPage, reportFileName, downloadDialog);
							} catch (PhrescoException e1) {
								PhrescoDialog.exceptionDialog(downloadDialog, e1);
							}
						}
						super.widgetSelected(e);
					}
				});

				TableEditor editor = new TableEditor(table);
				editor.horizontalAlignment = SWT.CENTER;
				editor.grabHorizontal = true;
				editor.setEditor(configureButtonPan, item, 2);
			}
			Button closeButton = new Button(downloadDialog, SWT.PUSH);
			closeButton.setText(Messages.CLOSE);
			closeButton.setLayoutData(new GridData(SWT.RIGHT, SWT.END, false, false));
			closeButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					downloadDialog.close();
					super.widgetSelected(e);
				}
			});
			downloadDialog.open();
		} catch (PhrescoException e) {
			PhrescoDialog.exceptionDialog(downloadDialog, e);
		}
		return downloadDialog;
	}
	
	private List<Map<String, String>> getExistingPDFs(String fromPage, ApplicationInfo appInfo) throws PhrescoException {
		List<Map<String, String>> pdfList = null;
		// popup showing list of pdf's already created
		String pdfDirLoc = "";
		if (StringUtils.isEmpty(fromPage) || FROMPAGE_ALL.equals(fromPage)) {
			pdfDirLoc = Utility.getProjectHome() + appInfo.getAppDirName() + File.separator + DO_NOT_CHECKIN_DIR
					+ File.separator + ARCHIVES + File.separator + CUMULATIVE;
		} else {
			pdfDirLoc = Utility.getProjectHome() + appInfo.getAppDirName() + File.separator + DO_NOT_CHECKIN_DIR
					+ File.separator + ARCHIVES + File.separator + fromPage;
		}
		File pdfFileDir = new File(pdfDirLoc);
		if (pdfFileDir.isDirectory()) {
			File[] children = pdfFileDir.listFiles(new FileNameFileFilter(DOT + PDF));
			QualityUtil util = new QualityUtil();
			if (children != null) {
				util.sortResultFile(children);
			}
			pdfList = new ArrayList<Map<String,String>>();
			for (File child : children) {
				Map<String, String> pdfDetails = new HashMap<String, String>();
				// three value
				DateFormat yymmdd = new SimpleDateFormat("MMM dd yyyy HH.mm");
				if (child.toString().contains("detail")) {
					pdfDetails.put("time", yymmdd.format(child.lastModified()));
					pdfDetails.put("type", "detail");
					pdfDetails.put("fileName", child.getName());
				} else if (child.toString().contains("crisp")) {
					pdfDetails.put("time", yymmdd.format(child.lastModified()));
					pdfDetails.put("type", "crisp");
					pdfDetails.put("fileName", child.getName());
				}
				pdfList.add(pdfDetails);
			}
		}
		return pdfList;
	}
	
	private void download(String fromPage, String reportFileName, Shell shell) throws PhrescoException {
		String pdfLOC = "";
		String applicationHome = PhrescoUtil.getApplicationHome();
		String archivePath = applicationHome + File.separator + DO_NOT_CHECKIN_DIR + File.separator + ARCHIVES
				+ File.separator;
		if ((FrameworkConstants.ALL).equals(fromPage)) {
			pdfLOC = archivePath + CUMULATIVE + File.separator + reportFileName;
		} else {
			pdfLOC = archivePath + fromPage + File.separator + reportFileName;
		}
		File pdfFile = new File(pdfLOC);
		if (pdfFile.isFile()) {
			FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
			fileDialog.setText("Save");
			fileDialog.setFileName(pdfFile.getName());
			fileDialog.setFilterPath("C:/");
			String[] filterExt = { "*.pdf"};
			fileDialog.setFilterExtensions(filterExt);
			String selected = fileDialog.open();
			try {
				FileUtils.copyFile(pdfFile, new File(selected), true);
			} catch (IOException e) {
				throw new PhrescoException(e);
			}
		}
	}
	
	/**
	 * The Class FileNameFileFilter.
	 */
	public class FileNameFileFilter implements FilenameFilter {
		
		/** The filter_. */
		private String filter_;

		/**
		 * Instantiates a new file name file filter.
		 *
		 * @param filter the filter
		 */
		public FileNameFileFilter(String filter) {
			filter_ = filter;
		}

		public boolean accept(File dir, String name) {
			return name.endsWith(filter_);
		}
	}

}
