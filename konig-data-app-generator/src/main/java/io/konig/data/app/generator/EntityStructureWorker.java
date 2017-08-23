package io.konig.data.app.generator;

/*
 * #%L
 * Konig Data App Generator
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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.dao.core.DaoConstants;
import io.konig.dao.core.DaoException;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.sql.runtime.EntityStructure;

public class EntityStructureWorker {

	private NamespaceManager nsManager;
	private ShapeManager shapeManager;
	private File baseDir;
	
	public EntityStructureWorker(NamespaceManager nsManager, ShapeManager shapeManager, File baseDir) {
		this.nsManager = nsManager;
		this.shapeManager = shapeManager;
		this.baseDir = baseDir;
	}
	
	public void run() throws KonigException, IOException {
		EntityStructureGenerator generator = new EntityStructureGenerator();
		EntityStructureWriter writer = new EntityStructureWriter(nsManager, baseDir);

		baseDir.mkdirs();
		
		Map<URI,List<Shape>> classMap = new HashMap<>();
		
	
		File mediaTypeMapFile = new File(baseDir, DaoConstants.MEDIA_TYPE_MAP_FILE_NAME);

		try (
			FileWriter mediaTypeMapWriter = new FileWriter(mediaTypeMapFile);
		) {
		
			MemoryNamespaceManager namespaceManager = new MemoryNamespaceManager();
			for (Shape shape : shapeManager.listShapes()) {
				Resource resource = shape.getId();
				if (resource instanceof URI) {
					URI shapeId = (URI) resource;
					
					EntityStructure e = generator.toEntityStructure(shape);
					if (e != null) {
						writer.write(e, shapeId);
	
						Namespace ns = nsManager.findByName(shapeId.getNamespace());
						namespaceManager.add(ns);
						
						String mediaType = shape.getMediaTypeBaseName();
						if (mediaType != null) {
							mediaTypeMapWriter.write(mediaType);
							mediaTypeMapWriter.write(",");
							mediaTypeMapWriter.write(shapeId.stringValue());
							mediaTypeMapWriter.write("\n");
						}
						
						URI targetClass = shape.getTargetClass();
						if (targetClass != null) {
							List<Shape> list = classMap.get(targetClass);
							if (list == null) {
								list = new ArrayList<>();
								classMap.put(targetClass, list);
							}
							list.add(shape);
						}
						
					}
				}
				writeClassMap(classMap);
				writeNamespaces(namespaceManager);
				
			}
		} 
		
	}

	

	private void writeClassMap(Map<URI, List<Shape>> classMap) throws KonigException {
		if (!classMap.isEmpty()) {

			File mediaTypeMapFile = new File(baseDir, DaoConstants.OWL_CLASS_MAP_FILE_NAME);
			
			try (
				FileWriter fileWriter = new FileWriter(mediaTypeMapFile);
				PrintWriter writer = new PrintWriter(fileWriter);
			) {
			
				for (Entry<URI, List<Shape>> entry : classMap.entrySet()) {
					URI owlClass = entry.getKey();
					List<Shape> list = entry.getValue();
					Shape defaultShape = null;
					if (list.size() == 1) {
						defaultShape = list.get(0);
					} else {
						// TODO: Handle case where there is more than one shape for the given OWL class.
					}
					if (defaultShape != null) {
						writer.print(owlClass.getLocalName().toLowerCase());
						writer.print(',');
						writer.println(defaultShape.getId().stringValue());
					}
				}
			} catch (IOException e) {
				throw new KonigException(e);
			}
		}
		
	}

	private void writeNamespaces(MemoryNamespaceManager namespaceManager) throws IOException {
		
		List<Namespace> list = new ArrayList<>(namespaceManager.listNamespaces());
		Collections.sort(list);
		
		File file = new File(baseDir, "namespaces.ttl");
		
		try (FileWriter out = new FileWriter(file)) {
			for (Namespace ns : list) {
				out.write("@prefix ");
				out.write(ns.getPrefix());
				out.write(": <");
				out.write(ns.getName());
				out.write("> .\n");
			}
		}
		
	}
	
}
