package io.konig.shacl.jsonld;

/*
 * #%L
 * konig-shacl
 * %%
 * Copyright (C) 2015 Gregory McFall
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.Context;
import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.Term;
import io.konig.core.Term.Kind;
import io.konig.core.Traversal;
import io.konig.core.Vertex;
import io.konig.core.impl.BasicContext;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

/**
 * A service which generates a JSON-LD context for a data shape.
 * @author Greg McFall
 *
 */
public class ContextGenerator {
	private static final Logger logger = LoggerFactory.getLogger(ContextGenerator.class);
	
	private ShapeManager shapeManager;
	private NamespaceManager nsManager;
	private ContextNamer namer;
	private Graph owl;
	
	private boolean ignoreTermNameClass = true;
	private boolean ignoreShapeId = true;
	

	public ContextGenerator(ShapeManager shapeManager, NamespaceManager nsManager, ContextNamer namer, Graph owl) {
		this.shapeManager = shapeManager;
		this.nsManager = nsManager;
		this.namer = namer;
		this.owl = owl;
	}


	public Context forShape(Shape shape) throws KonigException {
		
		Worker worker = new Worker();
		Context context = worker.forShape(shape);
		
		if (logger.isDebugEnabled()) {
			logger.debug("-----------GENERATED CONTEXT------------------------\n" + context.toString());
		}
		
		return context;
	}
	
	class Worker {
		private Context context;
		private Map<String, Term> inverse = new HashMap<String, Term>();
		private Set<String> memory = new HashSet<String>();
		
		public Worker() {
		}
		
		public Context forShape(Shape shape) {
			if (shape.getId() instanceof BNode) {
				throw new KonigException("Shape Id must be a URI");
			}
			URI shapeId = (URI) shape.getId();
			URI contextId = namer.forShape(shapeId);
			context = new BasicContext(contextId.stringValue());
			
			addShape(shape);
			
			context.sort();
			
			shape.setJsonldContext(context);
			
			return context;
		}
		
		private void addShape(Shape shape) {
			Resource shapeId = shape.getId();
			if (memory.contains(shapeId.stringValue())) {
				return;
			}
			memory.add(shapeId.stringValue());
			if (shapeId instanceof URI && !ignoreShapeId) {
				addIndividual((URI)shapeId);
			}
			addClassHierarchy(shape.getTargetClass());
			addProperties(shape);
		}
		
		private void addProperties(Shape shape) {
			List<PropertyConstraint> list = shape.getProperty();
			for (PropertyConstraint p : list) {
				addProperty(p);
			}
			
		}

		private void addProperty(PropertyConstraint p) {
			URI predicate = p.getPredicate();
			if (memory.contains(predicate.stringValue())) {
				return;
			}
			memory.add(predicate.stringValue());
			
			URI datatype = p.getDatatype();
			if (datatype != null) {
				Term datatypeTerm = addClass(datatype);
				addTerm(predicate, datatypeTerm.getKey(), Kind.PROPERTY);
				
			} else {

				Resource valueClass = p.getValueClass();
				Shape valueShape = p.getShape();
				URI directValueType = p.getDirectValueType();
				NodeKind nodeKind = p.getNodeKind();
				
				String type = null;
				if (nodeKind!=null && (nodeKind==NodeKind.IRI || nodeKind==NodeKind.BlankNode)) {
					type = "@id";
				}
				
				Term term = addTerm(predicate, type, Kind.PROPERTY);
				p.setTerm(term);

				
				
				
				
				if (directValueType != null) {
					addClass(directValueType);
				}
				if (valueClass != null) {
					addClassHierarchy(valueClass);
				}
				if (valueShape != null) {
					addShape(valueShape);
				}
				
			}
			
			// TODO: handle the following attributes of a PropertyConstraint
			//		hasValue
			
		}
		
		

		private Term addIndividual(URI uri) {
			return uri==null ? null : addTerm(uri, "@id", Kind.INDIVIDUAL);
		}
		
		private void addClassHierarchy(Resource classId) {
			if (classId instanceof URI) {

				addClass((URI) classId);
			}
			if (classId!=null && owl!=null) {
				Traversal traversal = owl.v(classId);
				traverseClassHierarchy(traversal);
			}
		}
		
		private void traverseClassHierarchy(Traversal traversal) {
			
			Traversal subclasses = traversal.in(RDFS.SUBCLASSOF);
			List<Vertex> vertexList = subclasses.toVertexList();
			if (!vertexList.isEmpty()) {
				for (Vertex v : subclasses.toVertexList()) {
					Resource id = v.getId();
					if (id instanceof URI) {
						addClass((URI)id);
					}
				}
				traverseClassHierarchy(subclasses);
			}
			
		}

		private Term addClass(URI uri) {
			return uri==null ? null : addTerm(uri, "@id", Kind.CLASS);
		}
		
		private Term addTerm(URI uri, String type,  Kind kind) {
			String key = uri.getLocalName();
			String value = uri.stringValue();
			Term term = context.getTerm(key);
			if (term == null) {
				Term ns = addNamespace(uri);
				
				if (ns != null) {
					value = ns.getKey() + ":" + key;
				}
				
				term = new Term(key, value, null, type);
				term.setKind(kind);
				term.setExpandedId(uri);
				
				context.add(term);
				
			} else {
				if (!uri.equals(term.getExpandedId())) {
					
					if (!ignoreTermNameClass) {
						throw new KonigException("Term name clash: " + uri.stringValue() + " " + term.getExpandedIdValue());
					}
					logger.warn("Ignoring term name clash: {} {}", uri.stringValue(), term.getExpandedId());
					return null;
					
				}
			}
			
			return term;
		}
		
		private Term addNamespace(URI uri) {
			String name = uri.getNamespace();
			Term term = inverse.get(name);
			if (term == null) {
				Namespace ns = nsManager.findByName(name);
				if (ns != null) {
					term = new Term(ns.getPrefix(), name, Kind.NAMESPACE);
					context.add(term);
					inverse.put(name, term);
				}
			}
			
			return term;
		}
		
		
		
	}

}
