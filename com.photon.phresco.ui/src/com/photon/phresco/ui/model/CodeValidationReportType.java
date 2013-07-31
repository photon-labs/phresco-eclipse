package com.photon.phresco.ui.model;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodeValidationReportType implements Serializable  {

	private static final long serialVersionUID = 1L;

	public CodeValidationReportType() {
		super();
	}

	 private Value validateAgainst;
	 private List<Value> options;
	
	public void setValidateAgainst(Value validateAgainst) {
		this.validateAgainst = validateAgainst;
	}

	public Value getValidateAgainst() {
		return validateAgainst;
	}

	public void setOptions(List<Value> options) {
		this.options = options;
	}

	public List<Value> getOptions() {
		return options;
	}
	
	public String toString() {
        return new ToStringBuilder(this,
                ToStringStyle.DEFAULT_STYLE)
                .append("validateAgainst", getValidateAgainst())
                .append("options", getOptions())
                .toString();
    }
}