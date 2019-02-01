package io.konig.shacl.filters;

import org.openrdf.model.URI;

import io.konig.datasource.DataSource;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeFilter;

public class DatasourceIsPartOfFilter implements ShapeFilter {
	private URI parentId;

	public DatasourceIsPartOfFilter(URI parentId) {
		this.parentId = parentId;
	}

	@Override
	public boolean accept(Shape shape) {
		for (DataSource ds : shape.getShapeDataSource()) {
			for (URI p : ds.getIsPartOf()) {
				if (parentId.equals(p)) {
					return true;
				}
			}
		}
		return false;
	}

}
