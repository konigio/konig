package io.konig.core.showl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openrdf.model.URI;

import io.konig.datasource.DataSource;
import io.konig.shacl.Shape;

public class HasDataSourceTypeSelector implements ShowlTargetNodeSelector {

	private URI dataSourceType;

	public HasDataSourceTypeSelector(URI dataSourceType) {
		this.dataSourceType = dataSourceType;
	}

	@Override
	public List<ShowlNodeShape> produceTargetNodes(ShowlFactory factory, Shape shape) throws ShowlProcessingException {
		
		for (DataSource ds : shape.getShapeDataSource()) {
			if (ds.isA(dataSourceType)) {
				List<ShowlNodeShape> list = new ArrayList<>();
				list.add(factory.createNodeShape(shape, ds));
				return list;
			}
		}
		return Collections.emptyList();
	}

}
