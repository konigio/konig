package io.konig.maven.datacatalog;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.datacatalog.DataCatalogBuilder;
import io.konig.datacatalog.DataCatalogException;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;

@Mojo( name = "generate-site")
public class KonigDataCatalogMojo extends AbstractMojo {

	@Parameter
	private File rdfDir;
	
	@Parameter(defaultValue="${basedir}/target/generated/datacatalog")
	private File siteDir;
	
	@Parameter
	private String ontology;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		NamespaceManager nsManager= new MemoryNamespaceManager();
		MemoryGraph graph = new MemoryGraph(nsManager);
		ShapeManager shapeManager = new MemoryShapeManager();
		
		DataCatalogBuilder builder = new DataCatalogBuilder();
		
		try {
			GcpShapeConfig.init();
			RdfUtil.loadTurtle(rdfDir, graph, nsManager);
			ShapeLoader shapeLoader = new ShapeLoader(shapeManager);
			shapeLoader.load(graph);
			URI ontologyId = new URIImpl(ontology);
			builder.build(ontologyId, siteDir, graph, shapeManager);
			
		} catch (RDFParseException | RDFHandlerException | IOException | DataCatalogException e) {
			throw new MojoExecutionException("Failed to generate DataCatalog site", e);
		}

	}

}
