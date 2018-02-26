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

public class AWS {

	public static final String NAMESPACE = "http://www.konig.io/ns/aws/";
	public static final URI DbCluster = new URIImpl("http://www.konig.io/ns/aws/DbCluster");
	public static final URI dbClusterId = new URIImpl("http://www.konig.io/ns/aws/dbClusterId");
	public static final URI dbClusterName = new URIImpl("http://www.konig.io/ns/aws/dbClusterName");
	public static final URI engine = new URIImpl("http://www.konig.io/ns/aws/engine");
	public static final URI engineVersion = new URIImpl("http://www.konig.io/ns/aws/engineVersion");
	public static final URI instanceClass = new URIImpl("http://www.konig.io/ns/aws/instanceClass");
	public static final URI availabilityZone = new URIImpl("http://www.konig.io/ns/aws/availabilityZone");
	public static final URI zone = new URIImpl("http://www.konig.io/ns/aws/zone");
	public static final URI backupRetentionPeriod = new URIImpl("http://www.konig.io/ns/aws/backupRetentionPeriod");
	public static final URI databaseName = new URIImpl("http://www.konig.io/ns/aws/databaseName");
	public static final URI dbSubnetGroupName = new URIImpl("http://www.konig.io/ns/aws/dbSubnetGroupName");
	public static final URI preferredBackupWindow = new URIImpl("http://www.konig.io/ns/aws/preferredBackupWindow");
	public static final URI preferredMaintenanceWindow = new URIImpl("http://www.konig.io/ns/aws/preferredMaintenanceWindow");
	public static final URI replicationSourceIdentifier = new URIImpl("http://www.konig.io/ns/aws/replicationSourceIdentifier");
	public static final URI storageEncrypted = new URIImpl("http://www.konig.io/ns/aws/storageEncrypted");
	
	public static final URI awsAuroraHost = new URIImpl("http://www.konig.io/ns/aws/awsAuroraHost");
	public static final URI awsSchema = new URIImpl("http://www.konig.io/ns/aws/awsSchema");
	public static final URI awsTableName = new URIImpl("http://www.konig.io/ns/aws/awsTableName");
}
