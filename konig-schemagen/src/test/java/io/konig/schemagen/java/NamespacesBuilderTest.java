package io.konig.schemagen.java;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import com.sun.codemodel.JCodeModel;

import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryNamespaceManager;

public class NamespacesBuilderTest {

	@Test
	public void test() throws Exception {
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", "http://schema.org/");
		nsManager.add("owl", "http://www.w3.org/2002/07/owl#");
		
		JavaNamer namer = new BasicJavaNamer("com.example", nsManager);

		JCodeModel model = new JCodeModel();
		
		NamespacesBuilder builder = new NamespacesBuilder(namer);
		
		builder.generateNamespaces(model, nsManager);

		File file = new File("target/java/src");
		file.mkdirs();
		model.build(file);
	}

}
