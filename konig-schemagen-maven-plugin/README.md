# Konig Schema Generator

The Konig Schema Generator takes [SHACL](https://www.w3.org/TR/shacl/) and
[OWL](https://en.wikipedia.org/wiki/Web_Ontology_Language) statements as input, and it
produces various schemas as output, including:

* [JSON Schema](http://spacetelescope.github.io/understanding-json-schema/)
* [Avro Schema](https://avro.apache.org/docs/1.8.0/spec.html)
* [Google BigQuery Table Definition](https://cloud.google.com/bigquery/docs/reference/v2/tables)

The generator also produces a JSON-LD context for each addressable data shape.

The generator has been implemented as a Maven plugin.  To use it, you must first [install Maven](https://maven.apache.org/install.html).

## Usage

To use the schema generator, add a maven plugin to your project as shown below.

```xml
<project>
  ...
  <build>
  	<plugins>
  		<plugin>
	  		<groupId>io.konig</groupId>
	  		<artifactId>konig-schemagen-maven-plugin</artifactId>
	  		<version>1.0.2</version>
	  		<configuration>
	  			<avroDir>${basedir}/src/main/avro</avroDir>
	  			<jsonldDir>${basedir}/src/main/jsonld</jsonldDir>
	  			<jsonSchemaDir>${basedir}/src/main/jsonschema</jsonSchemaDir>
          <bqSourceDir>${basedir}/src/main/bq-config</bqSourceDir>
          <bqOutDir>${basedir}/src/main/bigquery</bqOutDir>
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

The configuration is optional.  For the default values, see the discussion about configuration parameters below.

To run the generator, simply invoke the following command in your project's base directory:

```
    mvn generate-sources
```    

## Configuration Parameters

| Parameter       | Description                                                                                                                             |
|-----------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| sourceDir       |  The directory that contains the source SHACL and OWL files from which schemas will be generated<br>Default: `${basedir}/src/main/resources/shapes` |
| avroDir         | The output directory that will contain the generated Avro Schema files<br>Default: `${basedir}/target/generated/avro`         |
| jsonSchemaDir   | The output directory that will contain the generated JSON Schema files<br>Default: `${basedir}/target/generated/jsonschema` |
| jsonldDir       | The output directory that will contain the generated JSON-LD context files<br>Default: `${basedir}/target/generated/jsonld`  |
| summaryDir      | The output directory that contains summary information about the semantic model<br>Default: `${basedir}/target/generated/summary` |
| bqSourceDir     | The source directory that contains BigQuery configuration details. See [BigQuery Configiguration](#bq-config)  |
| bqOutDir        | The output directory where generated BigQuery table definitions will be stored |
| bqShapeBaseURL  | The base URL for tables created for a given OWL class (as opposed to tables based on a specific shape) |

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

## Summary information

The schema generator produces two files that summarize information about the semantic
models contained in your project.  The following table describes these output files.

| File                                    | Description                                |
|-----------------------------------------|--------------------------------------------|
| `${summaryDir}/namespaces.ttl` | Provides an overview of the namespaces used in your project.  Each namespace is declared to be an `owl:Ontology` and your prefix for the namespace is declared to be the `vann:preferredNamespacePrefix`. |
| `${summaryDir}/project.json`   | Collects all of the statements from your input files and serializes them within this document in JSON-LD format.  This single document is suitable for rendering documentation about your data model in a tool like [Ontodoc](https://github.com/konigio/konig-ontodoc) |

The schema generator publishes to `namespaces.ttl` any additional statements about your namespaces.
As a best practice, you should supply at least the `rdfs:label` and `rdfs:comment` properties.  
Thus, for each namespace, you should have statements like the following:

```
	<http://www.konig.io/ns/kcs> a owl:Ontology ;
		rdfs:label "Konig Change Set Vocabulary" ;
		rdfs:comment "A vocabulary for describing the differences between two graphs of data" .
```
## <a name="bg-config"></a>BigQuery Configuration
If you want to generate Google BigQuery Table definitions you must define the,
`bqShapeBaseURL` property, and you must provide a bit of configuration.

The configuration information is typically stored in a single file, but you can
distribute the information across multiple files.  By default, the Konig maven
plugin will search recursively under `${sourceDir}` for BigQuery configuration files.
If you want to put the configuration files in a different directory you must set
the `bqSourceDir` property.

Here's an example of a BigQuery configuration file, expressed in Turtle syntax:

```
@prefix gcp: <http://www.konig.io/ns/gcp/>.

[] a gcp:GoogleCloudProject ;
	gcp:projectId "example-dw" ;
	gcp:dataset [
		gcp:datasetId "directory" ;
		gcp:table [
			gcp:tableId "Person" ;
			gcp:description "Stores records about individual Person entities" ;
			gcp:tableShape <http://example.com/shapes/v1/schema/Person>
		],[
			gcp:tableId "Organization" ;
			gcp:description """
        Stores records about organizations such as schools, NGOs,
        corporations, clubs, etc.
      """ ;
			gcp:tableClass <http://schema.org/Organization>
		]
	]
	.
```

In a nutshell, the configuration file defines a Google Cloud Platform Project.
Each project contains one or more Datasets.  Each Dataset contains one or more
tables.  After loading the configuration file, the maven plugin will search for
resources of type `gcp:GoogleCloudProject` and will drill down to discover
the encapsulated datasets and tables.

The properties in a configuration file are listed below:

| Property        | Description                                                |
|-----------------|------------------------------------------------------------|
| projectId       | The identifier for the GCP project |
| dataset         | A Dataset contained within the specified project |
| datasetId       | The identifier for the GCP Dataset |
| description     | A description of the specified entity (Project, Dataset or Table) |
| tableShape      | The SHACL Shape used to generate the table definition |
| tableClass      | The OWL class for which a table definition will be generated |

For a given table, either `tableShape` or `tableClass` must be defined.
If `tableShape` is defined, the Maven plugin will generate a BigQuery table
definition based on that particular shape.  If `tableClass` is
defined, the Maven plugin will first generate a new Shape that merges the
PropertyConstraints from all shapes that list the `tableClass` or a class derived
from `tableClass` as the `scopeClass`.  The plugin will then generate a table
definition based on the merged shape.  The IRI of the merged shape will be created
by appending the local name of the `tableClass` to the URL given by the
`bqShapeBaseURL` property.


## Limitations

The generator is subject to the following limitations:

* `sh:datatype` values must come from the XML Schema namespace (e.g. `xsd:string`)
* Generic constraints such as `sh:not`, `sh:or`, `sh:and`, etc. are not supported.

We hope to remove these limitations in the future.  
