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

public class DCL {
	public static final URI Public = new URIImpl("http://schema.pearson.com/ns/dcl/DCL1");
	public static final URI InternalUse = new URIImpl("http://schema.pearson.com/ns/dcl/DCL2");
	public static final URI Confidential = new URIImpl("http://schema.pearson.com/ns/dcl/DCL3");
	public static final URI Restricted = new URIImpl("http://schema.pearson.com/ns/dcl/DCL4");
	public static final String NAMESPACE = "http://schema.pearson.com/ns/dcl/";
}
