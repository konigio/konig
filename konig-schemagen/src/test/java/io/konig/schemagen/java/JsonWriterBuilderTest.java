package io.konig.schemagen.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

import com.fasterxml.jackson.core.JsonGenerator;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;

import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.Schema;
import io.konig.runtime.io.BaseJsonWriter;
import io.konig.shacl.NodeKind;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeBuilder;
import io.konig.shacl.ShapeManager;

public class JsonWriterBuilderTest {
	
	@Test
	public void testOrConstraintLoop() throws Exception {

		URI partyShapeId = uri("http://example.com/shapes/v1/schema/PartyShape");
		URI personShapeId = uri("http://example.com/shapes/v1/schema/PersonShape");
		URI orgShapeId = uri("http://example.com/shapes/v1/schema/OrganizationShape");
		
		URI personShapeId2 = uri("http://example.com/shapes/v2/schema/PersonShape");
		
		ShapeBuilder shapeBuilder = new ShapeBuilder();
		
		shapeBuilder
		
			.beginShape(partyShapeId)
				.or(personShapeId, orgShapeId)
			.endShape()
			
			.beginShape(personShapeId)
				.targetClass(Schema.Person)
				.beginProperty(Schema.familyName)
					.datatype(XMLSchema.STRING)
					.minCount(1)
					.maxCount(1)
				.endProperty()
			.endShape()
			
			.beginShape(orgShapeId)
				.targetClass(Schema.Organization)
				.beginProperty(Schema.name)
					.datatype(XMLSchema.STRING)
					.minCount(1)
					.maxCount(1)				
				.endProperty()
			.endShape()
			
			
			.beginShape(personShapeId2)
				.targetClass(Schema.Person)
				.beginProperty(Schema.sponsor)
					.valueShape(partyShapeId)
					.minCount(0)
				.endProperty()
			.endShape()
			
			;
		
		ShapeManager shapeManager = shapeBuilder.getShapeManager();
		Shape shape = shapeManager.getShapeById(personShapeId2);
		
		Graph graph = new MemoryGraph();
		OwlReasoner reasoner = new OwlReasoner(graph);

		String basePackage = "com.example";
		MemoryNamespaceManager nsManager = MemoryNamespaceManager.getDefaultInstance();
		nsManager.add("schema1", "http://example.com/shapes/v1/schema/" );
		nsManager.add("schema2", "http://example.com/shapes/v2/schema/" );
		JavaNamer javaNamer = new BasicJavaNamer(basePackage, nsManager);
		JsonWriterBuilder writerBuilder = new JsonWriterBuilder(reasoner, shapeManager, javaNamer);

		JCodeModel model = new JCodeModel();
		writerBuilder.buildJsonWriter(shape, model);

		File outDir = new File("target/test/JsonWriterBuilderTest/orConstraintLoop");
		outDir.mkdirs();
		model.build(outDir);
	}
	
	
	@Ignore
	public void testOrConstraint() throws Exception {

		URI partyShapeId = uri("http://example.com/shapes/v1/schema/PartyShape");
		URI personShapeId = uri("http://example.com/shapes/v1/schema/PersonShape");
		URI orgShapeId = uri("http://example.com/shapes/v1/schema/OrganizationShape");
		
		URI personShapeId2 = uri("http://example.com/shapes/v2/schema/PersonShape");
		
		ShapeBuilder shapeBuilder = new ShapeBuilder();
		
		shapeBuilder
		
			.beginShape(partyShapeId)
				.or(personShapeId, orgShapeId)
			.endShape()
			
			.beginShape(personShapeId)
				.targetClass(Schema.Person)
				.beginProperty(Schema.familyName)
					.datatype(XMLSchema.STRING)
					.minCount(1)
					.maxCount(1)
				.endProperty()
			.endShape()
			
			.beginShape(orgShapeId)
				.targetClass(Schema.Organization)
				.beginProperty(Schema.name)
					.datatype(XMLSchema.STRING)
					.minCount(1)
					.maxCount(1)				
				.endProperty()
			.endShape()
			
			
			.beginShape(personShapeId2)
				.targetClass(Schema.Person)
				.beginProperty(Schema.sponsor)
					.valueShape(partyShapeId)
					.maxCount(1)
					.minCount(0)
				.endProperty()
			.endShape()
			
			;
		
		ShapeManager shapeManager = shapeBuilder.getShapeManager();
		Shape shape = shapeManager.getShapeById(personShapeId2);
		
		Graph graph = new MemoryGraph();
		OwlReasoner reasoner = new OwlReasoner(graph);

		String basePackage = "com.example";
		MemoryNamespaceManager nsManager = MemoryNamespaceManager.getDefaultInstance();
		JavaNamer javaNamer = new BasicJavaNamer(basePackage, nsManager);
		JsonWriterBuilder writerBuilder = new JsonWriterBuilder(reasoner, shapeManager, javaNamer);

		JCodeModel model = new JCodeModel();
		writerBuilder.buildJsonWriter(shape, model);

		File outDir = new File("target/test/JsonWriterBuilderTest/orConstraint");
		outDir.mkdirs();
		model.build(outDir);
	}
	
	@Ignore
	public void testEnumId() throws Exception {
		String basePackage = "com.example";
		URI genderTypeShapeId = uri("http://example.com/shapes/v1/schema/GenderType");

		MemoryNamespaceManager nsManager = MemoryNamespaceManager.getDefaultInstance();
		ShapeBuilder builder = new ShapeBuilder();
		builder.beginShape(genderTypeShapeId)
			.targetClass(Schema.GenderType)
			.nodeKind(NodeKind.IRI)
			.property(Schema.name)
				.minCount(1)
				.maxCount(1)
				.datatype(XMLSchema.STRING)
			.endProperty()
		.endShape();
		
		ShapeManager shapeManager = builder.getShapeManager();
		Shape shape = shapeManager.getShapeById(genderTypeShapeId);
		
		Graph graph = new MemoryGraph();
		graph.edge(Schema.GenderType, RDFS.SUBCLASSOF, Schema.Enumeration);
		OwlReasoner reasoner = new OwlReasoner(graph);
		
		JavaNamer javaNamer = new BasicJavaNamer(basePackage, nsManager);
		JsonWriterBuilder writerBuilder = new JsonWriterBuilder(reasoner, shapeManager, javaNamer);

		JCodeModel model = new JCodeModel();
		writerBuilder.buildJsonWriter(shape, model);

		File outDir = new File("target/test/JsonWriterBuilderTest/enumId");
		outDir.mkdirs();
		model.build(outDir);
	}
	
	@Ignore
	public void testMultipleEnum() throws Exception {
		String basePackage = "com.example";
		URI personShapeId = uri("http://example.com/shapes/v1/schema/Person");

		MemoryNamespaceManager nsManager = MemoryNamespaceManager.getDefaultInstance();
		ShapeBuilder builder = new ShapeBuilder();
		builder.beginShape(personShapeId)
			.targetClass(Schema.Person)
			.nodeKind(NodeKind.IRI)
			.property(Schema.gender)
				.minCount(1)
				.nodeKind(NodeKind.IRI)
				.valueClass(Schema.GenderType)
			.endProperty()
		.endShape();
		
		ShapeManager shapeManager = builder.getShapeManager();
		Shape personShape = shapeManager.getShapeById(personShapeId);
		
		Graph graph = new MemoryGraph();
		graph.edge(Schema.GenderType, RDFS.SUBCLASSOF, Schema.Enumeration);
		OwlReasoner reasoner = new OwlReasoner(graph);
		
		JavaNamer javaNamer = new BasicJavaNamer(basePackage, nsManager);
		JsonWriterBuilder writerBuilder = new JsonWriterBuilder(reasoner, shapeManager, javaNamer);

		JCodeModel model = new JCodeModel();
		JDefinedClass dc = writerBuilder.buildJsonWriter(personShape, model);
		

		File outDir = new File("target/test/JsonWriterBuilderTest/multipleEnum");
		outDir.mkdirs();
		model.build(outDir);
	}
	
	@Ignore
	public void testSingleEnum() throws Exception {
		String basePackage = "com.example";
		URI personShapeId = uri("http://example.com/shapes/v1/schema/Person");

		MemoryNamespaceManager nsManager = MemoryNamespaceManager.getDefaultInstance();
		ShapeBuilder builder = new ShapeBuilder();
		builder.beginShape(personShapeId)
			.targetClass(Schema.Person)
			.property(Schema.gender)
				.minCount(1)
				.maxCount(1)
				.nodeKind(NodeKind.IRI)
				.valueClass(Schema.GenderType)
			.endProperty()
		.endShape();
		
		ShapeManager shapeManager = builder.getShapeManager();
		Shape personShape = shapeManager.getShapeById(personShapeId);
		
		Graph graph = new MemoryGraph();
		graph.edge(Schema.GenderType, RDFS.SUBCLASSOF, Schema.Enumeration);
		OwlReasoner reasoner = new OwlReasoner(graph);
		
		JavaNamer javaNamer = new BasicJavaNamer(basePackage, nsManager);
		JsonWriterBuilder writerBuilder = new JsonWriterBuilder(reasoner, shapeManager, javaNamer);

		JCodeModel model = new JCodeModel();
		JDefinedClass dc = writerBuilder.buildJsonWriter(personShape, model);
		

		File outDir = new File("target/test/JsonWriterBuilderTest/singleEnum");
		outDir.mkdirs();
		model.build(outDir);
	}
	

	@Ignore
	public void testMultipleType() throws Exception {

		String basePackage = "com.example";
		URI personShapeId = uri("http://example.com/shapes/v1/schema/Person");

		MemoryNamespaceManager nsManager = MemoryNamespaceManager.getDefaultInstance();
		ShapeBuilder builder = new ShapeBuilder();
		builder.beginShape(personShapeId)
			.targetClass(Schema.Person)
			.property(RDF.TYPE)
				.minCount(1)
				.nodeKind(NodeKind.IRI)
			.endProperty()
		.endShape();
		
		ShapeManager shapeManager = builder.getShapeManager();
		Shape personShape = shapeManager.getShapeById(personShapeId);
		
		JavaNamer javaNamer = new BasicJavaNamer(basePackage, nsManager);
		JsonWriterBuilder writerBuilder = new JsonWriterBuilder(null, shapeManager, javaNamer);

		JCodeModel model = new JCodeModel();
		JDefinedClass dc = writerBuilder.buildJsonWriter(personShape, model);
		

		File outDir = new File("target/test/JsonWriterBuilderTest/multipleType");
		outDir.mkdirs();
		model.build(outDir);
	}
	
	
	@Ignore
	public void testSingleType() throws Exception {

		String basePackage = "com.example";
		URI personShapeId = uri("http://example.com/shapes/v1/schema/Person");

		MemoryNamespaceManager nsManager = MemoryNamespaceManager.getDefaultInstance();
		ShapeBuilder builder = new ShapeBuilder();
		builder.beginShape(personShapeId)
			.targetClass(Schema.Person)
			.property(RDF.TYPE)
				.minCount(1)
				.maxCount(1)
				.nodeKind(NodeKind.IRI)
			.endProperty()
		.endShape();
		
		ShapeManager shapeManager = builder.getShapeManager();
		Shape personShape = shapeManager.getShapeById(personShapeId);
		
		JavaNamer javaNamer = new BasicJavaNamer(basePackage, nsManager);
		JsonWriterBuilder writerBuilder = new JsonWriterBuilder(null, shapeManager, javaNamer);

		JCodeModel model = new JCodeModel();
		JDefinedClass dc = writerBuilder.buildJsonWriter(personShape, model);
		

		File outDir = new File("target/test/JsonWriterBuilderTest/singleType");
		outDir.mkdirs();
		model.build(outDir);
	}

	@Ignore
	public void test() throws Exception {
		
		String basePackage = "com.example";
		URI personShapeId = uri("http://example.com/shapes/v1/schema/Person");

		URI placeShapeId = uri("http://example.com/shapes/v1/schema/Place");
		URI orgShapeId = uri("http://example.com/shapes/v1/schema/Organization");
		
		URI age = uri("http://example.com/ns/age");
		URI nextMeetingTime = uri("http://example.com/ns/nextMeetingTime");
		
		MemoryNamespaceManager nsManager = MemoryNamespaceManager.getDefaultInstance();
		
		ShapeBuilder builder = new ShapeBuilder();
		builder.beginShape(personShapeId)
			.targetClass(Schema.Person)
			.beginProperty(Schema.name)
				.minCount(1)
				.maxCount(1)
				.datatype(XMLSchema.STRING)
			.endProperty()

			.beginProperty(Schema.email)
				.minCount(0)
				.datatype(XMLSchema.STRING)
			.endProperty()

			.beginProperty(age)
				.minCount(0)
				.maxCount(1)
				.datatype(XMLSchema.INT)
			.endProperty()

			.beginProperty(Schema.birthDate)
				.minCount(0)
				.maxCount(1)
				.datatype(XMLSchema.DATE)
			.endProperty()

			.beginProperty(nextMeetingTime)
				.minCount(0)
				.maxCount(1)
				.datatype(XMLSchema.DATETIME)
			.endProperty()
			
			.beginProperty(Schema.birthPlace)
				.minCount(0)
				.maxCount(1)
				.valueShape(placeShapeId)
			.endProperty()

			.beginProperty(Schema.alumniOf)
				.minCount(0)
				.valueShape(orgShapeId)
			.endProperty()
			
		.endShape()
		
		.beginShape(placeShapeId)
			.targetClass(Schema.Place)
		.endShape()

		.beginShape(orgShapeId)
			.targetClass(Schema.Organization)
		.endShape();
		
		ShapeManager shapeManager = builder.getShapeManager();
		Shape personShape = shapeManager.getShapeById(personShapeId);
		
		JavaNamer javaNamer = new BasicJavaNamer(basePackage, nsManager);
		JsonWriterBuilder writerBuilder = new JsonWriterBuilder(null, shapeManager, javaNamer);

		JCodeModel model = new JCodeModel();
		JDefinedClass dc = writerBuilder.buildJsonWriter(personShape, model);
		
		JClass extendsClass = dc._extends();
		assertTrue(extendsClass != null);
		assertEquals(BaseJsonWriter.class.getName(), extendsClass.fullName());
		
		
		JType objectClass = model._ref(Object.class);
		JType jsonGeneratorClass = model._ref(JsonGenerator.class);
		
		JMethod writeMethod = dc.getMethod("write", new JType[]{objectClass, jsonGeneratorClass});
		assertTrue(writeMethod != null);
		
		
		File outDir = new File("target/test/JsonWriterBuilderTest/general");
		outDir.mkdirs();
		model.build(outDir);
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}
}
