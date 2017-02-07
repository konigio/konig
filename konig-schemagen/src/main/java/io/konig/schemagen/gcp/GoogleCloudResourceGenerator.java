package io.konig.schemagen.gcp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.konig.core.KonigException;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeVisitor;

public class GoogleCloudResourceGenerator {
	
	
	private List<ShapeVisitor> visitors = new ArrayList<>();
	
	public void add(ShapeVisitor visitor) {
		visitors.add(visitor);
	}
	
	public void addBigQueryGenerator(File bigQuerySchemaDir) {

		BigQueryTableWriter tableWriter = new BigQueryTableWriter(bigQuerySchemaDir);
		BigQueryTableGenerator tableGenerator = new BigQueryTableGenerator();
		
		ShapeToBigQueryTransformer transformer = new ShapeToBigQueryTransformer(tableGenerator, tableWriter);
		add(transformer);
		
	}
	
	public void addCloudStorageBucketWriter(File bucketDir) {
		add(new GoogleCloudStorageBucketWriter(bucketDir));
	}

	public void dispatch(List<Shape> shapeList) throws KonigException {
		
		for (Shape shape : shapeList) {
			for (ShapeVisitor visitor : visitors) {
				visitor.visit(shape);
			}
		}
	}

}
