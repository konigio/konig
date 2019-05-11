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
import org.openrdf.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;

public class ReceivesDataFromSourceNodeFactory implements ShowlSourceNodeFactory {

	private static final Logger logger = LoggerFactory.getLogger(ReceivesDataFromSourceNodeFactory.class);
	
	private ShowlNodeShapeBuilder nodeShapeBuilder;
	private Graph graph;
	
	

	public ReceivesDataFromSourceNodeFactory(ShowlNodeShapeBuilder nodeShapeBuilder, Graph graph) {
		this.nodeShapeBuilder = nodeShapeBuilder;
		this.graph = graph;
	}

	@Override
	public Set<ShowlNodeShape> candidateSourceNodes(ShowlNodeShape targetNode) throws ShowlProcessingException {
		Set<ShowlNodeShape> set = new HashSet<>();
		ShowlDataSource dataSource = targetNode.getRoot().getShapeDataSource();
		if (dataSource != null) {
			
		
			Set<URI> sourceSystems = sourceSystems(dataSource.getDataSource());
			

			ShowlClass owlClass = targetNode.getOwlClass();
			for (ShowlNodeShape candidate : owlClass.getTargetClassOf()) {
				
				DataSource ds = candidateDataSource(candidate, sourceSystems);
				if (ds != null) {
					
					ShowlNodeShape sourceShape = nodeShapeBuilder.buildNodeShape(null, candidate.getShape());
					sourceShape.setShapeDataSource(new ShowlDataSource(sourceShape, ds));
					
					set.add(sourceShape);
					
					if (logger.isTraceEnabled()) {
						logger.trace("candidateSourceNodes: {} is candidate source for {}", 
								candidate.getPath(), targetNode.getPath());
					}
				}
				
			}
		} else {
			logger.warn(
				"Cannot find candidate source nodes because the target node {} does not have a data source defined.", 
				targetNode.getPath());
		}
		
		return set;
	}

	

	private DataSource candidateDataSource(ShowlNodeShape candidate, Set<URI> sourceSystems) {
		
		if (candidate.getShape() != null) {
			for (DataSource ds : candidate.getShape().getShapeDataSource()) {
				for (URI system : ds.getIsPartOf()) {
					if (sourceSystems.contains(system)) {
						return ds;
					}
				}
			}
		}
		
		return null;
	}

	/**
	 * Get the set of allowed source systems
	 * @param dataSource 
	 */
	private Set<URI> sourceSystems(DataSource dataSource) {
		Set<URI> result = new HashSet<>();
		
		for (URI systemId : dataSource.getIsPartOf()) {
			Vertex system = graph.getVertex(systemId);
			if (system != null) {
				for (Value value : system.getValueSet(Konig.receivesDataFrom)) {
					if (value instanceof URI) {
						result.add((URI) value);
					}
				}
			}
		}
		
		
		return result;
	}

}
