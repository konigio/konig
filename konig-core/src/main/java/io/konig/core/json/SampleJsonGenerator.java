package io.konig.core.json;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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
