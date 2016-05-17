package io.konig.ldp.impl;

/*
 * #%L
 * Konig Linked Data Platform
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.naming.BinaryRefAddr;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Graph;
import io.konig.core.vocab.LDP;
import io.konig.core.vocab.Schema;
import io.konig.ldp.HttpMethod;
import io.konig.ldp.LdpException;
import io.konig.ldp.LdpRequest;
import io.konig.ldp.LdpResponse;
import io.konig.ldp.LinkedDataPlatform;
import io.konig.ldp.RdfSource;
import io.konig.ldp.ResourceFile;

abstract public class LinkedDataPlatformTest {
	public static final String ROOT = "http://example.com/resources/";
	protected LinkedDataPlatform platform;
	

	
	
	@Test
	public void testGetContainer() throws Exception {
		
		String alice = "http://example.com/resources/person/alice";
		String bob = "http://example.com/resources/person/bob";
		String person = "http://example.com/resources/person/";
		
		URI personURI = uri(person);
		URI aliceURI = uri(alice);
		URI bobURI = uri(bob);
		
		put(alice, "<http://example.com/resources/person/alice> a <http://schema.org/Person> .");
		put(bob, "<http://example.com/resources/person/bob> a <http://schema.org/Person> .");
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		get(person, outputStream);
		
		RdfSource file = platform.getResourceBuilder()
				.entityBody(outputStream.toByteArray())
				.contentType("text/turtle")
				.rdfSource();
		
		Graph graph = file.createGraph();
		
		assertTrue(graph.contains(personURI, LDP.contains, aliceURI));
		assertTrue(graph.contains(personURI, LDP.contains, bobURI));
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}



	private void get(String resourceId, ByteArrayOutputStream outputStream) throws IOException, LdpException {
		
		LdpRequest request = platform.getRequestBuilder()
			.method(HttpMethod.GET)
			.address(resourceId)
			.build();
		
		LdpResponse response = platform.createResponse(outputStream);
		platform.serve(request, response);
		
		ResourceFile resource = response.getResource();
		
		resource.setEntityBody(outputStream.toByteArray());
		
		String text = resource.getContentType();
		
		
		
	}


	private void put(String resourceId, String body) throws IOException, LdpException {
		ResourceFile file = platform.getResourceBuilder()
			.contentLocation(resourceId)
			.contentType("text/turtle")
			.rdfSource();
		
		platform.put(file,  true);
	}
	
	@Test
	public void testPutService() throws Exception {
		
		String aliceAddress = "http://example.com/resources/person/alice";
		String aliceBody = 
			"@prefix schema: <http://schema.org/> . " +
			"<http://example.com/resources/person/alice> a schema:Person ;" +
			"  schema:givenName \"Alice\" ;" +
			"  schema:familyName \"Jones\" ."
			;
		URI aliceURI = uri(aliceAddress);
		URI containerURI = uri("http://example.com/resources/person/");
		
		LdpRequest request = platform.getRequestBuilder()
			.method(HttpMethod.PUT)
			.address(aliceAddress)
			.contentType("text/turtle")
			.body(aliceBody)
			.build();
		
		LdpResponse response = platform.createResponse();
		
		platform.serve(request, response);
		
		request = platform.getRequestBuilder()
			.method(HttpMethod.GET)
			.address(aliceAddress)
			.accept("text/turtle")
			.build();
		
		response = platform.createResponse();
		
		platform.serve(request, response);
		
		Graph graph = response.getResource().asRdfSource().createGraph();
		
		assertTrue(graph.contains(aliceURI, Schema.givenName, literal("Alice")));
		
		request = platform.getRequestBuilder()
			.method(HttpMethod.GET)
			.address("http://example.com/resources/person/")
			.accept("text/turtle")
			.build();
		
		response = platform.createResponse();
		platform.serve(request, response);
		
		graph = response.getResource().asRdfSource().createGraph();
		
		assertTrue(graph.contains(containerURI, LDP.contains, aliceURI));
		
	}
	
	private Value literal(String value) {
		
		return new LiteralImpl(value);
	}



	@Test
	public void testPost() throws Exception {
		String containerId = "http://example.com/person/";
		
		ResourceFile aliceExpected = aliceFile();
		
		LdpRequest request = platform.getRequestBuilder()
				.method(HttpMethod.POST)
				.address(containerId)
				.slug("alice")
				.entity(aliceExpected)
				.build();
		
		LdpResponse response = platform.createResponse();
		
		platform.serve(request, response);
		
		request = platform.getRequestBuilder()
			.method(HttpMethod.GET)
			.address(aliceExpected.getContentLocation())
			.accept("text/turtle")
			.build();
		
		response = platform.createResponse();
		
		platform.serve(request, response);
		
		RdfSource actual = response.getResource().asRdfSource();
		
		assertEquals(aliceExpected.getEntityBodyText(), actual.getEntityBodyText());
	
	}


	private ResourceFile aliceFile() {
		String resourceId = "http://example.com/person/alice";
		String body = "<http://example.com/alice> a <http://schema.org/Person> .";
		
		ResourceFile file = platform.getResourceBuilder()
			.contentLocation(resourceId)
			.contentType("text/turtle")
			.body(body)
			.rdfSource();
		
		return file;
	}
	@Test
	public void testPutTurtleGetJsonld() throws Exception {
		String aliceAddress = "http://example.com/resources/person/alice";
		String aliceBody = 
				  "@prefix schema: <http://schema.org/> ."
				+ "<http://example.com/resources/person/alice> a schema:Person ;"
				+ "  schema:givenName \"Alice\" ;"
				+ "  schema:familyName \"Jones\" . "
			;
		
		URI aliceURI = uri(aliceAddress);
		
		LdpRequest request = platform.getRequestBuilder()
			.method(HttpMethod.PUT)
			.address(aliceAddress)
			.contentType("text/turtle")
			.body(aliceBody)
			.build();
		
		LdpResponse response = platform.createResponse();
		
		platform.serve(request, response);
		
		request = platform.getRequestBuilder()
			.method(HttpMethod.GET)
			.address(aliceAddress)
			.accept("application/ld+json")
			.build();
		
		response = platform.createResponse();
		
		platform.serve(request, response);
		
		Graph graph = response.getResource().asRdfSource().createGraph();
		
		
		assertTrue(graph.contains(aliceURI, Schema.givenName, literal("Alice")));
		
		
	}

	@Test
	public void testPutJsonld() throws Exception {
		String aliceAddress = "http://example.com/resources/person/alice";
		String aliceBody = 
			"{ "
			+ "\"@context\" : {"
			+ "   \"schema\" : \"http://schema.org/\" "
			+ "},"
			+ "\"@id\" : \"http://example.com/resources/person/alice\","
			+ "\"schema:givenName\" : \"Alice\","
			+ "\"schema:familyName\" : \"Jones\""
			+ 
			"}";
		
		URI aliceURI = uri(aliceAddress);
		
		LdpRequest request = platform.getRequestBuilder()
			.method(HttpMethod.PUT)
			.address(aliceAddress)
			.contentType("application/ld+json")
			.body(aliceBody)
			.build();
		
		LdpResponse response = platform.createResponse();
		
		platform.serve(request, response);
		
		request = platform.getRequestBuilder()
			.method(HttpMethod.GET)
			.address(aliceAddress)
			.accept("application/ld+json")
			.build();
		
		response = platform.createResponse();
		
		platform.serve(request, response);
		
		Graph graph = response.getResource().asRdfSource().createGraph();
		
		
		assertTrue(graph.contains(aliceURI, Schema.givenName, literal("Alice")));
		
		
	}

	@Test
	public void testPut() throws Exception {
		
		ResourceFile file = platform.getResourceBuilder()
			.contentLocation("http://example.com/resources/alpha/beta/gamma")
			.contentType("text/turtle")
			.rdfSource();
		platform.put(file, true);
		
		ResourceFile root = platform.get("http://example.com/resources/alpha/");
		assertTrue(root != null);
		
		assertEquals("http://example.com/resources/alpha/", root.getContentLocation());
		assertTrue(root.isBasicContainer());
	}

	@Test
	public void testGet() throws Exception {

		
		String resourceId = "http://example.com/person/alice";
		String body = "<http://example.com/person/alice> a <http://schema.org/Person> .";
		
		ResourceFile file = platform.getResourceBuilder()
			.contentLocation(resourceId)
			.contentType("text/turtle")
			.body(body)
			.rdfSource();
		
		platform.put(file, false);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		LdpResponse response = platform.createResponse(out);
		
		LdpRequest request = platform.getRequestBuilder()
			.method(HttpMethod.GET)
			.accept("text/turtle")
			.address(resourceId).build();
		
		platform.serve(request, response);
		
		RdfSource outFile = response.getResource().asRdfSource();
		
		
		assertEquals(resourceId, outFile.getContentLocation());
		
		Graph graph = outFile.createGraph();
		assertTrue(graph.contains(uri(resourceId), RDF.TYPE, Schema.Person));
		
		
		
	}
	

}
