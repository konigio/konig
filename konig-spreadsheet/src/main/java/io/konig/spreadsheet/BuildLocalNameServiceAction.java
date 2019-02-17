package io.konig.spreadsheet;

import io.konig.core.Graph;
import io.konig.core.impl.SimpleLocalNameService;
import io.konig.shacl.ShapeManager;

public class BuildLocalNameServiceAction implements Action {
	
	private SimpleLocalNameService service;
	private Graph graph;
	private ShapeManager shapeManager;

	public BuildLocalNameServiceAction(SimpleLocalNameService service, Graph graph, ShapeManager shapeManager) {
		this.service = service;
		this.graph = graph;
		this.shapeManager = shapeManager;
	}

	@Override
	public void execute() throws SpreadsheetException {
		
		service.addAll(graph);
		service.addShapes(shapeManager.listShapes());

	}

}
