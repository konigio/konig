package io.konig.core.showl;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class ShowlServiceImpl implements ShowlService {
	private static Logger logger = LoggerFactory.getLogger(ShowlServiceImpl.class);
	
	private Map<URI,ShowlClass> classMap = new HashMap<>();
	private Map<URI,ShowlProperty> propertyMap = new HashMap<>();
	private Map<URI,Shape> enumNodeShapes = new HashMap<>();
	
	private OwlReasoner reasoner;
	private ShowlNodeShapeBuilder nodeBuilder;

	public ShowlServiceImpl(OwlReasoner reasoner) {
		this.reasoner = reasoner;
		nodeBuilder = new ShowlNodeShapeBuilder(this, this);
	}

	@Override
	public ShowlClass produceShowlClass(URI id) {
		ShowlClass owlClass = classMap.get(id);
		if (owlClass == null) {
			owlClass = new ShowlClass(reasoner, id);
			classMap.put(id,  owlClass);
		}
		return owlClass;
	}

	@Override
	public ShowlProperty produceProperty(URI predicate) {
		ShowlProperty property = propertyMap.get(predicate);
		if (property == null) {
			property = new ShowlProperty(predicate);
			propertyMap.put(predicate, property);

			URI domain = RdfUtil.uri(reasoner.getDomain(predicate));
			URI range = RdfUtil.uri(reasoner.getRange(predicate));
			
			if (domain != null) {
				property.setDomain(produceShowlClass(domain));
			}
			if (range != null) {
				property.setRange(produceShowlClass(range));
			}
		}
		return property;
	}

	@Override
	public ShowlNodeShape logicalNodeShape(URI owlClass) throws ShowlProcessingException {
		throw new ShowlProcessingException("not implemented yet");
	}

	@Override
	public ShowlNodeShape createNodeShape(Shape shape) throws ShowlProcessingException {
		return createShowlNodeShape(null, shape, produceShowlClass(shape.getTargetClass()));
	}

	@Override
	public ShowlNodeShape createNodeShape(Shape shape, DataSource ds) throws ShowlProcessingException {

		throw new ShowlProcessingException("not implemented yet");
	}

	@Override
	public ShowlClass inferDomain(ShowlProperty p) {
		return p.inferDomain(this);
	}

	@Override
	public ShowlClass inferRange(ShowlProperty p) {
		return p.inferRange(this);
	}

	@Override
	public ShowlClass mostSpecificClass(ShowlClass a, ShowlClass b) {

		ShowlClass result = 
			a==null ? b :
			b==null ? a :
			reasoner.isSubClassOf(a.getId(), b.getId()) ? a :
			b;
		
		return result==null ? produceShowlClass(Konig.Undefined) : result;
	}

	@Override
	public ShowlNodeShape createShowlNodeShape(ShowlPropertyShape accessor, Shape shape, ShowlClass owlClass) {
		
		return nodeBuilder.buildNodeShape(accessor, shape);
	}

	@Override
	public Set<ShowlNodeShape> selectCandidateSources(ShowlNodeShape targetShape) {
		throw new ShowlProcessingException("not implemented yet");
	}

	@Override
	public ShapeManager getShapeManager() {

		throw new ShowlProcessingException("not implemented yet");
	}

	@Override
	public Graph getGraph() {
		return reasoner.getGraph();
	}

	@Override
	public OwlReasoner getOwlReasoner() {
		return reasoner;
	}

	@Override
	public Shape enumNodeShape(ShowlClass enumClass) throws ShowlProcessingException {
		
		Shape result = enumNodeShapes.get(enumClass.getId());
		if (result == null) {
			result = enumShape(enumClass.getId());
			enumNodeShapes.put(enumClass.getId(), result);
		}
		
		return result;
	}

	private Shape enumShape(URI classId) throws ShowlProcessingException {

		Graph graph = reasoner.getGraph();
		NamespaceManager nsManager = graph.getNamespaceManager();
		Namespace ns = nsManager.findByName(classId.getNamespace());
		if (ns == null) {
			throw new ShowlProcessingException("Prefix not found for namespace <" + classId.getNamespace() + ">");
		}
		StringBuilder builder = new StringBuilder();
		builder.append(ShowlUtil.ENUM_SHAPE_BASE_IRI);
		builder.append(ns.getPrefix());
		builder.append('/');
		builder.append(classId.getLocalName());
		
		URI shapeId = new URIImpl(builder.toString());
		
		Shape shape = new Shape(shapeId);
		shape.setNodeKind(NodeKind.IRI);
		shape.setTargetClass(classId);
		
		
		ShowlClass owlClass = classMap.get(classId);
		if (owlClass == null) {
			throw new ShowlProcessingException("ShowlClass not defined: " + classId);
		}
		
		addProperties(shape, owlClass);
		
		return shape;
	}

	private void addProperties(Shape shape, ShowlClass owlClass) throws  ShowlProcessingException {
		
		URI classId = owlClass.getId();
		
		List<Vertex> individualList = reasoner.getGraph().v(classId).in(RDF.TYPE).toVertexList();
		
		if (individualList.isEmpty()) {
			throw new ShowlProcessingException("No members found in enumeration " + classId.getLocalName());
		}
		
		for (Vertex member : individualList) {
		
			
			for (Entry<URI,Set<Edge>> entry : member.outEdges()) {
				Set<Edge> edgeSet  = entry.getValue();
				URI predicate = entry.getKey();
				if (predicate.equals(RDF.TYPE)) {
					continue;
				}
				if (edgeSet.size()>1) {
					logger.warn("Ignoring property {}.{} because multi-valued properties are not supported yet.", 
							classId.getLocalName(), predicate.getLocalName());
					continue;
				}

				ShowlProperty property = propertyMap.get(predicate);
				
				URI range = (property==null) ? null :  property.inferRange(reasoner);
				
				if (range == null) {
					logger.warn("Ignoring property {}.{} because the range is not known.",
							classId.getLocalName(), predicate.getLocalName());
					continue;
				}

				PropertyConstraint constraint = shape.getPropertyConstraint(predicate);
				if (constraint==null) {
					constraint = new PropertyConstraint(predicate);
					shape.add(constraint);
				
					if (reasoner.isDatatype(range)) {
						constraint.setDatatype(range);
					} else {
						constraint.setValueClass(range);
						
						// For now, we don't support nested shapes on enumeration classes.
						constraint.setNodeKind(NodeKind.IRI);
						
					}
					// For now we require that every property be optional and single-valued
					constraint.setMinCount(0);
					constraint.setMaxCount(1);
			
				}
			}
		}
		if (shape.getProperty().isEmpty()) {
			throw new ShowlProcessingException("No properties found in enumeration class " + classId.getLocalName());
		}
	}

}
