package io.konig.spreadsheet;

import java.util.List;

import io.konig.core.impl.RdfUtil;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class BuildDataSourceAction implements Action {
	
	private WorkbookLocation location;
	private WorkbookProcessor processor;
	private DataSourceGenerator generator;
	private Shape shape;
	private ShapeManager shapeManager;
	private List<Function> functionList;

	

	public BuildDataSourceAction(WorkbookLocation location, WorkbookProcessor processor,
			DataSourceGenerator generator, Shape shape, ShapeManager shapeManager, List<Function> functionList) {
		this.location = location;
		this.processor = processor;
		this.generator = generator;
		this.shape = shape;
		this.shapeManager = shapeManager;
		this.functionList = functionList;
	}



	@Override
	public void execute() throws SpreadsheetException {

		for (Function function : functionList) {
			try {
				generator.generate(shape, function, shapeManager);
			} catch (Throwable oops) {
				
				processor.fail(oops, location, "Failed to generate Datasource for {0}", 
						RdfUtil.compactName(processor.getGraph().getNamespaceManager(), shape.getId()));
			}
		}

	}

}
