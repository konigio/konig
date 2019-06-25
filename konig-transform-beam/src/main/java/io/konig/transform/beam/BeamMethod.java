package io.konig.transform.beam;

/*
 * #%L
 * Konig Transform Beam
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlPropertyShape;

public class BeamMethod {
	
	private String methodNameBase;
	private String methodNameSuffix;
	private JMethod method;
	private List<BeamParameter> parameters = new ArrayList<>();
	private Set<ShowlPropertyShape> sourceProperties;
	
	public BeamMethod(JMethod method) {
		this.method = method;
	}
		public void addParameter(BeamParameter p) {
		parameters.add(p);
	}
		
	public String toString() {
		return "BeamMethod(" + method.name() + ")";
	}

	public JMethod getMethod() {
		return method;
	}

	public List<BeamParameter> getParameters() {
		return parameters;
	}
	
	public BeamParameter addListParam(AbstractJType paramType, String paramName) {
		JVar var = method.param(paramType, paramName);
		BeamParameter param = BeamParameter.ofList(var);
		addParameter(param);
		
		return param;
	}

	public void addErrorBuilderParam(AbstractJClass errorBuilderClass) {
		JVar param = method.param(errorBuilderClass, "errorBuilder");
		addParameter(BeamParameter.ofErrorBuilder(param));
	}
	public String getMethodNameBase() {
		return methodNameBase;
	}
	public void setMethodNameBase(String methodNameBase) {
		this.methodNameBase = methodNameBase;
	}
	public String getMethodNameSuffix() {
		return methodNameSuffix;
	}
	
	public void setMethodNameSuffix(String methodNameSuffix) {
		this.methodNameSuffix = methodNameSuffix;
	}
	public Set<ShowlPropertyShape> getSourceProperties() {
		return sourceProperties;
	}
	public void setSourceProperties(Set<ShowlPropertyShape> sourceProperties) {
		this.sourceProperties = sourceProperties;
	}

	
}
