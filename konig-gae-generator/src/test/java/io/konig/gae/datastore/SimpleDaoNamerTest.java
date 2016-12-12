package io.konig.gae.datastore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.Schema;
import io.konig.gae.datastore.SimpleDaoNamer;

public class SimpleDaoNamerTest {

	@Test
	public void test() {
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", Schema.NAMESPACE);
		
		String basePackage = "com.example.gae.datastore";
		
		
		SimpleDaoNamer namer = new SimpleDaoNamer(basePackage, nsManager);
		
		String daoClass = namer.daoName(Schema.Person);
		assertEquals("com.example.gae.datastore.schema.PersonDao", daoClass);
		
	}
}
