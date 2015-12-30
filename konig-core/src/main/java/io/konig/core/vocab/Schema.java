package io.konig.core.vocab;

/*
 * #%L
 * konig-core
 * %%
 * Copyright (C) 2015 Gregory McFall
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
	public static final URI name = new URIImpl("http://schema.org/name");
	public static final URI children = new URIImpl("http://schema.org/children");
	public static final URI parent = new URIImpl("http://schema.org/parent");
	public static final URI email = new URIImpl("http://schema.org/email");
	public static final URI knows = new URIImpl("http://schema.org/knows");

}
