package io.konig.schemagen.gcp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import io.konig.core.OwlReasoner;
import io.konig.gcp.datasource.SpannerTableReference;
import io.konig.schemagen.SchemaGeneratorException;
import io.konig.schemagen.merge.ShapeAggregator;
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
		
	}
	

	private void toField(PropertyConstraint p, SpannerTable table) {
		
		String fieldName = p.getPredicate().getLocalName();
		FieldMode fieldMode = fieldMode(p);
		SpannerDatatype fieldType = datatypeMap.type(p);
		
		SpannerTable.Field field = table.new Field(fieldName, fieldMode, fieldType);
		table.addField(field);
		
		// TBD: To check and add additional info for array type nodes
	}

	
	private FieldMode fieldMode(PropertyConstraint p) {
		Integer minCount = p.getMinCount();
		Integer maxCount = p.getMaxCount();
		
		if (maxCount==null || maxCount>1) {
			return FieldMode.REPEATED;
		}
		if (minCount!=null && maxCount!=null && minCount==1 && maxCount==1) {
			return FieldMode.REQUIRED;
		}
		return FieldMode.NULLABLE;
	}
	
}
