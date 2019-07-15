package io.konig.transform.beam;

import java.util.List;

import io.konig.core.showl.ShowlEffectiveNodeShape;

public class StructInfo {
	
	private List<ShowlEffectiveNodeShape> nodeList;
	private List<BeamMethod> methodList;
	public StructInfo(List<ShowlEffectiveNodeShape> nodeList, List<BeamMethod> methodList) {
		this.nodeList = nodeList;
		this.methodList = methodList;
	}
	public List<ShowlEffectiveNodeShape> getNodeList() {
		return nodeList;
	}
	public List<BeamMethod> getMethodList() {
		return methodList;
	}

	
}
