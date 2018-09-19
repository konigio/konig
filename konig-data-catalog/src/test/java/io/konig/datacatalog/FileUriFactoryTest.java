package io.konig.datacatalog;

/*
 * #%L
 * Konig Data Catalog
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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


import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;
import org.openrdf.model.URI;

public class FileUriFactoryTest {

	@Test
	public void test() {
		
		File baseDir = new File("target/test/ddl");
		
		FileUriFactory factory = new FileUriFactory(baseDir, DataCatalogBuilder.CATALOG_BASE_URI);
		
		File ddlFile = new File(baseDir, "GoogleBigQueryTable/schema.Person.json");
		
		URI uri = factory.toUri(ddlFile);
		
		
		assertEquals("urn:datacatalog/ddl/GoogleBigQueryTable/schema.Person.json", uri.stringValue());
	}

}
