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


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.datasource.DataSource;
import io.konig.shacl.Shape;

public class DestinationTypeTargetNodeShapeFactory implements ShowlTargetNodeShapeFactory {

	private Set<URI> datasourceType;
	private ShowlNodeShapeBuilder builder;

	public DestinationTypeTargetNodeShapeFactory(Set<URI> datasourceType, ShowlNodeShapeBuilder builder) {
		this.datasourceType = datasourceType;
		this.builder = builder;
	}

	@Override
	public List<ShowlNodeShape> createTargetNodeShapes(Shape shape) throws ShowlProcessingException {
		List<ShowlNodeShape> result = null;
		
		outerLoop : 
		for (DataSource ds : shape.getShapeDataSource()) {
			
			for (URI dsType : ds.getType()) {
				if (datasourceType.contains(dsType) && !builder.isEnumClass(shape.getTargetClass())) {

					ShowlNodeShape targetNode = builder.buildNodeShape(null, shape);
					targetNode.setShapeDataSource(new ShowlDataSource(targetNode, ds));
					if (result == null) {
						result = new ArrayList<>();
					}
					result.add(targetNode);
					continue outerLoop;
				}
			}
		}
		
		return result == null ? Collections.emptyList() : result;
	}

}
