package io.konig.core.vocab;

/*
 * #%L
 * Konig Core
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


import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public class OMCS {
	public static final String NAMESPACE = "http://www.konig.io/ns/omcs/";
	public static final String OMCS_TABLE_REFERENCE = "http://www.konig.io/ns/omcs/omcstableReference";
	
	public static final URI omcsTableReference = new URIImpl(OMCS_TABLE_REFERENCE);
	public static final URI omcsinstanceId = new URIImpl("http://www.konig.io/ns/omcs/instanceId");
	public static final URI omcsdatabaseId = new URIImpl("http://www.konig.io/ns/omcs/databaseId");
	public static final URI omcstableId = new URIImpl("http://www.konig.io/ns/omcs/tablesId");
}
