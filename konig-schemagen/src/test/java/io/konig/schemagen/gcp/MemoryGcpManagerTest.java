package io.konig.schemagen.gcp;

import static org.junit.Assert.*;

import org.junit.Test;

import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.Schema;

public class MemoryGcpManagerTest {

	@Test
	public void testDatasetForClass() {
		
		MemoryGraph graph = new MemoryGraph();
		
		Vertex owlClass = graph.vertex(Schema.Person);
		
		MemoryGoogleCloudManager manager = new MemoryGoogleCloudManager()
			.setProjectMapper(new SimpleProjectMapper("example-project"))
			.setDatasetMapper(new SimpleDatasetMapper("mydataset"));
		
		BigQueryDataset dataset = manager.datasetForClass(owlClass);
		assertTrue(dataset != null);
		assertEquals("mydataset", dataset.getDatasetId());
		
		GoogleCloudProject project = manager.getProjectById("example-project");
		assertTrue(project != null);
		
		BigQueryDataset fetched = project.findProjectDataset("mydataset");
		assertTrue(fetched != null);
		
	}

}
