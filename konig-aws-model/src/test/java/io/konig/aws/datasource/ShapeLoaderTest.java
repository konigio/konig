package io.konig.aws.datasource;

import java.io.File;
import java.io.IOException;

/*
 * #%L
 * Konig Google Cloud Platform Model
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.AWS;
import io.konig.core.vocab.GCP;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.SH;
import io.konig.datasource.DataSource;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;

public class ShapeLoaderTest {

	private ShapeManager shapeManager = new MemoryShapeManager();
	private ShapeLoader shapeLoader = new ShapeLoader(shapeManager);
	
	@Before
	public void setUp() {
		AwsShapeConfig.init();
	}
	
	@Test
	public void testS3() throws RDFParseException, RDFHandlerException, IOException {
		MemoryGraph graph = new MemoryGraph();
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		RdfUtil.loadTurtle(new File("src/test/resources"), graph, nsManager);
		
		shapeLoader.load(graph);
		
		
		URI shapeId = uri("http://example.com/shapes/SourcePersonShape");
		Shape shape = shapeManager.getShapeById(shapeId);
		S3Bucket bucket = getBucket(shape);
		
		assertTrue(bucket != null);
		NotificationConfiguration config = bucket.getNotificationConfiguration();
		assertTrue(config != null);
		
		TopicConfiguration topicConfig = config.getTopicConfiguration();
		assertTrue(topicConfig!=null);
		
		Topic topic = topicConfig.getTopic();
		assertTrue(topic!=null);
		assertEquals("arn:aws:sns:us-east-1:${aws-account-id}:SourcePersonShape_CreateEvent", topic.getId().stringValue());
		assertEquals("SourcePersonShape_CreateEvent", topic.getResourceName());
		assertEquals("us-east-1", topic.getRegion());
		assertEquals("${aws-account-id}", topic.getAccountId());
		
		QueueConfiguration queueConfig = config.getQueueConfiguration();
		assertTrue(queueConfig != null);
		
		Queue queue = queueConfig.getQueue();
		assertTrue(queue!=null);
		assertEquals("arn:aws:sqs:us-east-1:${aws-account-id}:SourcePersonShape_CreateEvent", queue.getId().stringValue());
		assertEquals("SourcePersonShape_CreateEvent", queue.getResourceName());
		assertEquals("${aws-account-id}", queue.getAccountId());
		
		
	}
	private S3Bucket getBucket(Shape shape) {
		List<DataSource> list = shape.getShapeDataSource();
		assertTrue(list != null);
		for (DataSource ds : list) {
			if (ds instanceof S3Bucket) {
				return (S3Bucket) ds;
			}
		}
		return null;
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}
}
