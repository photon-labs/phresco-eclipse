/**
 * Framework Web Archive
 *
 * Copyright (C) 1999-2013 Photon Infotech Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.photon.phresco.dynamicParameter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class DependantParameters implements Serializable {
    
	private static final long serialVersionUID = 1L;

	private Map<String, String> parentMap; 
    private String value;
    
    public DependantParameters() {
        parentMap = new HashMap<String, String>();

    }

    public DependantParameters(Map<String, String> parentMap) {
        this.parentMap = parentMap;
    }

    public DependantParameters(Map<String, String> parentMap, String value) {
        this.parentMap = parentMap;
        this.value = value;
    }
    
    public Map<String, String> getParentMap() {
        return parentMap;
    }

    public void setParentMap(Map<String, String> parentMap) {
        this.parentMap = parentMap;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String toString() {
        return new ToStringBuilder(this,
                ToStringStyle.DEFAULT_STYLE)
                .append("parentMap : ", getParentMap())
                .append("value : ", getValue())
                .toString();
    }
}