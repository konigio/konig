package io.konig.schemagen.gcp;

import java.util.List;

import com.google.api.services.bigquery.model.TableReference;
import com.google.api.services.bigquery.model.TableSchema;

import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.GoogleSpannerTable;
import io.konig.gcp.datasource.SpannerTableReference;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeVisitor;

public class ShapeToSpannerTransformer implements ShapeVisitor {
	
	private SpannerTableGenerator tableGenerator;
	private SpannerTableVisitor tableVisitor;
	
	public ShapeToSpannerTransformer(SpannerTableGenerator tableGenerator, SpannerTableVisitor tableVisitor) {
		this.tableGenerator = tableGenerator;
		this.tableVisitor = tableVisitor;
	}

	@Override
	public void visit(Shape shape) {
		
		List<DataSource> list = shape.getShapeDataSource();
		if (list != null) {
			for (DataSource dataSource : list) {
				if (dataSource instanceof GoogleSpannerTable) {
					SpannerTable table = toTable(shape, (GoogleSpannerTable) dataSource);
					tableVisitor.visit(table);
				}
			}
		}

	}
	
	private SpannerTable toTable(Shape shape, GoogleSpannerTable dataSource) {

		SpannerTable table = new SpannerTable();
		SpannerTableReference ref = dataSource.getTableReference();
		
		table.setTableReference(ref);
		table.setTableShape(shape);
		
		tableGenerator.toTableSchema(table);
		
		return table;
	}
}
