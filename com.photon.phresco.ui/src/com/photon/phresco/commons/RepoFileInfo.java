package com.photon.phresco.commons;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.tmatesoft.svn.core.wc.SVNStatusType;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepoFileInfo  implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String commitFilePath;
	private String status;
	private SVNStatusType contentsStatus;

	public RepoFileInfo() {
		super();
	}
	
	public RepoFileInfo(String commitFilePath, String status, SVNStatusType contentsStatus) {
		super();
		this.commitFilePath = commitFilePath;
		this.status = status;
		this.contentsStatus = contentsStatus;
	}

	public String getCommitFilePath() {
		return commitFilePath;
	}

	public void setCommitFilePath(String commitFilePath) {
		this.commitFilePath = commitFilePath;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setContentsStatus(SVNStatusType contentsStatus) {
		this.contentsStatus = contentsStatus;
	}

	public SVNStatusType getContentsStatus() {
		return contentsStatus;
	}

	public String toString() {
		 return new ToStringBuilder(this,
	                ToStringStyle.DEFAULT_STYLE)
	                .append(super.toString())
	                .append("commitFilePath", getCommitFilePath())
	                .append("status", getStatus())
	                .append("contentsStatus", getContentsStatus())
	                .toString();
	}

}
