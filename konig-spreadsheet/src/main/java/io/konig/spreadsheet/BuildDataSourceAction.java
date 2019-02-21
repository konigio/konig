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
