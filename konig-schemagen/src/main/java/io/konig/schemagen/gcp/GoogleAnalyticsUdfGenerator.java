package io.konig.schemagen.gcp;

import java.io.File;
import java.util.List;

import io.konig.core.io.ShapeFileFactory;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeVisitor;

public class GoogleAnalyticsUdfGenerator implements ShapeVisitor {
	

	private ShapeFileFactory fileFactory;
	
	
	@Override
	public void visit(Shape shape) {
		// Get a description of the target BigQuery table (i.e. the destination table)
		GoogleBigQueryTable targetTable = getTargetTable(shape);
		
		if (targetTable != null) {
			
			File file = fileFactory.createFile(shape);

			// TODO: Generate the User Defined Function and persist it in the given file.
		}
		
	}


	public GoogleBigQueryTable getTargetTable(Shape shape) {

		GoogleBigQueryTable table = null;
		DataSource googleAnalytics = null;
		List<DataSource> list = shape.getShapeDataSource();
		if (list != null) {
			for (DataSource ds : list) {
				if (Konig.GoogleAnalytics.equals(ds.getId())) {
					googleAnalytics = ds;
				}
				if (ds instanceof GoogleBigQueryTable) {
					table = (GoogleBigQueryTable) ds;
				}
			}
		}
		return googleAnalytics==null ? null : table;
	}
}
