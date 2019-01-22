package io.konig.core.impl;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.util.Literals;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.BasicWriterSettings;
import org.openrdf.rio.turtle.TurtleParserFactory;
import org.openrdf.rio.turtle.TurtleUtil;

import io.konig.core.Context;
import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.Path;
import io.konig.core.Term;
import io.konig.core.Vertex;
import io.konig.core.io.CompactTurtleWriter;
import io.konig.core.io.CompositeRdfHandler;
import io.konig.core.io.GraphLoadHandler;
import io.konig.core.io.NamespaceRDFHandler;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.core.path.OutStep;
import io.konig.core.path.Step;
import io.konig.core.vocab.Schema;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.io.ShapeLoader;

/*
 * #%L
 * konig-core
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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


public class RdfUtil {
	
	public static URI namespace(Graph graph, String value) {

		NamespaceManager nsManager = graph.getNamespaceManager();
		int index = value.length()-1;
		for (index=value.lastIndexOf('/', index); index>0; index=value.lastIndexOf('/', --index)) {
			String namespace = value.substring(0, index+1);
			Namespace ns = nsManager.findByName(namespace);
			if (ns != null) {
				return (URI) graph.vertex(new URIImpl(namespace)).getId();
			}
		}
		
		return null;
	}
	
	public static String compactId(Resource id, NamespaceManager nsManager) {
		if (id == null) {
			return null;
		}
		if (id instanceof BNode) {
			return "_:" + ((BNode)id).getID();
		}
		if (id instanceof URI) {
			URI iri = (URI) id;
			if (nsManager != null) {
				Namespace ns = nsManager.findByName(iri.getNamespace());
				if (ns != null) {
					return ns.getPrefix() + ":" + iri.getLocalName();
				}
				return "<" + iri.stringValue() + ">";
			}
		}

		throw new IllegalArgumentException("Unsuppported Resource type: " + id.getClass().getName());
	}
	
	public static String localName(Resource id) {
		if (id == null) {
			return "null";
		}
		if (id instanceof BNode) {
			return ((BNode)id).getID();
		}
		if (id instanceof URI) {
			return ((URI)id).getLocalName();
		}
		throw new IllegalArgumentException("Unsuppported Resource type: " + id.getClass().getName());
	}
	
	/**
	 * Count the number of distinct PropertyConstraints within a given Shape
	 */
	public static int countDistinctProperties(Shape shape) {
		Set<Shape> memory = new HashSet<>();
		
		return countProperties(memory, shape);
	}
	
	private static int countProperties(Set<Shape> memory, Shape shape) {
		int count = 0;
		if (!memory.contains(shape)) {
			memory.add(shape);
			List<PropertyConstraint> list = shape.getProperty();
			count += list.size();
			for (PropertyConstraint p : list) {
				Shape childShape = p.getShape();
				if (childShape != null) {
					count = countProperties(memory, childShape);
				}
			}
		}
		return count;
	}

	public static List<Namespace> collectNamespaces(NamespaceManager nsManager, Collection<Edge> edgeList) {
		Map<String, Namespace> map = new HashMap<>();
		for (Edge e : edgeList) {
			Resource subject = e.getSubject();
			URI predicate = e.getPredicate();
			Value object = e.getObject();
			if (subject instanceof URI) {
				addNamespace(map, nsManager, (URI)subject);
			}
			addNamespace(map, nsManager, predicate);
			if (object instanceof URI) {
				addNamespace(map, nsManager, (URI)object);
			}
			
		}
		return new ArrayList<Namespace>(map.values());
	}
	
	/**
	 * Sort a list of properties alphabetically by local name.
	 * @param properties
	 */
	public static void sortByLocalName(List<PropertyConstraint> properties) {
		Collections.sort(properties, new Comparator<PropertyConstraint>() {

			@Override
			public int compare(PropertyConstraint a, PropertyConstraint b) {
				String aName = a.getPredicate()==null ? "" : a.getPredicate().getLocalName();
				String bName = b.getPredicate()==null ? "" : b.getPredicate().getLocalName();
				
				return aName.compareTo(bName);
			}
		});
	}
	
	public static boolean nearEqual(Object a, Object b) {
		if (a instanceof Long && b instanceof Long) {
			return a.equals(b);
		}
		if (a instanceof Number && b instanceof Number) {
			Number x = (Number) a;
			Number y = (Number) b;
			
			double p = x.doubleValue();
			double q = y.doubleValue();
			
			double delta = Math.abs( q-p );
			double base = (q+p)/2;
			
			double fraction = delta/base;
			if (fraction < 0.00000000000001) {
				return true;
			} 
			return false;
			
		
		}
		
		return a.equals(b);
		
	}
	
	public static Object javaValue(Value value) {
		if (value instanceof Literal) {
			return javaValue((Literal)value);
		}
		return value;
	}
	
	public static void append(StringBuilder builder, Resource id) {

		if (id instanceof BNode) {
			builder.append("_:");
			builder.append(id.stringValue());
		} else {
			builder.append('<');
			builder.append(id.stringValue());
			builder.append('>');
		}
	}
	
	public static void append(StringBuilder builder, Object value) {
		if (value instanceof String) {
			builder.append('"');
			builder.append(value.toString());
			builder.append('"');
		} else {
			builder.append(value);
		}
	}
	
	public static String idValue(Resource id) {
		StringBuilder builder = new StringBuilder();
		append(builder, id);
		return builder.toString();
	}
	
	public static Set<Object> toJavaValue(Set<Value> source) {
		Set<Object> sink = new LinkedHashSet<>();
		for (Value v : source) {
			sink.add(javaValue(v));
		}
		
		return sink;
	}
	
	public static Object javaValue(Literal literal) {
		String value = literal.getLabel();
		URI type = literal.getDatatype();
		
		if (
			XMLSchema.INTEGER.equals(type) ||
			XMLSchema.INT.equals(type) ||
			XMLSchema.LONG.equals(type) ||
			Schema.Integer.equals(type)
		) {
			return Long.parseLong(value);
		} else if (
			XMLSchema.DOUBLE.equals(type) ||
			XMLSchema.FLOAT.equals(type) ||
			XMLSchema.DECIMAL.equals(type) ||
			Schema.Float.equals(type) ||
			Schema.Number.equals(type)
		) {
			return Double.parseDouble(value);
		}
		
		return literal;
	}
	
	
	/**
	 * Filter the namespaces within a given NamespaceManager and produce a new manager that contains only those 
	 * namespaces that are used within a certain reference graph.
	 * @param graph The reference graph
	 * @param nsManager The source NamespaceManager whose namespaces will be filtered based on the reference graph
	 * @return A new NamespaceManager that contains the subset of namespaces from nsManager that are used by the given graph.
	 */
	public static NamespaceManager filterNamespaces(Graph graph, NamespaceManager nsManager) {
		NamespaceManager sink = new MemoryNamespaceManager();
		
		for (Edge e : graph) {
			Resource subject = e.getSubject();
			URI predicate = e.getPredicate();
			Value object = e.getObject();

			copyNamespace(nsManager, subject, sink);
			copyNamespace(nsManager, predicate, sink);
			copyNamespace(nsManager, object, sink);
			
			if (object instanceof Literal) {
				Literal literal = (Literal) object;
				URI type = literal.getDatatype();
				if (type != null && !XMLSchema.STRING.equals(type)) {
					copyNamespace(nsManager, type, sink);
				}
			}
		}
		
		return sink;
	}
	
	public static void deepCopy(Vertex v, Graph target) {
		if (v != null) {
			Graph g = v.getGraph();
			Set<Entry<URI,Set<Edge>>> out = v.outEdges();
			for (Entry<URI,Set<Edge>> entry : out) {
				Set<Edge> set = entry.getValue();
				for (Edge e : set) {
					target.edge(e);
					Value object = e.getObject();
					if (object instanceof BNode) {
						BNode bnode = (BNode) object;
						Vertex w = g.getVertex(bnode);
						deepCopy(w, target);
					}
				}
			}
		}
	}
	
	private static void copyNamespace(NamespaceManager source, Value value, NamespaceManager sink) {
		if (value instanceof URI && !RDF.TYPE.equals(value)) {
			URI uri = (URI) value;
			String name = uri.getNamespace();
			Namespace ns = source.findByName(name);
			if (ns != null) {
				sink.add(ns);
			}
		}
		
	}

	public static void sortByPrefix(List<Namespace> list) {
		Collections.sort(list, new Comparator<Namespace>() {

			@Override
			public int compare(Namespace a, Namespace b) {
				String x = a.getPrefix();
				String y = b.getPrefix();
				return x.compareTo(y);
			}
		});
	}
	
	private static void addNamespace(Map<String, Namespace> map, NamespaceManager nsManager, URI uri) {
		String key = uri.getNamespace();
		Namespace ns = nsManager.findByName(key);
		if (ns != null) {
			map.put(key, ns);
		}
		
	}
	
	public static URI expand(NamespaceManager nsManager, String curie) throws KonigException {
		URI result = null;
		if (curie != null) {

			if (
				!commonInternetScheme(curie) &&
				!curie.startsWith("urn:")
			) {

				int colon = curie.indexOf(':');
				if (colon >0) {
					String prefix = curie.substring(0, colon);
					Namespace ns = nsManager.findByPrefix(prefix);
					if (ns != null) {
						StringBuilder builder = new StringBuilder();
						builder.append(ns.getName());
						builder.append(curie.substring(colon+1));
						result = new URIImpl(builder.toString());
					} else {
						throw new KonigException("Namespace not found for prefix: " + prefix);
					}
					
				}
			} else {
				result = new URIImpl(curie);
			}
		}
		
		return result;
	}
	

	private static final int WORD = 1;
	private static final int COLON = 2;
	private static final int SLASH = 3;
	private static boolean commonInternetScheme(String curie) {
		
		int state = WORD;
		
		for (int i=0; i<curie.length(); i++) {
			char c = curie.charAt(i);
			switch (state) {
			case WORD :
				if (c==':') {
					state = COLON;
				} else if (!Character.isLowerCase(c) && !Character.isDigit(c) && c!='.' && c!='-' && c!='+') {
					return false;
				}
				break;
				
			case COLON :
				if (c=='/') {
					state = SLASH;
				} else {
					return false;
				}
				
				break;
				
			case SLASH :
				if (c == '/') {
					return true;
				} else {
					return false;
				}
			}
			
		}
		
		
		return false;
	}
	
	/**
	 * Express a given URI as a Compact URI; fail if the namespace prefix is not defined.
	 * @param uri
	 * @param nsManager
	 * @return
	 */
	public static String curie(URI uri, NamespaceManager nsManager) {
		if (nsManager == null) {
			throw new KonigException("NamespaceManager must be defined");
		}
		Namespace ns = nsManager.findByName(uri.getNamespace());
		if (ns == null) {
			throw new KonigException("Prefix not found for namespace: " + uri.getNamespace());
		}
		StringBuilder builder = new StringBuilder();
		builder.append(ns.getPrefix());
		builder.append(':');
		builder.append(uri.getLocalName());
		return builder.toString();
		
	}

	public static String optionalCurie(NamespaceManager nsManager, URI uri) {
		if (nsManager != null) {
			Namespace ns = nsManager.findByName(uri.getNamespace());
			if (ns != null) {
				StringBuilder builder = new StringBuilder();
				builder.append(ns.getPrefix());
				builder.append(':');
				builder.append(uri.getLocalName());
				return builder.toString();
			}
		}
		
		return uri.stringValue();
	}
	

	public static String getDescription(Vertex subject) {
		
		Value value =null;
		return 
			((value=subject.getValue(RDFS.COMMENT))!=null) 			? value.stringValue() :
			((value=subject.getValue(DCTERMS.DESCRIPTION)) != null)		? value.stringValue() :
			((value=subject.getValue(Schema.description)) != null)	? value.stringValue() :
			null;
	}
	
	public static String getDescription(PropertyConstraint p, Graph graph) {
		
		String result = p.getComment();
		if (result == null) {
			URI predicate = p.getPredicate();
			if (predicate != null && graph!=null) {
				Vertex v = graph.getVertex(predicate);
				if (v != null) {
					result = getDescription(v);
				}
			}
		}
		return result;
	}
	
	public static List<Vertex> listSubtypes(List<Vertex> typeList) {
		Graph graph = null;
		if (!typeList.isEmpty()) {
			graph = typeList.get(0).getGraph();
		}
		Set<Resource> set = new HashSet<>();
		for (Vertex v : typeList) {
			set.add(v.getId());
			Set<Resource> subTypes = RdfUtil.subTypes(v);
			set.addAll(subTypes);
		}
		List<Vertex> result = new ArrayList<>();
		for (Resource r : set) {
			result.add(graph.getVertex(r));
		}
		
		return result;
	}
	
	public static String normalize(String value) {
		if (value != null) {
			StringTokenizer tokens = new StringTokenizer(value, "\r\n\t ");
			StringBuilder builder = new StringBuilder();
			while (tokens.hasMoreTokens()) {
				builder.append(tokens.nextToken());
				if (tokens.hasMoreTokens()) {
					builder.append(' ');
				}
			}
			value = builder.toString();
		}
		
		return value;
	}
	
	public static void prettyPrintTurtle(Graph graph, OutputStream out) throws IOException, RDFHandlerException {
		OutputStreamWriter writer = new OutputStreamWriter(out);
		prettyPrintTurtle(graph.getNamespaceManager(), graph, writer);
		writer.flush();
	}

	public static void prettyPrintTurtle(Graph graph, Writer writer) throws IOException, RDFHandlerException {
		prettyPrintTurtle(graph.getNamespaceManager(), graph, writer);
	}
	
	public static void prettyPrintTurtle(NamespaceManager nsManager, Graph graph, Writer writer) throws IOException, RDFHandlerException {
		
		nsManager = nsManager==null ? null : filterNamespaces(graph, nsManager);
		
		CompactTurtleWriter turtle = new CompactTurtleWriter(writer);
		turtle.getWriterConfig().set(BasicWriterSettings.PRETTY_PRINT, true);
		NaturalEdgeIterable sequence = new NaturalEdgeIterable(graph);
		
		turtle.startRDF();
		printNamespaces(nsManager, turtle);
		
		for (Edge e : sequence) {
			turtle.handleStatement(e);
			
		}
		turtle.endRDF();
	}
	
	public static void prettyPrintTurtle(NamespaceManager nsManager, Graph graph, File file) throws IOException, RDFHandlerException {
		file.getParentFile().mkdirs();
		FileWriter writer = new FileWriter(file);
		try {
			prettyPrintTurtle(nsManager, graph, writer);
		} finally {
			writer.close();
		}
	}
	
	private static void printNamespaces(NamespaceManager namespaceManager, CompactTurtleWriter turtle) throws RDFHandlerException {
		
		if (namespaceManager != null) {
			List<Namespace> list = new ArrayList<>(namespaceManager.listNamespaces());
			Collections.sort(list, new Comparator<Namespace>() {

				@Override
				public int compare(Namespace a, Namespace b) {
					return a.getPrefix().compareTo(b.getPrefix());
				}
			});
			for (Namespace n : list) {
				turtle.handleNamespace(n.getPrefix(), n.getName());
			}
		}
		
	}

	public static void loadTurtle(Graph graph, NamespaceManager nsManager, InputStream input, String baseURL)
		throws IOException, RDFParseException, RDFHandlerException {
	
		CompositeRdfHandler handler = new CompositeRdfHandler(
			new GraphLoadHandler(graph),
			new NamespaceRDFHandler(nsManager)
		);
		RDFParser parser = new TurtleParserFactory().getParser();
		parser.setRDFHandler(handler);
		Reader reader = new InputStreamReader(input);
		parser.parse(reader, baseURL);
	}
	
	public static void loadTurtle(File sourceDir, Graph graph, ShapeManager shapeManager) 
	throws RDFParseException, RDFHandlerException, IOException {
		
		loadTurtle(sourceDir, graph, graph.getNamespaceManager());
		ShapeLoader shapeLoader = new ShapeLoader(shapeManager);
		shapeLoader.load(graph);
	}
	
	public static void loadTurtle(File sourceDir, Graph graph) throws RDFParseException, RDFHandlerException, IOException {
		loadTurtle(sourceDir, graph, graph.getNamespaceManager());
	}
	
	/**
	 * From a given source directory, load all files with the *.ttl suffix into a given Graph.
	 * This method will search subdirectories recursively for Turtle files.
	 * @param sourceDir The source directory containing Turtle files.
	 * @param graph The graph into which RDF statements will be placed.
	 */
	public static void loadTurtle(File sourceDir, Graph graph, NamespaceManager nsManager)
		throws IOException, RDFParseException, RDFHandlerException {
		 
		RDFHandler handler = new GraphLoadHandler(graph);
		if (nsManager != null) {
			handler = new CompositeRdfHandler(handler, new NamespaceRDFHandler(nsManager));
		}
		RDFParser parser = new TurtleParserFactory().getParser();
		parser.setRDFHandler(handler);
		
		loadTurtle(sourceDir, parser);
		
	}
	
	private static void loadTurtle(File sourceDir, RDFParser parser) throws RDFParseException, RDFHandlerException, IOException  {
		File[] array = sourceDir.listFiles();
		if (array != null) {
			for (File file : array) {
				if (file.isDirectory()) {
					loadTurtle(file, parser);
				} else if (file.getName().endsWith(".ttl")){
					

						try {
							FileReader input = new FileReader(file);
							parser.parse(input, "");
						} catch (RDFParseException e) {
							throw new RDFParseException("Failed to parse file: " + file.getAbsolutePath(), e);
						} catch (RDFHandlerException e) {
							throw new RDFHandlerException("Failed to parse file: " + file.getAbsolutePath(), e);
						} catch (IOException e) {
							throw new IOException("Failed to parse file: " + file.getAbsolutePath(), e);
						}
				
				}
			}
		}
		
	}
	
	public static void loadTurtle(Graph graph, Reader input, String baseURL) throws RDFParseException, RDFHandlerException, IOException {
		GraphLoadHandler handler = new GraphLoadHandler(graph);
		RDFParser parser = new TurtleParserFactory().getParser();
		parser.setRDFHandler(handler);
		parser.parse(input, baseURL);
	}

	public static void loadTurtle(Graph graph, InputStream input, String baseURL) 
		throws IOException, RDFParseException, RDFHandlerException {
		
		InputStreamReader reader = new InputStreamReader(input);
		loadTurtle(graph, reader, baseURL);
	}
	
	/**
	 * Find the super types of a given OWL Class.
	 * @param owlClass The OWL class whose super types are to be found.
	 * @return The set of super types of the given class.
	 */
	public static Set<Resource> superTypes(Vertex owlClass) {
		
		Set<Resource> result = new HashSet<>();
		
		List<Vertex> stack = new ArrayList<>();
		stack.add(owlClass);
		for (int i=0; i<stack.size(); i++) {
			Vertex target = stack.get(i);
			List<Vertex> list = target.asTraversal().out(RDFS.SUBCLASSOF).toVertexList();
			for (Vertex v : list) {
				Resource id = v.getId();
				if (!id.equals(owlClass.getId()) && !result.contains(id)) {
					result.add(id);
					stack.add(v);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Compute the transitive closure of the subtypes of a given OWL Class.
	 * @param owlClass The OWL Class whose transitive closure is to be computed.
	 * @return The transitive closure of the given OWL Class.
	 */
	public static List<Vertex> subtypeList(Vertex owlClass) {
		return owlClass.asTraversal().inTransitive(RDFS.SUBCLASSOF).toVertexList();
	}
	
	public static Set<Resource> subTypes(Vertex owlClass) {
		
		Set<Resource> result = new HashSet<>();
		
		List<Vertex> stack = new ArrayList<>();
		stack.add(owlClass);
		for (int i=0; i<stack.size(); i++) {
			Vertex target = stack.get(i);
			List<Vertex> list = target.asTraversal().in(RDFS.SUBCLASSOF).toVertexList();
			for (Vertex v : list) {
				Resource id = v.getId();
				if (!id.equals(owlClass.getId()) && !result.contains(id)) {
					result.add(id);
					stack.add(v);
				}
			}
		}
		
		return result;
	}

	public static URI out(Path path, int index) {
		if (path != null) {
			List<Step> list = path.asList();
			if (index < list.size()) {
				Step s = list.get(index);
				if (s instanceof OutStep) {
					return ((OutStep) s).getPredicate();
				}
			}
		}
		return null;
	}
	
	public static boolean isSubClassOf(Vertex subject, Resource target) {
		
		List<Vertex> list = subject.asTraversal().out(RDFS.SUBCLASSOF).toVertexList();
		for (Vertex v : list) {
			if (v.getId().equals(target)) {
				return true;
			}
		}
		for (Vertex v : list) {
			if (isSubClassOf(v, target)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static void writeLiteral(PrettyPrintWriter writer, Literal literal) {

		String label = literal.getLabel();
		URI datatype = literal.getDatatype();
		
		if (XMLSchema.INTEGER.equals(datatype) || XMLSchema.DECIMAL.equals(datatype)
				|| XMLSchema.DOUBLE.equals(datatype) || XMLSchema.BOOLEAN.equals(datatype))
		{
			try {
				writer.write(XMLDatatypeUtil.normalize(label, datatype));
				return; // done
			}
			catch (IllegalArgumentException e) {
				// not a valid numeric typed literal. ignore error and write
				// as
				// quoted string instead.
			}
		}

		if (label.indexOf('\n') != -1 || label.indexOf('\r') != -1 || label.indexOf('\t') != -1) {
			// Write label as long string
			writer.write("\"\"\"");
			writer.write(TurtleUtil.encodeLongString(label));
			writer.write("\"\"\"");
		}
		else {
			// Write label as normal string
			writer.write("\"");
			writer.write(TurtleUtil.encodeString(label));
			writer.write("\"");
		}

		if (Literals.isLanguageLiteral(literal)) {
			// Append the literal's language
			writer.write("@");
			writer.write(literal.getLanguage());
		}
		else if (!XMLSchema.STRING.equals(datatype) ) {
			// Append the literal's datatype (possibly written as an abbreviated
			// URI)
			writer.write("^^");
			writeURI(writer, datatype);
		}
		
	}

	public static void writeURI(PrettyPrintWriter out, URI uri) {
		NamespaceManager nsManager = out.getNamespaceManager();
		if (nsManager != null) {
			Namespace ns = nsManager.findByName(uri.getNamespace());
			if (ns != null) {
				out.print(ns.getPrefix());
				out.print(':');
				out.print(uri.getLocalName());
				return;
			}
		}
		out.print('<');
		out.print(uri.stringValue());
		out.print('>');
	}
	
	public static String compactName(NamespaceManager nsManager, Resource resource) {
		
		if (resource instanceof BNode) {
			return "_:" + ((BNode)resource).getID();
		}
		String iriValue = resource.stringValue();
		
		if (nsManager != null) {
			URI id = (URI) resource;
			Namespace ns = nsManager.findByName(id.getNamespace());
			if (ns != null) {
				String prefix = ns.getPrefix();
				StringBuilder builder = new StringBuilder();
				builder.append(prefix);
				builder.append(':');
				builder.append(id.getLocalName());
				return builder.toString();
			}
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append('<');
		builder.append(iriValue);
		builder.append('>');
		return builder.toString();
	}
	
	public static String compactName(Context context, URI resource) {

		String iriValue = resource.stringValue();
		
		if (context != null) {
			Context inverse = context.inverse();
			Term term = inverse.getTerm(iriValue);
			if (term != null) {
				return term.getId();
			}
			
			term = inverse.getTerm(resource.getNamespace());
			if (term != null) {
				StringBuilder builder = new StringBuilder();
				builder.append(term.getId());
				builder.append(':');
				builder.append(resource.getLocalName());
				return builder.toString();
			}
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append('<');
		builder.append(iriValue);
		builder.append('>');
		return builder.toString();
	}

	public static URI uri(Value value) {
		return value instanceof URI ? (URI) value : null;
	}
	
}
