package io.konig.transform.mysql;

/*
 * #%L
 * Konig Transform
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.gcp.datasource.GoogleCloudSqlTable;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeBuilder;
import io.konig.transform.model.TDataSource;
import io.konig.transform.model.TNodeShape;
import io.konig.transform.sql.SqlTransform;

public class RoutedSqlTransformVisitorTest {
	
	@Mock
	private SqlTransformVisitor bigQueryVisitor;

	private RoutedSqlTransformVisitor visitor = new RoutedSqlTransformVisitor();
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testFilter() throws Exception {

		URI shapeId = uri("http://example.com/shape/PersonShape");
		URI orgShapeId = uri("http://example.com/shape/OrgShape");
		
		ShapeBuilder builder = new ShapeBuilder();
		
		builder
			.beginShape(shapeId)
				.beginDataSource(GoogleBigQueryTable.Builder.class)
				
				.endDataSource()
			.endShape()
			.beginShape(orgShapeId)
				.beginDataSource(GoogleCloudSqlTable.Builder.class)
				.endDataSource()
			.endShape();

		visitor.put(Konig.GoogleBigQueryTable, bigQueryVisitor);
		
		Shape shape = builder.getShape(shapeId);
		DataSource ds = shape.getShapeDataSource().get(0);
		boolean accepted = visitor.accept(shape, ds);
		assertTrue(accepted);
		
		TNodeShape targetShape = new TNodeShape(shape);
		SqlTransform transform = sqlTransform(targetShape);
		TDataSource tds = new TDataSource(ds, targetShape);
		targetShape.setTdatasource(tds);
		
		visitor.visit(transform);
		
		ArgumentCaptor<SqlTransform> bigQueryTransformCaptor = ArgumentCaptor.forClass(SqlTransform.class);
		
		verify(bigQueryVisitor).visit(bigQueryTransformCaptor.capture());
		
		SqlTransform capturedTransform = bigQueryTransformCaptor.getValue();
		assertTrue(transform == capturedTransform);
		
		shape = builder.getShape(orgShapeId);
		ds = shape.getShapeDataSource().get(0);
		
		accepted = visitor.accept(shape, ds);
		assertTrue(!accepted);
		
		

	}

	private SqlTransform sqlTransform(TNodeShape targetShape) {
		MemoryGraph graph = new MemoryGraph();
		OwlReasoner reasoner = new OwlReasoner(graph);
		
		return new SqlTransform(targetShape, reasoner);
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}
