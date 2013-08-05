package com.photon.phresco.dynamicParameter;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.codehaus.plexus.util.StringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.photon.phresco.api.DynamicPageParameter;
import com.photon.phresco.api.DynamicParameter;
import com.photon.phresco.commons.PhrescoConstants;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.model.ArtifactGroup;
import com.photon.phresco.commons.model.ArtifactInfo;
import com.photon.phresco.commons.model.BuildInfo;
import com.photon.phresco.commons.model.Customer;
import com.photon.phresco.commons.model.RepoInfo;
import com.photon.phresco.commons.util.PhrescoUtil;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value;
import com.photon.phresco.plugins.util.MojoProcessor;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.util.PhrescoDynamicLoader;

public class DynamicPossibleValues implements PhrescoConstants {
	private static Map<String, PhrescoDynamicLoader> pdlMap = new HashMap<String, PhrescoDynamicLoader>();
	private static int buildNo = 0;
	Map<String, Object> dynamicMap = new HashMap<String, Object>(); 
	public  Map<String, Object> setPossibleValuesInReq(MojoProcessor mojo, ApplicationInfo appInfo, List<Parameter> parameters, 
			Map<String, DependantParameters> watcherMap, String goal) throws PhrescoException {
		try {
			if (CollectionUtils.isNotEmpty(parameters)) {
				StringBuilder paramBuilder = new StringBuilder();
				ServiceManager serviceManager = PhrescoUtil.getServiceManager(PhrescoUtil.getUserId());
				for (Parameter parameter : parameters) {
					String parameterKey = parameter.getKey();
					if (DYNAMIC_PARAMETER.equalsIgnoreCase(parameter.getType()) && parameter.getDynamicParameter() != null) { 
						//Dynamic parameter
						Map<String, Object> constructMapForDynVals = constructMapForDynVals(appInfo, watcherMap, parameterKey);
						constructMapForDynVals.put("eclipseHome", PhrescoUtil.getApplicationHome());
						constructMapForDynVals.put("mojo", mojo);
						constructMapForDynVals.put("goal", goal);
						constructMapForDynVals.put("serviceManager", serviceManager);
						constructMapForDynVals.put("customerId", PhrescoUtil.getCustomerId());
						File buildInfoPath = PhrescoUtil.getBuildInfoPath();
						if (buildInfoPath.exists()) {
							Gson gson = new Gson();
							Type type = new TypeToken<List<BuildInfo>>() {}  .getType();
							FileReader reader = new FileReader(buildInfoPath);
							List<BuildInfo> buildInfos = (List<BuildInfo>)gson.fromJson(reader, type);
							if (CollectionUtils.isNotEmpty(buildInfos)) {
								Collections.sort(buildInfos, new BuildComparator());
								buildNo = buildInfos.get(0).getBuildNo();
							}
							constructMapForDynVals.put("buildNumber", String.valueOf(buildNo));
						}
						constructMapForDynVals.put("buildNumber", String.valueOf(buildNo));
						
						// Get the values from the dynamic parameter class
						List<Value> dynParamPossibleValues = getDynamicPossibleValues(constructMapForDynVals, parameter);
						dynamicMap.put(parameterKey, dynParamPossibleValues);
						
						addValueDependToWatcher(watcherMap, parameterKey, dynParamPossibleValues, parameter.getValue());
						if (watcherMap.containsKey(parameterKey)) {
							DependantParameters dependantParameters = (DependantParameters) watcherMap.get(parameterKey);
							if (CollectionUtils.isNotEmpty(dynParamPossibleValues)) {
								if (StringUtils.isNotEmpty(parameter.getValue())) {
									dependantParameters.setValue(parameter.getValue());
								} else {
									dependantParameters.setValue(dynParamPossibleValues.get(0).getValue());
								}
							}
						}

						//	setReqAttribute("possibleValues" + parameter.getKey(), dynParamPossibleValues);
						
						dynamicMap.put(parameterKey, dynParamPossibleValues);
						if (CollectionUtils.isNotEmpty(dynParamPossibleValues)) {
							if (StringUtils.isNotEmpty(parameter.getValue())) {
								addWatcher(watcherMap, parameter.getDependency(), parameterKey, parameter.getValue());
							} else {
								addWatcher(watcherMap, parameter.getDependency(), parameterKey, dynParamPossibleValues.get(0).getValue());
							}
						}
						if (StringUtils.isNotEmpty(paramBuilder.toString())) {
							paramBuilder.append("&");
						}
						paramBuilder.append(parameterKey);
						paramBuilder.append("=");
						if (CollectionUtils.isNotEmpty(dynParamPossibleValues)) {
							paramBuilder.append(dynParamPossibleValues.get(0).getValue());
						}
					} else if (parameter.getPossibleValues() != null) { //Possible values
						List<Value> values = parameter.getPossibleValues().getValue();

						if (watcherMap.containsKey(parameterKey)) {
							DependantParameters dependantParameters = (DependantParameters) watcherMap.get(parameterKey);
							if (StringUtils.isNotEmpty(parameter.getValue())) {
								dependantParameters.setValue(parameter.getValue());
							} else {
								dependantParameters.setValue(values.get(0).getValue());
							}
						}

						addValueDependToWatcher(watcherMap, parameterKey, values, parameter.getValue());
						if (CollectionUtils.isNotEmpty(values)) {
							if (StringUtils.isNotEmpty(parameter.getValue())) {
								addWatcher(watcherMap, parameter.getDependency(), parameterKey, parameter.getValue());
							} else {
								addWatcher(watcherMap, parameter.getDependency(), parameterKey, values.get(0).getKey());
							}
						}

						if (StringUtils.isNotEmpty(paramBuilder.toString())) {
							paramBuilder.append("&");
						}
						paramBuilder.append(parameterKey);
						paramBuilder.append("=");
						paramBuilder.append("");
					} else if (parameter.getType().equalsIgnoreCase("boolean") && StringUtils.isNotEmpty(parameter.getDependency())) {
						//Checkbox
						addWatcher(watcherMap, parameter.getDependency(), parameterKey, parameter.getValue());
						if (StringUtils.isNotEmpty(paramBuilder.toString())) {
							paramBuilder.append("&");
						}
						paramBuilder.append(parameterKey);
						paramBuilder.append("=");
						paramBuilder.append("");
					} else if("DynamicPageParameter".equalsIgnoreCase(parameter.getType())) {
						//	setReqAttribute(REQ_CUSTOMER_ID, getCustomerId());
						dynamicMap.put("customerId", PhrescoUtil.getCustomerId());
						Map<String, Object> dynamicPageParameterMap = getDynamicPageParameter(appInfo, watcherMap, parameter);
						List<? extends Object> dynamicPageParameter = (List<? extends Object>) dynamicPageParameterMap.get("valuesFromJson");
						String className = (String) dynamicPageParameterMap.get("className");
						//setReqAttribute("className", className);
						dynamicMap.put("className",className);
						//	setReqAttribute(REQ_DYNAMIC_PAGE_PARAMETER + parameter.getKey(), dynamicPageParameter);
						dynamicMap.put(parameter.getKey(),dynamicPageParameter);
					}
				}
				//	setAvailableParams(paramBuilder.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		dynamicMap.put(WATCHER_MAP, watcherMap);
		return dynamicMap;
	}

	public Map<String, Object> constructMapForDynVals(ApplicationInfo appInfo, Map<String, DependantParameters> watcherMap, String parameterKey) throws PhrescoException {
		Map<String, Object> paramMap = new HashMap<String, Object>(8);
		DependantParameters dependantParameters = watcherMap.get(parameterKey);
		if (dependantParameters != null) {
			paramMap.putAll(getDependantParameters(dependantParameters.getParentMap(), watcherMap));
		}
		paramMap.put(DynamicParameter.KEY_APP_INFO, appInfo);
		paramMap.put("customerId", PhrescoUtil.getCustomerId());
//		if (StringUtils.isNotEmpty(getReqParameter("buildNumber"))) {
//			paramMap.put(DynamicParameter.KEY_BUILD_NO, getReqParameter("buildNo"));
//		}

		return paramMap;
	}

	private Map<String, Object> getDependantParameters(Map<String, String> parentMap, Map<String, DependantParameters> watcherMap) {
		Map<String, Object> paramMap = new HashMap<String, Object>(8);
		Set<String> keySet = parentMap.keySet();
		for (String key : keySet) {
			if (watcherMap.get(key) != null) {
				String value = ((DependantParameters) watcherMap.get(key)).getValue();
				paramMap.put(key, value);
			}
		}
		return paramMap;
	}

	public List<Value> getDynamicPossibleValues(Map<String, Object> watcherMap, Parameter parameter) throws PhrescoException {
		PossibleValues possibleValue = getDynamicValues(watcherMap, parameter);
		List<Value> possibleValues = (List<Value>) possibleValue.getValue();
		return possibleValues;
	}


	public void addValueDependToWatcher(Map<String, DependantParameters> watcherMap, String parameterKey, List<Value> values, String previousValue) {
		for (Value value : values) {
			if (StringUtils.isNotEmpty(value.getDependency())) {
				if (StringUtils.isNotEmpty(previousValue)) {
					addWatcher(watcherMap, value.getDependency(), parameterKey, previousValue);
				} else {
					addWatcher(watcherMap, value.getDependency(), parameterKey, value.getKey());
				}
			}
		}
	}

	public void addWatcher(Map<String, DependantParameters> watcherMap, String dependency, String parameterKey, String parameterValue) {
		if (StringUtils.isNotEmpty(dependency)) {
			List<String> dependencyKeys = Arrays.asList(dependency.split("\\s*,\\s*"));
			for (String dependentKey : dependencyKeys) {
				DependantParameters dependantParameters;
				if (watcherMap.containsKey(dependentKey)) {
					dependantParameters = (DependantParameters) watcherMap.get(dependentKey);
				} else {
					dependantParameters = new DependantParameters();
				}
				dependantParameters.getParentMap().put(parameterKey, parameterValue);
				watcherMap.put(dependentKey, dependantParameters);
			}
		}

		addParentToWatcher(watcherMap, parameterKey, parameterValue);
	}

	private void addParentToWatcher(Map<String, DependantParameters> watcherMap, String parameterKey, String parameterValue) {

		DependantParameters dependantParameters;
		if (watcherMap.containsKey(parameterKey)) {
			dependantParameters = (DependantParameters) watcherMap.get(parameterKey);
		} else {
			dependantParameters = new DependantParameters();
		}
		dependantParameters.setValue(parameterValue);
		watcherMap.put(parameterKey, dependantParameters);
	}


	protected Map<String, Object> getDynamicPageParameter(ApplicationInfo appInfo, Map<String, DependantParameters> watcherMap, Parameter parameter) throws PhrescoException {
		String parameterKey = parameter.getKey();
		Map<String, Object> paramsMap = constructMapForDynVals(appInfo, watcherMap, parameterKey);
		String className = parameter.getDynamicParameter().getClazz();
		DynamicPageParameter dynamicPageParameter;
		PhrescoDynamicLoader phrescoDynamicLoader = pdlMap.get(PhrescoUtil.getCustomerId());
		if (MapUtils.isNotEmpty(pdlMap) && phrescoDynamicLoader != null) {
			dynamicPageParameter = phrescoDynamicLoader.getDynamicPageParameter(className);
		} else {
			//To get repo info from Customer object
			ServiceManager serviceManager = PhrescoUtil.getServiceManager(PhrescoUtil.getUserId());
			Customer customer = serviceManager.getCustomer(PhrescoUtil.getCustomerId());
			RepoInfo repoInfo = customer.getRepoInfo();
			//To set groupid,artfid,type infos to List<ArtifactGroup>
			List<ArtifactGroup> artifactGroups = new ArrayList<ArtifactGroup>();
			ArtifactGroup artifactGroup = new ArtifactGroup();
			artifactGroup.setGroupId(parameter.getDynamicParameter().getDependencies().getDependency().getGroupId());
			artifactGroup.setArtifactId(parameter.getDynamicParameter().getDependencies().getDependency().getArtifactId());
			artifactGroup.setPackaging(parameter.getDynamicParameter().getDependencies().getDependency().getType());
			//to set version
			List<ArtifactInfo> artifactInfos = new ArrayList<ArtifactInfo>();
			ArtifactInfo artifactInfo = new ArtifactInfo();
			artifactInfo.setVersion(parameter.getDynamicParameter().getDependencies().getDependency().getVersion());
			artifactInfos.add(artifactInfo);
			artifactGroup.setVersions(artifactInfos);
			artifactGroups.add(artifactGroup);

			//dynamically loads specified Class
			phrescoDynamicLoader = new PhrescoDynamicLoader(repoInfo, artifactGroups);
			dynamicPageParameter = phrescoDynamicLoader.getDynamicPageParameter(className);
			pdlMap.put(PhrescoUtil.getCustomerId(), phrescoDynamicLoader);
		}

		return dynamicPageParameter.getObjects(paramsMap);
	}

	private PossibleValues getDynamicValues(Map<String, Object> watcherMap, Parameter parameter) throws PhrescoException {
		try {
			String className = parameter.getDynamicParameter().getClazz();
			String grpId = parameter.getDynamicParameter().getDependencies().getDependency().getGroupId();
			String artfId = parameter.getDynamicParameter().getDependencies().getDependency().getArtifactId();
			String jarVersion = parameter.getDynamicParameter().getDependencies().getDependency().getVersion();

			DynamicParameter dynamicParameter;
			PhrescoDynamicLoader phrescoDynamicLoader = pdlMap.get(PhrescoUtil.getCustomerId() + grpId + artfId + jarVersion);

			if (MapUtils.isNotEmpty(pdlMap) && phrescoDynamicLoader != null) {
				dynamicParameter = phrescoDynamicLoader.getDynamicParameter(className);
			} else {
				//To get repo info from Customer object

				ServiceManager serviceManager = PhrescoUtil.getServiceManager(PhrescoUtil.getUserId());
				Customer customer = serviceManager.getCustomer(PhrescoUtil.getCustomerId());
				RepoInfo repoInfo = customer.getRepoInfo();
				//To set groupid,artfid,type infos to List<ArtifactGroup>
				List<ArtifactGroup> artifactGroups = new ArrayList<ArtifactGroup>();
				ArtifactGroup artifactGroup = new ArtifactGroup();
				artifactGroup.setGroupId(grpId);
				artifactGroup.setArtifactId(artfId);
				artifactGroup.setPackaging(parameter.getDynamicParameter().getDependencies().getDependency().getType());
				//to set version
				List<ArtifactInfo> artifactInfos = new ArrayList<ArtifactInfo>();
				ArtifactInfo artifactInfo = new ArtifactInfo();
				artifactInfo.setVersion(jarVersion);
				artifactInfos.add(artifactInfo);
				artifactGroup.setVersions(artifactInfos);
				artifactGroups.add(artifactGroup);

				//dynamically loads specified Class
				phrescoDynamicLoader = new PhrescoDynamicLoader(repoInfo, artifactGroups);
				dynamicParameter = phrescoDynamicLoader.getDynamicParameter(className);
				pdlMap.put(PhrescoUtil.getCustomerId() + grpId + artfId + jarVersion, phrescoDynamicLoader);
			}

			return dynamicParameter.getValues(watcherMap);
		} catch (Exception e) {
			throw new PhrescoException(e);
		}
	}

	class BuildComparator implements Comparator<BuildInfo> {
		public int compare(BuildInfo buildInfo1, BuildInfo buildInfo2) {
			DateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy hh:mm:ss");
			Date  buildTime1 = new Date();
			Date buildTime2 = new Date();
			try {
				buildTime1 = (Date)formatter.parse(buildInfo1.getTimeStamp());
				buildTime2 = (Date)formatter.parse(buildInfo2.getTimeStamp());
			} catch (ParseException e) {
			}
			return buildTime2.compareTo(buildTime1);
		}
	}
	
	public int getBuildNumer() {
		return buildNo;
	}
	
}
