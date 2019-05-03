package io.konig.lineage;

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

import org.openrdf.rio.RDFHandlerException;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.io.Emitter;

public class LineageEmitter implements Emitter {

	private File outFile;
	
	

	public LineageEmitter(File outFile) {
		this.outFile = outFile;
	}


	@Override
	public void emit(Graph graph) throws IOException, KonigException {
		outFile.getParentFile().mkdirs();
		
		try (FileWriter out = new FileWriter(outFile)) {
			LineageWriter writer = new LineageWriter();
			writer.writeDatasourceProperties(out, graph.getNamespaceManager());
		} catch (RDFHandlerException e) {
			throw new KonigException("Failed to write file: " + outFile.getName(), e);
		}

	}

}
