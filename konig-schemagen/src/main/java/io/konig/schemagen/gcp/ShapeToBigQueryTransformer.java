package io.konig.schemagen.gcp;

import java.util.List;

import com.google.api.services.bigquery.model.Table;
import com.google.api.services.bigquery.model.TableReference;
import com.google.api.services.bigquery.model.TableSchema;

import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.BigQueryTableReference;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeVisitor;

/**
 * A utility that transforms a Shape into a BigQuery Table.
 * @author Greg McFall
 *
 */
public class ShapeToBigQueryTransformer implements ShapeVisitor {
	
	private BigQueryTableGenerator tableGenerator;
	private BigQueryTableVisitor tableVisitor;
	
	public ShapeToBigQueryTransformer(BigQueryTableGenerator tableGenerator, BigQueryTableVisitor tableVisitor) {
		this.tableGenerator = tableGenerator;
		this.tableVisitor = tableVisitor;
	}

	@Override
	public void visit(Shape shape) {
		
		List<DataSource> list = shape.getShapeDataSource();
		if (list != null) {
			for (DataSource dataSource : list) {
				if (dataSource instanceof GoogleBigQueryTable) {
					Table table = toTable(shape, (GoogleBigQueryTable) dataSource);
					tableVisitor.visit(table);
				}
			}
		}

	}

	private Table toTable(Shape shape, GoogleBigQueryTable dataSource) {

		Table table = new Table();
		BigQueryTableReference ref = dataSource.getTableReference();
		
		TableReference reference = new TableReference();
		reference.setProjectId(ref.getProjectId());
		reference.setDatasetId(ref.getDatasetId());
		reference.setTableId(ref.getTableId());
		
		table.setTableReference(reference);
		
		TableSchema tableSchema = tableGenerator.toTableSchema(shape);
		table.setSchema(tableSchema);
		table.setExternalDataConfiguration(dataSource.getExternalDataConfiguration());
		
		return table;
		
	}

}
