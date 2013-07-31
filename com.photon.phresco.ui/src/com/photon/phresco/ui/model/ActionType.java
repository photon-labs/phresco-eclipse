package com.photon.phresco.ui.model;

public enum ActionType {

	BUILD("phresco:package"), DEPLOY("phresco:deploy"), UNIT_TEST("phresco:unit-test"), COMPONENT_TEST("phresco:component-test"), FUNCTIONAL_TEST("phresco:functional-test"), 
	LOAD_TEST("phresco:load-test"), PERFORMANCE_TEST("phresco:performance-test"), CODE_VALIDATE("phresco:validate-code"), SITE_REPORT("clean site"), INSTALL("install"), 
	START("t7:run-forked"), STOP("t7:stop-forked"), RUNAGAINSTSOURCE("phresco:start"), STOPSERVER("phresco:stop"), 
	START_HUB("phresco:start-hub"), STOP_HUB("phresco:stop-hub"), START_NODE("phresco:start-node"), STOP_NODE("phresco:stop-node"), PDF_REPORT("phresco:pdf-report"),
	MINIFY("yuicompressor:compress"), IPA_DOWNLOAD("xcode:ipaBuilder"), THEME_VALIDATOR("phresco:theme-validator"), CONTENT_VALIDATOR("phresco:content-validator"),
	THEME_CONVERTOR("phresco:theme-convertor"), CONTENT_CONVERTOR("phresco:content-convertor"), PROCESS_BUILD("phresco:process-build"), ECLIPSE("phresco:eclipse");
	
	private String actionType;

	private ActionType(String actionType) {
		this.actionType = actionType;
	}

	public String getActionType() {
		return actionType;
	}
}

