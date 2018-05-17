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
	public static final String TABLE_REFERENCE = "http://www.konig.io/ns/aws/tableReference";
	public static final URI tableReference = new URIImpl(TABLE_REFERENCE);
	public static final URI awsAuroraHost = new URIImpl("http://www.konig.io/ns/aws/awsAuroraHost");
	public static final URI awsSchema = new URIImpl("http://www.konig.io/ns/aws/awsSchema");
	public static final URI awsTableName = new URIImpl("http://www.konig.io/ns/aws/awsTableName");
	public static final URI CloudFormationTemplate = new URIImpl("http://www.konig.io/ns/aws/CloudFormationTemplate");
	public static final String STACK_NAME = "http://www.konig.io/ns/aws/stackName";
	public static final String TEMPLATE = "http://www.konig.io/ns/aws/template";
	public static final URI stackName = new URIImpl(STACK_NAME);
	public static final URI template = new URIImpl(TEMPLATE);	
	public static final String BUCKET_KEY = "http://www.konig.io/ns/aws/bucketKey";
	public static final String BUCKET_NAME = "http://www.konig.io/ns/aws/bucketName";
	public static final String REGION = "http://www.konig.io/ns/aws/bucketRegion";
	public static final String BUCKET_MEDIA_TYPE = "http://www.konig.io/ns/aws/bucketMediaType";
	public static final String NOTIFICATION_CONFIGURATION = "http://www.konig.io/ns/aws/notificationConfiguration";
	public static final String TOPIC_CONFIGURATION = "http://www.konig.io/ns/aws/topicConfiguration";
	public static final String QUEUE_CONFIGURATION = "http://www.konig.io/ns/aws/queueConfiguration";
	public static final String TOPIC = "http://www.konig.io/ns/aws/topic";
	public static final String QUEUE = "http://www.konig.io/ns/aws/queue";
	public static final String EVENT_TYPE = "http://www.konig.io/ns/aws/eventType";
	public static final String ACCOUNT_ID = "http://www.konig.io/ns/aws/accountId";
	public static final String RESOURCE_NAME = "http://www.konig.io/ns/aws/resourceName";
	public static final String TOPIC_REGION = "http://www.konig.io/ns/aws/region";
	public static final String AWS_REGION = "http://www.konig.io/ns/aws/region";
	public static final String AWS_AURORA_HOST = "http://www.konig.io/ns/aws/awsAuroraHost";
	public static final String AWS_SCHEMA = "http://www.konig.io/ns/aws/awsSchema";
	public static final String AWS_TABLE_NAME = "http://www.konig.io/ns/aws/awsTableName";
	public static final URI notificationConfiguration= new URIImpl(NOTIFICATION_CONFIGURATION);
	public static final URI queueConfiguration= new URIImpl(QUEUE_CONFIGURATION);
	public static final URI topic= new URIImpl(TOPIC);
	public static final URI eventType= new URIImpl(EVENT_TYPE);
	public static final URI accountId= new URIImpl(EVENT_TYPE);
	public static final URI resourceName= new URIImpl(EVENT_TYPE);
	public static final URI region= new URIImpl(EVENT_TYPE);
	public static final URI awsRegion= new URIImpl(AWS_REGION);

	public static final URI topicConfiguration= new URIImpl(TOPIC_CONFIGURATION);

}
