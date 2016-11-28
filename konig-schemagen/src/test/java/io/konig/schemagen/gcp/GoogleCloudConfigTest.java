package io.konig.schemagen.gcp;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;

import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.schemagen.ShapeNamer;
import io.konig.schemagen.SimpleShapeNamer;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class GoogleCloudConfigTest {

	@Test
	public void test() throws Exception {
		
		String bqShapeBaseURL = "http://example.com/shape/bq/";
		File sourceDir = new File("src/test/resources/GoogleCloudConfigTest");
		File targetProjectsFile = new File("target/test/GoogleCloudConfig/gcp.ttl");
		
		MemoryGraph source = new MemoryGraph();
		source.setNamespaceManager(new MemoryNamespaceManager());
		
		RdfUtil.loadTurtle(sourceDir, source, source.getNamespaceManager());
		
		ShapeManager shapeManager = new MemoryShapeManager();

		LocalNameTableMapper tableMapper = new LocalNameTableMapper();
		SimpleProjectMapper projectMapper = new SimpleProjectMapper("test-project");
		SimpleDatasetMapper datasetMapper = new SimpleDatasetMapper("test-dataset");
		
		OwlReasoner reasoner = new OwlReasoner(source);
		ShapeNamer shapeNamer = new SimpleShapeNamer(source.getNamespaceManager(), bqShapeBaseURL);
		
		BigQueryTableGenerator generator = new BigQueryTableGenerator(shapeManager, shapeNamer, reasoner)
			.setTableMapper(tableMapper);
		
		GoogleCloudManager manager = new MemoryGoogleCloudManager(shapeManager)
			.setProjectMapper(projectMapper)
			.setDatasetMapper(datasetMapper);
		
		GoogleCloudConfig config = new GoogleCloudConfig(manager, generator);
		
		config.load(source);
		config.generateEnumTables(source);
		config.writeProjects(source, targetProjectsFile);
		
		// TODO: Add assertions
	}

}
