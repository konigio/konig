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

public enum GoogleCloudSqlTier implements HasURI {
	db_f1_micro(GCP.db_f1_micro),
	db_g1_small(GCP.db_g1_small),
	db_n1_standard_1(GCP.db_n1_standard_1),
	db_n1_standard_2(GCP.db_n1_standard_2),
	db_n1_standard_4(GCP.db_n1_standard_4),
	db_n1_standard_8(GCP.db_n1_standard_8),
	db_n1_standard_16(GCP.db_n1_standard_16),
	db_n1_standard_32(GCP.db_n1_standard_32),
	db_n1_standard_64(GCP.db_n1_standard_64),
	db_n1_highmem_2(GCP.db_n1_highmem_2),
	db_n1_highmem_4(GCP.db_n1_highmem_4),
	db_n1_highmem_8(GCP.db_n1_highmem_8),
	db_n1_highmem_16(GCP.db_n1_highmem_16),
	db_n1_highmem_32(GCP.db_n1_highmem_32),
	db_n1_highmem_64(GCP.db_n1_highmem_64);
	
	private URI uri;
	
	private GoogleCloudSqlTier(URI uri) {
		this.uri = uri;
	}
	
	public URI getURI() {
		return uri;
	}

	public static GoogleCloudSqlTier fromURI(URI uri) {
		for (GoogleCloudSqlTier value : values()) {
			if (value.getURI().equals(uri)) {
				return value;
			}
		}
		throw new KonigException("Value not found: " + uri.stringValue());
	}

	public static GoogleCloudSqlTier fromLocalName(String localName) {
		for (GoogleCloudSqlTier value : values()) {
			if (value.getURI().getLocalName().equals(localName)) {
				return value;
			}
		}
		throw new KonigException("Value not found: " +localName);
	}
	
}
