package io.konig.core.showl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.konig.core.vocab.Konig;
import io.konig.shacl.Shape;

public class IdentityMappingSelector implements ShowlSourceNodeSelector {

	@Override
	public Set<Shape> selectCandidateSources(ShowlNodeShape targetShape) {
		
		Shape target = targetShape.getShape();
		
		if (target.hasDataSourceType(Konig.GoogleBigQueryTable) && target.hasDataSourceType(Konig.GoogleCloudStorageBucket)) {
			Set<Shape> set = new HashSet<>();
			set.add(target);
			return set;
		}
		
		return Collections.emptySet();
	}


}
