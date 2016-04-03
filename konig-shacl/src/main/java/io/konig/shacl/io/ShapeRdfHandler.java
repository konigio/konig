package io.konig.shacl.io;

import java.util.ArrayList;

/*
 * #%L
 * konig-shacl
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;

import io.konig.core.ListHandler;
import io.konig.core.vocab.SH;
import io.konig.shacl.AndConstraint;
import io.konig.shacl.Constraint;
import io.konig.shacl.NodeKind;
import io.konig.shacl.OrConstraint;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeConsumer;
import io.konig.shacl.ShapeManager;

public class ShapeRdfHandler extends RDFHandlerBase implements ListHandler{
	
	private ShapeManager shapeManager;
	
	private Map<String,PropertyConstraint> propertyConstraint = new HashMap<>();
	private Map<String, Shape> shapeMap = new HashMap<>();
	
	private List<Statement> constraintList = new ArrayList<>();
	private Map<String,ConstraintList> constraintMap = new HashMap<>();
	

	public ShapeRdfHandler(ShapeManager shapeManager) {
		this.shapeManager = shapeManager;
	}



	public void handleStatement(Statement st) throws RDFHandlerException {
		Resource subject = st.getSubject();
		URI predicate = st.getPredicate();
		Value object = st.getObject();
		
		
		if (predicate.equals(SH.property)) {
			shape(subject).add(property(object));
		} else if (predicate.equals(SH.scopeClass)) {
			shape(subject).setScopeClass((URI)object);
		} else if (predicate.equals(SH.predicate)) {
			property(subject).setPredicate((URI)object);
		} else if (predicate.equals(SH.datatype)) {
			property(subject).setDatatype((URI)object);
		} else if (predicate.equals(SH.directType)) {
			property(subject).setDirectValueType((URI)object);
		} else if (predicate.equals(SH.hasValue)) {
			property(subject).addHasValue(object);
		} else if (predicate.equals(SH.minCount)) {
			property(subject).setMinCount(integer(object));
		} else if (predicate.equals(SH.maxCount)) {
			property(subject).setMaxCount(integer(object));
		} else if (predicate.equals(SH.minExclusive)) {
			property(subject).setMinExclusive(real(object));
		} else if (predicate.equals(SH.maxExclusive)) {
			property(subject).setMaxExclusive(real(object));
		} else if (predicate.equals(SH.minInclusive)) {
			property(subject).setMinInclusive(real(object));
		} else if (predicate.equals(SH.maxInclusive)) {
			property(subject).setMaxInclusive(real(object));
		} else if (predicate.equals(SH.minLength)) {
			property(subject).setMinLength(integer(object));
		} else if (predicate.equals(SH.maxLength)) {
			property(subject).setMaxLength(integer(object));
		} else if (predicate.equals(SH.nodeKind)) {
			property(subject).setNodeKind(nodeKind(object));
		} else if (predicate.equals(SH.pattern)) {
			property(subject).setPattern(object.stringValue());
		} else if (predicate.equals(SH.valueClass)) {
			property(subject).setValueClass((Resource)object);
		} else if (predicate.equals(SH.valueShape)) {
			property(subject).setValueShape(shape((Resource)object));
		} else if (predicate.equals(SH.constraint)) {
			constraintList.add(st);
		}
		
	}



	public void endRDF() throws RDFHandlerException	{
		for (Statement s : constraintList) {
			Resource subject = s.getSubject();
			Value object = s.getObject();
			
			Shape targetShape = shape(subject);
		
			ConstraintList clist = constraintMap.get(object.stringValue());
			if (clist != null) {
				URI predicate = clist.predicate;
				
				Constraint c = predicate.equals(SH.or) ? new OrConstraint() : new AndConstraint();
				targetShape.setConstraint(c);
				
				ShapeConsumer consumer = (ShapeConsumer) c;
				
				for (Value value : clist.list) {
					if (value instanceof Resource) {
						Resource shapeId = (Resource) value;
						Shape child = shape(shapeId);
						consumer.add(child);
					}
				}
				
			}
			
		}
	}





	private NodeKind nodeKind(Value object) throws RDFHandlerException {
		
		if (SH.IRI.equals(object)) {
			return NodeKind.IRI;
		}
		if (SH.BlankNode.equals(object)) {
			return NodeKind.BlankNode;
		}
		if (SH.Literal.equals(object)) {
			return NodeKind.Literal;
		}
		throw new RDFHandlerException("Invalid nodeKind: " + object.stringValue());
	}



	private Double real(Value object) {
		Literal literal = (Literal) object;
		return literal.doubleValue();
	}



	private Integer integer(Value object) {
		Literal literal = (Literal)object;
		return literal.intValue();
	}



	private PropertyConstraint property(Value id) {
		PropertyConstraint property = propertyConstraint.get(id.stringValue());
		if (property == null) {
			property = new PropertyConstraint((Resource)id, null);
			propertyConstraint.put(id.stringValue(), property);
		}
		return property;
	}

	private Shape shape(Resource subject) {
		Shape shape = shapeMap.get(subject.stringValue());
		if (shape == null) {
			if (subject instanceof URI) {
				shape = shapeManager.getShapeById((URI) subject);
				if (shape == null) {
					shape = new Shape(subject);
					shapeManager.addShape(shape);
				}
			} else {
				shape = new Shape(subject);
			}
			shapeMap.put(subject.stringValue(), shape);
		}
		return shape;
	}



	@Override
	public void handleList(Resource subject, URI predicate, List<Value> list) throws RDFHandlerException {
		
		if (predicate.equals(SH.in)) {
			PropertyConstraint property = property(subject);
			for (Value value : list) {
				property.addAllowedValue(value);
			}
		}
		if (predicate.equals(SH.or) || predicate.equals(SH.and)) {
			constraintMap.put(subject.stringValue(), new ConstraintList(predicate, list));
		}
		
	}


	private static class ConstraintList {
		private URI predicate;
		private List<Value> list;
		public ConstraintList(URI predicate, List<Value> list) {
			this.predicate = predicate;
			this.list = list;
		}
		
		
	}

	
}
