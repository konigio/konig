@prefix reg : <http://example.com/ns/registrar/> .
@prefix org: <http://www.w3.org/ns/org#> .
@prefix schema : <http://schema.org/> .
@prefix alias : <http://example.com/ns/alias/> .
@prefix schema1 : <http://example.com/shapes/v1/schema/> .
@prefix schema2 : <http://example.com/shapes/v2/schema/> .
@prefix org1 : <http://example.com/shapes/v1/org/> .
@prefix org2 : <http://example.com/shapes/v2/org/> .
@prefix dc: <http://purl.org/dc/terms/> .
@prefix shape: <http://example.com/shape/> .
@prefix konig: <http://www.konig.io/ns/core/> .

@columnNamespace alias 



CREATE TABLE registrar.Person (
	person_id BIGINT PRIMARY KEY NOT NULL
		SEMANTICS path /reg:registrarId,
		
	first_name VARCHAR(64)
		SEMANTICS path /schema:givenName,
		
	last_name VARCHAR(64)
		SEMANTICS path /schema:familyName
)
SEMANTICS
	class schema:Person ;
	
	physical {
		@id shape:PersonMasterShape ;
		konig:iriTemplate "http://example.com/person/{person_id}" ;
		
		konig:shapeDataSource {
			@id <http://example.com/db/schemas/registrar/tables/Person> ;
			a konig:OracleTable , konig:AuthoritativeDataSource 
		} , {
		  @id <gs://com.example.prod.person> ;
			a konig:GoogleCloudStorageBucket
		} , {
		  @id <urn:bigquery:example.registrar.StagingPerson> ;
			a konig:GoogleBigQueryTable ;
			konig:bigQuerySource <gs://com.example.prod.person>
		}
	} ;
	
	logical {
		@id shape:PersonReportingShape ;
		
		konig:shapeDataSource {
		  @id <urn:bigquery:example.registrar.Person> ;
			a konig:GoogleBigQueryTable 
		}
	}
.

CREATE TABLE registrar.CourseSection (
	section_id BIGINT PRIMARY KEY NOT NULL
		SEMANTICS path /reg:registrarId,
		
	section_name VARCHAR(255) NOT NULL
		SEMANTICS path /schema:name,
		
	start_date DATE NOT NULL
		SEMANTICS path /schema:startDate,
		
	end_date DATE NOT NULL
		SEMANTICS path /schema:endDate
)
SEMANTICS
	class schema:CourseInstance ;
	
	physical {
		@id shape:CourseInstanceMasterShape ;
		konig:iriTemplate "http://example.com/section/{section_id}" ;
		
		konig:shapeDataSource {
			@id <http://example.com/db/schemas/registrar/tables/CourseSection> ;
			a konig:OracleTable , konig:AuthoritativeDataSource 
		} , {
		  @id <gs://com.example.prod.course_instance> ;
			a konig:GoogleCloudStorageBucket
		} , {
		  @id <urn:bigquery:example.registrar.CourseInstance> ;
			a konig:GoogleBigQueryTable ;
			konig:bigQuerySource <gs://com.example.prod.course_instance>
		}
	} ;
	
	logical {
		@id shape:CourseInstanceReportingShape ;
		
		konig:shapeDataSource {
		  @id <urn:bigquery:example.registrar.CourseInstance> ;
			a konig:GoogleBigQueryTable 
		}
	}
.	

CREATE TABLE registrar.CourseSectionPerson (
	person_id BIGINT NOT NULL 
		SEMANTICS path /org:member/reg:registrarId,

	section_id BIGINT NOT NULL
		SEMANTICS path /org:organization/reg:registrarId,
		
	role_id BIGINT NOT NULL
		SEMANTICS path /org:role/reg:registrarId,
		
	CONSTRAINT FK_PERSON FOREIGN KEY (person_id) REFERENCES registrar.Person (person_id),
	CONSTRAINT FK_SECTION FOREIGN KEY (section_id) REFERENCES registrar.CourseSection (section_id),
	CONSTRAINT FK_ROLE FOREIGN KEY (role_id) REFERENCES registrar.Role (role_id)
)
SEMANTICS
	class org:Membership ;
	
	physical {
		@id shape:MembershipMasterShape ;
		
		konig:shapeDataSource {
			@id <http://example.com/db/schemas/registrar/tables/CourseSectionPerson> ;
			a konig:OracleTable , konig:AuthoritativeDataSource 
		} , {
		  @id <gs://com.example.prod.course_section_person> ;
			a konig:GoogleCloudStorageBucket
		} , {
		  @id <urn:bigquery:example.registrar.Membership> ;
			a konig:GoogleBigQueryTable ;
			konig:bigQuerySource <gs://com.example.prod.course_section_person>
		}
	} ;
	
	logical {
		@id shape:MembershipReportingShape ;
		
		konig:shapeDataSource {
		  @id <urn:bigquery:example.registrar.Membership> ;
			a konig:GoogleBigQueryTable 
		}
	}
.	

CREATE TABLE registrar.Role (
	role_id BIGINT PRIMARY KEY NOT NULL
		SEMANTICS path /reg:registrarId,
		
	role_name VARCHAR(16) NOT NULL
		SEMANTICS path /schema:name
)
SEMANTICS
	class org:Role ;
	
	physical {
		@id shape:RoleMasterShape ;
		konig:iriTemplate "http://example.com/role/{role_name}" ;
		
		konig:shapeDataSource {
			@id <http://example.com/db/schemas/registrar/tables/Role> ;
			a konig:OracleTable , konig:AuthoritativeDataSource 
		} , {
		  @id <gs://com.example.prod.role> ;
			a konig:GoogleCloudStorageBucket
		} , {
		  @id <urn:bigquery:example.registrar.Role> ;
			a konig:GoogleBigQueryTable ;
			konig:bigQuerySource <gs://com.example.prod.role>
		}
	} ;
	
	logical {
		@id shape:RoleReportingShape ;
		
		konig:shapeDataSource {
		  @id <urn:bigquery:example.registrar.Role> ;
			a konig:GoogleBigQueryTable 
		}
	}
.	