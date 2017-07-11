package io.konig.schemagen.gcp;

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

import io.konig.core.OwlReasoner;
import io.konig.schemagen.SchemaGeneratorException;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapeNamer;

public class SpannerTableGenerator {
	private ShapeManager shapeManager;
	private SpannerDatatypeMapper datatypeMap = new SpannerDatatypeMapper();
	private OwlReasoner owl;
	private ShapeNamer shapeNamer;
	private TableMapper tableMapper;
	
	
	public SpannerTableGenerator(ShapeManager shapeManager) {
		this.shapeManager = shapeManager;
	}
	
	public SpannerTableGenerator() {
	}

	public SpannerTableGenerator(ShapeManager shapeManager, ShapeNamer shapeNamer, OwlReasoner reasoner) {
		this.shapeManager = shapeManager;
		this.shapeNamer = shapeNamer;
		owl = reasoner;
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

	public TableMapper getTableMapper() {
		return tableMapper;
	}


	public SpannerTableGenerator setTableMapper(TableMapper tableMapper) {
		this.tableMapper = tableMapper;
		return this;
	}

	public void toTableSchema(SpannerTable table) {
		Shape shape = table.getTableShape();
		if (shape == null) {
			throw new SchemaGeneratorException("Shape is not defined");
		}
		
		List<PropertyConstraint> plist = shape.getProperty();
		
		
		for (PropertyConstraint p : plist) {
			toField(p, table);
		}
		
		Integer primaryKeyCount = table.getPrimaryKeyCount();
		if (primaryKeyCount == 0) {
			throw new SchemaGeneratorException("Primary Key is a must for Spanner Tables");
		}
	}
	

	private void toField(PropertyConstraint p, SpannerTable table) {
		
		String fieldName = p.getPredicate().getLocalName();
		FieldMode fieldMode = fieldMode(p);
		SpannerDatatype fieldType = datatypeMap.type(p);
		Integer fieldLength = p.getMaxLength();
		
		SpannerTable.Field field = table.new Field(fieldName, fieldMode, fieldType, fieldLength);
		if (p.getNodeKind() == NodeKind.IRI) {
			field.setIsPrimaryKey(true);
		}
		table.addField(field);
		
	}

	
	private FieldMode fieldMode(PropertyConstraint p) {
		Integer minCount = p.getMinCount();
		Integer maxCount = p.getMaxCount();
		
		if (maxCount==null || maxCount>1) {
			throw new SchemaGeneratorException("Unsupported construct");
		}
		if (minCount!=null && maxCount!=null && minCount==1 && maxCount==1) {
			return FieldMode.REQUIRED;
		}
		return FieldMode.NULLABLE;
	}
	
}
