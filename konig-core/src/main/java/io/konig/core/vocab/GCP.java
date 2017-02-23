package io.konig.core.vocab;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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

public class GCP {

	public static final String NAMESPACE = "http://www.konig.io/ns/gcp/";
	public static final String TABLE_REFERENCE = "http://www.konig.io/ns/gcp/tableReference";
	public static final String STORAGE_CLASS = "http://www.konig.io/ns/gcp/storageClass";
	public static final String NAME = "http://www.konig.io/ns/gcp/name";
	public static final String LOCATION = "http://www.konig.io/ns/gcp/location";
	public static final String PROJECT_ID = "http://www.konig.io/ns/gcp/projectId";
	
	public static final URI tableReference = new URIImpl(TABLE_REFERENCE);
	public static final URI projectId = new URIImpl(PROJECT_ID);
	public static final URI datasetId = new URIImpl("http://www.konig.io/ns/gcp/datasetId");
	public static final URI tableId = new URIImpl("http://www.konig.io/ns/gcp/tableId");
	public static final URI description = new URIImpl("http://www.konig.io/ns/gcp/description");
	public static final URI name = new URIImpl(NAME);
	public static final URI location = new URIImpl(LOCATION);
	public static final URI storageClass = new URIImpl(STORAGE_CLASS);
	public static final URI externalDataConfiguration = new URIImpl("http://www.konig.io/ns/gcp/externalDataConfiguration");
	public static final URI sourceUris = new URIImpl("http://www.konig.io/ns/gcp/sourceUris");
	public static final URI sourceFormat = new URIImpl("http://www.konig.io/ns/gcp/sourceFormat");
	public static final URI csvOptions = new URIImpl("http://www.konig.io/ns/gcp/csvOptions");
	public static final URI skipLeadingRows = new URIImpl("http://www.konig.io/ns/gcp/skipLeadingRows");
	
}
