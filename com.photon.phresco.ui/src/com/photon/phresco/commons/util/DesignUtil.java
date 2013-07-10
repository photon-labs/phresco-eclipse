package com.photon.phresco.commons.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;

public class DesignUtil {
	
	public static Font getLabelFont() {
		return new Font(null, "Times New Roman", 10, SWT.NONE);
	}
	
	public static Font getHeaderFont() {
		return new Font(null, "Times New Roman", 10, SWT.BOLD);
	}

}
