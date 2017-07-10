package io.konig.data.app.common;

/*
 * #%L
 * Konig DAO Core
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

import java.io.FileReader;
import java.io.StringWriter;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.vocab.Schema;
import io.konig.dao.core.Format;
import io.konig.yaml.YamlReader;

public class BasicDataAppTest {

	private BasicDataApp app = new BasicDataApp();
	
	@Test
	public void testYaml() throws Exception {
		
		try (
			FileReader reader = new FileReader("src/test/resources/BasicDataAppTest/app.yaml");
			YamlReader yaml = new YamlReader(reader);
		) {
			yaml.addDeserializer(URI.class, new UriDeserializer());
			
			BasicDataApp app = yaml.readObject(BasicDataApp.class);
			ExtentContainer container = app.getContainerForSlug("person");
			assertTrue(container != null);
			assertEquals(Schema.Person, container.getExtentClass());
			assertEquals(uri("http://example.com/shapes/PersonShape"), container.getDefaultShape());
			
			container = app.getContainerForSlug("product");
			assertTrue(container!=null);
			assertEquals(Schema.Product, container.getExtentClass());
			assertEquals(uri("http://example.com/shapes/ProductShape"), container.getDefaultShape());
		}
	}
	
	@Ignore
	public void testCreateGetJob() throws Exception {
		
		ExtentContainer container = new ExtentContainer();
		container.setSlug("person");
		
		app.addContainer(container);

		StringWriter writer = new StringWriter();
		JobRequest jobRequest = new JobRequest();
		jobRequest.setPath("person/http%3A%2F%2Fexample.com%2Fperson%2Falice");
		jobRequest.setWriter(writer);
		
		GetJob job = app.createGetJob(jobRequest);
		
		GetRequest request = job.getRequest();
		assertEquals(job.getContainer(), container);
		assertEquals(request.getIndividualId(), uri("http://example.com/person/alice"));
		assertEquals(request.getFormat(), Format.JSONLD);
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}
