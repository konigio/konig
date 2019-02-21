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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.Graph;
import io.konig.core.impl.SimpleLocalNameService;
import io.konig.shacl.ShapeManager;

public class BuildLocalNameServiceAction implements Action {
	private static final Logger logger = LoggerFactory.getLogger(BuildLocalNameServiceAction.class);
	
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
		logger.debug("execute");
		
		service.addAll(graph);
		service.addShapes(shapeManager.listShapes());

	}

}
