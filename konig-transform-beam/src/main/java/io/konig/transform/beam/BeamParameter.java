package io.konig.transform.beam;

import com.helger.jcodemodel.AbstractJType;

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


import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlEffectiveNodeShape;
import io.konig.core.showl.ShowlNodeShape;

public class BeamParameter implements Comparable<BeamParameter> {

	private BeamParameterType paramType;
	private AbstractJType varType;
	private String varName;
	private JVar var;
	private ShowlEffectiveNodeShape node;
	
	public static BeamParameter pattern(BeamParameterType type, ShowlEffectiveNodeShape node) {
		return new BeamParameter(null, null, node, type);
	}
	
	public static BeamParameter ofNodeRow(AbstractJType varType, String varName, ShowlEffectiveNodeShape node) {
		return new BeamParameter(varType, varName, node, BeamParameterType.TABLE_ROW);
	}
	
	public static BeamParameter ofList(AbstractJType varType, String varName) {
		return new BeamParameter(varType, BeamParameterType.LIST_VALUE, varName);
	}
	
	public static BeamParameter ofErrorBuilder(AbstractJType varType) {
		return new BeamParameter(varType, BeamParameterType.ERROR_BUILDER, "errorBuilder");
	}
	
	public static BeamParameter ofMappedValue(AbstractJType varType, String varName) {
		return new BeamParameter(varType, BeamParameterType.MAPPED_VALUE, varName);
	}
	
	public static BeamParameter ofEnumValue(AbstractJType varType, String varName, ShowlEffectiveNodeShape node) {
		return new BeamParameter(varType, varName, node, BeamParameterType.ENUM_VALUE);
	}
	
	public static BeamParameter ofPipelineOptions(AbstractJType varType) {
		return new BeamParameter(varType, BeamParameterType.PIPELINE_OPTIONS, "options");
	}
	
	public String toString() {
		return "BeamParameter(" + varName + ")";
	}
	
	private BeamParameter(AbstractJType varType, String varName, ShowlEffectiveNodeShape node, BeamParameterType type) {
		this.varType = varType;
		this.varName = varName;
		this.node = node;
		this.paramType = type;
	}
	
	private BeamParameter(AbstractJType varType, BeamParameterType paramType, String varName) {
		this.varType = varType;
		this.paramType = paramType;
		this.varName = varName;
	}

	private BeamParameter(ShowlEffectiveNodeShape node, BeamParameterType paramType, AbstractJType varType, String varName, JVar var) {
		this.node = node;
		this.paramType = paramType;
		this.varType = varType;
		this.varName = varName;
		this.var = var;
	}
		
	public String getVarName() {
		return varName;
	}

	public BeamParameterType getParamType() {
		return paramType;
	}

	public JVar getVar() {
		return var;
	}

	public void setVar(JVar var) {
		this.var = var;
	}

	public AbstractJType getVarType() {
		return varType;
	}

	public ShowlEffectiveNodeShape getNode() {
		return node;
	}
	
	public boolean matches(BeamParameter other) {
		return node==other.node && paramType==other.paramType;
	}

	@Override
	public int compareTo(BeamParameter other) {
		return var.name().compareTo(other.var.name());
	}
	
	public BeamParameter copy(JVar var) {
		return new BeamParameter(node, paramType, var.type(), var.name(), var);
	}


}
