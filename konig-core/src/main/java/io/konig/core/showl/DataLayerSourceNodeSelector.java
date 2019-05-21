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


import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.Graph;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.shacl.Shape;

public class DataLayerSourceNodeSelector implements ShowlSourceNodeSelector {


	@Override
	public Set<ShowlNodeShape> selectCandidateSources(ShowlService factory, ShowlNodeShape targetShape) {
		Set<ShowlNodeShape> result = new HashSet<>();
		URI targetClass = targetShape.getOwlClass().getId();
		if (!Konig.Undefined.equals(targetClass)) {
			Set<URI> sourceSystemSet = sourceSystemSet(factory, targetShape);
			if (!sourceSystemSet.isEmpty()) {
				ShowlClass owlClass = targetShape.getOwlClass();
				for (ShowlNodeShape sourceNode : owlClass.getTargetClassOf()) {
					Shape sourceShape = sourceNode.getShape();
					if (sourceShape == targetShape.getShape()) {
						continue;
					}
					for (DataSource ds : sourceShape.getShapeDataSource()) {
						if (acceptableSource(ds, sourceSystemSet)) {
							result.add( factory.createNodeShape(sourceShape, ds) );
						}
					}
				}
			}
		}
		return result;
	}

	private boolean acceptableSource(DataSource ds, Set<URI> sourceSystemSet) {
		for (URI systemId : ds.getIsPartOf()) {
			if (sourceSystemSet.contains(systemId)) {
				return true;
			}
		}
		return false;
	}

	private Set<URI> sourceSystemSet(ShowlService factory, ShowlNodeShape targetShape) {
		Graph graph = factory.getGraph();
		Set<URI> result = new HashSet<>();
		ShowlNodeShape root = targetShape.getRoot();
		
		if (root.getShapeDataSource() != null) {
			for (URI targetSystem : root.getShapeDataSource().getDataSource().getIsPartOf()) {
				Set<URI> sourceSystemSet = graph.v(targetSystem).out(Konig.receivesDataFrom).toUriSet();
				result.addAll(sourceSystemSet);
			}
		}
		return result;
	}

}
