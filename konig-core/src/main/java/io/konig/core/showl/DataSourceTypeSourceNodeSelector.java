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
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.datasource.DataSource;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class DataSourceTypeSourceNodeSelector implements ShowlSourceNodeSelector {

	private ShapeManager shapeManager;
	private URI originDataSource;
	


	public DataSourceTypeSourceNodeSelector(ShapeManager shapeManager, URI originDataSource) {
		this.shapeManager = shapeManager;
		this.originDataSource = originDataSource;
	}


	@Override
	public Set<ShowlNodeShape> selectCandidateSources(ShowlService factory, ShowlNodeShape targetShape) {
		
		Set<ShowlNodeShape> result = new HashSet<>();
		
		for (DataSource ds : targetShape.getShape().getShapeDataSource()) {
			
			if (ds.getType().contains(originDataSource)) {
				// Self-mapping
				addSourceShape(result, factory, targetShape.getShape(), ds);
				return result;
			}
		}
		
		URI owlClass = targetShape.getOwlClass().getId();
		if (owlClass != null) {
			List<Shape> candidates = shapeManager.getShapesByTargetClass(owlClass);
			for (Shape shape : candidates) {
				DataSource ds = shape.findDataSourceByType(originDataSource);
				if (ds != null) {
					addSourceShape(result, factory, shape, ds);
				}
			}
		}
		
		return result;
	}


	private void addSourceShape(Set<ShowlNodeShape> result, ShowlService factory, Shape shape, DataSource ds) {

		ShowlNodeShape sourceShape = factory.createNodeShape(shape);
		sourceShape.setShapeDataSource(new ShowlDataSource(sourceShape, ds));
		result.add(sourceShape);
		
	}

}
