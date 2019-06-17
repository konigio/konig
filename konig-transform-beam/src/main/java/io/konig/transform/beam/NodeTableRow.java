package io.konig.transform.beam;

import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlNodeShape;

public class NodeTableRow  {
	private ShowlNodeShape node;
	private JVar tableRowVar;
	
	public NodeTableRow(ShowlNodeShape node, JVar tableRowVar) {
		this.node = node;
		this.tableRowVar = tableRowVar;
	}
	
	public ShowlNodeShape getNode() {
		return node;
	}

	public JVar getTableRowVar() {
		return tableRowVar;
	}

}
