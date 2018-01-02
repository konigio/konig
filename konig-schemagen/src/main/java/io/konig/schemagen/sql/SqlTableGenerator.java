package io.konig.schemagen.sql;

/*
 * #%L
 * Konig Schema Generator
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


import java.text.MessageFormat;

import org.openrdf.model.URI;

import io.konig.schemagen.SchemaGeneratorException;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class SqlTableGenerator {

	private SqlTableNameFactory nameFactory;
	private SqlDatatypeMapper datatypeMapper;

	public SqlTableGenerator() {
		nameFactory = new SqlTableNameFactory();
		datatypeMapper = new SqlDatatypeMapper();
	}
	
	
	public SqlTable generateTable(Shape shape) throws SchemaGeneratorException {
		
		String tableName = nameFactory.getTableName(shape);
		SqlTable table = new SqlTable(tableName);
		for (PropertyConstraint p : shape.getProperty()) {
			SqlColumn column = column(shape, p);
			if (column != null) {
				table.addColumn(column);
			}
		}
		return table;
	}


	private SqlColumn column(Shape shape, PropertyConstraint p) {
		
		URI predicate = p.getPredicate();
		if (predicate != null) {
			if (p.getShape() != null) {
				String message = MessageFormat.format("Nested shape is not allowed in property ''{0}'' in {1}", 
						predicate.getLocalName(), shape.getId().stringValue());
				throw new SchemaGeneratorException(message);
			}
			FacetedSqlDatatype datatype = datatypeMapper.type(p);
			Integer minCount = p.getMinCount();
			boolean nullable = (minCount!=null && minCount>0) ? false : true;
			// TODO: specify the key type
			return new SqlColumn(predicate.getLocalName(), datatype, null, nullable);
		}
		return null;
	}

}
