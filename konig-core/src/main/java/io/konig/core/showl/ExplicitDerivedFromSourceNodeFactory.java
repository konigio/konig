package io.konig.core.showl;

/*
 * #%L
 * Konig Core
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


import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.shacl.Shape;

public class ExplicitDerivedFromSourceNodeFactory implements ShowlSourceNodeFactory {

	private ShowlNodeShapeBuilder nodeShapeBuilder;

	public ExplicitDerivedFromSourceNodeFactory(ShowlNodeShapeBuilder nodeShapeBuilder) {
		this.nodeShapeBuilder = nodeShapeBuilder;
	}

	@Override
	public Set<ShowlNodeShape> candidateSourceNodes(ShowlNodeShape targetNode) throws ShowlProcessingException {
		
		Set<Shape> explicitDerivedFrom = targetNode.getShape().getExplicitDerivedFrom();
		if (!explicitDerivedFrom.isEmpty()) {
			Set<ShowlNodeShape> result = new LinkedHashSet<>();
			
			for (Shape sourceShape : explicitDerivedFrom) {
				DataSource ds = dataSource(sourceShape);
				if (ds != null) {
					ShowlNodeShape node = nodeShapeBuilder.buildNodeShape(null, sourceShape);
	
					node.setTargetNode(targetNode);
					node.setShapeDataSource(new ShowlDataSource(node, ds));
					result.add(node);
				}
				
			}
			
			if (!result.isEmpty()) {
				return result;
			}
		}
		return Collections.emptySet();
	}

	private DataSource dataSource(Shape sourceShape) {
		for (DataSource ds : sourceShape.getShapeDataSource()) {
			// For now, we only support sources from a storage folder.
			// We'll likely need to support other business rules later.
			
			if (ds.isA(Konig.GoogleCloudStorageFolder)) {
				return ds;
			}
			if (ds.isA(Konig.GoogleCloudStorageBucket)) {
				return ds;
			}
			if (ds.isA(Konig.GoogleBigQueryTable)) {
				return ds;
			}
		}
		return null;
	}

}
