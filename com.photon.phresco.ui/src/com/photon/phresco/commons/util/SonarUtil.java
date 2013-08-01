package com.photon.phresco.commons.util;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.xml.sax.SAXException;

import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.exception.ConfigurationException;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.framework.param.impl.IosTargetParameterImpl;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value;
import com.photon.phresco.plugins.util.MojoProcessor;
import com.photon.phresco.ui.model.CodeValidationReportType;
import com.photon.phresco.util.Constants;
import com.phresco.pom.exception.PhrescoPomException;
import com.phresco.pom.util.PomProcessor;

public class SonarUtil implements PhrescoConstants {


	public static int getSonarServerStatus() throws PhrescoException {
		int responseCode = 0;
		try {
			URL sonarURL = new URL(getSonarHomeURL());
			String protocol = sonarURL.getProtocol();
			HttpURLConnection connection = null;
			if (protocol.equals(HTTP_PROTOCOL)) {
				connection = (HttpURLConnection) sonarURL.openConnection();
				responseCode = connection.getResponseCode();
			} else {
				responseCode = getHttpsResponse(getSonarURL());
			}
			return responseCode;
		} catch (Exception e) {
			return responseCode;
		}
	}

	
	public static int getSonarServerStatus(String url) throws PhrescoException {
		int responseCode = 0;
		try {
			URL sonarURL = new URL(url);
			String protocol = sonarURL.getProtocol();
			HttpURLConnection connection = null;
			if (protocol.equals(HTTP_PROTOCOL)) {
				connection = (HttpURLConnection) sonarURL.openConnection();
				responseCode = connection.getResponseCode();
			} else {
				responseCode = getHttpsResponse(url);
			}
			return responseCode;
		} catch (Exception e) {
			return responseCode;
		}
	}
	

	public static String getSonarHomeURL() {
		String url = SONAR_HOST_URL;
		return url;
	}

	public static String getSonarReportPath() {
		String reportPath = "/sonar/dashboard/index/";
		return reportPath;
	}

	public static String getSonarURL() throws PhrescoException {
		String serverUrl = getSonarHomeURL();
		String sonarReportPath = SONAR_REPORT_URL;
		String[] sonar = sonarReportPath.split("/");
		serverUrl = serverUrl.concat(FORWARD_SLASH + sonar[1]);
		return serverUrl;
	}


	private static int getHttpsResponse(String  url) throws PhrescoException {
		URL httpsUrl;
		try {
			SSLContext ssl_ctx = SSLContext.getInstance("TLS");
			TrustManager[] trust_mgr = get_trust_mgr();
			ssl_ctx.init(null, trust_mgr, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(ssl_ctx.getSocketFactory());
			httpsUrl = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) httpsUrl.openConnection();
			con.setHostnameVerifier(new HostnameVerifier() {
				// Guard against "bad hostname" errors during handshake.	
				public boolean verify(String host, SSLSession sess) {
					return true;
				}
			});
			return con.getResponseCode();
		} catch (MalformedURLException e) {
			throw new PhrescoException(e);
		} catch (IOException e) {
			throw new PhrescoException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new PhrescoException(e);
		} catch (KeyManagementException e) {
			throw new PhrescoException(e);
		}
	}

	private static TrustManager[ ] get_trust_mgr() {
		TrustManager[ ] certs = new TrustManager[ ] {
				new X509TrustManager() {
					@Override
					public void checkClientTrusted(
							java.security.cert.X509Certificate[] chain, String authType)
									throws CertificateException {

					}
					@Override
					public void checkServerTrusted(
							java.security.cert.X509Certificate[] chain, String authType)
									throws CertificateException {
						// TODO Auto-generated method stub

					}
					@Override
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						// TODO Auto-generated method stub
						return null;
					}
				}
		};
		return certs;
	}

	private static void createBrowser(Shell shell) 
	{
		Browser browser = new Browser(shell, SWT.BORDER);

		GridData data = new GridData();
		data.horizontalSpan = 3;

		Button button = new Button(shell, SWT.PUSH);
		button.setText("Test");

		Label labelAddress = new Label(shell, SWT.NONE);
		labelAddress.setText("Report Type");

		final Combo combo = new Combo(shell, SWT.BORDER | SWT.READ_ONLY);

		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.horizontalSpan = 1;
		data.grabExcessHorizontalSpace = true;
		combo.setLayoutData(data);

		try {
			browser = new Browser(shell, SWT.NONE);
		} catch (SWTError e) {
			System.out.println("Could not instantiate Browser: " + e.getMessage());
			return;
		}
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.horizontalSpan = 3;
		browser.setLayoutData(data);


		combo.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				Combo item = (Combo)e.widget;
				String string = item.getText();

			}
		});     
	}


	public static  List<CodeValidationReportType> getCodeValidationReportTypes() {
		List<CodeValidationReportType> codeValidationReportTypes = new ArrayList<CodeValidationReportType>();
		try {
			File infoFileDir = PhrescoUtil.getValidateCodeInfoConfigurationPath();

			ApplicationInfo appInfo = PhrescoUtil.getApplicationInfo();

			// To get parameter values for Iphone technology
			PomProcessor pomProcessor = PhrescoUtil.getPomProcessor(PhrescoUtil.getApplicationInfo().getAppDirName());
			String validateReportUrl = pomProcessor.getProperty(Constants.POM_PROP_KEY_VALIDATE_REPORT);
			if (StringUtils.isNotEmpty(validateReportUrl)) {
				CodeValidationReportType codeValidationReportType = new CodeValidationReportType();
				List<Value> clangReports = getClangReports(appInfo);
				for (Value value : clangReports) {
					codeValidationReportType.setValidateAgainst(value);
				}
				codeValidationReportTypes.add(codeValidationReportType);
			}
			MojoProcessor processor = new MojoProcessor(new File(infoFileDir.getPath()));
			boolean status = checkFunctionalDir();
			Parameter parameter = processor.getParameter(Constants.PHASE_VALIDATE_CODE, "sonar");
			PossibleValues possibleValues = parameter.getPossibleValues();
			List<Value> values = possibleValues.getValue();
			for (Value value : values) {
				CodeValidationReportType codeValidationReportType = new CodeValidationReportType();
				String key = value.getKey();
				Parameter depParameter = processor.getParameter(Constants.PHASE_VALIDATE_CODE, key);
				if (depParameter != null && depParameter.getPossibleValues() != null) {
					PossibleValues depPossibleValues = depParameter.getPossibleValues();
					List<Value> depValues = depPossibleValues.getValue();
					codeValidationReportType.setOptions(depValues);
				}
				codeValidationReportType.setValidateAgainst(value);
				codeValidationReportTypes.add(codeValidationReportType);
			}
		} catch (PhrescoException e) {
			e.printStackTrace();
		} catch (PhrescoPomException e) {
			e.printStackTrace();
		}
		return codeValidationReportTypes;
	}

	public static boolean checkFunctionalDir() {
		boolean status = false;
		try {
			String appDirName = PhrescoUtil.getApplicationInfo().getAppDirName();
			PomProcessor processor = PhrescoUtil.getPomProcessor(appDirName);
			String functionalDir = processor.getProperty("phresco.functionalTest.dir");
			if (StringUtils.isNotEmpty(functionalDir)) {
				status = true;
			}
		} catch (PhrescoException e) {
			e.printStackTrace();
		} catch (PhrescoPomException e) {
			e.printStackTrace();
		}
		return status;
	}


	private static List<Value> getClangReports(ApplicationInfo appInfo) throws PhrescoException {
		try {
			IosTargetParameterImpl targetImpl = new IosTargetParameterImpl();
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("applicationInfo", appInfo);
			PossibleValues possibleValues = targetImpl.getValues(paramMap);
			List<Value> values = possibleValues.getValue();
			return values;
		} catch (IOException e) {
			throw new PhrescoException(e);
		} catch (ParserConfigurationException e) {
			throw new PhrescoException(e);
		} catch (SAXException e) {
			throw new PhrescoException(e);
		} catch (ConfigurationException e) {
			throw new PhrescoException(e);
		} catch (PhrescoException e) {
			throw new PhrescoException(e);
		}
	}




}
