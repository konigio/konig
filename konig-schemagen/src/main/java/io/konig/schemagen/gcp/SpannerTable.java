package io.konig.schemagen.gcp;

import java.io.Serializable;
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
	
	private transient Activity wasGeneratedBy;
	private transient List<Field> fields = new ArrayList<>();
	private SpannerTableReference reference; 
	
	class Field {

		private String fieldName;
		private FieldMode fieldMode;
		private SpannerDatatype fieldType;
		
		public Field(String fieldName, FieldMode fieldMode, SpannerDatatype fieldType) {
			this.setFieldName(fieldName);
			this.setFieldMode(fieldMode);
			this.setFieldType(fieldType);
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
	
}
