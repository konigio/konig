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


import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlNodeShape;

public class BeamParameter {

	private BeamParameterType paramType;
	private JVar var;
	private ShowlNodeShape sourceNode;
	
	public static BeamParameter ofSourceRow(JVar var, ShowlNodeShape node) {
		return new BeamParameter(BeamParameterType.SOURCE_TABLE_ROW, var, node);
	}
	
	public static BeamParameter ofTargetRow(JVar var, ShowlNodeShape node) {
		return new BeamParameter(BeamParameterType.TARGET_TABLE_ROW, var, node);
	}
	
	public static BeamParameter ofList(JVar list) {
		return new BeamParameter(BeamParameterType.LIST_VALUE, list, null);
	}
	
	public static BeamParameter ofErrorBuilder(JVar var) {
		return new BeamParameter(BeamParameterType.ERROR_BUILDER, var, null);
	}
	
	public static BeamParameter ofEnumValue(JVar var) {
		return new BeamParameter(BeamParameterType.ENUM_VALUE, var, null);
	}
	
	
	public String toString() {
		return "BeamParameter(" + var.name() + ")";
	}
	
	private BeamParameter(BeamParameterType paramType, JVar var, ShowlNodeShape sourceNode) {
		this.paramType = paramType;
		this.var = var;
		this.sourceNode = sourceNode;
	}

	public BeamParameterType getParamType() {
		return paramType;
	}

	public JVar getVar() {
		return var;
	}

	public ShowlNodeShape getSourceNode() {
		return sourceNode;
	}
	

}
