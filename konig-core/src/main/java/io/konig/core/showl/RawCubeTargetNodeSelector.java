package io.konig.core.showl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.shacl.Shape;

public class RawCubeTargetNodeSelector implements ShowlTargetNodeSelector {


	@Override
	public List<ShowlNodeShape> produceTargetNodes(ShowlFactory factory, Shape shape) throws ShowlProcessingException {
		if (shape.getNodeShapeCube() != null) {
			List<ShowlNodeShape> list = new ArrayList<>();
			for (DataSource ds : shape.getShapeDataSource()) {
				list.add(factory.createNodeShape(shape, ds));
			}
			
			return list;
		}
		return Collections.emptyList();
	}

}
