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

import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.shacl.Shape;
import io.konig.transform.model.ShapeTransformException;
import io.konig.transform.sql.SqlTransform;

public class RoutedSqlTransformVisitor implements SqlTransformVisitor, SqlTransformFilter {
	
	private Map<URI,SqlTransformConfig> map = new HashMap<>();
	
	public void put(URI dsType, SqlTransformVisitor visitor, boolean inspectProcessingInstruction) {
		map.put(dsType, new SqlTransformConfig(dsType, visitor, inspectProcessingInstruction));
	}

	@Override
	public void visit(SqlTransform transform) throws ShapeTransformException {
		
		for (URI dsType : transform.getTargetShape().getTdatasource().getDatasource().getType()) {
			SqlTransformConfig config = map.get(dsType);
			if (config != null) {
				SqlTransformVisitor visitor = config.getVisitor();
				if (visitor != null) {
					visitor.visit(transform);
				}
			}
		}
	}

	@Override
	public boolean accept(Shape shape, DataSource ds) {
		if (shape.getInputShapeOf() == null) {
			for (URI type : ds.getType()) {
				SqlTransformConfig config = map.get(type);
				if (config != null) {
					return config.isInspectProcessingInstruction() ?
						shape.getShapeProcessing().stream()
							.filter(e -> e.equals(Konig.SqlTransform))
							.findAny()
							.isPresent() :
						true;
				}
			}
		}
		return false;
	}
	
	private static class SqlTransformConfig {
		private URI datasourceType;
		private SqlTransformVisitor visitor;
		private boolean inspectProcessingInstruction;
		
		public SqlTransformConfig(URI datasourceType, SqlTransformVisitor visitor,
				boolean inspectProcessingInstruction) {
			this.datasourceType = datasourceType;
			this.visitor = visitor;
			this.inspectProcessingInstruction = inspectProcessingInstruction;
		}
		
		public URI getDatasourceType() {
			return datasourceType;
		}
		
		public SqlTransformVisitor getVisitor() {
			return visitor;
		}
		
		public boolean isInspectProcessingInstruction() {
			return inspectProcessingInstruction;
		}
		
		
	}

}
