package io.konig.sql;

/*
 * #%L
 * Konig SQL
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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


import org.openrdf.model.URI;

import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class SQLTableShapeGenerator {
	
	private SQLDatatypeMapper datatypeMapper;
	private SQLNamer tableNamer;
	
	
	
	public SQLTableShapeGenerator(SQLDatatypeMapper datatypeMapper, SQLNamer tableNamer) {
		this.datatypeMapper = datatypeMapper;
		this.tableNamer = tableNamer;
	}


	public SQLTableShapeGenerator(SQLNamer tableNamer) {
		this.tableNamer = tableNamer;
	}

	public Shape toShape(SQLTableSchema tableSchema) {
		
		if (datatypeMapper == null) {
			datatypeMapper = new XsdSQLDatatypeMapper();
		}
		
		URI tableId = tableNamer.tableId(tableSchema);
		Shape shape = new Shape(tableId);
		
		for (SQLColumnSchema column : tableSchema.listColumns()) {
			addProperty(shape, column);
		}
		
		if (tableSchema.getPrimaryKeyConstraint() != null) {
			shape.setNodeKind(NodeKind.IRI);
		}
		
		
		return shape;
	}
	



	private void addProperty(Shape shape, SQLColumnSchema column) {
		
		URI predicate = tableNamer.rdfPredicate(column);
		URI datatype = datatypeMapper.rdfDatatype(column.getColumnType().getDatatype());
		
		PropertyConstraint p = new PropertyConstraint(predicate);
		p.setDatatype(datatype);
		
		int minCount = column.isNotNull() ? 1 : 0;
		p.setMinCount(minCount);
		p.setMaxCount(1);
		
		shape.add(p);
		
		
	}

}
