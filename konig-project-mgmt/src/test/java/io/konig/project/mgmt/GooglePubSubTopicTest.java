package io.konig.project.mgmt;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;
import io.konig.pojo.io.SimplePojoFactory;
import io.konig.shacl.Shape;

public class GooglePubSubTopicTest {

	/**
	 * Verify that we can load GooglePubSub topic resources from an RDF graph.
	 */
	@Test
	public void testPojoCreation() {
		
		URI topicId = uri("http://example.com/topic/Login");
		URI loginShapeId_v1 = uri("http://example.com/shape/v1/xas/LoginActivityShape");
		URI loginShapeId_v2 = uri("http://example.com/shape/v2/xas/LoginActivityShape");
		URI loginClass = uri("http://example.com/ns/activity/LoginActivity");
		URI templateId = uri("file:///c:/git/myproject/topic/login.json");
		
		URI projectId = uri("http://example.com/project/analytics");
		
		MemoryGraph graph = new MemoryGraph();
		graph.builder()
			.beginSubject(topicId)
				.addLiteral(Schema.description, "A topic containing login events")
				.addLiteral(Konig.topicName, "login")
				.addProperty(Konig.acceptsShape, loginShapeId_v1)
				.addProperty(Konig.acceptsShape, loginShapeId_v2)
				.addProperty(Konig.jsonTemplate, templateId)
				.addProperty(Konig.topicProject, projectId)
			.endSubject()
			.beginSubject(loginShapeId_v1)
				.addProperty(SH.targetClass, loginClass)
			.endSubject()
			.beginSubject(loginShapeId_v2)
				.addProperty(SH.targetClass, loginClass)
			.endSubject();
		
		Vertex subject = graph.getVertex(topicId);
		
		SimplePojoFactory factory = new SimplePojoFactory();
		
		GooglePubSubTopic topic = factory.create(subject, GooglePubSubTopic.class);
		
		assertEquals("A topic containing login events", topic.getDescription());
		assertEquals("login", topic.getTopicName());
		assertEquals(templateId, topic.getJsonTemplate());
		
		Collection<Shape> shapeList = topic.getAcceptsShape();
		assertTrue(shapeList != null);
		assertEquals(2, shapeList.size());
		
		assertContainsShape(shapeList, loginShapeId_v1);
		assertContainsShape(shapeList, loginShapeId_v2);
		
		KonigProject project = topic.getTopicProject();
		assertTrue(project != null);
		assertEquals(projectId, project.getId());
		
		Collection<GooglePubSubTopic> topicList = project.getProjectTopic();
		assertTrue(topicList != null);
		assertContainsTopic(topicList, topic);
		
	}

	private void assertContainsTopic(Collection<GooglePubSubTopic> topicList, GooglePubSubTopic topic) {
		
		for (GooglePubSubTopic t : topicList) {
			if (t.equals(topic)) {
				return;
			}
		}
		fail("Topic not found: " + topic.getId());
		
	}

	private void assertContainsShape(Collection<Shape> list, URI shapeId) {
		for (Shape shape : list) {
			if (shapeId.equals(shape.getId())) {
				return;
			}
		}
		fail("acceptsShape not found: " + shapeId);
		
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}
