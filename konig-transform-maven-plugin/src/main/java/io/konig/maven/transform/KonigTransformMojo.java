package io.konig.maven.transform;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;
import io.konig.transform.bigquery.BigQueryTransformGenerator;

@Mojo( name = "generate")
public class KonigTransformMojo extends AbstractMojo{
	
	@Parameter
	private File shapesDir;
	
	@Parameter
	private File outDir;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		GcpShapeConfig.init();
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		ShapeManager shapeManager = new MemoryShapeManager();

		Graph graph = new MemoryGraph();
		try {
			RdfUtil.loadTurtle(shapesDir, graph, nsManager);
		} catch (RDFParseException | RDFHandlerException | IOException e1) {
			throw new MojoExecutionException("Failed to load graph from " + shapesDir);
		}
		ShapeLoader shapeLoader = new ShapeLoader(null, shapeManager, nsManager);
		shapeLoader.load(graph);
		
		BigQueryTransformGenerator generator = new BigQueryTransformGenerator(shapeManager, outDir, new OwlReasoner(graph));
		generator.generateAll();
		List<Throwable> errorList = generator.getErrorList();
		if (errorList != null && !errorList.isEmpty()) {
			Log logger = getLog();
			for (Throwable e : errorList) {
				logger.error(e.getMessage());
			}
			throw new MojoExecutionException("Failed to generate BigQuery Transform", errorList.get(0));
		}
		
	}

}
