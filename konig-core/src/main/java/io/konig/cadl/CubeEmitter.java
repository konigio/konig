package io.konig.cadl;

/*
 * #%L
 * Konig Core
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


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandlerException;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.io.BaseDirectiveHandler;
import io.konig.core.io.Emitter;
import io.konig.core.io.TurtleConfig;
import io.konig.core.pojo.EmitContext;
import io.konig.core.pojo.SimplePojoEmitter;
import io.konig.core.vocab.CADL;

public class CubeEmitter implements Emitter {
	private File outDir;
	private CubeManager cubeManager;
	

	public CubeEmitter(File outDir, CubeManager cubeManager) {
		this.outDir = outDir;
		this.cubeManager = cubeManager;
	}


	@Override
	public void emit(Graph graph) throws IOException, KonigException {

		SimplePojoEmitter emitter = SimplePojoEmitter.getInstance();
		
		Map<String, CubeNamespace> map = buildMap(graph.getNamespaceManager());
		for (CubeNamespace ns : map.values()) {
			write(ns, emitter, graph.getNamespaceManager());
		}
		

	}
	

	private void write(CubeNamespace ns, SimplePojoEmitter emitter, NamespaceManager nsManager) throws IOException {
		
		Graph graph = new MemoryGraph(nsManager);

		EmitContext context = new EmitContext(graph);
		context.addIriReference(CADL.rollUpTo);
		
		for (Cube cube : ns.getCubeList()) {
			emitter.emit(cube, graph);
		}
		
		outDir.mkdirs();
		TurtleConfig config = new TurtleConfig();
		config.setBaseDirectiveHandler(new CubeBaseDirectiveHandler(cubeManager));
		File outFile = new File(outDir, ns.getNamespace().getPrefix() + ".ttl");
		try (FileWriter writer = new FileWriter(outFile)) {
			RdfUtil.prettyPrintTurtle(graph.getNamespaceManager(), null, graph, writer, config);
		} catch (RDFHandlerException e) {
			throw new KonigException(e);
		}
	}


	private Map<String, CubeNamespace> buildMap(NamespaceManager nsManager) {
		Map<String, CubeNamespace> map = new HashMap<>();
		for (Cube cube : cubeManager.listCubes()) {
			URI cubeId = cube.getId();
			String namespaceName = cubeId.getNamespace();
			Namespace namespace = nsManager.findByName(namespaceName);
			if (namespace == null) {
				throw new KonigException("Namespace prefix not found for namespace " + namespaceName);
			}
			CubeNamespace cn = map.get(namespaceName);
			if (cn == null) {
				cn = new CubeNamespace(namespace);
				map.put(namespaceName, cn);
			}
			cn.addCube(cube);
		}
		return map;
	}

	private static class CubeNamespace {
		private Namespace namespace;
		private List<Cube> cubeList = new ArrayList<>();
		
		public CubeNamespace(Namespace namespace) {
			this.namespace = namespace;
		}
		
		void addCube(Cube cube) {
			cubeList.add(cube);
		}

		public Namespace getNamespace() {
			return namespace;
		}

		public List<Cube> getCubeList() {
			return cubeList;
		}
		
	}
	
	private static class CubeBaseDirectiveHandler implements BaseDirectiveHandler {

		private CubeManager manager;
		
		public CubeBaseDirectiveHandler(CubeManager manager) {
			this.manager = manager;
		}


		@Override
		public String injectBaseDirective(URI resource) {
			return  manager.findById(resource) != null ?
				resource.stringValue() + "/" :
				null;
		}
		
	}

}
