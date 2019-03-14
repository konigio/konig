package io.konig.spreadsheet;

/*
 * #%L
 * Konig Spreadsheet
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class BaseSheetProcessorTest {

	@Test
	public void test() {
		
		MemoryGraph graph = new  MemoryGraph(MemoryNamespaceManager.getDefaultInstance());
		ShapeManager shapeManager = new MemoryShapeManager();
		File templateDir = new File("target/templateDir");
		WorkbookProcessorImpl processor = new WorkbookProcessorImpl(graph, shapeManager, templateDir);
		processor.init();
		
		List<SheetProcessor> list = new ArrayList<SheetProcessor>();
		
		OntologySheet onto = processor.service(OntologySheet.class);
		IndividualSheet ind = processor.service(IndividualSheet.class);
		SettingsSheet settings = processor.service(SettingsSheet.class);
		list.add(onto);
		list.add(ind);
		list.add(settings);
		
		assertEquals(1, ind.compareTo(settings));
		
		Collections.sort(list);
		
		assertTrue(list.get(0).getClass() == OntologySheet.class);
		assertTrue(list.get(1).getClass() == SettingsSheet.class);
		assertTrue(list.get(2).getClass() == IndividualSheet.class);
		
		
	}

}
