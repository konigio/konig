package io.konig.core;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.impl.KonigLiteral;

public class KonigTest {
	private int bnodeCount = 1;
	
	protected URI uri(String value) {
		return new URIImpl(value);
	}
	
	protected BNode bnode() {
		return new BNodeImpl("bnode" + (bnodeCount++));
	}
	
	protected BNode bnode(String value) {
		return new BNodeImpl(value);
	}
	
	protected Literal literal(String value) {
		return new KonigLiteral(value);
	}
	
	protected ContextBuilder personContext() {
		ContextBuilder builder = new ContextBuilder("http://example.com/ctx/person");
		
		builder
			.namespace("schema", "http://schema.org/")
			.namespace("xsd", "http://www.w3.org/2001/XMLSchema#")
			.type("Address", "schema:PostalAddress")
			.type("Person", "schema:Person")
			.property("streetAddress", "schema:streetAddress", "xsd:string")
			.property("addressLocality", "schema:addressLocality", "xsd:string")
			.property("addressRegion", "schema:addressRegion", "xsd:string")
			.property("givenName", "schema:givenName", "xsd:string")
			.property("familyName", "schema:familyName", "xsd:string")
			.objectProperty("address", "schema:address")
			.objectProperty("parent", "schema:parent")
			.objectProperty("child", "schema:children");
		
		return builder;
	}

}
