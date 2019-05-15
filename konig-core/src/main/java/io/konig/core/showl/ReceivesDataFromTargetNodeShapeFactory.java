package io.konig.core.showl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.shacl.Shape;

public class ReceivesDataFromTargetNodeShapeFactory implements ShowlTargetNodeShapeFactory {

	private Set<URI> targetSystems;
	private Graph graph;
	private ShowlNodeShapeBuilder builder;

	public ReceivesDataFromTargetNodeShapeFactory(Set<URI> targetSystems, Graph graph, ShowlNodeShapeBuilder builder) {
		this.targetSystems = targetSystems;
		this.graph = graph;
		this.builder = builder;
	}

	@Override
	public List<ShowlNodeShape> createTargetNodeShapes(Shape shape) throws ShowlProcessingException {
		List<ShowlNodeShape> result = null;
		
		outerLoop : 
		for (DataSource ds : shape.getShapeDataSource()) {
			for (URI dataSourceSystem : ds.getIsPartOf()) {
				if (targetSystems.contains(dataSourceSystem)) {
					
					Vertex v = graph.getVertex(dataSourceSystem);
					if (v != null && v.getValue(Konig.receivesDataFrom)!=null) {
					
						ShowlNodeShape targetNode = builder.buildNodeShape(null, shape);
						targetNode.setShapeDataSource(new ShowlDataSource(targetNode, ds));
						if (result == null) {
							result = new ArrayList<>();
						}
						result.add(targetNode);
						continue outerLoop;
					}
				}
			}
		}
		
		return result == null ? Collections.emptyList() : result;
	}

}
