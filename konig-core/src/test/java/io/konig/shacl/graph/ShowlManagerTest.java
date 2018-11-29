package io.konig.shacl.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.showl.ShowlManager;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;
import io.konig.datasource.DataSource;
import io.konig.shacl.ShapeBuilder;

public class ShowlManagerTest {
	
	private Graph graph = new MemoryGraph(MemoryNamespaceManager.getDefaultInstance());
	private OwlReasoner reasoner = new OwlReasoner(graph);
	private ShapeBuilder shapeBuilder = new ShapeBuilder();
	private ShowlManager showlManager = new ShowlManager();
	
	
	
	@Test
	public void testNestedRecord() {

		URI sourcePersonId = uri("http://example.com/shapes/SourcePersonShape");
		URI targetPersonId = uri("http://example.com/shapes/TargetPersonShape");
		URI targetAddressId = uri("http://example.com/shapes/TargetAddressShape");
		
		URI city = uri("http://example.com/alias/city");
		URI first_name = uri("http://example.com/alias/first_name");
		
		String firstNameFormula = 
				  "@term givenName <http://schema.org/givenName>\n"
				+ "$.givenName";
		
		String cityFormula =
			  	  "@term address <http://schema.org/address>\n"
				+ "@term addressLocality <http://schema.org/addressLocality>\n"
				+ "$.address.addressLocality";
		
		shapeBuilder
			.beginShape(sourcePersonId)
				.beginProperty(first_name)
					.datatype(XMLSchema.STRING)
					.formula(firstNameFormula)
				.endProperty()
				.beginProperty(city)
					.datatype(XMLSchema.STRING)
					.formula(cityFormula)
				.endProperty()
				.beginDataSource(DataSource.Builder.class)
					.id("http://example.com/table/PERSON_STG")
				.endDataSource()
			.endShape()
			.beginShape(targetPersonId)
				.targetClass(Schema.Person)
				.beginProperty(Schema.givenName)
					.datatype(XMLSchema.STRING)
				.endProperty()
				.beginProperty(Schema.address)
					.beginValueShape(targetAddressId)
						.targetClass(Schema.PostalAddress)
						.beginProperty(Schema.addressLocality)
							.datatype(XMLSchema.STRING)
						.endProperty()
					.endValueShape()
				.endProperty()
				.beginDataSource(DataSource.Builder.class)
					.id("http://example.com/table/Person")
				.endDataSource()
			.endShape();
		
		load();
		ShowlNodeShape sourcePersonShape = showlManager.getNodeShape(sourcePersonId);
		
		assertTrue(sourcePersonShape != null);
		assertTrue(sourcePersonShape.getOwlClass()!=null);
		assertEquals(Schema.Person, sourcePersonShape.getOwlClass().getId());
		
		ShowlPropertyShape cityPropertyShape = sourcePersonShape.getProperty(city);
		assertTrue(cityPropertyShape != null);
		
		ShowlNodeShape targetPersonShape = showlManager.getNodeShape(targetPersonId);
		assertTrue(targetPersonShape != null);
		ShowlPropertyShape addressLocalityShape = targetPersonShape
				.getProperty(Schema.address)
				.getValueShape()
				.getProperty(Schema.addressLocality);
		
		assertTrue(cityPropertyShape.getGroup() == addressLocalityShape.getGroup());
		
		
		
	}
	
	@Ignore
	public void testInferNullTargetClass() {
		URI sourceShapeId = uri("http://example.com/shapes/SourcePersonShape");
		URI targetShapeId = uri("http://example.com/shapes/TargetPersonShape");
		URI first_name = uri("http://example.com/ns/first_name");
		
		String formula = 
				  "@term givenName <http://schema.org/givenName>\n"
				+ "$.givenName";
		
		shapeBuilder
			.beginShape(targetShapeId)
				.targetClass(Schema.Person)
				.beginProperty(Schema.givenName)
					.datatype(XMLSchema.STRING)
				.endProperty()
				.beginDataSource(DataSource.Builder.class)
					.id("http://example.com/ds/foo")
				.endDataSource()
			.endShape()
			.beginShape(sourceShapeId)
				.targetClass(null)
				.beginProperty(first_name)
					.datatype(XMLSchema.STRING)
					.formula(formula)
				.endProperty()
				.beginDataSource(DataSource.Builder.class)
					.id("http://example.com/ds/bar")
				.endDataSource()
			.endShape();
		
		load();
		
		ShowlNodeShape sourceNode = showlManager.getNodeShape(sourceShapeId);
		
		URI owlClassId = sourceNode.getOwlClass().getId();
		
		assertEquals(Schema.Person, owlClassId);
	}

	private void load() {

		showlManager.load(shapeBuilder.getShapeManager(), reasoner);
		
	}

	@Ignore
	public void testInferUndefinedTargetClass() {

		URI sourceShapeId = uri("http://example.com/shapes/SourcePersonShape");
		URI targetShapeId = uri("http://example.com/shapes/TargetPersonShape");
		URI first_name = uri("http://example.com/ns/first_name");
		
		String formula = 
				  "@term givenName <http://schema.org/givenName>\n"
				+ "$.givenName";
		
		shapeBuilder
			.beginShape(targetShapeId)
				.targetClass(Schema.Person)
				.beginProperty(Schema.givenName)
					.datatype(XMLSchema.STRING)
				.endProperty()
				.beginDataSource(DataSource.Builder.class)
					.id("http://example.com/ds/foo")
				.endDataSource()
			.endShape()
			.beginShape(sourceShapeId)
				.targetClass(Konig.Undefined)
				.beginProperty(first_name)
					.datatype(XMLSchema.STRING)
					.formula(formula)
				.endProperty()
				.beginDataSource(DataSource.Builder.class)
					.id("http://example.com/ds/bar")
				.endDataSource()
			.endShape();
		
		showlManager.load(shapeBuilder.getShapeManager(), reasoner);
		
		ShowlNodeShape sourceNode = showlManager.getNodeShape(sourceShapeId);
		
		URI owlClassId = sourceNode.getOwlClass().getId();
		
		assertEquals(Schema.Person, owlClassId);
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}
