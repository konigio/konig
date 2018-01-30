package io.konig.gcp.datasource;

/*
 * #%L
 * Konig Google Cloud Platform Model
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

import io.konig.core.HasURI;
import io.konig.core.KonigException;
import io.konig.core.vocab.GCP;

public enum GoogleCloudSqlInstanceType implements HasURI {
	CLOUD_SQL_INSTANCE(GCP.CLOUD_SQL_INSTANCE),
	READ_REPLICA_INSTANCE(GCP.READ_REPLICA_INSTANCE),
	ON_PREMISES_INSTANCE(GCP.ON_PREMISES_INSTANCE) ;
	
	private URI uri;
	
	private GoogleCloudSqlInstanceType(URI uri) {
		this.uri = uri;
	}

	public URI getURI() {
		return uri;
	}

	public static GoogleCloudSqlInstanceType fromURI(URI uri) {
		for (GoogleCloudSqlInstanceType value : values()) {
			if (value.getURI().equals(uri)) {
				return value;
			}
		}
		throw new KonigException("Value not found: " + uri.stringValue());
	}

	public static GoogleCloudSqlInstanceType fromLocalName(String localName) {
		for (GoogleCloudSqlInstanceType value : values()) {
			if (value.getURI().getLocalName().equals(localName)) {
				return value;
			}
		}
		throw new KonigException("Value not found: " + localName);
	}
	
	
	
}
