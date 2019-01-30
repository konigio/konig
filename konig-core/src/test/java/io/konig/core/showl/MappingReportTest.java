package io.konig.core.showl;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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


import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class MappingReportTest {
	
	private MappingReport report = new MappingReport();
	private ShowlManager showlManager;
	private NamespaceManager nsManager;

	@Test
	public void testDirect() throws Exception {
		
		load("src/test/resources/MappingReportTest/direct");
		StringWriter writer = new StringWriter();
		report.write(writer, showlManager, nsManager);
		
		String text = writer.toString();
		
		assertText("src/test/resources/MappingReportTest/direct/expected.txt", text);
		
		
	}

	private void assertText(String path, String text) throws Exception {
		
		List<String> expected = asList(new String(Files.readAllBytes(Paths.get(path))));
		List<String> actual = asList(text);
		assertEquals(expected.size(), actual.size());
		for (int i=0; i<expected.size(); i++) {
			assertEquals(expected.get(i), actual.get(i));
		}
		
	}

	private List<String> asList(String text) {
		String[] array = text.split("\\r?\\n");
		List<String> list = new ArrayList<>();
		for (String value : array) {
			value = value.trim();
			if (value.length()>0) {
				list.add(value);
			}
		}
		return list;
	}

	private void load(String filePath) throws RDFParseException, RDFHandlerException, IOException {
		File sourceDir = new File(filePath);
		nsManager = new MemoryNamespaceManager();
		Graph graph = new MemoryGraph(nsManager);
		OwlReasoner reasoner = new OwlReasoner(graph);
		ShapeManager shapeManager = new MemoryShapeManager();
		
		RdfUtil.loadTurtle(sourceDir, graph, shapeManager);
		
		showlManager = new ShowlManager();
		showlManager.load(shapeManager, reasoner);
		
	}

}
