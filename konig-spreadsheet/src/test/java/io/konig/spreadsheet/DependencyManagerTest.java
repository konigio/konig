package io.konig.spreadsheet;

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
