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
import java.util.ArrayList;
import java.util.List;

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
	
	public SqlTableGenerator(SqlDatatypeMapper datatypeMapper) {
		nameFactory = new SqlTableNameFactory();
		this.datatypeMapper = datatypeMapper;
	}
	
	public SqlTable generateTable(Shape shape) throws SchemaGeneratorException {
		
		Shape shapeObj = new Shape();
		String tableName = nameFactory.getTableName(shape);
		SqlTable table = new SqlTable(tableName);
		boolean reqflag = false;
		if(shape.getOr()!=null){
			reqflag = true;
			List<Shape> shapeList = shape.getOr().getShapes();
			List<PropertyConstraint> propertyConstraintList = new ArrayList<PropertyConstraint>();
			for(int i =0; i<shapeList.size(); i++){
				for (PropertyConstraint p : shapeList.get(i).getProperty()) {
					if(propertyConstraintList.size()>0)
					{
						int count =0;
						for(int j = 0; j<propertyConstraintList.size(); j++){
							
							if((propertyConstraintList.get(j).getPredicate().toString().equals(p.getPredicate().toString()))){
								 count++;
							}
						}
						if(count==0){
							 p.setRequired(false);
							propertyConstraintList.add(p);
						}
						
					}else{
					propertyConstraintList .add(p);
					}
				}
					}
			shapeObj.setProperty(propertyConstraintList);

		}else{
			shapeObj = shape;
		}
		for (PropertyConstraint p : shapeObj.getProperty()) {
			SqlColumn column = column(shapeObj, p, reqflag);
			if (column != null) {
				table.addColumn(column);
			}
		}
		return table;
	}


	private SqlColumn column(Shape shape, PropertyConstraint p, boolean reqflag) {
		
		URI predicate = p.getPredicate();
		if (predicate != null) {
			if (p.getShape() != null) {
				String message = MessageFormat.format("Nested shape is not allowed in property ''{0}'' in {1}", 
						predicate.getLocalName(), shape.getId().stringValue());
				throw new SchemaGeneratorException(message);
			}
			FacetedSqlDatatype datatype = datatypeMapper.type(p);
			Integer minCount = p.getMinCount();
			boolean nullable = ( minCount!=null && minCount>0) ? false : true;
				if(p.isRequired() && reqflag){
					nullable = false;
				}else if(!p.isRequired() && reqflag) {
					nullable = true;
				}
				
			// TODO: specify the key type
			return new SqlColumn(predicate.getLocalName(), datatype, getKeyType(p), nullable);
		}
		return null;
	}
	
	private SqlKeyType getKeyType(PropertyConstraint p) throws SchemaGeneratorException {
		SqlKeyType keyType = null;

		if (p.getStereotype() != null) {

			switch (p.getStereotype().getLocalName()) {
			case "primaryKey":
				keyType = SqlKeyType.PRIMARY_KEY;
				break;
			case "uniqueKey":
				keyType = SqlKeyType.UNIQUE_KEY;
				break;
			default:
				throw new SchemaGeneratorException("Invalid Key Type: " + p.getStereotype().getLocalName());
			}
		}

		return keyType;
	}
}
