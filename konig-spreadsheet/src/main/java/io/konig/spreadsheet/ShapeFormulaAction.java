package io.konig.spreadsheet;

/*
 * #%L
 * Konig Spreadsheet
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


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
	
	public void addShapeFormulaBuilder(Shape shape, boolean withShapeNames, ShapeFormulaBuilder builder) {
		Resource shapeId = shape.getId();
		ShapeFormulaBuilderInvoker invoker = map.get(shapeId);
		if (invoker == null) {
			invoker = new ShapeFormulaBuilderInvoker(shape, withShapeNames);
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
		private boolean withShapeNames=true;
		private List<ShapeFormulaBuilder> list = new ArrayList<>();
		
		ShapeFormulaBuilderInvoker(Shape shape, boolean withShapeNames) {
			this.shape = shape;
			this.withShapeNames = withShapeNames;
		}
		
		private LocalNameService nameService(SimpleLocalNameService global) {
			
			if (!withShapeNames) {
				return global;
			}
			
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
