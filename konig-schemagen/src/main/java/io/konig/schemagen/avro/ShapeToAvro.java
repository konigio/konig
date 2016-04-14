package io.konig.schemagen.avro;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;

import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.turtle.TurtleParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryContextManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.io.CompositeRdfHandler;
import io.konig.core.io.GraphLoadHandler;
import io.konig.core.io.JsonldParser;
import io.konig.core.io.NamespaceRDFHandler;
import io.konig.core.io.ResourceFile;
import io.konig.core.io.ResourceManager;
import io.konig.schemagen.GraphLoadException;
import io.konig.schemagen.avro.impl.SimpleAvroNamer;

public class ShapeToAvro {
	
	private static final Logger logger = LoggerFactory.getLogger(ShapeToAvro.class);
	
	private MemoryContextManager contextManager;
	
	public ShapeToAvro(File contextDir) throws IOException {
		if (contextDir != null) {
			contextManager.loadFromFile(contextDir);
		}
	}
	
	public void generateAvro(File sourceDir, File targetDir, Graph graph) throws IOException {
		targetDir.mkdirs();
		GraphLoadHandler loadHandler = null;
		if (graph == null) {
			graph = new MemoryGraph();
			loadHandler = new GraphLoadHandler(graph);
		}
		NamespaceManager nsManager = new MemoryNamespaceManager();
		
		loadGraph(nsManager, sourceDir, graph, loadHandler);
		ResourceManager resourceManager = new FileManager(targetDir);
		SimpleAvroNamer namer = new SimpleAvroNamer();
		AvroSchemaGenerator generator = new AvroSchemaGenerator(namer, nsManager);
		
		generator.generateAll(graph, resourceManager);
	}


	private void loadGraph(NamespaceManager nsManager, File source, Graph graph, GraphLoadHandler loadHandler) throws IOException {
		
		if (source.isDirectory()) {
			File[] kids = source.listFiles();
			for (int i=0; i<kids.length; i++) {
				loadGraph(nsManager, kids[i], graph, loadHandler);
			}
		} else {
			String name = source.getName();
			if (name.endsWith(".ttl")) {
				loadTurtle(nsManager, source, graph, loadHandler);
			} else if (name.endsWith(".json") || name.endsWith(".jsonld")) {
				loadJsonld(source, graph);
			}
		}
		
	}


	private void loadTurtle(NamespaceManager nsManager, File source, Graph graph, GraphLoadHandler loadHandler) throws IOException {
		
		TurtleParser parser = new TurtleParser();
		NamespaceRDFHandler nsHandler = new NamespaceRDFHandler(nsManager);
		
		CompositeRdfHandler composite = new CompositeRdfHandler(nsHandler);
		if (loadHandler != null) {
			composite.add(loadHandler);
		}
		parser.setRDFHandler(composite);
		
		FileReader input = new FileReader(source);
		try {
			parser.parse(input, "");
		} catch (RDFParseException | RDFHandlerException e) {
			throw new GraphLoadException("Failed to load file " + source.getName(), e);
		} finally {
			close(input);
		}
	}
	
	private void close(Reader input) {
		try {
			input.close();
		} catch (IOException e) {
			logger.warn("Failed to close file reader");
		}
		
	}

	private void loadJsonld(File source, Graph graph) throws IOException {
		JsonldParser parser = new JsonldParser(contextManager);
		GraphLoadHandler handler = new GraphLoadHandler(graph);
		parser.setRDFHandler(handler);
		
		FileReader input = new FileReader(source);
		try {
			parser.parse(input, "");
		} catch (RDFParseException | RDFHandlerException e) {
			throw new GraphLoadException("Failed to load file " + source.getName(), e);
		} finally {
			close(input);
		}
		
	}

	private static class FileManager implements ResourceManager {
		private File outDir;
		

		public FileManager(File outDir) {
			this.outDir = outDir;
		}

		@Override
		public ResourceFile createResource(String location, String type, String entityBody) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void delete(String contentLocation) throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public ResourceFile get(String contentLocation) throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void put(ResourceFile file) throws IOException {
			String avroName = file.getProperty(AvroSchemaGenerator.AVRO_SCHEMA);
			File outFile = new File(outDir, avroName);
			
			FileWriter writer = new FileWriter(outFile);
			try {
				writer.write(file.asText());
			} finally {
				close(writer);
			}
			
		}

		private void close(FileWriter writer) {
			try {
				writer.close();
			} catch (IOException oops) {
				logger.warn("Failed to close file", oops);
			}
			
		}

		@Override
		public Collection<ResourceFile> get(Iterable<String> resourceLocations) throws IOException {
			
			
			return null;
		}
		
	}

}
