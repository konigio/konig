package io.konig.core.showl;

import org.openrdf.model.URI;

import io.konig.core.Graph;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.shacl.Shape;

public class ConsumesDataFromFilter implements ShowlMappingFilter {
	
	private Graph graph;
	

	public ConsumesDataFromFilter(Graph graph) {
		this.graph = graph;
	}


	@Override
	public boolean allowMapping(ShowlNodeShape source, ShowlNodeShape target) {
		
		Shape sourceShape = source.getShape();
		Shape targetShape = target.getShape();
		if (sourceShape!=null && targetShape!=null) {
			for (DataSource targetChannel : targetShape.getShapeDataSource()) {
				for (URI targetSystem : targetChannel.getIsPartOf()) {
					if (graph.getVertex(targetSystem) != null) {
						for (DataSource sourceChannel : sourceShape.getShapeDataSource()) {
							for (URI sourceSystem : sourceChannel.getIsPartOf()) {
								if (graph.contains(targetSystem, Konig.consumesDataFrom, sourceSystem)) {
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

}
