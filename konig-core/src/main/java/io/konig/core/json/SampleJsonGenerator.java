package io.konig.core.json;

import java.io.IOException;
import java.io.Writer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.shacl.Shape;
import io.konig.shacl.io.json.JsonWriter;
import io.konig.shacl.sample.SampleGenerator;

public class SampleJsonGenerator {
	private OwlReasoner reasoner;
	
	
	public SampleJsonGenerator(OwlReasoner reasoner) {
		this.reasoner = reasoner;
	}


	public void generate(Shape shape, Writer out) throws IOException {
		JsonFactory factory = new JsonFactory();
		JsonGenerator generator = factory.createGenerator(out);
		generator.useDefaultPrettyPrinter();
		
		JsonWriter jsonWriter = new JsonWriter(reasoner, generator);
		SampleGenerator sampleGenerator = new SampleGenerator(reasoner);
		Graph graph = new MemoryGraph();
		Vertex subject = sampleGenerator.generate(shape, graph);
		jsonWriter.write(shape, subject);
		generator.close();
		
	}
	
	

}
