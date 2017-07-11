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


import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;

import io.konig.activity.Activity;
import io.konig.gcp.datasource.SpannerTableReference;
import io.konig.shacl.Shape;

public class SpannerTable {

	private String tableName;
	private Shape tableShape;
	private URI tableClass;
	
	private Activity wasGeneratedBy;
	private List<Field> fields = new ArrayList<>();
	private SpannerTableReference reference; 
	private Integer primaryKeyCount = 0;
	
	class Field {

		private String fieldName;
		private FieldMode fieldMode;
		private SpannerDatatype fieldType;
		private Integer fieldLength;
		private Boolean isPrimaryKey = false;
		
		public Field(String fieldName, FieldMode fieldMode, SpannerDatatype fieldType, 
				Integer fieldLength) {
			this.setFieldName(fieldName);
			this.setFieldMode(fieldMode);
			this.setFieldType(fieldType);
			this.setFieldLength(fieldLength);
		}

		public String getFieldName() {
			return fieldName;
		}

		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}

		public FieldMode getFieldMode() {
			return fieldMode;
		}

		public void setFieldMode(FieldMode fieldMode) {
			this.fieldMode = fieldMode;
		}

		public SpannerDatatype getFieldType() {
			return fieldType;
		}

		public void setFieldType(SpannerDatatype fieldType) {
			this.fieldType = fieldType;
		}

		public Integer getFieldLength() {
			return fieldLength;
		}

		public void setFieldLength(Integer fieldLength) {
			this.fieldLength = fieldLength;
		}

		public Boolean getIsPrimaryKey() {
			return isPrimaryKey;
		}

		public void setIsPrimaryKey(Boolean isPrimaryKey) {
			this.isPrimaryKey = isPrimaryKey;
			if (isPrimaryKey == true) {
				SpannerTable.this.addToPrimaryKeyCount();
			}
		}
	}
	
	public void addField(Field field) {
		fields.add(field);
	}
	
	public List<Field> getFields() {
		return fields;
	}	

	public SpannerTableReference getTableReference() {
		return reference;
	}
	
	public void setTableReference(SpannerTableReference reference) {
		this.reference = reference;
	}

	/**
	 * Get the Shape of data recorded in the table.
	 * Either tableShape or tableClass must be defined.
	 * @return The Shape of data recorded in the table.
	 */
	public Shape getTableShape() {
		return tableShape;
	}

	public void setTableShape(Shape tableShape) {
		this.tableShape = tableShape;
	}

	/**
	 * Get the OWL class for instances recorded in the table.
	 * Either the tableClass or tableShape property must be defined.
	 * @return The OWL class for instances recorded in the table.
	 */
	public URI getTableClass() {
		return tableClass;
	}

	public void setTableClass(URI tableClass) {
		this.tableClass = tableClass;
	}

	public String getTableName() {
		return tableName;
	}


	public void setTableName(String tableName) {
		this.tableName = tableName;
	}


	public Activity getWasGeneratedBy() {
		return wasGeneratedBy;
	}

	public void setWasGeneratedBy(Activity wasGeneratedBy) {
		this.wasGeneratedBy = wasGeneratedBy;
	}

	public Integer getPrimaryKeyCount() {
		return primaryKeyCount;
	}

	public void addToPrimaryKeyCount() {
		this.primaryKeyCount++;
	}
}
