package io.konig.schemagen.java;

/*
 * #%L
 * Konig Schema Generator
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
