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

import org.apache.velocity.VelocityContext;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandlerException;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.io.Emitter;
import io.konig.schemagen.gcp.TurtleGenerator;

public class GenericEmitter implements Emitter {
	
	private URI owlClass;
	private File outDir;
	private VelocityContext context;

	

	public GenericEmitter(URI owlClass, File outDir, VelocityContext context) {
		this.owlClass = owlClass;
		this.outDir = outDir;
		this.context = context;
	}



	@Override
	public void emit(Graph graph) throws IOException, KonigException {
		TurtleGenerator generator = new TurtleGenerator();
		try {
			generator.generateAll(owlClass, outDir, graph, context);
		} catch (RDFHandlerException e) {
			throw new KonigException("Failed to generate all instances of " + owlClass.getLocalName());
		}

	}

}
