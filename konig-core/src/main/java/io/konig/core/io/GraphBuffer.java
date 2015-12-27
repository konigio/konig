package io.konig.core.io;

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
