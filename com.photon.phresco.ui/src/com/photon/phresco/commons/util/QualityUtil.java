package com.photon.phresco.commons.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.framework.param.impl.PerformanceTestResultNamesImpl.XmlNameFileFilter;
import com.photon.phresco.util.Constants;

public class QualityUtil implements PhrescoConstants {
	
	public boolean testResultAvail(String appDirName, List<String> testAgainsts, String action) throws PhrescoException {
		boolean resultAvailable = false;
        try {
        	String reportDir = "";
        	String resultExtension = "";
        	ApplicationInfo appInfo = PhrescoUtil.getApplicationInfo();
        	if (Constants.PHASE_PERFORMANCE_TEST.equals(action)) {
        		reportDir =PhrescoUtil.getPerformanceTestReportDir(appInfo);
        		resultExtension = PhrescoUtil.getPerformanceResultFileExtension(appDirName);
        	} else {
        		reportDir = PhrescoUtil.getLoadTestReportDir(appInfo);
        		resultExtension = PhrescoUtil.getLoadResultFileExtension(appInfo);
        	}
            for (String testAgainst: testAgainsts) {
            	StringBuilder sb = new StringBuilder(PhrescoUtil.getApplicationHome());
            	if (Constants.PHASE_PERFORMANCE_TEST.equals(action)) {
            		reportDir =PhrescoUtil.getPerformanceTestReportDir(appInfo);
            	} else {
            		reportDir = PhrescoUtil.getLoadTestReportDir(appInfo);
            	}
               
                if (StringUtils.isNotEmpty(reportDir) && StringUtils.isNotEmpty(testAgainst)) {
                    Pattern p = Pattern.compile(TEST_DIRECTORY);
                    Matcher matcher = p.matcher(reportDir);
                    reportDir = matcher.replaceAll(testAgainst);
                    sb.append(reportDir); 
                }
                
                File file = new File(sb.toString());
                if (StringUtils.isNotEmpty(resultExtension) && file.exists()) {
                	File[] children = file.listFiles(new XmlNameFileFilter(resultExtension));
                	if (!ArrayUtils.isEmpty(children)) {
                		resultAvailable = true;
                		break;
                	}
                }
            }
            
            if (CollectionUtils.isEmpty(testAgainsts) && Constants.PHASE_PERFORMANCE_TEST.equals(action) 
            			&& StringUtils.isNotEmpty(reportDir)) {
            	 StringBuilder sb = new StringBuilder(PhrescoUtil.getApplicationHome());
            	 sb.append(reportDir);
            	 File file = new File(sb.toString());
                 if (StringUtils.isNotEmpty(resultExtension)) {
                 	File[] children = file.listFiles(new XmlNameFileFilter(resultExtension));
                 	if (!ArrayUtils.isEmpty(children)) {
                 		resultAvailable = true;
                 	}
                 }
            }
            
        } catch(Exception e) {
        	throw new PhrescoException(e);
        }

        return resultAvailable;
    }
	
	public class XmlNameFileFilter implements FilenameFilter {
		private String filter_;
		public XmlNameFileFilter(String filter) {
			filter_ = filter;
		}

		public boolean accept(File dir, String name) {
			return name.endsWith(filter_);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void sortResultFile(File[] children) {
		Arrays.sort( children, new Comparator() {
        	public int compare(Object o1, Object o2) {
            	if (((File)o1).lastModified() > ((File)o2).lastModified()) {
            		return -1;
            	} else if (((File)o1).lastModified() < ((File)o2).lastModified()) {
            		return +1;
            	} else {
            		return 0;
            	}
        	}
        });
	}

}
