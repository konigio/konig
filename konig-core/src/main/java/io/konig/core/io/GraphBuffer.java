package io.konig.core.io;

/*
 * #%L
 * konig-core
 * %%
 * Copyright (C) 2015 Gregory McFall
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


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.fasterxml.jackson.core.JsonGenerator;

import io.konig.core.Context;
import io.konig.core.ContextManager;
import io.konig.core.Graph;


public class GraphBuffer {
	
	
	public byte[] writeGraph(Graph graph, Context context) throws IOException {
		
		BinaryGraphWriter writer = new BinaryGraphWriter();
		return writer.write(graph, context);
	}
	
	public void readGraph(byte[] data, Graph graph, ContextManager manager) throws KonigReadException {
		GraphReader reader = new GraphReader();
		reader.read(data, graph, manager);
	}
	
	public void writeJSON(byte[] data, ContextManager manager, JsonGenerator generator) throws KonigWriteException {
		
		GraphBufferJsonWriter writer = new GraphBufferJsonWriter();
		writer.write(data, manager, generator);
		
	}
	
	
	
}
