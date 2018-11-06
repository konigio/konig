package io.konig.schemagen.io;

/*
 * #%L
 * Konig Schema Generator
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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


import java.io.File;
import java.io.IOException;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandlerException;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.io.Emitter;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.io.ShapeFileGetter;
import io.konig.shacl.io.ShapeWriter;

public class ShapeToFileEmitter implements Emitter {

	private ShapeManager shapeManager;
	private File outDir;

	public ShapeToFileEmitter(ShapeManager shapeManager, File outDir) {
		this.shapeManager = shapeManager;
		this.outDir = outDir;
	}




	@Override
	public void emit(Graph graph) throws IOException, KonigException {
		if (!outDir.exists()) {
			outDir.mkdirs();
		}
		NamespaceManager nsManager = graph.getNamespaceManager();
		ShapeFileGetter fileGetter = new ShapeFileGetter(outDir, nsManager);
		ShapeWriter shapeWriter = new ShapeWriter();
		for (Shape shape : shapeManager.listShapes()) {
			Resource shapeId = shape.getId();
			if (shapeId instanceof URI) {
				URI shapeURI = (URI) shapeId;

				Graph shapeGraph = new MemoryGraph(graph.getNamespaceManager());
				shapeWriter.emitShape(shape, shapeGraph);
				File shapeFile = fileGetter.getFile(shapeURI);
				try {
					RdfUtil.prettyPrintTurtle(nsManager, shapeGraph, shapeFile);
				} catch (RDFHandlerException e) {
					throw new KonigException("Failed to save Shape: " + shapeId, e);
				}
			}

		}

	}

}
