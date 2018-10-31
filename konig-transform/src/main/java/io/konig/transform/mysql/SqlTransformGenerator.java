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



import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.OwlReasoner;
import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.gcp.datasource.GoogleCloudSqlTable;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.transform.model.ShapeTransformException;
import io.konig.transform.model.TNodeShape;
import io.konig.transform.model.TransformModelBuilder;
import io.konig.transform.sql.SqlTransform;
import io.konig.transform.sql.SqlTransformBuilder;

public class SqlTransformGenerator {
	private SqlTransformFilter filter;
	private SqlTransformVisitor visitor;
	
	
	public SqlTransformGenerator(SqlTransformFilter filter, SqlTransformVisitor visitor) {
		this.filter = filter;
		this.visitor = visitor;
	}

	public void buildAll(ShapeManager shapeManager, OwlReasoner reasoner) throws ShapeTransformException {
		
		TransformModelBuilder modelBuilder = new TransformModelBuilder(shapeManager, reasoner);
		SqlTransformBuilder transformBuilder = new SqlTransformBuilder();
		
		for (Shape shape : shapeManager.listShapes()) {
			List<DataSource> list = datasourceList(shape);
			for (DataSource ds : list) {
				TNodeShape model = modelBuilder.build(shape, ds);
				SqlTransform transform = new SqlTransform(model, reasoner);
				transformBuilder.build(transform);
				visitor.visit(transform);
			}
		}
	}

	private List<DataSource> datasourceList(Shape shape) {

		List<DataSource> result = null;
		if (shape.getInputShapeOf() == null) {
			for (DataSource ds : shape.getShapeDataSource()) {
				if (filter.accept(shape, ds)) {
					if (result == null) {
						result = new ArrayList<>();
					}
					result.add(ds);
				}
			}
		}
		
		
		return result == null ? Collections.emptyList() : result;
	}



}
