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

import org.openrdf.model.Namespace;

import io.konig.cadl.Cube;
import io.konig.cadl.CubeShapeBuilder;
import io.konig.cadl.CubeShapeException;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class BuildRawCubeShapeAction implements Action {

	private WorkbookLocation location;
	private WorkbookProcessor processor;
	private DataSourceGenerator dsGenerator;
	private Cube cube;
	private List<Function> functionList;
	

	public BuildRawCubeShapeAction(WorkbookLocation location, WorkbookProcessor processor,
			DataSourceGenerator dsGenerator, Cube cube, List<Function> functionList) {
		this.location = location;
		this.processor = processor;
		this.dsGenerator = dsGenerator;
		this.cube = cube;
		this.functionList = functionList;
	}


	@Override
	public void execute() throws SpreadsheetException {
		
		CubeShapeBuilder builder = cubeShapeBuilder();

		NamespaceManager nsManager = processor.getGraph().getNamespaceManager();
		try {
			Shape nodeShape = builder.buildShape(cube);
			
			ShapeManager shapeManager = processor.service(ShapeManager.class);
			
			for (Function function : functionList) {
				try {
					dsGenerator.generate(nodeShape, function, shapeManager);
				} catch (Throwable oops) {
					
					processor.fail(oops, location, "Failed to generate Datasource for {0}", 
							RdfUtil.compactName(nsManager, nodeShape.getId()));
				}
			}
			
		} catch (CubeShapeException e) {
			processor.fail(e, location, "Failed to build NodeShape for raw data of {0}", 
					RdfUtil.compactName( nsManager, cube.getId()));
		}
	}


	private CubeShapeBuilder cubeShapeBuilder() throws SpreadsheetException {
		CubeShapeBuilder builder = processor.getServiceManager().getService(CubeShapeBuilder.class);
		if (builder == null) {
			NamespaceManager nsManager = processor.getGraph().getNamespaceManager();
			Namespace ns = nsManager.findByPrefix("shape");
			if (ns == null) {
				processor.fail(location, "Failed to generate NodeShape for raw data of {0} because the 'shape' namespace is not defined.", 
						RdfUtil.compactName(nsManager, cube.getId()));
			}
			builder = new CubeShapeBuilder(processor.getOwlReasoner(), processor.getShapeManager(), ns.getName());
			processor.getServiceManager().addService(CubeShapeBuilder.class, builder);
		}
		return builder;
	}

}
