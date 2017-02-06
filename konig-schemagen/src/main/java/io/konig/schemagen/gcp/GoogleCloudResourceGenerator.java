package io.konig.schemagen.gcp;

import java.io.File;
import java.util.List;

import io.konig.core.KonigException;
import io.konig.shacl.Shape;

public class GoogleCloudResourceGenerator {

	public void generateBigQueryTables(List<Shape> shapeList, File bigQuerySchemaDir) throws KonigException {
		
		
		BigQueryTableWriter tableWriter = new BigQueryTableWriter(bigQuerySchemaDir);
		BigQueryTableGenerator tableGenerator = new BigQueryTableGenerator();
		
		ShapeToBigQueryTransformer transformer = new ShapeToBigQueryTransformer(tableGenerator, tableWriter);
		
		for (Shape shape : shapeList) {
			transformer.visit(shape);
		}
	}

}
