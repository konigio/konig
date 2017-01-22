package io.konig.schemagen.merge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.AS;
import io.konig.core.vocab.Schema;
import io.konig.schemagen.SimpleShapeNamer;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeBuilder;

public class ShapeAggregatorTest {
	
	@Test
	public void testValueShape() {
		Graph graph = new MemoryGraph();
		graph.edge(Schema.VideoObject, RDFS.SUBCLASSOF, Schema.MediaObject);
		graph.edge(Schema.MediaObject, RDFS.SUBCLASSOF, Schema.CreativeWork);
		graph.edge(Schema.WebPage, RDFS.SUBCLASSOF, Schema.CreativeWork);
		graph.edge(Schema.CreativeWork, RDFS.SUBCLASSOF, Schema.Thing);
		
		OwlReasoner owl = new OwlReasoner(graph);
		
		String aName = "http://example.com/v1/as/Activity";
		String bName = "http://example.com/v2/as/Activity";
		
		String videoShapeName = "http://example.com/v1/schema/VideoObject";
		String webPageShapeName = "http://example.com/v1/schema/WebPage";
		
		URI bitrate = uri("http://schema.org/bitrate");
		URI author = uri("http://schema.org/author");
		
		ShapeBuilder builder = new ShapeBuilder()
			.beginShape(aName)
				.beginProperty(AS.object)
					.beginValueShape(videoShapeName)
						.beginProperty(bitrate)
							.datatype(XMLSchema.STRING)
							.minCount(1)
							.maxCount(1)
						.endProperty()
					.endValueShape()
				.endProperty()
			.endShape()
			.beginShape(bName)
				.beginProperty(AS.object)
					.beginValueShape(webPageShapeName)
						.beginProperty(author)
							.nodeKind(NodeKind.IRI)
							.valueClass(Schema.Person)
						.endProperty()
					.endValueShape()
				.endProperty()
			.endShape()
			;
		
		Shape a = builder.getShape(aName);
		Shape b = builder.getShape(bName);
		
		ShapeAggregator aggregator = new ShapeAggregator(owl, builder.getShapeManager());
		URI name = namer().shapeName(AS.Activity);
		
		Shape s = aggregator.merge(name, a, b);
		PropertyConstraint c = s.getPropertyConstraint(AS.object);
		assertTrue(c != null);
		
		Shape obj = c.getShape();
		assertTrue(obj != null);
		assertEquals("http://example.com/shape/dw/as/Activity.object", obj.getId().stringValue());
		
		c = obj.getPropertyConstraint(bitrate);
		assertTrue(c != null);
		
		Integer minCount = c.getMinCount();
		assertEquals(0, minCount.intValue());
		
		c = obj.getPropertyConstraint(author);
		assertTrue(c != null);
	}

	
	
	@Test
	public void testValueClass() {

		Graph graph = new MemoryGraph();
		graph.edge(Schema.VideoObject, RDFS.SUBCLASSOF, Schema.MediaObject);
		graph.edge(Schema.MediaObject, RDFS.SUBCLASSOF, Schema.CreativeWork);
		graph.edge(Schema.WebPage, RDFS.SUBCLASSOF, Schema.CreativeWork);
		graph.edge(Schema.CreativeWork, RDFS.SUBCLASSOF, Schema.Thing);
		
		OwlReasoner owl = new OwlReasoner(graph);
		
		String aName = "http://example.com/v1/as/Activity";
		String bName = "http://example.com/v2/as/Activity";
		ShapeBuilder builder = new ShapeBuilder()
			.beginShape(aName)
				.beginProperty(AS.object)
					.valueClass(Schema.VideoObject)
				.endProperty()
			.endShape()
			.beginShape(bName)
				.beginProperty(AS.object)
					.valueClass(Schema.WebPage)
				.endProperty()
			.endShape()
			;
		
		Shape a = builder.getShape(aName);
		Shape b = builder.getShape(bName);
		

		ShapeAggregator aggregator = new ShapeAggregator(owl, builder.getShapeManager());
		URI name = namer().shapeName(AS.Activity);
		Shape s = aggregator.merge(name, a, b);
		assertEquals(Schema.CreativeWork, s.getPropertyConstraint(AS.object).getValueClass());
	}
	

	@Test
	public void testMaxCount() {
		
		String aName = "http://example.com/v1/schema/Person";
		String bName = "http://example.com/v2/schema/Person";
		ShapeBuilder builder = new ShapeBuilder()
			.beginShape(aName)
				.beginProperty(Schema.name)
					.datatype(XMLSchema.STRING)
					.maxCount(2)
				.endProperty()
			.endShape()
			.beginShape(bName)
				.beginProperty(Schema.name)
					.datatype(XMLSchema.STRING)
					.maxCount(5)
				.endProperty()
			.endShape()
			;
		
		Shape a = builder.getShape(aName);
		Shape b = builder.getShape(bName);
		

		ShapeAggregator aggregator = new ShapeAggregator(null, builder.getShapeManager());
		URI name = namer().shapeName(Schema.Person);
		Shape s = aggregator.merge(name, a, b);
		
		int maxCount = s.getPropertyConstraint(Schema.name).getMaxCount();
		assertEquals(5, maxCount);
	}
	
	
	@Test
	public void testMinCount() {
		
		String aName = "http://example.com/v1/schema/Person";
		String bName = "http://example.com/v2/schema/Person";
		ShapeBuilder builder = new ShapeBuilder()
			.beginShape(aName)
				.beginProperty(Schema.name)
					.datatype(XMLSchema.STRING)
					.minCount(2)
				.endProperty()
			.endShape()
			.beginShape(bName)
				.beginProperty(Schema.name)
					.datatype(XMLSchema.STRING)
					.minCount(5)
				.endProperty()
			.endShape()
			;
		
		Shape a = builder.getShape(aName);
		Shape b = builder.getShape(bName);
		

		ShapeAggregator aggregator = new ShapeAggregator(null, builder.getShapeManager());
		URI name = namer().shapeName(Schema.Person);
		Shape s = aggregator.merge(name, a, b);
		
		int minCount = s.getPropertyConstraint(Schema.name).getMinCount();
		assertEquals(2, minCount);
	}
	
	@Test
	public void testAllowedValues() {
		
		String aName = "http://example.com/v1/schema/Person";
		String bName = "http://example.com/v2/schema/Person";
		URI harvard = uri("http://dbpedia.org/resource/Harvard");
		URI yale = uri("http://dbpedia.org/resource/Yale");
		URI stanford = uri("http://dbpedia.org/resource/Stanford");
		URI mit = uri("http://dbpedia.org/resource/MIT");
		ShapeBuilder builder = new ShapeBuilder()
			.beginShape(aName)
				.beginProperty(Schema.alumniOf)
					.allowedValue(harvard)
					.allowedValue(yale)
				.endProperty()
			.endShape()
			.beginShape(bName)
				.beginProperty(Schema.alumniOf)
					.allowedValue(stanford)
					.allowedValue(mit)
				.endProperty()
			.endShape()
			;
		
		Shape a = builder.getShape(aName);
		Shape b = builder.getShape(bName);
		
		ShapeAggregator aggregator = new ShapeAggregator(null, builder.getShapeManager());
		URI name = namer().shapeName(Schema.Person);
		Shape s = aggregator.merge(name, a, b);
		
		PropertyConstraint c = s.getPropertyConstraint(Schema.alumniOf);
		assertTrue(c != null);
		List<Value> list = c.getIn();
		assertTrue(list != null);
		assertTrue(list.contains(harvard));
		assertTrue(list.contains(mit));
		assertTrue(list.contains(stanford));
		assertTrue(list.contains(yale));
	}
	

	private URI uri(String string) {
		return new URIImpl(string);
	}


	@Test
	public void testPropertyUnion() {
		
		String aName = "http://example.com/v1/schema/Person";
		String bName = "http://example.com/v2/schema/Person";
		ShapeBuilder builder = new ShapeBuilder()
			.beginShape(aName)
				.beginProperty(Schema.givenName)
					.datatype(XMLSchema.STRING)
				.endProperty()
			.endShape()
			.beginShape(bName)
				.beginProperty(Schema.familyName)
					.datatype(XMLSchema.STRING)
				.endProperty()
			.endShape()
			;
		
		Shape a = builder.getShape(aName);
		Shape b = builder.getShape(bName);
		

		ShapeAggregator aggregator = new ShapeAggregator(null, builder.getShapeManager());
		URI name = namer().shapeName(Schema.Person);
		Shape s = aggregator.merge(name, a, b);
		
		assertEquals(2, s.getProperty().size());
		assertTrue(s.getPropertyConstraint(Schema.givenName) != null);
		assertTrue(s.getPropertyConstraint(Schema.familyName) != null);
	}
	
	private SimpleShapeNamer namer() {
		MemoryNamespaceManager manager = new MemoryNamespaceManager();
		manager.add("schema", "http://schema.org/");
		manager.add("as", "http://www.w3.org/ns/activitystreams#");
		
		return new SimpleShapeNamer(manager, "http://example.com/shape/dw/");
	}

}
