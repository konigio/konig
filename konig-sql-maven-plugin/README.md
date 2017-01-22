# Konig SQL Shape Maven Plugin

A Maven plugin which generates SHACL shapes for SQL Tables.

Given CREATE TABLE statements annotated with semantic markup, the
plugin generates two Shapes:

- *Origin Shape*: Matches the structure and naming conventions of
the original table exactly.

- *Target Shape*: Contains the same information as the original table,
but it uses structures and naming conventions consistent with some semantic model.

It's easiest to understand the difference between these two shapes with an example.

Suppose you have the following table definition:

```
CREATE TABLE registrar.Person (
  person_name VARCHAR(64) NOT NULL,
  birth_date DATETIME,
  address_street VARCHAR(255),
  address_city VARCHAR(64),
  address_state CHAR(2)
)
```

If you want to describe a Person in JSON using this schema, you might have
something like this:

```
{
  "person_name" : "Alice Jones",
  "birth_date" : "1997-11-22",
  "address_street" : "101 Main Street",
  "address_city" : "Springfield",
  "address_state" : "AZ"
}
```

But if you wanted to describe the same Person in a manner consistent with the
[Schema.org]("http://schema.org/") semantic model, you would use a JSON document
like this instead:

```
{
  "name" : "Alice Jones",
  "birthDate" : "1997-11-22",
  "address" : {
    "streetAddress" : "101 Main Street",
    "addressLocality : "Springfield",
    "addressRegion" : "AZ"
  }
}
```

Alternatively, if you wanted to describe this Person in a manner consistent
with the [vCard](https://www.w3.org/TR/vcard-rdf/) standard, you would use a 
JSON document like this:

```
{
  "fn" : "Alice Jones",
  "bday" : "1997-11-22",
  "hasAddress" : {
    "street-address" : "101 Main Street",
    "locality : "Springfield",
    "region" : "AZ"
  }
}
```

These three JSON documents have different shapes.  We call the first shape the
*Origin Shape* because it is consistent with the original table structure.

The other two JSON documents correspond to possible *Target Shapes*.  

The Konig SQL Shape Maven Plugin will generate SHACL definitions for the
*Origin Shape* plus one *Target Shape* that you design.

## Semantic Markup

You use semantic markup to enrich the description of tables and columns. In the 
sections below, we discuss the various kinds of semantic markup.

### Namespace Prefixes

You can define namespace prefixes using Turtle syntax.

For instance, you might define some namespace prefixes like this:

```
@prefix schema : <http://schema.org/> .
@prefix alias  : <http://example.com/ns/alias/> .
@prefix reg    : <http://example.com/ns/registrar/> .
@prefix shape : <http://example.com/shape/> .
```
The following namespace prefixes are declared by default.  You do not need to define them
explicitly:

```
@prefix sh    : <http://www.w3.org/ns/shacl#> .
@prefix xsd   : <http://www.w3.org/2001/XMLSchema#> .
@prefix konig : <http://www.konig.io/ns/core/> .
```

By convention, prefix definitions typically appear at the top of the file.
However, they may appear anywhere outside of the CREATE TABLE statements.

Prefixes must be defined before they are used.

### Column Annotations
Columns may contain the following semantic annotations.

| Annotation Property  | Description                                            |
|-----------------|-------------------------------------------------------------|
| predicate       | The IRI for the `sh:predicate` associated with the column. |
| path            | A SPARQL Path that maps the column to a corresponding property in the *Target Shape* |

These annotation properties appear after the `SEMANTICS` keyword at the end of the
column specification.  Multiple properties are separated by semicolons.

Here's an example showing columns with semantic markup:

```
CREATE TABLE registrar.Person (
  person_name VARCHAR(64) NOT NULL SEMANTICS
    predicate alias:person_name ;
    path /schema:name ,

  birth_date DATETIME SEMANTICS
    predicate alias:birth_date ;
    path /schema:birthDate ,

  address_street VARCHAR(255) SEMANTICS
    predicate alias:address_street ;
    path /schema:address/schema:streetAddress ,

  address_city VARCHAR(64) SEMANTICS
    predicate alias:address_city ;
    path /schema:address/schema:addressLocality ,

  address_state CHAR(2) SEMANTICS
    predicate alias:address_street ;
    path /schema:address/schema:addressRegion
)
```

The `predicate` value specifies the IRI for the `sh:predicate` associated with the
column.

For instance, the generated *Origin Shape* will contain the following property
constraint for the `person_name` column:

```
  sh:property [
    sh:predicate alias:person_name ;
    sh:minCount 1 ;
    sh:maxCount 1 ;
    sh:datatype xsd:string
  ]
```

One rarely defines `predicate` values explicitly because they tend to follow a
common pattern.  It is more convenient to define the `predicate` values via an
IRI template.  We discuss IRI templates later.

The `path` value is a SPARQL Path that maps the column to a corresponding
property in the *Target Shape* .  The path may include a sequence of more than one
predicate to indicate a nested structure.

For instance, the *Target Shape* in the example above will nest all of the address
properties like this:

```
  sh:property [
    sh:predicate schema:address ;
    sh:shape [
      sh:predicate schema:streetAddress ;
      sh:minCount 0 ;
      sh:maxCount 1 ;
      sh:datatype xsd:string
    ] , [
      sh:predicate schema:addressLocality ;
      sh:minCount 0 ;
      sh:maxCount 1 ;
      sh:datatype xsd:string
    ] , [
      sh:predicate schema:addressRegion ;
      sh:minCount 0 ;
      sh:maxCount 1 ;
      sh:datatype xsd:string
    ]
  ]

```

### Table Annotations
Tables may contain the following semantic annotations:

| Annotation Property   | Description                                                      |
|-----------------|------------------------------------------------------------|
| `hasShape`      | The IRI for the *Origin Shape* that matches the structure and naming conventions of the original table exactly |
| `targetShape` | The IRI for the *Target Shape* that contains the same information as the *Origin Shape* but using structures and naming conventions consistent with your semantic model. |
| `targetClass` | The OWL Class associated with the table.  Each row is understood to be an instance of this class. |
| `stagingTableId` | The value of the `konig:bigQueryTableId` property of the *Origin Shape*.  Set this value if you intend to extract data from the table and stage it in Google BigQuery (possibly as a federated table). |
| `targetTableId` | The value of the `konig:bigQueryTableId` property of the *Target Shape*.  Set this value if you intend to transform the `Origin Shape` to the `Target Shape` and store the transformed data in Google BigQuery. |
| `columnNamespace` | The default namespace to use when mapping column names to predicates within the *Origin Shape*. The value can be either a fully-qualified namespace or a previously defined namespace prefix. |
| `columnPathTemplate` | A template for the SPARQL Path that maps properties from the *Origin Shape* to corresponding properties in the *Target Shape*. |


These annotation properties appear at the end of the CREATE TABLE statement following
the `SEMANTICS` keyword.

Here's an example:

```
CREATE TABLE registrar.Person (...)
SEMANTICS
  targetClass schema:Person ;
  hasShape shape:OriginPersonShape ;
  targetShape shape:TargetPersonShape ;
  stagingTableId staging.Person ;
  targetTableId warehouse.Person ;
  columnNamespace alias
.  
```

Individual annotation properties are separated by semicolons,
and the full list of annotations is terminated with a period.

With these annotations, the *Origin* and *Target* shapes would look like this:

**Origin Shape**
```
shape:OriginPersonShape a sh:Shape ;
  sh:targetClass schema:Person ;
  konig:bigQueryTableId "staging.Person" ;
  sh:property [...]
  ...
.
```

**Target Shape**
```
shape:TargetPersonShape a sh:Shape ;
  sh:targetClass schema:Person ;
  konig:bigQueryTableId "warehouse.Person" ;
  sh:property [...]
  ...
.
```

These snippets omit details about the `PropertyConstraint` elements
within the SHACL Shapes. 

The `PropertyConstraint` elements are affected by the `columnNamespace` and
`columnPathTemplate` annotations.

In the example above, we have the annotation:

```
  columnNamespace alias
```
 
Column names will (by default) be scoped within the specified namespace.  For instance,
the `PropertyConstraint` generated for the `birth_date` column looks like this:

```
	[
		sh:predicate alias:birth_date ;
		sh:minCount 0 ;
		sh:maxCount 1 ;
		sh:datatype xsd:dateTime
	]
```

Notice that `sh:predicate` value is a CURIE in the `alias` namespace.

You can override the default namespace by explicitly setting the `predicate` annotation
on individual columns.




### Global Templates


## Usage

To use the Konig SQL Shape plugin in your Maven project you must:

1. Add SQL file(s) to your project
2. Annotate your SQL file(s) with semantic markup.
3. Add `konig-sql-shape-maven-plugin` to your POM
4. Run `mvn generate-sources`

We discuss these steps in more detail below.

### Add SQL Files to your project

As a best practice, you should put your SQL files in the directory:

```
  ${basedir}/src/sql
```

The plugin will look in this directory for any file with the  `.sql` suffix.

You can configure the plugin to look in a different directory by setting the
`sqlSourceDir` parameter.

### Annotate your SQL files

You need to add semantic markup to your SQL files so that the plugin will know
how tables and columns relate to classes and properties in your semantic model.

Broadly speaking, there are three kinds of semantic markup:

1. Global Directives
2. Table-Level Semantics
3. Column-Level Semantics

We discuss these categories below.

#### Global Directives

Global directives consist of namespace prefix definitions and various templates.

Within you SQL File, you can define namespace prefixes using Turtle syntax.

For instance, you could might define some namespace prefixes like this:

```
@prefix reg : <http://example.com/ns/registrar/> .
@prefix org: <http://www.w3.org/ns/org#> .
```

IRI Templates are useful if you adopt common naming conventions for classes




Add a `<plugin>` description like this:

```
<plugin>
	  		<groupId>io.konig</groupId>
	  		<artifactId>konig-sql-shape-maven-plugin</artifactId>
	  		<version>${konig.version}</version>
	  		<executions>
	  			<execution>
		  			<phase>generate-sources</phase>
		  			<goals>
		  				<goal>generate</goal>
		  			</goals>
	  			</execution>
	  		</executions>
  		</plugin>
```
