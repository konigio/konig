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

import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class RawCubeSourceNodeSelector implements ShowlSourceNodeSelector {
	private ShapeManager shapeManager;
	

	public RawCubeSourceNodeSelector(ShapeManager shapeManager) {
		this.shapeManager = shapeManager;
	}



	@Override
	public Set<ShowlNodeShape> selectCandidateSources(ShowlFactory factory, ShowlNodeShape targetShape) {
		
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
					
					DataSource ds = candidate.findDataSourceByType(Konig.GoogleCloudStorageBucket);
					
					if (ds != null) {
						result.add(factory.createNodeShape(candidate, ds));
					}
				}
				
				return result;
			}
			
		}
		return Collections.emptySet();
	}


}
