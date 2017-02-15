# Konig Schema Generator

The Konig Schema Generator can generate [OWL](https://en.wikipedia.org/wiki/Web_Ontology_Language) 
and [SHACL](https://www.w3.org/TR/shacl/) statements from information in a spreadsheet, 
or it can start with OWL and SHACL statements as input.
 
The generator can produce the following kinds of output:

* [JSON Schema](http://spacetelescope.github.io/understanding-json-schema/)
* [Avro Schema](https://avro.apache.org/docs/1.8.0/spec.html)
* [JSON-LD](https://www.w3.org/TR/json-ld/) Contexts
* [Google BigQuery Table Definition](https://cloud.google.com/bigquery/docs/reference/v2/tables)
* [PlantUML](http://plantuml.com/) Models
* Java POJOs
* Java Data Access Objects
* Ontology Summary
* Online documentation of your Ontology


The generator has been implemented as a Maven plugin.  To use it, you must first 
[install Maven](https://maven.apache.org/install.html).

## Usage

If you are going to generate OWL and SHACL statements from a spreadsheet, you 
should start by making a copy of the 
[Data Model Workbook](https://docs.google.com/spreadsheets/d/1mhL1hylgRJuMBft0sHg7onKwxscdlIJRuzNlF_iDKK0/edit?usp=sharing) 
template.

To use the schema generator, add a maven plugin to your project as shown below.

```xml
<project 
	xmlns="http://maven.apache.org/POM/4.0.0" 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.example</groupId>
  <artifactId>data-model</artifactId>
  <version>1.0.0</version>
  <name>Example Data Model</name>

  <build>
  	<plugins>
  		<plugin>
	  		<groupId>io.konig</groupId>
	  		<artifactId>konig-schemagen-maven-plugin</artifactId>
	  		<version>2.0.0-8</version>
	  		<configuration>
          <workbookFile>${basedir}/src/dataModel.xlsx</workbookFile>
          <inferRdfPropertyDefinitions>true</inferRdfPropertyDefinitions>
          <owlOutDir>${basedir}/target/generated/src/main/rdf/owl</owlOutDir>
          <shapesOutDir>${basedir}/target/generated/src/main/rdf/shapes</shapesOutDir>
          <sourceDir>${basedir}/target/generated/src/main/rdf</sourceDir>
	  			<jsonldDir>${basedir}/target/generated/src/main/jsonld</jsonldDir>
          <bqOutDir>${basedir}/target/generated/src/main/bigquery</bqOutDir>
	  			<avroDir>${basedir}/target/generated/src/main/avro</avroDir>
	  			<jsonSchemaDir>${basedir}/target/generated/src/main/jsonschema</jsonSchemaDir>
	  			<javaDir>${basedir}/target/generated/src/main/java</javaDir>
	  			<javaPackageRoot>com.example</javaPackageRoot>
	  			<gcpDir>${basedir}/target/generated/src/main/gcp</gcpDir>
	  			<plantUMLDomainModelFile>${basedir}/target/generated/src/main/domainModel.plantuml</plantUMLDomainModelFile>
	  			<domainModelPngFile>${basedir}/target/generated/src/main/domainModel.png</domainModelPngFile>
	  			<projectJsonldFile>${basedir}/target/generated/src/main/summary/project.jsonld</projectJsonldFile>
	  			<namespacesFile>${basedir}/target/generated/src/main/summary/namespaces.ttl</namespacesFile>
	  			<daoPackage>com.example.dao</daoPackage>
	  			<excludeNamespace>
			  				<param>http://www.w3.org/1999/02/22-rdf-syntax-ns#</param>
			  				<param>http://www.w3.org/2000/01/rdf-schema#</param>
			  				<param>http://www.w3.org/2002/07/owl#</param>
			  				<param>http://www.w3.org/ns/shacl#</param>
			  				<param>http://www.konig.io/ns/core/</param>
			  				<param>http://purl.org/vocab/vann/</param>
			  				<param>http://www.w3.org/2001/XMLSchema#</param>
	  			</excludeNamespace>
	  		</configuration>
	  		<executions>
	  			<execution>
            <phase>generate-sources</phase>
		  			<goals>
              <goal>generate</goal>
		  			</goals>
	  			</execution>
	  		</executions>
  		</plugin>
  	</plugins>
  </build>
</project>
```

To run the generator, simply invoke the following command in your project's base directory:

```
    mvn generate-sources
```    
## Goals

| Goal          | Description |
|---------------|---------------------------------------------------------------------------------------------|
| generate      | Generate the artifacts specified in the configuration                                       |


## Configuration Parameters

| Parameter          | Description                                                                                                    |
|--------------------|----------------------------------------------------------------------------------------------------------------|
| sourceDir          | The directory that contains the source SHACL and OWL files from which schemas will be generated                |
| avroDir            | The output directory that will contain the generated Avro Schema files                                         |
| jsonSchemaDir      | The output directory that will contain the generated JSON Schema files                                         |
| jsonldDir          | The output directory that will contain the generated JSON-LD context files                                     |
| bqOutDir           | The output directory where generated BigQuery table definitions will be stored                                 |
| bqShapeBaseURL     | The base URL for tables created for a given OWL class (as opposed to tables based on a specific shape)         |
| workbookFile       | A Microsoft Excel workbook (*.xlsx) containing a description of the data model                                 |
| javaDir            | The directory where generated POJO source code will be stored                                                  |
| javaPackageRoot    | The root package name under which POJO classes will be generated.                                              |
| excludeNamespace   | The set of namespaces to be excluded from the projectJsonldFile                                                |
| gcpDir             | The output directory for generated Google Cloud Platform resources                                             |
| workbookFile       | The location of a workbook that contains definitions for ontologies, classes, properties, individuals, shapes and property constraints |
| shapesOutDir       | The output directory into which shapes generated from the `workbookFile` will be stored                    |
| owlOutDir          | The location where OWL ontologies from the `workbookFile` will be stored                                   |
| plantUMLDomainModelFile | The location where a PlantUML domain model will be stored                                                 |
| domainModelPngFile | The location where a PlantUML domain model image will be stored                                                |
| namespacesFile     | The location where a summary of namespace prefixes in Turtle format will be stored                             |
| projectJsonldFile  | The location where a JSON-LD file containing a description of the OWL ontologies and SHACL Shapes will be stored |
| daoPackage         | The root package under which Java Data Access Objects will be stored                                           |
| inferRdfPropertyDefinitions | Infer RDF Property definitions from SHACL Property Constraints                                       |






## Naming Conventions

The generator utilizes a set of rigid naming conventions.  In the future, we hope to support pluggable,
user-defined naming conventions.  But for now the naming conventions are fixed.

SHACL data shapes must have names of the form

```
    {base-url}/{qualifier}/{version}/{namespace-prefix}/{local-class-name}
```

For instance, you might have a source file that contains a shape definition like this:

```
   <http://www.example.com/shapes/v1/schema/Person> a sh:Shape ;
   ...
```

### Media Type Names

Each SHACL Shape is a associated with a suite of vendor-specific media types, one for
each data format.  The base name for the media types has the form:

```
   vnd.{your-domain-name}.{version}.{namespace-prefix}.{local-class-name}
```

You add a suffix to get the name of a vendor specific media
type for the data shape in a particular format.

For the example given above, you would have the following media types:


| Format       | Vendor-specific Media Type        |
|--------------|-----------------------------------|
| JSON-LD      | vnd.example.v1.schema.person+json |
| Avro         | vnd.example.v1.schema.person+avro |


### JSON-LD Context and JSON Schema URLs

The URL for the associated JSON-LD context and JSON Schema is formed by appending a suffix to
the URL for the data shape.

Continuing with our example, you would have the following artifacts:

| Artifact        | URL                                                       |
|-----------------|-----------------------------------------------------------|
| JSON-LD Context | http://www.example.com/shapes/v1/schema/Person/context    |
| JSON Schema     | http://www.example.com/shapes/v1/schema/Person/jsonschema |

The URL for the JSON Schema appears as the `id` field in the JSON Schema specification.

### Avro Schema Names

Names for the generated Avro schemas have the following format:

```
   {top-level-internet-domain}.{your-domain-name}.{qualifier}.{version}.{namespace-prefix}.{local-class-name}
```

For our example, you would have the following Avro Schema name:

```
   com.example.shapes.v1.schema.Person
```


## Limitations

The generator is subject to the following limitations:

* `sh:datatype` values must come from the XML Schema namespace (e.g. `xsd:string`)
* Generic constraints such as `sh:not`, `sh:or`, `sh:and`, etc. are not supported.

We hope to remove these limitations in the future.  
