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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class DependencyManagerTest {
	
	DependencyManager<WorkbookListener> manager = new DependencyManager<WorkbookListener>();

	@Test
	public void test() {
		DataSourceGeneratorFactory factory  = new DataSourceGeneratorFactory(null, null, null);
		SettingsSheet settings = new SettingsSheet(null);
		
		List<WorkbookListener> list = new ArrayList<>();
		list.add(factory);
		list.add(settings);
		
		manager.sort(list);
		
		assertEquals(settings, list.get(0));
		assertEquals(factory, list.get(1));
		
	}

}
