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

import org.openrdf.model.URI;

import io.konig.datasource.DataSource;
import io.konig.shacl.Shape;

public class HasDataSourceTypeSelector implements ShowlTargetNodeSelector {

	private URI dataSourceType;

	public HasDataSourceTypeSelector(URI dataSourceType) {
		this.dataSourceType = dataSourceType;
	}

	@Override
	public List<ShowlNodeShape> produceTargetNodes(ShowlService factory, Shape shape) throws ShowlProcessingException {
		
		for (DataSource ds : shape.getShapeDataSource()) {
			if (ds.isA(dataSourceType)) {
				List<ShowlNodeShape> list = new ArrayList<>();
				list.add(factory.createNodeShape(shape, ds));
				return list;
			}
		}
		return Collections.emptyList();
	}

}
