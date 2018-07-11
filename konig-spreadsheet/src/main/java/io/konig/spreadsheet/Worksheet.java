package io.konig.spreadsheet;

public class Worksheet {
	public static final int UNDEFINED = -1;

	public static final String ONTOLOGY_NAME = "Ontology Name";
	public static final String COMMENT = "Comment";
	public static final String NAMESPACE_URI = "Namespace URI";
	public static final String PREFIX = "Prefix";
	public static final String IMPORTS = "Imports";

	public static final String CLASS_NAME = "Class Name";
	public static final String CLASS_ID = "Class Id";
	public static final String CLASS_SUBCLASS_OF = "Subclass Of";

	public static final String PROPERTY_NAME = "Property Name";
	public static final String PROPERTY_ID = "Property Id";
	public static final String DOMAIN = "Domain";
	public static final String RANGE = "Range";
	public static final String INVERSE_OF = "Inverse Of";
	public static final String PROPERTY_TYPE = "Property Type";
	public static final String SUBPROPERTY_OF = "Subproperty Of";
	

	public static final String INDIVIDUAL_NAME = "Individual Name";
	public static final String INDIVIDUAL_ID = "Individual Id";
	public static final String INDIVIDUAL_TYPE = "Individual Type";
	public static final String INDIVIDUAL_CODE_VALUE = "Code Value";

	public static final String SHAPE_ID = "Shape Id";
	public static final String TARGET_CLASS = "Target Class";
	public static final String SCOPE_CLASS = "Scope Class";
	public static final String MEDIA_TYPE = "Media Type";
    public static final String SHAPE_TYPE = "Shape Type";
	public static final String AGGREGATION_OF = "Aggregation Of";
	public static final String ROLL_UP_BY = "Roll-up By";
	public static final String BIGQUERY_TABLE = "BigQuery Table";
	public static final String DATASOURCE = "Datasource";
	public static final String IRI_TEMPLATE = "IRI Template";
	public static final String DEFAULT_FOR = "Default For";
	public static final String TERM_STATUS = "Term Status";
	public static final String TABULAR_ORIGIN_SHAPE = "Tabular Origin Shape";

	public static final String SETTING_NAME = "Setting Name";
	public static final String SETTING_VALUE = "Setting Value";
	public static final String PATTERN = "Pattern";
	public static final String REPLACEMENT = "Replacement";
	
	public static final String PROPERTY_PATH = "Property Path";
	public static final String VALUE_TYPE = "Value Type";
	public static final String MIN_COUNT = "Min Count";
	public static final String MAX_COUNT = "Max Count";
	public static final String UNIQUE_LANG = "Unique Lang";
	public static final String VALUE_CLASS = "Value Class";
	public static final String STEREOTYPE = "Stereotype";
	public static final String VALUE_IN = "Value In";
	public static final String EQUALS = "Equals";
	public static final String EQUIVALENT_PATH = "Equivalent Path";
	public static final String SOURCE_PATH = "Source Path";
	public static final String PARTITION_OF = "Partition Of";
	public static final String FORMULA = "Formula";
	public static final String MIN_INCLUSIVE = "Min Inclusive";
	public static final String MAX_INCLUSIVE = "Max Inclusive";
	public static final String MIN_EXCLUSIVE = "Min Exclusive";
	public static final String MAX_EXCLUSIVE = "Max Exclusive";
	public static final String MIN_LENGTH = "Min Length";
	public static final String MAX_LENGTH = "Max Length";
	public static final String DECIMAL_PRECISION = "Decimal Precision";
	public static final String DECIMAL_SCALE = "Decimal Scale";
	public static final String SECURITY_CLASSIFICATION ="Security Classification";

	// Cloud SQL Instance
	public static final String INSTANCE_NAME = "Instance Name";
	public static final String INSTANCE_TYPE = "Instance Type";
	public static final String BACKEND_TYPE = "Backend Type";
	public static final String REGION = "Region";
	public static final String VERSION = "Database Version";
	public static final String TIER = "Tier";
    public static final String SHAPE_OF = "Input Of";
    public static final String ONE_OF = "One Of";

	//Cloud Formation Templates
	public static final String STACK_NAME="Stack name";
	public static final String AWS_REGION="AWS Region";
	public static final String CLOUD_FORMATION_TEMPLATES="Cloud Formation Templates";
	
	//Data Dictionary Template
	public static final String SOURCE_SYSTEM = "Source System";
	public static final String SOURCE_OBJECT_NAME = "Source Object Name";
	public static final String FIELD = "Field";
	public static final String DATA_TYPE = "Data Type";
	public static final String CONSTRAINTS = "Constraints";
	public static final String BUSINESS_NAME = "Business Name";
	public static final String BUSINESS_DEFINITION = "Business Definition (Optional)";
	public static final String DATA_STEWARD = "Data Steward";
	public static final String TARGET_OBJECT_NAME = "Target Object Name\n(Flatfile or Aurora Tables)";
	public static final String TARGET_FIELD_NAME = "Target field Name";
	
	//Data Dictionary Abbreviations
	public static final String TERM = "Term";
	public static final String ABBREVIATION = "Abbreviation";

	
	/** Security Tags **/
	public static final String SECURITY_TAGS = "Security Tags";
	public static final String AMAZON_RESOURCE_NAME = "Amazon Resource Name";
	public static final String TAG_KEY = "Tag Key";
	public static final String TAG_VALUE = "Tag Value";
	public static final String ENVIRONMENT = "Environment";
	
	public static final String ENUMERATION_DATASOURCE_TEMPLATE = "enumerationDatasourceTemplate";
	public static final String ENUMERATION_SHAPE_ID = "enumerationShapeId";

	public static final String SUBJECT = "Subject";
	public static final String LABEL = "Label";
	public static final String LANGUAGE = "Language";

	public static final String UNBOUNDED = "unbounded";
	
	public static final String AWS_DB_CLUSTER_NAME = "DB Cluster Name";
	public static final String AWS_DB_CLUSTER_ENGINE = "Engine";
	public static final String AWS_DB_CLUSTER_ENGINE_VERSION = "Engine Version";
	public static final String AWS_DB_CLUSTER_INSTANCE_CLASS = "Instance Class";
	public static final String AWS_DB_CLUSTER_AVAILABILITY_ZONE = "Availability Zone";
	public static final String AWS_DB_CLUSTER_BACKUP_PERIOD = "Backup Retention Period (days)";
	public static final String AWS_DB_CLUSTER_DATABASE_NAME = "Database Name";
	public static final String AWS_DB_CLUSTER_DB_SUBNET = "DB Subnet Group Name";
	public static final String AWS_DB_CLUSTER_BACKUP_WINDOW = "Preferred Backup Window";
	public static final String AWS_DB_CLUSTER_MAINTENANCE_WINDOW = "Preferred Maintenance Window";
	public static final String AWS_DB_CLUSTER_REPLICATION_SOURCE = "Replication Source Identifier";
	public static final String AWS_DB_CLUSTER_STORAGE_ENCRYPTED = "Storage Encrypted";
	
	public static final int COL_NAMESPACE_URI = 0x1;
	public static final int COL_CLASS_ID = 0x2;
	public static final int COL_PROPERTY_PATH = 0x4;
	public static final int COL_PROPERTY_ID = 0x4;
	public static final int COL_INDIVIDUAL_ID = 0x8;
	public static final int COL_SHAPE_ID = 0x10;
	public static final int COL_SETTING_NAME = 0x20;
	public static final int COL_INSTANCE_NAME = 0x40;
	public static final int COL_LABEL = 0x80;
	public static final int COL_AMAZON_DB_CLUSTER = 0x100;
	public static final int COL_CLOUD_FORMATION_TEMPLATE = 0x200;
	public static final int COL_SECURITY_TAGS = 0x400;
	public static final int COL_SOURCE_SYSTEM = 0x800;
	public static final int COL_TERM = 0x1000;
	
	public static final int SHEET_ONTOLOGY = COL_NAMESPACE_URI;
	public static final int SHEET_CLASS = COL_CLASS_ID;
	public static final int SHEET_PROPERTY = COL_PROPERTY_ID;
	public static final int SHEET_INDIVIDUAL = COL_INDIVIDUAL_ID;
	public static final int SHEET_SHAPE = COL_SHAPE_ID;
	public static final int SHEET_PROPERTY_CONSTRAINT = COL_SHAPE_ID | COL_PROPERTY_PATH;
	public static final int SHEET_SETTING = COL_SETTING_NAME;
	public static final int SHEET_DB_INSTANCE = COL_INSTANCE_NAME;
	public static final int SHEET_LABEL = COL_LABEL;
	public static final int SHEET_AMAZON_RDS_CLUSTER = COL_AMAZON_DB_CLUSTER;
	public static final int SHEET_CLOUD_FORMATION_TEMPLATE = COL_CLOUD_FORMATION_TEMPLATE;
	public static final int SHEET_SECURITY_TAGS = COL_SECURITY_TAGS;
	public static final int SHEET_DATA_DICTIONARY_TEMPLATE=COL_SOURCE_SYSTEM;
	public static final int SHEET_DATA_DICTIONARY_ABBREVIATIONS=COL_TERM;

	public static final String USE_DEFAULT_NAME = "useDefaultName";
	
}
