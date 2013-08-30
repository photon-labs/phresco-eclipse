package com.photon.phresco.commons.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Text;

public class PhrescoValidation {

	public static void validateText(Text text) {
		text.addVerifyListener(new VerifyListener() {
			
			@Override
			public void verifyText(VerifyEvent e) {
				String REGEX = "^[a-zA-Z0-9._]+$"; 
			    Pattern pattern = Pattern.compile(REGEX);  
				String string = e.text;
		        char[] chars = new char[string.length()];
		        string.getChars(0, chars.length, chars, 0);

		        Text text = (Text)e.getSource();

		        e.text = new String(chars);
		        final String oldS = text.getText();
		        String newS = oldS.substring(0, e.start) + e.text + oldS.substring(e.end);
		        Matcher matcher = pattern.matcher(newS);
		        if(StringUtils.isEmpty(newS)) {
		        	 e.doit = true;
			            return;
		        }
		        if (!matcher.matches()) {
		            e.doit = false;
		            return;
		        }
			}
		});
	}
}
