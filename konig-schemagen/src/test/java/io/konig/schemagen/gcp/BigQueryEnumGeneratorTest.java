package io.konig.schemagen.gcp;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.Graph;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.Schema;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class BigQueryEnumGeneratorTest {
	

	@Test
	public void test() throws Exception {
		
		Graph graph = new MemoryGraph();
		graph.edge(Schema.GenderType, RDFS.SUBCLASSOF, Schema.Enumeration);
		graph.edge(Schema.Male, RDF.TYPE, Schema.GenderType);
		graph.edge(Schema.Male, Schema.name, literal("Male"));
		ShapeManager shapeManager = new MemoryShapeManager();
		File outDir = new File("target/test/BigQueryEnumGeneratorTest");
		DatasetMapper datasetMapper = new SimpleDatasetMapper("example");
		TableMapper tableMapper = new LocalNameTableMapper();
		DataFileMapper dataFileMapper = new DataFileMapperImpl(outDir, datasetMapper, tableMapper);
		BigQueryEnumGenerator generator = new BigQueryEnumGenerator(shapeManager);
		
		generator.generate(graph, dataFileMapper);
		
	}

	private Value literal(String value) {
		return new LiteralImpl(value);
	}

}
