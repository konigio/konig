package io.konig.app;

import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openrdf.model.Namespace;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.turtle.TurtleParser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import io.konig.core.Context;
import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.KonigValueFactory;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.io.CompositeRdfHandler;
import io.konig.core.io.GraphLoadHandler;
import io.konig.core.io.JsonldGraphWriter;
import io.konig.core.io.NamespaceRDFHandler;
import io.konig.core.io.impl.JsonUtil;
import io.konig.core.io.impl.JsonldGraphWriterImpl;

public class TurtleToJsonld {
	
	public void transform(File[] input, Writer output) throws KonigException, IOException {

		NamespaceManager namespaceManager = new MemoryNamespaceManager();
		Graph graph = new MemoryGraph();
		
		TurtleParser parser = new TurtleParser(new KonigValueFactory());
		
		CompositeRdfHandler handler = new CompositeRdfHandler(
			new GraphLoadHandler(graph),
			new NamespaceRDFHandler(namespaceManager)
		);
		
		parser.setRDFHandler(handler);
		for (File file : input) {

			Reader reader = new FileReader(file);
			try {
				parser.parse(reader, "");
			} catch (RDFParseException | RDFHandlerException e) {
				throw new KonigException("Failed to read file: " + file, e);
			} finally {
				close(reader);
			}
			
		}
		
		List<Namespace> list = new ArrayList<Namespace>(namespaceManager.listNamespaces());
		
		Collections.sort(list, new Comparator<Namespace>() {

			@Override
			public int compare(Namespace a, Namespace b) {
				return a.getPrefix().compareTo(b.getPrefix());
			}
		});
		
		
		Context context = JsonUtil.createContext(list);
		
		JsonFactory factory = new JsonFactory();
		JsonGenerator json = factory.createGenerator(output);
		json.useDefaultPrettyPrinter();
		JsonldGraphWriter graphWriter = new JsonldGraphWriterImpl();
		graphWriter.write(graph, context, json);
		json.flush();
	}
	
	
	public static void main(String[] args) throws IOException {
		
		TurtleToJsonld worker = new TurtleToJsonld();
		
		if (args.length < 2) {
			System.err.println("USAGE: ttl2jsonld <turtle-input-file>+ <jsonld-output-file>");
		}
		
		File[] array = new File[args.length-1];
		for (int i=0; i<args.length-1; i++) {
			array[i] = new File(args[i]);
		}
		
		File outputFile = new File(args[args.length-1]);
		outputFile.getParentFile().mkdirs();
		
			
		Writer output = new FileWriter(outputFile);
		try {
			worker.transform(array, output);
		} finally {
			close(output);
		}
	}

	private static void close(Closeable closeable) {
		try {
			closeable.close();
		} catch (Throwable ignore) {
			
		}
		
	}

}
