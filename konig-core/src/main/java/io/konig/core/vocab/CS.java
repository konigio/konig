package io.konig.core.vocab;

/*
 * #%L
 * konig-core
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


import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public class CS {
	
	public static final URI Dictum = new URIImpl("http://www.konig.io/ns/kcs/Dictum");
	public static final URI Falsity = new URIImpl("http://www.konig.io/ns/kcs/Falsity");
	public static final URI KeyValue = new URIImpl("http://www.konig.io/ns/kcs/KeyValue");
	public static final URI KeyTerm = new URIImpl("http://www.konig.io/ns/kcs/KeyTerm");
	
}
