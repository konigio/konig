package io.konig.core.vocab;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public class Schema {

	public static final URI Person = new URIImpl("http://schema.org/Person");
	public static final URI PostalAddress = new URIImpl("http://schema.org/PostalAddress");
	public static final URI address = new URIImpl("http://schema.org/address");
	public static final URI streetAddress = new URIImpl("http://schema.org/streetAddress");
	public static final URI addressLocality = new URIImpl("http://schema.org/addressLocality");
	public static final URI addressRegion = new URIImpl("http://schema.org/addressRegion");
	public static final URI givenName = new URIImpl("http://schema.org/givenName");
	public static final URI familyName = new URIImpl("http://schema.org/familyName");
	public static final URI children = new URIImpl("http://schema.org/children");
	public static final URI parent = new URIImpl("http://schema.org/parent");
	public static final URI email = new URIImpl("http://schema.org/email");
	public static final URI knows = new URIImpl("http://schema.org/knows");

}
