package io.konig.schemagen.gcp;

import java.util.ArrayList;

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


import java.util.List;

import org.openrdf.model.URI;

import io.konig.core.OwlReasoner;
import io.konig.core.vocab.Konig;
import io.konig.schemagen.SchemaGeneratorException;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapeNamer;

public class SpannerTableGenerator {
	private ShapeManager shapeManager;
	private SpannerDatatypeMapper datatypeMap = new SpannerDatatypeMapper();
	private ShapeNamer shapeNamer;
	private List<SpannerTable> spannerTables = new ArrayList<SpannerTable>();
	
	
	public SpannerTableGenerator(ShapeManager shapeManager) {
		this.shapeManager = shapeManager;
	}
	
	public SpannerTableGenerator() {
	}

	public SpannerTableGenerator(ShapeManager shapeManager, ShapeNamer shapeNamer, OwlReasoner reasoner) {
		this.shapeManager = shapeManager;
		this.shapeNamer = shapeNamer;
	}
	
	public ShapeManager getShapeManager() {
		return shapeManager;
	}

	public SpannerTableGenerator setShapeManager(ShapeManager shapeManager) {
		this.shapeManager = shapeManager;
		return this;
	}

	public ShapeNamer getShapeNamer() {
		return shapeNamer;
	}

	public SpannerTableGenerator setShapeNamer(ShapeNamer shapeNamer) {
		this.shapeNamer = shapeNamer;
		return this;
	}

	public void toTableSchema(SpannerTable table) {
		Shape shape = table.getTableShape();
		if (shape == null) {
			throw new SchemaGeneratorException("Shape is not defined");
		}
		
		spannerTables.add(table);
		
		List<PropertyConstraint> plist = shape.getProperty();
		
		
		for (PropertyConstraint propertyConstraint : plist) {
			toField(propertyConstraint, table);
		}
		
		Integer primaryKeyCount = table.getPrimaryKeyCount();
		if (primaryKeyCount == 0) {
			createPrimaryKeyField(table);
		}
	}
	

	private void createPrimaryKeyField(SpannerTable table) {
		String fieldName = Konig.UUID.getLocalName();
		FieldMode fieldMode = FieldMode.REQUIRED;
		SpannerDatatype fieldType = SpannerDatatype.STRING;
		Integer fieldLength = 36;
		
		SpannerTable.Field field = table.new Field(fieldName, fieldMode, fieldType, fieldLength);
		field.setIsPrimaryKey(true);
		table.addField(field);
	}

	private void toField(PropertyConstraint propertyConstraint, SpannerTable table) {
		
		SpannerTable.Field field = null;
		
		if ( (propertyConstraint.getPredicate() == null) && (propertyConstraint.getShape() != null) ) {
			
			SpannerTable.Field parentPrimaryKey = toInterleaveParent(propertyConstraint, table);
			field = table.new Field(parentPrimaryKey);
			
		} else {
		
			String fieldName = propertyConstraint.getPredicate().getLocalName();
			FieldMode fieldMode = fieldMode(propertyConstraint);
			SpannerDatatype fieldType = datatypeMap.type(propertyConstraint);
			Integer fieldLength = propertyConstraint.getMaxLength();
			
			field = table.new Field(fieldName, fieldMode, fieldType, fieldLength);
			if (isStereoTypePrimary(propertyConstraint) == true) field.setIsPrimaryKey(true);
		}
		
		table.addField(field);
		
	}

	private Boolean isStereoTypePrimary(PropertyConstraint propertyConstraint) {
		Boolean ret = false;
		if ( (propertyConstraint.getStereotype() != null) 
				&& (propertyConstraint.getStereotype().getLocalName().compareTo(Konig.primaryKey.getLocalName()) == 0) ) {
			
			ret = true;
		}
		return ret;
	}

	
	private SpannerTable.Field toInterleaveParent(PropertyConstraint propertyConstraint, SpannerTable table) {
		SpannerTable.Field parentPrimaryField = null;
			
		SpannerTable parentTable = findParentTable(propertyConstraint);
		
		for (SpannerTable.Field parentField : parentTable.getFields()) {
			if (parentField.getIsPrimaryKey() == true) {
				parentPrimaryField = parentField;
				
				table.setInterleaveParentTable(parentTable.getTableName());
				break;
			}
		}
		
		return parentPrimaryField;
	}

	private SpannerTable findParentTable(PropertyConstraint propertyConstraint) {
		
		String interleaveParentName = propertyConstraint.getShape().getTargetClass().getLocalName();
		
		SpannerTable parentSpannerTable = null;
		
		for (SpannerTable spannerTable : spannerTables) {
			if (spannerTable.getTableName().compareTo(interleaveParentName) == 0) {
				parentSpannerTable = spannerTable;
				break;	
			}
		}
		
		if (parentSpannerTable == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("Improper interleaving parent shape ").append(interleaveParentName);
			throw new SchemaGeneratorException(sb.toString());
		}
		
		return parentSpannerTable;
	}

	private FieldMode fieldMode(PropertyConstraint p) {
		Integer minCount = p.getMinCount();
		Integer maxCount = p.getMaxCount();
		
		if (minCount!=null && maxCount!=null && minCount==1 && maxCount==1) {
			return FieldMode.REQUIRED;
		}
		return FieldMode.NULLABLE;
	}
	
}
