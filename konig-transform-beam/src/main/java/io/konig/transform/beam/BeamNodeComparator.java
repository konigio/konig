package io.konig.transform.beam;

import java.util.Comparator;

import io.konig.core.impl.RdfUtil;
import io.konig.core.showl.ShowlNodeShape;

public class BeamNodeComparator implements Comparator<ShowlNodeShape> {

	@Override
	public int compare(ShowlNodeShape a, ShowlNodeShape b) {
		int aKind = nodeKind(a);
		int bKind = nodeKind(b);
		
		return (aKind==bKind) ? RdfUtil.localName(a.getId()).compareTo(RdfUtil.localName(b.getId())) : aKind-bKind;
	}

	private int nodeKind(ShowlNodeShape node) {
		
		return node.isTargetNode() ? 0 : 1;
	}


}
