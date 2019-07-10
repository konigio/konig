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
	public static final URI NAMESPACE_ID = new URIImpl("http://www.konig.io/ns/core/");
	
	public static final String JAVA_NAMESPACE = "http://www.konig.io/ns/java/";
	public static final String ACTIVIY_BASE_URL = "http://www.konig.io/activity/";
	
	public static final String GOOGLE_BIG_QUERY_TABLE = "http://www.konig.io/ns/core/GoogleBigQueryTable";
	public static final String SHAPE_DATA_SOURCE = "http://www.konig.io/ns/core/shapeDataSource";
	public static final String INPUT_SHAPE_OF = "http://www.konig.io/ns/core/inputShapeOf";
	public static final String IRI_TEMPLATE = "http://www.konig.io/ns/core/iriTemplate";
	public static final String IRI_FORMULA = "http://www.konig.io/ns/core/iriFormula";
	public static final String TABULAR_FIELD_NAMESPACE = "http://www.konig.io/ns/core/tabularFieldNamespace";
	public static final String BIG_QUERY_SOURCE = "http://www.konig.io/ns/core/bigQuerySource";
	public static final String EQUIVALENT_PATH = "http://www.konig.io/ns/core/equivalentPath";
	public static final String SOURCE_PATH = "http://www.konig.io/ns/core/sourcePath";
	public static final String TERM_STATUS = "http://www.konig.io/ns/core/termStatus";
	
	public static final String RELATIVE_PATH = "http://www.konig.io/ns/core/relativePath";
	public static final String BASE_PROJECT = "http://www.konig.io/ns/core/baseProject";
	public static final String DDL_FILE = "http://www.konig.io/ns/core/ddlFile";
	public static final String TRANSFORM_FILE = "http://www.konig.io/ns/core/transformFile";

	public static final String ShapeMaxRowLength = "http://www.konig.io/ns/core/shapeMaxRowLength";
	public static final String TargetDatasource = "http://www.konig.io/ns/core/targetDatasource";
	public static final String MaxRowLength = "http://www.konig.io/ns/core/maxRowLength";

	public static final String MEASURE = "http://www.konig.io/ns/core/measure";
	public static final String ATTRIBUTE = "http://www.konig.io/ns/core/attribute";
	public static final String DIMENSION = "http://www.konig.io/ns/core/dimension";
	public static final String SHAPE_PROCESSING = "http://www.konig.io/ns/core/shapeProcessing";
	public static final String EXPLICIT_DERIVED_FROM = "http://www.konig.io/ns/core/explicitDerivedFrom";
	public static final String DERIVED_PROPERTY = "http://www.konig.io/ns/core/derivedProperty";
	public static final String ID = "http://www.konig.io/ns/core/id";
	
	public static class Terms {
		public static final String DataSource = "http://www.konig.io/ns/core/DataSource"; 
		public static final String dataSourceShape = "http://www.konig.io/ns/core/dataSourceShape";
		public static final String nodeShapeCube = "http://www.konig.io/ns/core/nodeShapeCube";
		public static final String propertyPath = "http://www.konig.io/ns/core/propertyPath";
		public static final String propertySource = "http://www.konig.io/ns/core/propertySource";
		public static final String generatedFrom = "http://www.konig.io/ns/core/generatedFrom";
		public static final String generatorInputOf = "http://www.konig.io/ns/core/generatorInputOf";
		public static final String generatorInput = "http://www.konig.io/ns/core/generatorInput";
		public static final String generatorOutput = "http://www.konig.io/ns/core/generatorOutput";
		public static final String DatasourceProperty = "http://www.konig.io/ns/core/DatasourceProperty";
		public static final String PropertyGenerator = "http://www.konig.io/ns/core/PropertyGenerator";
		public static final String receivesDataFrom = "http://www.konig.io/ns/core/receivesDataFrom";
		public static final String etlPattern = "http://www.konig.io/ns/core/etlPattern";
		public static final String OverlayPattern = "http://www.konig.io/ns/core/OverlayPattern";
	}
	
	// ChangeSet terms
	public static final URI Assertion = new URIImpl("http://www.konig.io/ns/core/Assertion");
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
	public static final URI TermStatus = new URIImpl("http://www.konig.io/ns/core/TermStatus");
	public static final URI termStatus = new URIImpl("http://www.konig.io/ns/core/termStatus");
	// Generic Data Modeling

	public static final URI EnumNamespace = new URIImpl("http://www.konig.io/ns/core/EnumNamespace");
	public static final URI id = new URIImpl(ID);
	public static final URI mediaTypeBaseName = new URIImpl("http://www.konig.io/ns/core/mediaTypeBaseName");
	public static final URI tabularOriginShape = new URIImpl("http://www.konig.io/ns/core/tabularOriginShape");
	public static final URI usesAbbreviationScheme = new URIImpl("http://www.konig.io/ns/core/usesAbbreviationScheme");
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
	public static final URI derivedProperty = new URIImpl(DERIVED_PROPERTY);
	public static final URI NullShape = new URIImpl("http://www.konig.io/shapes/NullShape");
	public static final URI tabularFieldNamespace = new URIImpl(TABULAR_FIELD_NAMESPACE);
	public static final URI receivesDataFrom = new URIImpl(Terms.receivesDataFrom);
	public static final URI etlPattern = new URIImpl(Terms.etlPattern);
	public static final URI OverlayPattern = new URIImpl(Terms.OverlayPattern);
	

	public static final URI propertyPath = new URIImpl(Terms.propertyPath);
	public static final URI propertySource = new URIImpl(Terms.propertySource);
	public static final URI generatedFrom = new URIImpl(Terms.generatedFrom);
	public static final URI generatorInputOf = new URIImpl(Terms.generatorInputOf);
	public static final URI generatorInput = new URIImpl(Terms.generatorInput);
	public static final URI generatorOutput = new URIImpl(Terms.generatorOutput);
	public static final URI DatasourceProperty = new URIImpl(Terms.DatasourceProperty);
	public static final URI PropertyGenerator = new URIImpl(Terms.PropertyGenerator);
	
	/**
	 * A sub-property of konig:derivedFrom which asserts that the "derived from" relationship was defined
	 * explicitly by a human data modeler.
	 */
	public static final URI explicitDerivedFrom = new URIImpl(EXPLICIT_DERIVED_FROM);
	public static final URI derivedFrom = new URIImpl("http://www.konig.io/ns/core/derivedfrom");
	public static final URI shapeProcessing = new URIImpl("http://www.konig.io/ns/core/shapeProcessing");
	public static final URI SqlTransform = new URIImpl("http://www.konig.io/ns/core/SqlTransform");
	
	public static final String PREFERRED_TABULAR_SHAPE = "http://www.konig.io/shapes/preferredTabularShape";
	public static final URI preferredTabularShape = new URIImpl(PREFERRED_TABULAR_SHAPE);
	
	public static final URI Undefined = new URIImpl("http://www.konig.io/ns/core/Undefined");
	//public static final URI DerivedShape = new URIImpl("http://www.konig.io/shapes/DerivedShape");
	
	// Data Sources

	public static final URI DataSource = new URIImpl(Terms.DataSource);
	public static final URI CurrentState = new URIImpl("http://www.konig.io/ns/core/CurrentState");
	public static final URI AuthoritativeDataSource = new URIImpl("http://www.konig.io/ns/core/AuthoritativeDataSource");
	public static final URI StagingDataSource = new URIImpl("http://www.konig.io/ns/core/StagingDataSource");
	public static final URI ReportingDataSource = new URIImpl("http://www.konig.io/ns/core/ReportingDataSource");
	public static final URI GoogleCloudStorageBucket = new URIImpl("http://www.konig.io/ns/core/GoogleCloudStorageBucket");
	public static final URI GoogleCloudStorageFolder = new URIImpl("http://www.konig.io/ns/core/GoogleCloudStorageFolder");
	public static final URI GoogleCloudSqlTable = new URIImpl("http://www.konig.io/ns/core/GoogleCloudSqlTable");
	public static final URI OracleTable = new URIImpl("http://www.konig.io/ns/core/OracleTable");
	public static final URI AwsAuroraTable = new URIImpl("http://www.konig.io/ns/core/AwsAurora");
	public static final URI AwsAuroraView = new URIImpl("http://www.konig.io/ns/core/AwsAuroraView");
	public static final URI S3Bucket = new URIImpl("http://www.konig.io/ns/core/S3Bucket");
	public static final URI shapeDataSource = new URIImpl(SHAPE_DATA_SOURCE);
	public static final URI nodeShapeCube = new URIImpl(Terms.nodeShapeCube);
	public static final URI dataSourceShape = new URIImpl(Terms.dataSourceShape);
	public static final URI bigQuerySource = new URIImpl(BIG_QUERY_SOURCE);
	public static final URI primaryKey = new URIImpl("http://www.konig.io/ns/core/primaryKey");
	public static final URI uniqueKey = new URIImpl("http://www.konig.io/ns/core/uniqueKey");
	public static final URI syntheticKey = new URIImpl("http://www.konig.io/ns/core/syntheticKey");
	public static final URI ddlFile = new URIImpl("http://www.konig.io/ns/core/ddlFile");
	public static final URI transformFile = new URIImpl("http://www.konig.io/ns/core/transformFile");
	
	// Shape Transform Vocabulary
	
	public static final URI sourceShape = new URIImpl("http://www.konig.io/ns/core/sourceShape");
	public static final URI updateWhen = new URIImpl("http://www.konig.io/ns/core/updateWhen");
	
	/**
	 * The triple (subject consumesDataFrom object) implies:
	 * <ul>
	 *   <li> subject is a System
	 *   <li> object is a System
	 *   <li> ETL processes that extract from object and load into subject are permitted. 
	 * </ul>
	 */
	public static final URI consumesDataFrom = new URIImpl("http://www.konig.io/ns/core/consumesDataFrom");
	
	// Fact Modeling

	public static final URI measure = new URIImpl(MEASURE);
	public static final URI dimension = new URIImpl(DIMENSION);
	public static final URI attribute = new URIImpl(ATTRIBUTE);
	public static final URI variable = new URIImpl("http://www.konig.io/ns/core/variable");
	public static final URI stereotype = new URIImpl("http://www.konig.io/ns/core/stereotype");
	public static final URI relationshipDegree = new URIImpl("http://www.konig.io/ns/core/relationshipDegree");
	public static final URI securityClassification = new URIImpl("http://www.konig.io/ns/core/securityClassification");
	public static final URI qualifiedSecurityClassification = new URIImpl("http://www.konig.io/ns/core/qualifiedSecurityClassification");
	public static final URI totalCount = new URIImpl("http://www.konig.io/ns/core/totalCount");
	public static final URI durationUnit = new URIImpl("http://www.konig.io/ns/core/durationUnit");
	public static final URI aggregationOf = new URIImpl("http://www.konig.io/ns/core/aggregationOf");
	public static final URI rollUpBy = new URIImpl("http://www.konig.io/ns/core/rollUpBy");
	public static final URI fromAggregationSource = new URIImpl("http://www.konig.io/ns/core/fromAggregationSource");
	public static final URI timeInterval = new URIImpl("http://www.konig.io/ns/core/timeInterval");
	public static final URI sourcePath = new URIImpl(SOURCE_PATH);
	public static final URI TimeUnit = new URIImpl("http://www.konig.io/ns/core/TimeUnit");
	public static final URI TimeInterval = new URIImpl("http://www.konig.io/ns/core/TimeInterval");
	public static final URI intervalStart = new URIImpl("http://www.konig.io/ns/core/intervalStart");
	
	// Time Units
	
	public static final URI Day = new URIImpl("http://www.konig.io/ns/core/Day");
	public static final URI Week = new URIImpl("http://www.konig.io/ns/core/Week");
	public static final URI Month = new URIImpl("http://www.konig.io/ns/core/Month");
	public static final URI Year = new URIImpl("http://www.konig.io/ns/core/Year");
	
	// Software Projects 
	public static final URI Project = new URIImpl("http://www.konig.io/ns/core/Project");
	public static final URI baseProject = new URIImpl(BASE_PROJECT);
	public static final URI relativePath = new URIImpl(RELATIVE_PATH);
	

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
	public static final URI dataSteward = new URIImpl("http://www.konig.io/ns/core/dataSteward");
	public static final URI TabularNodeShape = new URIImpl("http://www.konig.io/ns/core/TabularNodeShape");
	public static final URI abbreviationLabel = new URIImpl("http://www.konig.io/ns/core/abbreviationLabel");
	public static final URI OneToOne = new URIImpl("http://www.konig.io/ns/core/OneToOne");
	public static final URI OneToMany = new URIImpl("http://www.konig.io/ns/core/OneToMany");
	public static final URI ManyToOne = new URIImpl("http://www.konig.io/ns/core/ManyToOne");
	public static final URI ManyToMany = new URIImpl("http://www.konig.io/ns/core/ManyToMany");
	public static final URI uid = new URIImpl("http://www.konig.io/ns/core/uid");
	
	public static URI javaMethodId(Class<?> javaClass, String methodName) {
		StringBuilder builder = new StringBuilder();
		builder.append(Konig.JAVA_NAMESPACE);
		builder.append(javaClass.getName());
		builder.append("#");
		builder.append(methodName);
		return new URIImpl(builder.toString());
	}
}
