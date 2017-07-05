# Konig Schema Generator

The Konig Schema Generator can generate [OWL](https://en.wikipedia.org/wiki/Web_Ontology_Language)
and [SHACL](https://www.w3.org/TR/shacl/) statements from information in a spreadsheet,
or it can start with OWL and SHACL statements as input.

The generator can produce the following kinds of output:

* [JSON Schema](http://spacetelescope.github.io/understanding-json-schema/)
* [Avro Schema](https://avro.apache.org/docs/1.8.0/spec.html)
* [JSON-LD Contexts](https://www.w3.org/TR/json-ld/)
* [Google BigQuery Table Definition](https://cloud.google.com/bigquery/docs/reference/v2/tables)
* [plantUML Models](http://plantUML.com/)
* Java POJOs
* Java Data Access Objects


The generator has been implemented as a Maven plugin.  To use it, you must first
[install Maven](https://maven.apache.org/install.html).

## Usage

If you are going to generate OWL and SHACL statements from a spreadsheet, you
should start by making a copy of the <br>
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
          <defaults>
            <rdfDir>${basedir}/target/generated/rdf</rdfDir>
            <owlDir>${basedir}/target/generated/rdf/owl</owlDir>
            <shapesDir>${basedir}/target/generated/rdf/shapes</shapesDir>
          </defaults>
          <workbook>
            <workbookFile>${basedir}/src/data-model.xlsx</workbookFile>
            <owlDir>${basedir}/target/generated/rdf/owl</owlDir>
            <shapesDir>${basedir}/target/generated/rdf/shapes</shapesDir>
            <inferRdfPropertyDefinitions>true</inferRdfPropertyDefinitions>
          </workbook>
          <rdfSourceDir>${basedir}/target/generated/rdf</rdfSourceDir>
          <jsonld>
            <jsonldDir>${basedir}/target/generated/src/main/jsonld</jsonldDir>
            <uriTemplate>http://example.com/jsonld/{shapeLocalName}</uriTemplate>
          </jsonld>
          <javaCodeGenerator>
            <javaDir>${basedir}/target/generated/java</javaDir>
            <packageRoot>com.example</packageRoot>
            <generateCanonicalJsonReaders>true</generateCanonicalJsonReaders>
            <googleDatastoreDaoPackage>com.example.gae.datastore</googleDatastoreDaoPackage>
          </javaCodeGenerator>
          <plantUML>
            <classDiagram>
              <file>${basedir}/target/generated/docs/classDiagram.plantUML</file>
              <excludeClass>
                <param>http://example.com/ns/SomeClass</param>
                <param>http://example.com/ns/AnotherClass</param>
              </excludeClass>
              <includeClass>
                <param>http://example.com/ns/WantedClass</param>
                <param>http://example.com/ns/AnotherWantedClass</param>
              </includeClass>
              <showAssociations>true</showAssociations>
              <showEnumerationClasses>true</showEnumerationClasses>
              <showOwlThing>true</showOwlThing>
              <showSubClassOf>true</showSubClassOf>
            </classDiagram>
          </plantUML>
          <googleCloudPlatform>
            <gcpDir>${basedir}/target/generated/gcp</gcpDir>
            <bigQueryDatasetId>mydataset</bigQueryDatasetId>
            <enumShapeDir>${basedir}/target/generated/rdf/shapes</enumShapeDir>
            <enumShapeNameTemplate>http://example.com/shapes/Bq{targetClassLocalName}Shape</enumShapeNameTemplate>
          </googleCloudPlatform>
          <avroDir>${basedir}/target/generated/src/main/avro</avroDir>
          <jsonSchemaDir>${basedir}/target/generated/src/main/json-schema</jsonSchemaDir>
          <rdfOutput>
            <rdfDir>${basedir}/target/generated/rdf</rdfDir>
            <owlDir>${basedir}/target/generated/rdf/owl</owlDir>
            <shapesDir>${basedir}/target/generated/rdf/shapes</shapesDir>
          </rdfOutput>
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
| defaults           | A container for default values                                                                        |
| defaults/rdfDir    | The directory where RDF resources (OWL Ontologies, SHACL Shapes, etc.) are stored                     |
| defaults/owlDir    | The directory where OWL ontologies are stored.  Typically a subdirectory of `rdfDir`.  By default, equals `${rdfDir}/owl`. |
| defaults/shapesDir | The directory where SHACL Shape descriptions are stored.  Typically a subdirectory of `rdfDir`.  By default, equals `${rdfDir}/shapes`. |
| workbook  | A container for the parameters that control the generation of data model artifacts from a spreadsheet          |
| workbook/workbookFile | A Microsoft Excel workbook (*.xlsx) containing a description of the data model                     |
| workbook/owlDir | The location where OWL ontologies from the `workbookFile` will be stored                              |
| workbook/shapesDir | The output directory into which shapes generated from the `workbookFile` will be stored            |
| workbook/inferRdfPropertyDefinitions | A flag that specifies whether to infer RDF Property definitions from predicates described by SHACL Property Constraints |
| rdfSourceDir       | The directory that contains the source SHACL and OWL files from which other artifacts will be generated        |
| jsonldDir          | The output directory that will contain the generated JSON-LD context files                                     |
| jsonld            | A container for parameters that control the JSON-LD context generator                                     |
| jsonld/jsonldDir  | The output directory for generated JSON-LD context resources                                              |
| jsonld/uriTemplate | The URI template used to generate the `@id` for each JSON-LD context associated with a `sh:Shape`. The template may include any of the following variables: <ul><li>`shapeId`</li><li>`shapeLocalName`</li><li>`shapeNamespacePrefix`</li><li>`targetClassId`</li><li>`targetClassLocalName`</li><li>`targetClassNamespacePrefix`</li></ul> The default template is `{shapeId}/context` |
| javaCodeGenerator | A container for parameters that control Java code generation                                              |
| javaCodeGenerator/javaDir | The output directory for generated Java code                                                      |
| javaCodeGenerator/packageRoot | The default root package for generated Java code                                              |
| javaCodeGenerator/googleDatastoreDaoPackage | The root package for generated data-access-objects for Google Datastore         |
| javaCodeGenerator/generateCanonicalJsonReaders | A boolean flag that specifies whether canonical JSON readers should be generated |
| plantUML     | A container for the parameters that control the generation of plantUML diagrams                                |
| plantUML/classDiagram | A container for configuration parameters of a UML class diagram                                        |
| plantUML/classDiagram/file | The location where the generated UML Class diagram should be saved.  The file name should end with the *.plantuml suffix  |
| plantUML/classDiagram/excludeClass | A container of names (fully-qualified or CURIE) for OWL classes to be excluded from the diagram.      |
| plantUML/classDiagram/includeClass | A container of names (fully-qualified or CURIE) for OWL classes to be included in the diagram.        |
| plantUML/classDiagram/showAssociations | A boolean flag which specifies whether associations between classes should be displayed in the class diagram |
| plantUML/classDiagram/showEnumerationClasses | A boolean flag which specifies whether sub-classes of schema:Enumeration should be included in the class diagram |
| googleCloudPlatform | A container for the parameters that control the generation of Google Cloud resources                    |
| googleCloudPlatform/gcpDir | The output directory for generated Google Cloud Platform resources                               |
| googleCloudPlatform/bigQueryDatasetId | The default identifier to use for the Dataset to which BigQuery tables belong         |
| googleCloudPlatform/enumShapeDir | The output directory that holds generated SHACL shapes of BigQuery tables for reference data based on sub-classes of schema:Enumeration |
| googleCloudPlatform/enumShapeNameTemplate | A template used to generate IRI values for SHACL shapes of BigQuery tables that contain reference data based on sub-classes of schema:Enumeration |
| avroDir            | The output directory that will contain the generated Avro Schema files                                         |
| jsonSchemaDir      | The output directory that will contain the generated JSON Schema files                                         |
| rdfOutput          | Define this element if you want annotations on the RDF resources to be saved. |
| rdfOutput/rdfDir   | The directory where RDF resources will be saved. If undefined the value from `${defaults.rdfDir}` will be used. |
| rdfOutput/shapesDir   | The directory where RDF resources will be saved.  If undefined, the following defaults apply (in priority order): <ul><li>${defaults.shapesDir}</li><li>${rdfOutput.rdfDir}/shapes</li></ul> |



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

## Multi-Project Generation
This plugin can be used to generate a suite of Maven projects:

* {baseName}-parent
* {baseName}-rdf-model
* {baseName}-java-model
* {baseName}-gcp-model
* {baseName}-data-catalog
* {baseName}-appengine

## Limitations

The generator is subject to the following limitations:

* `sh:datatype` values must come from the XML Schema namespace (e.g. `xsd:string`)
* Generic constraints such as `sh:not`, `sh:or`, `sh:and`, etc. are not supported.

We hope to remove these limitations in the future.  
