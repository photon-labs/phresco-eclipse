package com.photon.phresco.commons.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.exception.PhrescoException;

public class SonarUtil implements PhrescoConstants {


	public static int getSonarServerStatus() throws PhrescoException {
		int responseCode = 0;
		try {
			URL sonarURL = new URL(getSonarHomeURL());
			String protocol = sonarURL.getProtocol();
			HttpURLConnection connection = null;
			if (protocol.equals("http")) {
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


	public static String getSonarHomeURL() {
		String url = "http://localhost:9000";
		return url;
	}

	public static String getSonarReportPath() {
		String reportPath = "/sonar/dashboard/index/";
		return reportPath;
	}

	public static String getSonarURL() throws PhrescoException {
		String serverUrl = getSonarHomeURL();
		String sonarReportPath = getSonarReportPath();
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




}
