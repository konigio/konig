package io.konig.shacl.graph;

/*
 * #%L
 * Konig Core
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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.showl.ShowlJoinCondition;
import io.konig.core.showl.ShowlManager;
import io.konig.core.showl.ShowlMapping;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;
import io.konig.datasource.DataSource;
import io.konig.shacl.NodeKind;
import io.konig.shacl.ShapeBuilder;

public class ShowlManagerTest {
	
	private Graph graph = new MemoryGraph(MemoryNamespaceManager.getDefaultInstance());
	private OwlReasoner reasoner = new OwlReasoner(graph);
	private ShapeBuilder shapeBuilder = new ShapeBuilder();
	private ShowlManager showlManager = new ShowlManager();
	
	
	@Ignore
	public void testSourceToSource() {
		
		URI aPersonShapeId = uri("http://example.com/shapes/APersonShape");
		URI bPersonShapeId = uri("http://example.com/shapes/BPersonShape");
		
		URI aCity = uri("http://example.com/alias/a_city");
		URI bCity = uri("http://example.com/alias/b_city");
		
		graph.edge(Schema.addressLocality, RDFS.DOMAIN, Schema.PostalAddress);
		
		shapeBuilder
			.beginShape(aPersonShapeId)
			.targetClass(Schema.Person)
				.nodeKind(NodeKind.IRI)
				.beginProperty(aCity)
					.formula("$.address.addressLocality", Schema.address, Schema.addressLocality)
				.endProperty()
				.beginDataSource(DataSource.Builder.class)
				.endDataSource()
			.endShape()
			.beginShape(bPersonShapeId)
				.targetClass(Schema.Person)
				.nodeKind(NodeKind.IRI)
				.beginProperty(bCity)
					.formula("$.address.addressLocality", Schema.address, Schema.addressLocality)
				.endProperty()
				.beginDataSource(DataSource.Builder.class)
				.endDataSource()
			.endShape();
		load();
		
		ShowlNodeShape aPersonShape = showlManager.getNodeShape(aPersonShapeId).findAny();
		ShowlNodeShape bPersonShape = showlManager.getNodeShape(bPersonShapeId).findAny();
		
		ShowlJoinCondition join = aPersonShape.getJoinCondition(bPersonShape);
		assertTrue(join != null);
		
		
		ShowlPropertyShape aCityProperty = aPersonShape.findProperty(aCity);
		ShowlPropertyShape bCityProperty = bPersonShape.findProperty(bCity);
		
		ShowlMapping mapping = aCityProperty.getMapping(join);
		assertTrue(mapping != null);
		assertEquals(bCityProperty, mapping.findOther(aCityProperty));
		
		mapping = bCityProperty.getMapping(join);
		assertTrue(mapping != null);
		assertEquals(aCityProperty, mapping.findOther(bCityProperty));
		
	}
	
	@Test
	public void testNestedRecord() {

		URI sourcePersonId = uri("http://example.com/shapes/SourcePersonShape");
		URI targetPersonId = uri("http://example.com/shapes/TargetPersonShape");
		URI targetAddressId = uri("http://example.com/shapes/TargetAddressShape");
		
		URI city = uri("http://example.com/alias/city");
		URI first_name = uri("http://example.com/alias/first_name");
		
		
		shapeBuilder
			.beginShape(sourcePersonId)
				.nodeKind(NodeKind.IRI)
				.beginProperty(first_name)
					.datatype(XMLSchema.STRING)
					.formula("$.givenName", Schema.givenName)
				.endProperty()
				.beginProperty(city)
					.datatype(XMLSchema.STRING)
					.formula("$.address.addressLocality", Schema.address, Schema.addressLocality)
				.endProperty()
				.beginDataSource(DataSource.Builder.class)
					.id("http://example.com/table/PERSON_STG")
				.endDataSource()
			.endShape()
			.beginShape(targetPersonId)
				.nodeKind(NodeKind.IRI)
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
		ShowlNodeShape sourcePersonShape = showlManager.getNodeShape(sourcePersonId).findAny();
		
		assertTrue(sourcePersonShape != null);
		assertTrue(sourcePersonShape.getOwlClass()!=null);
		assertEquals(Schema.Person, sourcePersonShape.getOwlClass().getId());
		
		ShowlPropertyShape cityPropertyShape = sourcePersonShape.getProperty(city);
		assertTrue(cityPropertyShape != null);
		
		ShowlNodeShape targetPersonShape = showlManager.getNodeShape(targetPersonId).findAny();
		assertTrue(targetPersonShape != null);
		ShowlPropertyShape targetCity = targetPersonShape
				.getProperty(Schema.address)
				.getValueShape()
				.getProperty(Schema.addressLocality);
		
		assertTrue(targetCity != null);
		
		ShowlJoinCondition join = targetPersonShape.getJoinCondition(sourcePersonShape);
		assertTrue(join != null);
		
		ShowlPropertyShape sourceCity = sourcePersonShape.findProperty(city);
		assertTrue(sourceCity != null);
		
		ShowlMapping mapping = targetCity.getMapping(join);
		assertTrue(mapping != null);
		
		assertEquals(sourceCity, mapping.findOther(targetCity));
		
		
		
		
		
	}
	
	@Ignore
	public void testInferNullTargetClass() {
		URI sourceShapeId = uri("http://example.com/shapes/SourcePersonShape");
		URI targetShapeId = uri("http://example.com/shapes/TargetPersonShape");
		URI first_name = uri("http://example.com/ns/first_name");
		
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
					.formula("$.givenName", Schema.givenName)
				.endProperty()
				.beginDataSource(DataSource.Builder.class)
					.id("http://example.com/ds/bar")
				.endDataSource()
			.endShape();
		
		load();
		
		ShowlNodeShape sourceNode = showlManager.getNodeShape(sourceShapeId).findAny();
		
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
					.formula("$.givenName", Schema.givenName)
				.endProperty()
				.beginDataSource(DataSource.Builder.class)
					.id("http://example.com/ds/bar")
				.endDataSource()
			.endShape();
		
		showlManager.load(shapeBuilder.getShapeManager(), reasoner);
		
		ShowlNodeShape sourceNode = showlManager.getNodeShape(sourceShapeId).findAny();
		
		URI owlClassId = sourceNode.getOwlClass().getId();
		
		assertEquals(Schema.Person, owlClassId);
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}
