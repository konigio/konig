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

public enum GoogleCloudSqlRegion implements HasURI {
	northamerica_northeast1(GCP.northamerica_northeast1),
	us_central(GCP.us_central),
	us_east1(GCP.us_east1),
	us_east4(GCP.us_east4),
	us_west1(GCP.us_west1),
	southamerica_east(GCP.southamerica_east),
	europe_west1(GCP.europe_west1),
	europe_west2(GCP.europe_west2),
	europe_west3(GCP.europe_west3),
	asia_east1(GCP.asia_east1),
	asia_northeast1(GCP.asia_northeast1),
	asia_south1(GCP.asia_south1),
	australia_southeast1(GCP.australia_southeast1);
	
	private URI uri;
	
	private GoogleCloudSqlRegion(URI uri) {
		this.uri = uri;
	}
	
	public URI getURI() {
		return uri;
	}

	public static GoogleCloudSqlRegion fromURI(URI uri) {
		for (GoogleCloudSqlRegion value : values()) {
			if (value.getURI().equals(uri)) {
				return value;
			}
		}
		throw new KonigException("Value not found: " + uri.stringValue());
	}

	public static GoogleCloudSqlRegion fromLocalName(String localName) {
		for (GoogleCloudSqlRegion value : values()) {
			if (value.getURI().getLocalName().equals(localName)) {
				return value;
			}
		}
		throw new KonigException("Value not found: " +localName);
	}
	
}
