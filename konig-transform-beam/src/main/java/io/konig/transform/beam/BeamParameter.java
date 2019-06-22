package io.konig.transform.beam;

import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlNodeShape;

public class BeamParameter {

	private BeamParameterType paramType;
	private JVar var;
	private ShowlNodeShape sourceNode;
	
	public static BeamParameter ofSourceRow(JVar var, ShowlNodeShape node) {
		return new BeamParameter(BeamParameterType.SOURCE_TABLE_ROW, var, node);
	}
	
	public static BeamParameter ofList(JVar list) {
		return new BeamParameter(BeamParameterType.LIST_VALUE, list, null);
	}
	
	public static BeamParameter ofErrorBuilder(JVar var) {
		return new BeamParameter(BeamParameterType.ERROR_BUILDER, var, null);
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
