package io.konig.shacl.context;

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

import io.konig.core.Context;
import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.Term;
import io.konig.core.Term.Kind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapeNotFoundException;

/**
 * A service which generates a JSON-LD context for a data shape.
 * @author Greg McFall
 *
 */
public class ContextGenerator {
	
	private ShapeManager shapeManager;
	private NamespaceManager nsManager;
	private ContextNamer namer;
	

	public ContextGenerator(ShapeManager shapeManager, NamespaceManager nsManager, ContextNamer namer) {
		this.shapeManager = shapeManager;
		this.nsManager = nsManager;
		this.namer = namer;
	}


	public Context forShape(Shape shape) throws KonigException {
		
		Worker worker = new Worker();
		Context context = worker.forShape(shape);
		
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
			context = new Context(contextId.stringValue());
			
			addShape(shape);
			
			context.sort();
			
			return context;
		}
		
		private void addShape(Shape shape) {
			Resource shapeId = shape.getId();
			if (memory.contains(shapeId.stringValue())) {
				return;
			}
			memory.add(shapeId.stringValue());
			if (shapeId instanceof URI) {
				addIndividual((URI)shapeId);
			}
			addClass(shape.getScopeClass());
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
				// For now we assume that every JSON-LD document will have a mostly
				// flat structure, and all object properties are represented as an id reference.
				// TODO: Allow a shape to declare nested structures for object properties.

				addTerm(predicate, "@id", Kind.PROPERTY);
				
				URI valueShapeId = p.getValueShapeId();
				if (valueShapeId != null) {
					Shape valueShape = p.getValueShape();
					if (valueShape == null) {
						valueShape = shapeManager.getShapeById(valueShapeId);
						if (valueShape == null) {
							throw new ShapeNotFoundException(valueShapeId.stringValue());
						}
						addShape(valueShape);
					}
				}
				
				if (p.getDirectType() != null) {
					addClass(p.getDirectType());
				}
				if (p.getType() != null) {
					addClass(p.getType());
					// TODO: add term for all the subClasses of this type also.
				}
				
			}
			
			// TODO: handle the following attributes of a PropertyConstraint
			//		hasValue
			//  	in
			
		}
		
		

		private Term addIndividual(URI uri) {
			return uri==null ? null : addTerm(uri, "@id", Kind.INDIVIDUAL);
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
					throw new KonigException("Term name clash: " + uri.stringValue() + term.getExpandedIdValue());
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
