package io.konig.transform.mysql;

/*
 * #%L
 * Konig Transform
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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


import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.URI;

import io.konig.datasource.DataSource;
import io.konig.shacl.Shape;
import io.konig.transform.model.ShapeTransformException;
import io.konig.transform.sql.SqlTransform;

public class RoutedSqlTransformVisitor implements SqlTransformVisitor, SqlTransformFilter {
	
	private Map<URI,SqlTransformVisitor> map = new HashMap<>();
	
	public void put(URI dsType, SqlTransformVisitor visitor) {
		map.put(dsType, visitor);
	}

	@Override
	public void visit(SqlTransform transform) throws ShapeTransformException {
		
		for (URI dsType : transform.getTargetShape().getTdatasource().getDatasource().getType()) {
			SqlTransformVisitor visitor = map.get(dsType);
			if (visitor != null) {
				visitor.visit(transform);
			}
		}
	}

	@Override
	public boolean accept(Shape shape, DataSource ds) {
		if (shape.getInputShapeOf() == null) {
			for (URI type : ds.getType()) {
				if (map.get(type) != null) {
					return true;
				}
			}
		}
		return false;
	}

}
