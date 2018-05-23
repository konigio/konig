package io.konig.schemagen;

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

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.aws.datasource.AwsShapeConfig;
import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.shacl.impl.MemoryShapeManager;

public class SchemaGeneratorTest {


	protected NamespaceManager nsManager = new MemoryNamespaceManager();
	protected Graph graph = new MemoryGraph(nsManager);
	protected MemoryShapeManager shapeManager = new MemoryShapeManager();

	

	protected URI iri(String value) {
		return new URIImpl(value);
	}

	protected void load(String path) throws RDFParseException, RDFHandlerException, IOException {

		GcpShapeConfig.init();
		AwsShapeConfig.init();
		File sourceDir = new File(path);
		RdfUtil.loadTurtle(sourceDir, graph, shapeManager);
		
	}

}
