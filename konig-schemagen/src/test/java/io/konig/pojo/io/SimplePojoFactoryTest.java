package io.konig.pojo.io;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.KDG;
import io.konig.core.vocab.Schema;
import io.konig.core.vocab.TemporalUnit;
import io.konig.datagen.DataGeneratorConfig;
import io.konig.datagen.ShapeConfig;

public class SimplePojoFactoryTest {

	@Test
	public void test() {
		
		URI targetShape = uri("http://example.com/shape/foo");
		MemoryGraph graph = new MemoryGraph();
	
		graph.builder().beginSubject()
			.addProperty(RDF.TYPE, KDG.DataGeneratorConfig)
			.beginBNode(KDG.generate)
				.addProperty(KDG.targetShape, targetShape)
				.addProperty(KDG.shapeCount, 5)
			.endSubject()
		.endSubject();
		
		Vertex v = graph.v(KDG.DataGeneratorConfig).in(RDF.TYPE).firstVertex();
		
		
		SimplePojoFactory factory = new SimplePojoFactory();
		
		DataGeneratorConfig config = factory.create(v, DataGeneratorConfig.class);
		
		List<ShapeConfig> list = config.getShapeConfigList();
		
		assertEquals(1, list.size());
		
		ShapeConfig shapeConfig = list.get(0);
		
		assertEquals(targetShape, shapeConfig.getTargetShape());
		assertEquals(new Integer(5), shapeConfig.getShapeCount());
		
	}
	
	@Test
	public void testList() {
		MemoryGraph graph = new MemoryGraph();
		URI aliceId = uri("http://example.com/alice");
		
		graph.builder().beginSubject(aliceId)
			.addLiteral(Schema.name, "Alice")
			.addLiteral(Schema.name, "Babe");
		
		Vertex v = graph.vertex(aliceId);
		
		SimplePojoFactory factory = new SimplePojoFactory();
		Person person = factory.create(v, Person.class);
		
		List<String> list = person.getName();
		assertTrue(list != null);
	}
	
	@Test
	public void testEnumValue() {
		
		URI TimeValueClass = uri("http://schema.example.com/TimeValue");
		URI valuePredicate = uri("http://schema.example.com/value");
		URI unitPredicate = uri("http://schema.example.com/timeUnit");
		
		MemoryGraph graph = new MemoryGraph();
		graph.builder().beginSubject()
			.addProperty(RDF.TYPE, TimeValueClass)
			.addFloat(valuePredicate, 2.5f)
			.addProperty(unitPredicate, TemporalUnit.HOUR.getURI());
		
		Vertex v = graph.v(TimeValueClass).in(RDF.TYPE).firstVertex();

		SimplePojoFactory factory = new SimplePojoFactory();
		
		TimeValue pojo = factory.create(v, TimeValue.class);
		
		assertEquals(2.5f, pojo.getValue(), 0.0001);
		assertEquals(TemporalUnit.HOUR, pojo.getTimeUnit());
		 
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}
	
	public static class Person {
		private URI id;
		private List<String> name;
		
		public URI getId() {
			return id;
		}
		public void setId(URI id) {
			this.id = id;
		}
		public List<String> getName() {
			return name;
		}
		public void setName(List<String> name) {
			this.name = name;
		}
		
		public void addName(String name) {
			if (this.name == null) {
				this.name = new ArrayList<>();
			}
			this.name.add(name);
		}
		
		
	}
	
	static class TimeValue {
		private float value;
		private TemporalUnit timeUnit;
		public float getValue() {
			return value;
		}
		public void setValue(float value) {
			this.value = value;
		}
		public TemporalUnit getTimeUnit() {
			return timeUnit;
		}
		public void setTimeUnit(TemporalUnit timeUnit) {
			this.timeUnit = timeUnit;
		}
	}

}
