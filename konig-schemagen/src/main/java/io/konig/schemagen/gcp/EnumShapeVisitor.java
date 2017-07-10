package io.konig.schemagen.gcp;

/*
 * #%L
 * Konig Schema Generator
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapeVisitor;
import io.konig.shacl.io.ShapeFileGetter;
import io.konig.shacl.io.ShapeWriter;

public class EnumShapeVisitor implements ShapeVisitor {
	
	private ShapeFileGetter fileGetter;
	private ShapeManager shapeManager;
	
	public EnumShapeVisitor(ShapeFileGetter fileGetter, ShapeManager shapeManager) {
		this.fileGetter = fileGetter;
		this.shapeManager = shapeManager;
	}

	@Override
	public void visit(Shape shape)  {

		Resource shapeId = shape.getId();
		if (shapeId instanceof URI) {
			URI shapeURI = (URI) shapeId;
			shapeManager.addShape(shape);
			ShapeWriter shapeWriter = new ShapeWriter();

			Graph graph = new MemoryGraph();
			shapeWriter.emitShape(shape, graph);
			File shapeFile = fileGetter.getFile(shapeURI);
			try {
				RdfUtil.prettyPrintTurtle(fileGetter.getNamespaceManager(), graph, shapeFile);
			} catch (RDFHandlerException | IOException e) {
				throw new KonigException(e);
			}
		}
	}

}
