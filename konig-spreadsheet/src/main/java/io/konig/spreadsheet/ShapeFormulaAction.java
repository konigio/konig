package io.konig.spreadsheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Resource;

import io.konig.core.LocalNameService;
import io.konig.core.impl.CompositeLocalNameService;
import io.konig.core.impl.SimpleLocalNameService;
import io.konig.core.path.NamespaceMapAdapter;
import io.konig.formula.FormulaParser;
import io.konig.rio.turtle.NamespaceMap;
import io.konig.shacl.Shape;

public class ShapeFormulaAction implements Action {
	
	private Map<Resource,ShapeFormulaBuilderInvoker> map = new HashMap<>();
	private WorkbookProcessor processor;

	public ShapeFormulaAction(WorkbookProcessor processor) {
		this.processor = processor;
	}
	
	public void addShapeFormulaBuilder(Shape shape, ShapeFormulaBuilder builder) {
		Resource shapeId = shape.getId();
		ShapeFormulaBuilderInvoker invoker = map.get(shapeId);
		if (invoker == null) {
			invoker = new ShapeFormulaBuilderInvoker(shape);
			map.put(shapeId, invoker);
		}
		invoker.add(builder);
	}

	@Override
	public void execute() throws SpreadsheetException {
		
		SimpleLocalNameService global = processor.service(SimpleLocalNameService.class);

		NamespaceMap nsMap = new NamespaceMapAdapter(
			processor.getGraph().getNamespaceManager());
		
		for (ShapeFormulaBuilderInvoker invoker : map.values()) {
			invoker.execute(processor, nsMap, global);
		}

	}
	
	private static class ShapeFormulaBuilderInvoker  {
		private Shape shape;
		private List<ShapeFormulaBuilder> list = new ArrayList<>();
		
		ShapeFormulaBuilderInvoker(Shape shape) {
			this.shape = shape;
		}
		
		private LocalNameService nameService(SimpleLocalNameService global) {
			
			SimpleLocalNameService shapeNames = new SimpleLocalNameService();
			shapeNames.addShape(shape);

			
			return new CompositeLocalNameService(shapeNames, global);
		}
		void add(ShapeFormulaBuilder builder) {
			list.add(builder);
		}
		
		void execute(WorkbookProcessor processor, NamespaceMap nsMap, 
				SimpleLocalNameService global) throws SpreadsheetException {
			
			FormulaParser parser = formulaParser(processor, nsMap, global);
			for (ShapeFormulaBuilder builder : list) {
				builder.build(processor, shape, parser);
			}
		}

		private FormulaParser formulaParser(
				WorkbookProcessor processor, NamespaceMap nsMap, SimpleLocalNameService global) {
			
				LocalNameService nameService = nameService(global);
				return new FormulaParser(null, nameService, nsMap);
		}


		

	}

}
