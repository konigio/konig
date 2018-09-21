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
	public static final String NOTIFICATION_INFO = "http://www.konig.io/ns/gcp/notificationInfo";
	public static final String NOTIFICATION_TOPIC = "http://www.konig.io/ns/gcp/topic";
	public static final String NOTIFICATION_EVENT_TYPES = "http://www.konig.io/ns/gcp/eventTypes";
	
	public static final URI tableReference = new URIImpl(TABLE_REFERENCE);
	public static final URI projectId = new URIImpl(PROJECT_ID);
	public static final URI datasetId = new URIImpl("http://www.konig.io/ns/gcp/datasetId");
	public static final URI tableId = new URIImpl("http://www.konig.io/ns/gcp/tableId");
	public static final URI preferredGcpDatasetId = new URIImpl("http://www.konig.io/ns/gcp/preferredGcpDatasetId");
	public static final URI notificationInfo = new URIImpl(NOTIFICATION_INFO);
	public static final URI notificationTopic = new URIImpl(NOTIFICATION_TOPIC);
	public static final URI notificationEventTypes = new URIImpl(NOTIFICATION_EVENT_TYPES);
	
	public static final URI description = new URIImpl("http://www.konig.io/ns/gcp/description");
	public static final URI name = new URIImpl(NAME);
	public static final URI location = new URIImpl(LOCATION);
	public static final URI storageClass = new URIImpl(STORAGE_CLASS);
	public static final URI externalDataConfiguration = new URIImpl("http://www.konig.io/ns/gcp/externalDataConfiguration");
	public static final URI sourceUris = new URIImpl("http://www.konig.io/ns/gcp/sourceUris");
	public static final URI sourceFormat = new URIImpl("http://www.konig.io/ns/gcp/sourceFormat");
	public static final URI csvOptions = new URIImpl("http://www.konig.io/ns/gcp/csvOptions");
	public static final URI skipLeadingRows = new URIImpl("http://www.konig.io/ns/gcp/skipLeadingRows");
	
	// Cloud SQL Terms

	public static final String BACKEND_TYPE = "http://www.konig.io/ns/gcp/backendType";
	public static final String INSTANCE_TYPE = "http://www.konig.io/ns/gcp/instanceType";
	public static final String DATABASE_VERSION = "http://www.konig.io/ns/gcp/databaseVersion";
	public static final String REGION = "http://www.konig.io/ns/gcp/region";
	public static final String SETTINGS = "http://www.konig.io/ns/gcp/settings";
	public static final String INSTANCE = "http://www.konig.io/ns/gcp/instance";
	public static final String DATABASE = "http://www.konig.io/ns/gcp/database";
	public static final String TIER = "http://www.konig.io/ns/gcp/tier";

	public static final URI GoogleCloudSqlInstance = new URIImpl("http://www.konig.io/ns/gcp/GoogleCloudSqlInstance");
	public static final URI backendType = new URIImpl(BACKEND_TYPE);
	public static final URI instanceType = new URIImpl(INSTANCE_TYPE);
	public static final URI databaseVersion = new URIImpl(DATABASE_VERSION);
	public static final URI region = new URIImpl(REGION);
	public static final URI tier = new URIImpl(TIER);
	public static final URI settings = new URIImpl(SETTINGS);
	// Regions
	public static final String NORTHAMERICA_NORTHEAST1 = "http://www.konig.io/ns/gcp/northamerica-northeast1";
	public static final String US_CENTRAL = "http://www.konig.io/ns/gcp/us-central";
	public static final String US_CENTRAL1 = "http://www.konig.io/ns/gcp/us-central1";
	public static final String US_EAST1 = "http://www.konig.io/ns/gcp/us-east1";
	public static final String US_EAST4 = "http://www.konig.io/ns/gcp/us-east4";
	public static final String US_WEST1 = "http://www.konig.io/ns/gcp/us-west1";
	public static final String SOUTHAMERICA_EAST1 = "http://www.konig.io/ns/gcp/southamerica-east1";
	public static final String EUROPE_WEST1 = "http://www.konig.io/ns/gcp/europe-west1";
	public static final String EUROPE_WEST2 = "http://www.konig.io/ns/gcp/europe-west2";
	public static final String EUROPE_WEST3 = "http://www.konig.io/ns/gcp/europe-west3";
	public static final String ASIA_EAST1 = "http://www.konig.io/ns/gcp/asia-east1";
	public static final String ASIA_NORTHEAST1 = "http://www.konig.io/ns/gcp/asia-northeast1";
	public static final String ASIA_SOUTH1 = "http://www.konig.io/ns/gcp/asia-south1";
	public static final String AUSTRALIA_SOUTHEAST1 = "http://www.konig.io/ns/gcp/australia-southeast1";

	public static final URI northamerica_northeast1 = new URIImpl(NORTHAMERICA_NORTHEAST1);
	public static final URI us_central = new URIImpl(US_CENTRAL);
	public static final URI us_central1 = new URIImpl(US_CENTRAL1);
	public static final URI us_east1 = new URIImpl(US_EAST1);
	public static final URI us_east4 = new URIImpl(US_EAST4);
	public static final URI us_west1 = new URIImpl(US_WEST1);
	public static final URI southamerica_east = new URIImpl(SOUTHAMERICA_EAST1);
	public static final URI europe_west1 = new URIImpl(EUROPE_WEST1);
	public static final URI europe_west2 = new URIImpl(EUROPE_WEST2);
	public static final URI europe_west3 = new URIImpl(EUROPE_WEST3);
	public static final URI asia_east1 = new URIImpl(ASIA_EAST1);
	public static final URI asia_northeast1 = new URIImpl(ASIA_NORTHEAST1);
	public static final URI asia_south1 = new URIImpl(ASIA_SOUTH1);
	public static final URI australia_southeast1 = new URIImpl(AUSTRALIA_SOUTHEAST1);
	
	public static final URI db_f1_micro = new URIImpl("http://www.konig.io/ns/gcp/db-f1-micro");
	public static final URI db_g1_small = new URIImpl("http://www.konig.io/ns/gcp/db-g1-small");
	public static final URI db_n1_standard_1 = new URIImpl("http://www.konig.io/ns/gcp/db-n1-standard-1");
	public static final URI db_n1_standard_2 = new URIImpl("http://www.konig.io/ns/gcp/db-n1-standard-2");
	public static final URI db_n1_standard_4 = new URIImpl("http://www.konig.io/ns/gcp/db-n1-standard-4");
	public static final URI db_n1_standard_8 = new URIImpl("http://www.konig.io/ns/gcp/db-n1-standard-8");
	public static final URI db_n1_standard_16 = new URIImpl("http://www.konig.io/ns/gcp/db-n1-standard-16");
	public static final URI db_n1_standard_32 = new URIImpl("http://www.konig.io/ns/gcp/db-n1-standard-32");
	public static final URI db_n1_standard_64 = new URIImpl("http://www.konig.io/ns/gcp/db-n1-standard-64");
	public static final URI db_n1_highmem_2 = new URIImpl("http://www.konig.io/ns/gcp/db-n1-highmem-2");
	public static final URI db_n1_highmem_4 = new URIImpl("http://www.konig.io/ns/gcp/db-n1-highmem-4");
	public static final URI db_n1_highmem_8 = new URIImpl("http://www.konig.io/ns/gcp/db-n1-highmem-8");
	public static final URI db_n1_highmem_16 = new URIImpl("http://www.konig.io/ns/gcp/db-n1-highmem-16");
	public static final URI db_n1_highmem_32 = new URIImpl("http://www.konig.io/ns/gcp/db-n1-highmem-32");
	public static final URI db_n1_highmem_64 = new URIImpl("http://www.konig.io/ns/gcp/db-n1-highmem-64");
	

	public static final URI FIRST_GEN = new URIImpl("http://www.konig.io/ns/gcp/FIRST_GEN");
	public static final URI SECOND_GEN = new URIImpl("http://www.konig.io/ns/gcp/SECOND_GEN");

	public static final URI MYSQL_5_7 = new URIImpl("http://www.konig.io/ns/gcp/MYSQL_5_7");
	public static final URI MYSQL_5_6 = new URIImpl("http://www.konig.io/ns/gcp/MYSQL_5_6");
	public static final URI POSTGRES_9_6 = new URIImpl("http://www.konig.io/ns/gcp/POSTGRES_9_6");
	
	
	
	
	public static final URI CLOUD_SQL_INSTANCE = new URIImpl("http://www.konig.io/ns/gcp/CLOUD_SQL_INSTANCE");
	public static final URI READ_REPLICA_INSTANCE = new URIImpl("http://www.konig.io/ns/gcp/READ_REPLICA_INSTANCE");
	public static final URI ON_PREMISES_INSTANCE = new URIImpl("http://www.konig.io/ns/gcp/ON_PREMISES_INSTANCE");
	
	
	
	
	
}
