package io.konig.transform.beam;

import java.text.MessageFormat;

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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlPropertyShape;

public class BeamMethod implements Comparable<BeamMethod> {
	private static final Logger logger = LoggerFactory.getLogger(BeamMethod.class);
	
	private JMethod method;
	private RdfJavaType returnType;
	private List<BeamParameter> parameters = new ArrayList<>();
	private Set<ShowlPropertyShape> sourceProperties;
	
	private Set<BeamMethod> invocationSet = null;
	
	private Set<BeamParameter> excludeParam;
	private ShowlPropertyShape targetProperty;
	
	public BeamMethod(JMethod method) {
		this.method = method;
		if (logger.isTraceEnabled()) {
			logger.trace("new BeamMethod({})", method.name());
		}
	}
	
	public BeamParameter addParameter(BeamParameter p) throws BeamTransformGenerationException {
		if (!accept(p)) {
			return null;
		}
		if (p.getParamType() == BeamParameterType.TABLE_ROW) {
			for (BeamParameter q : parameters) {
				if (
						(q.getParamType() == p.getParamType() && q.getNode()==p.getNode()) ||
						q.getVarName().equals(p.getVarName())
				) {
					String msg = MessageFormat.format("Duplicate parameter ''{0}'' in method {1}", p.getVarName(), name());
//					throw new BeamTransformGenerationException(msg);
				}
			}
		}
		JVar var = method.param(p.getVarType(), p.getVarName());
		p.setVar(var);
		parameters.add(p);
		return p;
	}
		
	public String toString() {
		return "BeamMethod(" + method.name() + ")";
	}

	
	public String name() {
		return method.name();
	}
	public JMethod getMethod() {
		return method;
	}

	public ShowlPropertyShape getTargetProperty() {
		return targetProperty;
	}

	public void setTargetProperty(ShowlPropertyShape targetProperty) {
		this.targetProperty = targetProperty;
	}

	public List<BeamParameter> getParameters() {
		return parameters;
	}
	
	public BeamParameter addListParam(AbstractJType paramType, String paramName) throws BeamTransformGenerationException {
		BeamParameter param = BeamParameter.ofList(paramType, paramName);
		addParameter(param);
		
		return param;
	}

	public void addErrorBuilderParam(AbstractJClass errorBuilderClass) throws BeamTransformGenerationException {
		addParameter(BeamParameter.ofErrorBuilder(errorBuilderClass));
	}
	public Set<ShowlPropertyShape> getSourceProperties() {
		return sourceProperties;
	}
	public void setSourceProperties(Set<ShowlPropertyShape> sourceProperties) {
		this.sourceProperties = sourceProperties;
	}
	public RdfJavaType getReturnType() {
		return returnType;
	}
	public void setReturnType(RdfJavaType returnType) {
		this.returnType = returnType;
	}
	
	@Override
	public int compareTo(BeamMethod other) {
		String thisName = method.name();
		String otherName = other.getMethod().name();
		return thisName.compareTo(otherName);
	}

	/**
	 * Assert that this method invokes another method.
	 * @param method
	 */
	public void addInvocation(BeamMethod method) {
		if (invocationSet==null) {
			invocationSet = new HashSet<>();
		}
		invocationSet.add(method);
	}
	
	public Set<BeamMethod> getInvocationSet() {
		return invocationSet==null ? Collections.emptySet() : invocationSet;
	}
	
	public void excludeParamFor(BeamParameter param) {
		if (excludeParam == null) {
			excludeParam = new HashSet<>();
		}
		excludeParam.add(param);
	}
	
	public boolean accept(BeamParameter param) {
		
		if (excludeParam!=null) {
			for (BeamParameter exclude : excludeParam) {
				if (exclude.matches(param)) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	public BeamParameter copyParam(BeamParameter param) throws BeamTransformGenerationException {
		String paramName = param.getVar().name();
		AbstractJType paramType = param.getVar().type();
		BeamParameter copy = param.copy(method.param(paramType, paramName));
		addParameter(copy);
		return copy;
	}
	
	
}
