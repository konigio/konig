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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.abbrev.AbbreviationManager;
import io.konig.abbrev.AbbreviationScheme;
import io.konig.core.vocab.Konig;
import io.konig.schemagen.SchemaGeneratorException;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class SqlTableGenerator {
	
	private static final Logger LOG = LoggerFactory.getLogger(SqlTableGenerator.class);
	private SqlTableNameFactory nameFactory;
	private SqlDatatypeMapper datatypeMapper;
	
	public SqlTableGenerator() {
		nameFactory = new SqlTableNameFactory();
		datatypeMapper = new SqlDatatypeMapper();
	}	
	public SqlTableGenerator(SqlDatatypeMapper datatypeMapper) {
		nameFactory = new SqlTableNameFactory();
		this.datatypeMapper = datatypeMapper;
	}
	
	public SqlTable generateTable(Shape shape, AbbreviationManager abbrevManager) throws SchemaGeneratorException {
		
		String tableName = nameFactory.getTableName(shape);
		SqlTable table = new SqlTable(tableName);
		if(shape.getOr()!=null){
			
			ShapeMerger merger = new ShapeMerger();
			try {
				shape = merger.merge(shape);
			} catch (ShapeMergeException e) {
				throw new SchemaGeneratorException(e);
			}
			
		}addIdColumn(shape,table);
		
		for (PropertyConstraint p : shape.getProperty()) {
			SqlColumn column = column(shape, p, abbrevManager);
			if (column != null) {
				table.addColumn(column);
			}
		}
		return table;
	}


	private void addIdColumn(Shape shape, SqlTable table) {
		SqlKeyType keyType = SqlKeyType.PRIMARY_KEY;
		if(shape.getNodeKind() == NodeKind.IRI){
			for (PropertyConstraint p : shape.getProperty()) {
				if (p.getStereotype() != null && Konig.primaryKey.equals(p.getStereotype()) ) {
						keyType = null;
				}
			}

			SqlColumn column = new SqlColumn("id", FacetedSqlDatatype.IRI, keyType, false);
			table.addColumn(column);
		}
		
	}

	private SqlColumn column(Shape shape, PropertyConstraint p, AbbreviationManager abbrev) {
		
		URI predicate = p.getPredicate();	
		SqlKeyType keyType=SqlTableGeneratorUtil.getKeyType(p);
		if(!SqlTableGeneratorUtil.isValidRdbmsShape(shape) && 
				SqlKeyType.SYNTHETIC_KEY.equals(keyType)){
			LOG.error("konig:synthicKey is applicable only for shapes with datasource GoogleCloudSqlTable or AwsAurora");
			p.setStereotype(null);
			keyType=null;
		}
		if (predicate != null) {
			if (p.getShape() != null) {
				String message = MessageFormat.format("Nested shape is not allowed in property ''{0}'' in {1}", 
						predicate.getLocalName(), shape.getId().stringValue());
				throw new SchemaGeneratorException(message);
			}
			FacetedSqlDatatype datatype = datatypeMapper.type(p);
			boolean nullable = !p.isRequiredSingleValue();	
			String columnName=predicate.getLocalName();
			if(shape.getUsesAbbreviationScheme()!=null && abbrev != null){
				AbbreviationScheme scheme = abbrev.getSchemeById(shape.getUsesAbbreviationScheme());
				if (scheme == null) {
					String msg = MessageFormat.format(
							"For shape <{0}>, abbreviation scheme not found: <{1}>", 
							shape.getId(), shape.getUsesAbbreviationScheme());
					throw new SchemaGeneratorException(msg);
				}
				columnName = scheme.abbreviate(columnName).toUpperCase();
			}
			return new SqlColumn(columnName, datatype, keyType, nullable);
		}
		return null;
	}
}
