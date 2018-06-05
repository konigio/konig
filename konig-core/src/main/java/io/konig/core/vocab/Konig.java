package io.konig.core.vocab;

/*
 * #%L
 * Konig Core
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

public class Konig {
	
	public static final String NAMESPACE = "http://www.konig.io/ns/core/";
	
	public static final String JAVA_NAMESPACE = "http://www.konig.io/ns/java/";
	public static final String ACTIVIY_BASE_URL = "http://www.konig.io/activity/";
	
	public static final String DATA_SOURCE = "http://www.konig.io/ns/core/DataSource"; 
	public static final String GOOGLE_BIG_QUERY_TABLE = "http://www.konig.io/ns/core/GoogleBigQueryTable";
	public static final String SHAPE_DATA_SOURCE = "http://www.konig.io/ns/core/shapeDataSource";
	public static final String INPUT_SHAPE_OF = "http://www.konig.io/ns/core/inputShapeOf";
	public static final String IRI_TEMPLATE = "http://www.konig.io/ns/core/iriTemplate";
	public static final String IRI_FORMULA = "http://www.konig.io/ns/core/iriFormula";
	public static final String TABULAR_FIELD_NAMESPACE = "http://www.konig.io/ns/core/tabularFieldNamespace";
	public static final String BIG_QUERY_SOURCE = "http://www.konig.io/ns/core/bigQuerySource";
	public static final String EQUIVALENT_PATH = "http://www.konig.io/ns/core/equivalentPath";
	public static final String SOURCE_PATH = "http://www.konig.io/ns/core/sourcePath";

	public static final String MEASURE = "http://www.konig.io/ns/core/measure";
	public static final String ATTRIBUTE = "http://www.konig.io/ns/core/attribute";
	public static final String DIMENSION = "http://www.konig.io/ns/core/dimension";
	public static final URI DERIVEDFROM = new URIImpl("http://www.konig.io/ns/core/derivedfrom");
	
	// ChangeSet terms
	public static final URI Dictum = new URIImpl("http://www.konig.io/ns/core/Dictum");
	public static final URI Falsehood = new URIImpl("http://www.konig.io/ns/core/Falsehood");
	public static final URI KeyValue = new URIImpl("http://www.konig.io/ns/core/KeyValue");
	public static final URI KeyTerm = new URIImpl("http://www.konig.io/ns/core/KeyTerm");
	
	// Data generator terms
	public static final URI SyntheticGraphConstraints = new URIImpl("http://www.konig.io/ns/core/SyntheticGraphConstraints");
	public static final URI SyntheticClassConstraints = new URIImpl("http://www.konig.io/ns/core/SyntheticClassConstraints");
	public static final URI SyntheticShapeConstraints = new URIImpl("http://www.konig.io/ns/core/SyntheticShapeConstraints");
	public static final URI classConstraints = new URIImpl("http://www.konig.io/ns/core/classConstraints");
	public static final URI shapeConstraints = new URIImpl("http://www.konig.io/ns/core/shapeConstraints");
	public static final URI constrainedClass = new URIImpl("http://www.konig.io/ns/core/constrainedClass");
	public static final URI constrainedShape = new URIImpl("http://www.konig.io/ns/core/constrainedShape");
	public static final URI classInstanceCount = new URIImpl("http://www.konig.io/ns/core/classInstanceCount");
	public static final URI shapeInstanceCount = new URIImpl("http://www.konig.io/ns/core/shapeInstanceCount");
	
	
	// Generic Data Modeling

	public static final URI id = new URIImpl("http://www.konig.io/ns/core/id");
	public static final URI mediaTypeBaseName = new URIImpl("http://www.konig.io/ns/core/mediaTypeBaseName");
	public static final URI rdbmsOriginShape = new URIImpl("http://www.konig.io/ns/core/rdbmsOriginShape");
	public static final URI preferredJsonSchema = new URIImpl("http://www.konig.io/ns/core/preferredJsonSchema");
	public static final URI avroSchemaRendition = new URIImpl("http://www.konig.io/ns/core/avroSchemaRendition");
	public static final URI PreferredClass = new URIImpl("http://www.konig.io/ns/core/PreferredClass");
	public static final URI formula = new URIImpl("http://www.konig.io/ns/core/formula");
	public static final URI preferredJsonldContext = new URIImpl("http://www.konig.io/ns/core/preferredJsonldContext");
	public static final URI hasShape = new URIImpl("http://www.konig.io/ns/core/hasShape");
	public static final URI defaultShapeFor = new URIImpl("http://www.konig.io/ns/core/defaultShapeFor");
	public static final URI equivalentPath = new URIImpl(EQUIVALENT_PATH);
	public static final URI LoadModelFromSpreadsheet = new URIImpl("http://www.konig.io/ns/core/LoadModelFromSpreadsheet");
	public static final URI iriTemplate = new URIImpl(IRI_TEMPLATE);
	public static final URI iriFormula = new URIImpl(IRI_FORMULA);
	public static final URI idFormat = new URIImpl("http://www.konig.io/ns/core/idFormat");
	public static final URI Curie = new URIImpl("http://www.konig.io/ns/core/Curie");
	public static final URI LocalName = new URIImpl("http://www.konig.io/ns/core/LocalName");
	public static final URI FullyQualifiedIri = new URIImpl("http://www.konig.io/ns/core/FullyQualifiedIri");
	public static final URI modified = new URIImpl("http://www.konig.io/ns/core/modified");
	public static final URI deleted = new URIImpl("http://www.konig.io/ns/core/deleted");
	public static final URI derivedProperty = new URIImpl("http://www.konig.io/ns/core/derivedProperty");
	public static final URI NullShape = new URIImpl("http://www.konig.io/shapes/NullShape");
	public static final URI tabularFieldNamespace = new URIImpl(TABULAR_FIELD_NAMESPACE);
	
	public static final URI Undefined = new URIImpl("http://www.konig.io/ns/core/Undefined");
	//public static final URI DerivedShape = new URIImpl("http://www.konig.io/shapes/DerivedShape");
	
	// Data Sources

	public static final URI DataSource = new URIImpl(DATA_SOURCE);
	public static final URI CurrentState = new URIImpl("http://www.konig.io/ns/core/CurrentState");
	public static final URI AuthoritativeDataSource = new URIImpl("http://www.konig.io/ns/core/AuthoritativeDataSource");
	public static final URI StagingDataSource = new URIImpl("http://www.konig.io/ns/core/StagingDataSource");
	public static final URI ReportingDataSource = new URIImpl("http://www.konig.io/ns/core/ReportingDataSource");
	public static final URI GoogleCloudStorageBucket = new URIImpl("http://www.konig.io/ns/core/GoogleCloudStorageBucket");
	public static final URI GoogleCloudSqlTable = new URIImpl("http://www.konig.io/ns/core/GoogleCloudSqlTable");
	public static final URI OracleTable = new URIImpl("http://www.konig.io/ns/core/OracleTable");
	public static final URI AwsAuroraTable = new URIImpl("http://www.konig.io/ns/core/AwsAurora");
	public static final URI AwsAuroraView = new URIImpl("http://www.konig.io/ns/core/AwsAuroraView");
	public static final URI S3Bucket = new URIImpl("http://www.konig.io/ns/core/S3Bucket");
	public static final URI shapeDataSource = new URIImpl(SHAPE_DATA_SOURCE);
	public static final URI bigQuerySource = new URIImpl(BIG_QUERY_SOURCE);
	public static final URI primaryKey = new URIImpl("http://www.konig.io/ns/core/primaryKey");
	public static final URI uniqueKey = new URIImpl("http://www.konig.io/ns/core/uniqueKey");
	public static final URI syntheticKey = new URIImpl("http://www.konig.io/ns/core/syntheticKey");
	// Shape Transform Vocabulary
	
	public static final URI sourceShape = new URIImpl("http://www.konig.io/ns/core/sourceShape");
	public static final URI updateWhen = new URIImpl("http://www.konig.io/ns/core/updateWhen");
	
	// Fact Modeling

	public static final URI measure = new URIImpl(MEASURE);
	public static final URI dimension = new URIImpl(DIMENSION);
	public static final URI attribute = new URIImpl(ATTRIBUTE);
	public static final URI variable = new URIImpl("http://www.konig.io/ns/core/variable");
	public static final URI stereotype = new URIImpl("http://www.konig.io/ns/core/stereotype");
	public static final URI securityClassification = new URIImpl("http://www.konig.io/ns/core/securityClassification");
	public static final URI qualifiedSecurityClassification = new URIImpl("http://www.konig.io/ns/core/qualifiedSecurityClassification");
	public static final URI totalCount = new URIImpl("http://www.konig.io/ns/core/totalCount");
	public static final URI durationUnit = new URIImpl("http://www.konig.io/ns/core/durationUnit");
	public static final URI aggregationOf = new URIImpl("http://www.konig.io/ns/core/aggregationOf");
	public static final URI rollUpBy = new URIImpl("http://www.konig.io/ns/core/rollUpBy");
	public static final URI fromAggregationSource = new URIImpl("http://www.konig.io/ns/core/fromAggregationSource");
	public static final URI timeInterval = new URIImpl("http://www.konig.io/ns/core/timeInterval");
	public static final URI sourcePath = new URIImpl(SOURCE_PATH);
	public static final URI partitionOf = new URIImpl("http://www.konig.io/ns/core/partitionOf");
	public static final URI TimeUnit = new URIImpl("http://www.konig.io/ns/core/TimeUnit");
	public static final URI TimeInterval = new URIImpl("http://www.konig.io/ns/core/TimeInterval");
	public static final URI intervalStart = new URIImpl("http://www.konig.io/ns/core/intervalStart");
	
	// Time Units
	
	public static final URI Day = new URIImpl("http://www.konig.io/ns/core/Day");
	public static final URI Week = new URIImpl("http://www.konig.io/ns/core/Week");
	public static final URI Month = new URIImpl("http://www.konig.io/ns/core/Month");
	public static final URI Year = new URIImpl("http://www.konig.io/ns/core/Year");
	

	// Project Management terms

	public static final URI GooglePubSubTopic = new URIImpl("http://www.konig.io/ns/core/GooglePubSubTopic");
	public static final URI projectName = new URIImpl("http://www.konig.io/ns/core/projectName");
	public static final URI topicName = new URIImpl("http://www.konig.io/ns/core/topicName");
	public static final URI acceptsShape = new URIImpl("http://www.konig.io/ns/core/acceptsShape");
	public static final URI jsonTemplate = new URIImpl("http://www.konig.io/ns/core/jsonTemplate");
	public static final URI topicProject = new URIImpl("http://www.konig.io/ns/core/topicProject");
	public static final URI projectTopic = new URIImpl("http://www.konig.io/ns/core/projectTopic");
	public static final URI datasetTemplate = new URIImpl("http://www.konig.io/ns/core/datasetTemplate");

	
	// Google Cloud Platform terms

	public static final URI GoogleCloudProject = new URIImpl("http://www.konig.io/ns/core/GoogleCloudProject");
	public static final URI GoogleBigQueryTable = new URIImpl(GOOGLE_BIG_QUERY_TABLE);
	public static final URI GoogleBigQueryView = new URIImpl( "http://www.konig.io/ns/core/GoogleBigQueryView");
	public static final URI GoogleAnalytics = new URIImpl("http://www.konig.io/ns/core/GoogleAnalytics");
	public static final URI tableShape = new URIImpl("http://www.konig.io/ns/core/tableShape");
	public static final URI projectDataset = new URIImpl("http://www.konig.io/ns/core/projectDataset");
	public static final URI datasetTable = new URIImpl("http://www.konig.io/ns/core/datasetTable");
//	public static final URI projectId = new URIImpl("http://www.konig.io/ns/core/projectId");
//	public static final URI datasetId = new URIImpl("http://www.konig.io/ns/core/datasetId");
//	public static final URI tableId = new URIImpl("http://www.konig.io/ns/core/tableId");
	public static final URI GenerateEnumTables = new URIImpl("http://www.konig.io/ns/core/GenerateEnumTables");
	public static final URI bigQueryTableId = new URIImpl("http://www.konig.io/ns/core/bigQueryTableId");
   // public static final URI SourceShape=new URIImpl("http://www.konig.io/ns/core/SourceShape");
 //   public static final URI TargetShape=new URIImpl("http://www.konig.io/ns/core/TargetShape");
	public static final URI inputShapeOf = new URIImpl("http://www.konig.io/ns/core/inputShapeOf");
	

	public static final URI decimalPrecision = new URIImpl("http://www.konig.io/ns/core/decimalPrecision");
	public static final URI decimalScale = new URIImpl("http://www.konig.io/ns/core/decimalScale");
	public static final URI environment = new URIImpl("http://www.konig.io/ns/core/environment");
	
	public static URI javaMethodId(Class<?> javaClass, String methodName) {
		StringBuilder builder = new StringBuilder();
		builder.append(Konig.JAVA_NAMESPACE);
		builder.append(javaClass.getName());
		builder.append("#");
		builder.append(methodName);
		return new URIImpl(builder.toString());
	}
}
