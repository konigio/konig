package io.konig.core.showl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class GoogleStorageBucketSourceNodeSelector implements ShowlSourceNodeSelector {

	private ShapeManager shapeManager;
	

	public GoogleStorageBucketSourceNodeSelector(ShapeManager shapeManager) {
		this.shapeManager = shapeManager;
	}


	@Override
	public Set<ShowlNodeShape> selectCandidateSources(ShowlFactory factory, ShowlNodeShape targetShape) {
		
		Set<ShowlNodeShape> result = new HashSet<>();
		
		for (DataSource ds : targetShape.getShape().getShapeDataSource()) {
			
			if (ds.getType().contains(Konig.GoogleCloudStorageBucket)) {
				// Self-mapping
				addSourceShape(result, factory, targetShape.getShape(), ds);
				return result;
			}
		}
		
		URI owlClass = targetShape.getOwlClass().getId();
		if (owlClass != null) {
			List<Shape> candidates = shapeManager.getShapesByTargetClass(owlClass);
			for (Shape shape : candidates) {
				DataSource ds = shape.findDataSourceByType(Konig.GoogleCloudStorageBucket);
				if (ds != null) {
					addSourceShape(result, factory, shape, ds);
				}
			}
		}
		
		return result;
	}


	private void addSourceShape(Set<ShowlNodeShape> result, ShowlFactory factory, Shape shape, DataSource ds) {

		ShowlNodeShape sourceShape = factory.createNodeShape(shape);
		sourceShape.setShapeDataSource(new ShowlDataSource(sourceShape, ds));
		result.add(sourceShape);
		
	}

}
