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
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandlerException;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.CompositeLocalNameService;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.impl.SimpleLocalNameService;
import io.konig.core.io.Emitter;
import io.konig.core.pojo.EmitContext;
import io.konig.core.pojo.SimplePojoEmitter;
import io.konig.datasource.DataSource;
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
		Set<DataSource> dataSourceList = new HashSet<>();
		for (Shape shape : shapeManager.listShapes()) {
			Resource shapeId = shape.getId();
			if (shapeId instanceof URI) {
				URI shapeURI = (URI) shapeId;

				addDataSources(dataSourceList, shape);
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
		
		emitDataSources(graph, dataSourceList);

	}


	private void emitDataSources(Graph graph, Set<DataSource> dataSourceList) {
		
		for (DataSource ds : dataSourceList) {
			String fileName = dataSourceFileName(ds);
			File file = new File(outDir, fileName);
			Graph fileGraph = new MemoryGraph(graph.getNamespaceManager());
			

			SimplePojoEmitter emitter = SimplePojoEmitter.getInstance();
			CompositeLocalNameService nameService = new CompositeLocalNameService(
				SimpleLocalNameService.getDefaultInstance(), graph);
			
			EmitContext context = new EmitContext(graph);
			context.setLocalNameService(nameService);
			emitter.emit(context, ds, fileGraph);
			
			try {
				RdfUtil.prettyPrintTurtle(graph.getNamespaceManager(),  fileGraph, file);
			} catch (Throwable oops) {
				throw new KonigException("Failed to save DataSource " + ds.getId().stringValue(), oops);
			}
			
		}
		
	}


	private String dataSourceFileName(DataSource ds) {
		String fileName = null;
		Resource id = ds.getId();
		if (id instanceof URI) {
			// Remove unsatisfied variables like ${environmentName}
			
			String idValue = id.stringValue().replaceAll("\\$\\{[^}]*}", "");
			int start = idValue.indexOf(':');
			int end = idValue.length();
			
			// Trim leading and trailing slashes
			while (++start < idValue.length() && idValue.charAt(start)=='/');
			while (--end>0 && idValue.charAt(end)=='/');
			
			// Trim trailing dashes
			while (end>0 && idValue.charAt(end)=='-') {
				end--;
			}
			
			fileName = idValue.substring(start, end+1);
			
			if (fileName.length()==0) {
				throw new KonigException("Invalid IRI: " + id.stringValue());
			}
			
			fileName = fileName + ".ttl";
			
		}
		return fileName;
	}


	private void addDataSources(Set<DataSource> dataSourceList, Shape shape) {
		for (DataSource ds : shape.getShapeDataSource()) {
			if (!ds.isEmbeddabled()) {
				dataSourceList.add(ds);
			}
		}
		
	}

}
