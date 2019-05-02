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
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class RawCubeSourceNodeSelector implements ShowlSourceNodeSelector {
	private static Logger logger = LoggerFactory.getLogger(RawCubeSourceNodeSelector.class);
	private ShapeManager shapeManager;
	

	public RawCubeSourceNodeSelector(ShapeManager shapeManager) {
		this.shapeManager = shapeManager;
	}



	@Override
	public Set<ShowlNodeShape> selectCandidateSources(ShowlFactory factory, ShowlNodeShape targetShape) {
		
		ShowlPropertyShape accessor = targetShape.getAccessor();
		if (accessor instanceof ShowlVariablePropertyShape) {
			
		}
		
		Shape targetNode = targetShape.getShape();
		
		if (
			(targetNode.getNodeShapeCube() != null) &&
			(targetNode.getVariable().size()==1) 
		) {
			PropertyConstraint variable = targetNode.getVariable().get(0);
			URI owlClass = RdfUtil.uri( variable.getValueClass() );
			if (owlClass != null) {
				Set<ShowlNodeShape> result = new HashSet<>();
				for (Shape candidate : shapeManager.getShapesByTargetClass(owlClass)) {
					// For now, we only consider candidates that have a BigQuery data source.
					// We should relax this constraint later.
					
					DataSource ds = candidate.findDataSourceByType(Konig.GoogleBigQueryTable);
					
					if (ds != null) {
						ShowlPropertyShape var = targetShape.findOut(variable.getPredicate());
						if (var == null) {
							logger.warn("Variable ?{} not found in target node {}", variable.getPredicate().getLocalName(), targetShape.getPath());
							continue;
						}
						ShowlNodeShape sourceNode = factory.createNodeShape(candidate, ds);
						if (var.getValueShape() == null) {
							logger.warn("Variable ?{} does not have any nested properties in target node {}",
									variable.getPredicate().getLocalName(), targetShape.getPath());
							continue;
						}
						sourceNode.setTargetNode(var.getValueShape());
						
						result.add(sourceNode);
					}
				}
				
				return result;
			}
			
		}
		return Collections.emptySet();
	}


}
