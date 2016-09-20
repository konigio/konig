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

public class KOL {

	public static final String NAMESPACE = "http://www.konig.io/ns/kol/";
	public static final URI knownValue = new URIImpl("http://www.konig.io/ns/kol/knownValue");
	public static final URI id = new URIImpl("http://www.konig.io/ns/kol/id");
	public static final URI mediaTypeBaseName = new URIImpl("http://www.konig.io/ns/kol/mediaTypeBaseName");
	public static final URI jsonSchemaRendition = new URIImpl("http://www.konig.io/ns/kol/jsonSchemaRendition");
	public static final URI avroSchemaRendition = new URIImpl("http://www.konig.io/ns/kol/avroSchemaRendition");
	public static final URI PreferredClass = new URIImpl("http://www.konig.io/ns/kol/PreferredClass");
	public static final URI localKey = new URIImpl("http://www.konig.io/ns/kol/localKey");

	public static final URI equivalentRelationalShape = new URIImpl("http://www.konig.io/ns/kol/equivalentRelationalShape");
	
}
