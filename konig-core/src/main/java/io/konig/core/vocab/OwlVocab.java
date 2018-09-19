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

public class OwlVocab {

	public static final URI onDatatype = new URIImpl("http://www.w3.org/2002/07/owl#onDatatype");
	public static final URI withRestrictions = new URIImpl("http://www.w3.org/2002/07/owl#withRestrictions");
	public static final URI NamedIndividual = new URIImpl("http://www.w3.org/2002/07/owl#NamedIndividual");
	public static final URI ReflexiveProperty = new URIImpl("http://www.w3.org/2002/07/owl#ReflexiveProperty");
	public static final URI IrreflexiveProperty = new URIImpl("http://www.w3.org/2002/07/owl#IrreflexiveProperty");
	public static final URI AsymetricProperty = new URIImpl("http://www.w3.org/2002/07/owl#AsymetricProperty");
	public static final URI maxQualifiedCardinality = new URIImpl("http://www.w3.org/2002/07/owl#maxQualifiedCardinality");
}
