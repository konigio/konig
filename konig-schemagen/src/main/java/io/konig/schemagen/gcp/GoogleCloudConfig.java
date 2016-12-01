package io.konig.schemagen.gcp;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.RDFHandlerException;

import io.konig.activity.Activity;
import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.impl.CompositeLocalNameService;
import io.konig.core.impl.CompositeNamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.impl.SimpleLocalNameService;
import io.konig.core.io.FileGetter;
import io.konig.core.pojo.EmitContext;
import io.konig.core.pojo.SimplePojoEmitter;
import io.konig.core.pojo.SimplePojoFactory;
import io.konig.core.vocab.AS;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.PROV;
import io.konig.core.vocab.SH;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class GoogleCloudConfig {

	private GoogleCloudManager manager;
	private BigQueryTableGenerator bigQueryGenerator;
	
	public GoogleCloudConfig(GoogleCloudManager manager, BigQueryTableGenerator bigQueryGenerator) {
		this.manager = manager;
		this.bigQueryGenerator = bigQueryGenerator;
	}
	
	

	public GoogleCloudManager getManager() {
		return manager;
	}

	/**
	 * Load the Google Cloud configuration from a Graph.
	 * @graph The source graph from which the Google Cloud configuration will be loaded.
	 */
	public void load(Graph graph) {
		
		SimplePojoFactory factory = new SimplePojoFactory();
		List<Vertex> projectList = graph.v(Konig.GoogleCloudProject).in(RDF.TYPE).toVertexList();
		for (Vertex v : projectList) {
			GoogleCloudProject project = factory.create(v, GoogleCloudProject.class);
			manager.add(project);
		}
	}
	
	public void writeBigQueryEnumMembers(Graph graph, File outDir) throws IOException {
		outDir.mkdirs();
		ShapeManager shapeManager = bigQueryGenerator.getShapeManager();
		BigQueryEnumGenerator generator = new BigQueryEnumGenerator(shapeManager);

		DatasetMapper datasetMapper = manager.getDatasetMapper();
		BigQueryTableMapper tableMapper = bigQueryGenerator.getTableMapper();
		DataFileMapperImpl dataFileMapper = new DataFileMapperImpl(outDir, datasetMapper, tableMapper);
		generator.generate(graph, dataFileMapper);
	}
	
	public void writeBigQueryTableDefinitions(File outDir) {
		outDir.mkdirs();
		BigQueryTableWriter tableWriter = new BigQueryTableWriter(outDir, bigQueryGenerator);
		
		Collection<GoogleCloudProject> projectList = manager.listProjects();
		for (GoogleCloudProject project : projectList) {
			Collection<BigQueryDataset> datasetList = project.getProjectDataset();
			for (BigQueryDataset dataset : datasetList) {
				Collection<BigQueryTable> tableList = dataset.getDatasetTable();
				for (BigQueryTable table : tableList) {
					tableWriter.add(table);
				}
			}
		}
	}
	
	/**
	 * Generate BigQuery tables for the sub-classes of schema:Enumeration.
	 * 
	 * Shape instances will be added to the ShapeManager registered with the BigQueryTableGenerator 
	 * passed to the constructor.
	 * 
	 * BigQueryTable instances will be added to the GoogleCloudManager registered with this configuration.
	 * 
	 * The Shapes and BigQueryTables will be decorated with provenance information.
	 * 
	 * @param graph The source graph containing OWL class definitions.
	 */
	public void generateEnumTables(Graph graph) {
		bigQueryGenerator.generateEnumTables(graph, manager);
	}
	
	
	public void writeEnumTableShapes(NamespaceManager nsManager, FileGetter shapeFileGetter) throws RDFHandlerException, IOException {
		SimplePojoEmitter emitter = SimplePojoEmitter.getInstance();
		
		ShapeManager shapeManager = bigQueryGenerator.getShapeManager();
		Collection<Shape> shapeList = shapeManager.listShapes();
		for (Shape shape : shapeList) {
			Resource id = shape.getId();
			if (id instanceof URI) {
				Activity activity = shape.getWasGeneratedBy();
				if (activity != null && Konig.GenerateEnumTables.equals(activity.getType())) {
					File file = shapeFileGetter.getFile((URI)id);
					if (file != null) {

						NamespaceManager namespaceManager = new CompositeNamespaceManager(
								nsManager, MemoryNamespaceManager.getDefaultInstance());
						EmitContext context = new EmitContext(
								namespaceManager, SimpleLocalNameService.getDefaultInstance());
						
						MemoryGraph graph = new MemoryGraph();
						emitter.emit(context, shape, graph);
						RdfUtil.prettyPrintTurtle(namespaceManager, graph, file);
						
					}
				}
			}
		}
	}
	
	public void writeProjects(Graph ontologies, File file) throws RDFHandlerException, IOException {
		
		EmitContext context = new EmitContext(ontologies);
		CompositeLocalNameService nameService = new CompositeLocalNameService(
			SimpleLocalNameService.getDefaultInstance(), ontologies);
		context.setLocalNameService(nameService);
		
		NamespaceManager ontologyNamespaces = ontologies.getNamespaceManager();
		
		NamespaceManager nsManager = ontologyNamespaces == null ? new MemoryNamespaceManager() :
			new CompositeNamespaceManager(ontologyNamespaces);
		
		nsManager.add("as", AS.NAMESPACE);
		nsManager.add("prov", PROV.NAMESPACE);
		nsManager.add("sh", SH.NAMESPACE);
		nsManager.add("konig", Konig.NAMESPACE);
		nsManager.add("xsd", XMLSchema.NAMESPACE);
		nsManager.add("activity", Konig.ACTIVIY_BASE_URL);
		
		MemoryGraph graph = new MemoryGraph();
		graph.setNamespaceManager(nsManager);
		
		
		SimplePojoEmitter emitter = new SimplePojoEmitter();
		for (GoogleCloudProject project : manager.listProjects()) {
			emitter.emit(context, project, graph);
		}
		RdfUtil.prettyPrintTurtle(graph.getNamespaceManager(), graph, file);
		
	}
	

}
