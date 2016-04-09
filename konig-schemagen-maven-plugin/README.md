# Konig Schema Generator

The Konig Schema Generator takes [SHACL](https://www.w3.org/TR/shacl/) shapes as input and produces various schemas as output, including:

* [JSON Schema](http://spacetelescope.github.io/understanding-json-schema/)
* [Avro Schema](https://avro.apache.org/docs/1.8.0/spec.html)

The generator also produces a JSON-LD context for each addressable data shape.

The generator has been implemented as a Maven plugin.  To use it, you must first [install Maven](https://maven.apache.org/install.html).

## Usage

To use the schema generator, you must add a maven plugin to your project as shown below.

```
<project>
  ...
  <build>
  	<plugins>
  		<plugin>
	  		<groupId>io.konig</groupId>
	  		<artifactId>konig-schemagen-maven-plugin</artifactId>
	  		<version>1.0.1</version>
	  		<configuration>
	  			<avroDir>${basedir}/src/main/resources/avro</avroDir>
	  			<jsonldDir>${basedir}/src/main/resources/jsonld</jsonldDir>
	  			<jsonSchemaDir>${basedir}/src/main/resources/jsonschema</jsonSchemaDir>
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

* `sh:datatype` values must come from the XML Schema namespace (e.g. `xsd:string`).
* Generic constraints such as `sh:not`, `sh:or`, `sh:and`, etc. are not supported.

We hope to remove these limitations in the future.   






