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

public class LDP {

	public static final String NAMESPACE = "http://www.w3.org/ns/ldp/";
	public static final URI BasicContainer = new URIImpl("http://www.w3.org/ns/ldp/BasicContainer");
	public static final URI DirectContainer = new URIImpl("http://www.w3.org/ns/ldp/DirectContainer");
	public static final URI contains = new URIImpl("http://www.w3.org/ns/ldp/contains");
}
