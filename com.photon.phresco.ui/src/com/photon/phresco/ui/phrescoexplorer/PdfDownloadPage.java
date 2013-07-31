package com.photon.phresco.ui.phrescoexplorer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import com.photon.phresco.commons.FrameworkConstants;
import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.PhrescoDialog;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.commons.util.QualityUtil;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.util.Utility;

public class PdfDownloadPage extends AbstractHandler implements PhrescoConstants {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);
		final Shell downloadDialog = new Shell(shell, SWT.APPLICATION_MODAL |  SWT.DIALOG_TRIM | SWT.MIN | SWT.TITLE | SWT.RESIZE);
		GridLayout layout = new GridLayout(1, false);
		downloadDialog.setLocation(downloadDialog.getLocation());
		downloadDialog.setLayout(layout);
		downloadDialog.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite composite = new Composite(downloadDialog, SWT.NONE);
		GridLayout compLayout = new GridLayout(2, false);
		composite.setLayout(compLayout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		try {
			ApplicationInfo appInfo = PhrescoUtil.getApplicationInfo();
			final String fromPage = "All";

			List<Map<String,String>> existingPDFs = getExistingPDFs(fromPage, appInfo);

			System.out.println("available report====> " + existingPDFs);

			Table table = new Table(downloadDialog, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
			table.setLayoutData(new GridData(GridData.FILL_BOTH));
			table.setHeaderVisible(true);

			String[] columnValues = {"Existing Reports", "Type", "Download"};
			for (int i = 0; i < columnValues.length; i++) {
				TableColumn column = new TableColumn(table, SWT.FILL);
				column.setText(columnValues[i]);
			}

			for (Map<String,String> pdfMap : existingPDFs) {
				TableItem item = new TableItem(table, SWT.FILL);
				final String time = pdfMap.get("time");
				String type = pdfMap.get("type");
				String reportFileName = pdfMap.get("fileName");
				item.setText(0, time);
				item.setText(1, type);

				Composite configureButtonPan = new Composite(table, SWT.NONE);
				configureButtonPan.setLayout(new FillLayout());

				final Button downloadButton = new Button(configureButtonPan, SWT.PUSH);
				downloadButton.setText("download");
				downloadButton.setData(time, reportFileName);
				downloadButton.pack();

				downloadButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						String reportFileName = (String) downloadButton.getData(time);
						if(StringUtils.isNotEmpty(reportFileName)) {
							try {
								download(fromPage, reportFileName);
							} catch (PhrescoException e1) {
								PhrescoDialog.exceptionDialog(downloadDialog, e1);
							}
						}
						super.widgetSelected(e);
					}
				});

				TableEditor editor = new TableEditor(table);
				editor.minimumWidth = downloadButton.getSize().x +10;
				editor.horizontalAlignment = SWT.CENTER;
				editor.grabHorizontal = true;
				editor.setEditor(configureButtonPan, item, 2);
			}
			for (int i=0; i<columnValues.length; i++) {
				table.getColumn (i).pack ();
			}  

			int x = table.getSize().x + 250;
			int y = table.getSize().y + 150;

			downloadDialog.setSize(x, y);
			downloadDialog.setLocation(x, y);
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
	
	private void download(String fromPage, String reportFileName) throws PhrescoException {
		String pdfLOC = "";
		FileInputStream fileInputStream = null;
		String applicationHome = PhrescoUtil.getApplicationHome();
		String archivePath = applicationHome + File.separator + DO_NOT_CHECKIN_DIR + File.separator + ARCHIVES
				+ File.separator;
		if ((FrameworkConstants.ALL).equals(fromPage)) {
			pdfLOC = archivePath + CUMULATIVE + File.separator + reportFileName;
		} else {
			pdfLOC = archivePath + fromPage + File.separator + reportFileName;
		}
		File pdfFile = new File(pdfLOC);
		System.out.println("file===> " + pdfFile);
		if (pdfFile.isFile()) {
			try {
				Process p = Runtime
						   .getRuntime()
						   .exec("rundll32 url.dll,FileProtocolHandler "+pdfFile);
						p.waitFor();
			} catch (FileNotFoundException e) {
				throw new PhrescoException(e);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
