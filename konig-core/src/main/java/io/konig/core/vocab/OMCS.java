package io.konig.core.vocab;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

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


public class OMCS {
	public static final String NAMESPACE = "http://www.konig.io/ns/omcs/";
	
	public static final String TABLE_REFERENCE = "http://www.konig.io/ns/omcs/tableReference";
	
	public static final URI tableReference = new URIImpl(TABLE_REFERENCE);
	public static final URI host = new URIImpl("http://www.konig.io/ns/omcs/oracleHost");
	public static final URI instanceId = new URIImpl("http://www.konig.io/ns/omcs/omcsInstanceId");
	public static final URI schema = new URIImpl("http://www.konig.io/ns/omcs/oracleSchema");
	public static final URI tableId = new URIImpl("http://www.konig.io/ns/omcs/omcsTableId");

}
