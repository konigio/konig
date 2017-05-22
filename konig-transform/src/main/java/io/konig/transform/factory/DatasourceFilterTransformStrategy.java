package io.konig.transform.factory;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;

import io.konig.shacl.Shape;

public class DatasourceFilterTransformStrategy extends AbstractTransformStrategy {
	private List<URI> acceptableDatasourceType;

	public DatasourceFilterTransformStrategy(List<URI> acceptableDatasourceType) {
		this.acceptableDatasourceType = acceptableDatasourceType;
	}

	@Override
	public List<SourceShape> findCandidateSourceShapes(TargetShape target) throws TransformBuildException {
		List<SourceShape> result = new ArrayList<>();
		URI targetClass = target.getShape().getTargetClass();
		if (targetClass != null) {
			List<Shape> list = factory.getShapeManager().getShapesByTargetClass(targetClass);
			for (Shape shape : list) {
				if (accept(shape)) {
					SourceShape source = SourceShape.create(shape);
					target.match(source);
					result.add(source);
				}
			}
		}
		return result;
	}

	private boolean accept(Shape shape) {
		for (URI type : acceptableDatasourceType) {
			if (shape.hasDataSourceType(type)) {
				return true;
			}
		}
		return false;
	}

}
