package io.konig.transform.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.BigQueryTableReference;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.gcp.datasource.GoogleCloudStorageBucket;
import io.konig.shacl.Shape;

public class BigQueryTransformStrategy extends AbstractTransformStrategy {

	@Override
	public List<SourceShape> findCandidateSourceShapes(TargetShape target) throws TransformBuildException {

		List<SourceShape> result = new ArrayList<>();
		URI targetClass = target.getShape().getTargetClass();
		if (targetClass != null) {
			List<Shape> list = factory.getShapeManager().getShapesByTargetClass(targetClass);
			for (Shape shape : list) {
				DataSource datasource = findDatasource(shape);
				if (datasource != null) {
					SourceShape source = SourceShape.create(shape);
					source.setDataSource(datasource);
					target.match(source);
					result.add(source);
				}
			}
		}
		return result;
	}
	
	private DataSource findDatasource(Shape shape) {
		List<DataSource> list = shape.getShapeDataSource();
		if (list != null) {
			GoogleBigQueryTable bigQueryTable = null;
			GoogleCloudStorageBucket bucket = null;
			for (DataSource source : list) {
				if (source instanceof GoogleBigQueryTable) {
					bigQueryTable = (GoogleBigQueryTable) source;
				} else if (source instanceof GoogleCloudStorageBucket) {
					bucket = (GoogleCloudStorageBucket) source;
				}
			}
			
			if (bucket !=null && bigQueryTable != null) {
				return bigQueryTable;
			}
			
		}
		return null;
	}

}
