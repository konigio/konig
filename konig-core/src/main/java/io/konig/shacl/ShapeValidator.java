package io.konig.shacl;

/*
 * #%L
 * Konig Core
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
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Edge;
import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.impl.RdfUtil;
import io.konig.core.path.PathImpl;

public class ShapeValidator {
	
	private boolean failFast = true;
	private boolean closed = false;
	

	public ShapeValidator() {
	}
	
	
	
	public boolean isFailFast() {
		return failFast;
	}
	
	public void setFailFast(boolean failFast) {
		this.failFast = failFast;
	}



	public boolean isClosed() {
		return closed;
	}



	public void setClosed(boolean closed) {
		this.closed = closed;
	}



	public boolean conforms(Vertex focusNode, Shape shape) {
		return validate(focusNode, shape, null);
	}

	public boolean validate(Vertex focusNode, Shape shape, ValidationReport report) {
		
		Context context = report==null ? null : new Context(report, focusNode, shape);
		return doValidate(focusNode, shape, context);
	}
	
	


	private boolean doValidate(Vertex focusNode, Shape shape, Context context) {

		boolean ok = true;
		List<PropertyConstraint> list = shape.getProperty();
		for (PropertyConstraint p : list) {
			if (!validateProperty(focusNode, shape, p, context)) {
				ok = false;
				if (failFast) {
					break;
				}
			}
		}
		
		if (closed) {
			Set<Entry<URI,Set<Edge>>> out = focusNode.outEdges();
			for (Entry<URI,Set<Edge>> e : out) {
				URI predicate = e.getKey();
				if (predicate.equals(RDF.TYPE)) {
					continue;
				}
				if (context != null) {
					context.push(predicate);
				}
				PropertyConstraint p = shape.getPropertyConstraint(predicate);
				if (p == null) {
					ok = false;
					if (context != null) {
						context.violation("Property not permitted in closed shape");
					}
					if (failFast) {
						return ok;
					}
				}
				
				if (context != null) {
					context.pop();
				}
			}
		}
		
		return ok;
	}

	private boolean validateProperty(Vertex focusNode, Shape shape, PropertyConstraint p, Context context) {
		boolean ok = true;
		
		URI predicate = p.getPredicate();
		if (predicate != null) {
			
			if (context != null) {
				context.push(predicate);
			}
			
			Set<Edge> set = focusNode.outProperty(predicate);
			Integer minCount = p.getMinCount();
			
			if (minCount!=null && set.size()<minCount) {
				ok = false;
				if (context!=null) {
					StringBuilder msg = new StringBuilder();
					msg.append("Expected at least ");
					msg.append(minCount);
					msg.append(minCount==1 ? " value but found " : " values but found ");
					msg.append(set.size());
					
					context.violation().setMessage(msg.toString());

					if (failFast) {
						return false;
					}
				}
			}
			
			Integer maxCount = p.getMaxCount();
			if (maxCount != null && set.size()>maxCount) {
				ok = false;
				if (context!=null) {
					StringBuilder msg = new StringBuilder();
					msg.append("Expected at most ");
					msg.append(maxCount);
					msg.append(maxCount==1 ? " value but found " : " values but found ");
					msg.append(set.size());
					
					context.violation().setMessage(msg.toString());

					if (failFast) {
						return false;
					}
				}
			}

			URI datatype = p.getDatatype();
			Shape childShape = p.getShape();
			NodeKind nodeKind = p.getNodeKind();
			
			for (Edge edge : set) {

				Value object = edge.getObject();
				if (datatype != null) {
					if (object instanceof Literal) {
						Literal literal = (Literal) object;
						URI type = literal.getDatatype();
						if (type == null) {
							type = XMLSchema.STRING;
						}
						if (!datatype.equals(type)) {
							ok = false;
							if (context != null) {

								StringBuilder msg = new StringBuilder();
								msg.append("Expected value of type ");
								msg.append(context.curie(datatype));
								msg.append(" but found ");
								msg.append(context.curie(type));
								
								context.violation(msg.toString());
							}
							if (failFast) {
								return ok;
							}
						}
					} else {
						ok = false;
						if (context != null) {
							StringBuilder msg = new StringBuilder();
							msg.append("Expected value of type ");
							msg.append(context.curie(datatype));
							msg.append(" but found ");
							if (object instanceof BNode) {
								msg.append(" a BNode");
							} else {
								msg.append(" <");
								msg.append(object.stringValue());
								msg.append(">");
							}
							context.violation(msg.toString());
						}
						if (failFast) {
							return ok;
						}
					}
				}
				
				if (childShape != null) {
					
					if (object instanceof Literal) {
						ok = false;
						if (context != null) {
							StringBuilder msg = new StringBuilder();
							msg.append("Value must not be a literal but found '");
							msg.append(object.stringValue());
							msg.append("'");
							context.violation(msg.toString());
						}
						if (failFast) {
							return ok;
						}
					} else {
						Vertex child = context.getVertex(object);
						if (child == null) {
							throw new KonigException("Resource not found: " + object.stringValue());
						}
						
						ok = doValidate(child, childShape, context);
						if (!ok && failFast) {
							return ok;
						}
					}
				}
				
				if (nodeKind != null) {
					switch (nodeKind) {
					case BlankNode:
						
						if (!(object instanceof BNode)) {
							ok = false;
							if (context != null) {
								StringBuilder msg = new StringBuilder();
								msg.append("Expected a BNode but found ");
								if (object instanceof URI) {
									msg.append("<");
									msg.append(object.stringValue());
									msg.append('>');
								} else {
									msg.append("'");
									msg.append(object.stringValue());
									msg.append("'");
								}
								context.violation(msg.toString());
							}
							if (failFast) {
								return ok;
							}
						}
						
						break;
						
					case BlankNodeOrIRI:
						if (object instanceof Literal) {
							ok = false;
							if (context != null) {
								StringBuilder msg = new StringBuilder();
								msg.append("Expected a BNode or IRI but found '");
								msg.append(object.stringValue());
								msg.append("'");
								context.violation(msg.toString());
							}
							if (failFast) {
								return ok;
							}
						}
						break;
						
					case IRI:
						if (!(object instanceof URI)) {

							ok = false;
							if (context != null) {
								StringBuilder msg = new StringBuilder();
								msg.append("Expected an IRI but found ");
								if (object instanceof BNode) {
									msg.append(" a BNode");
								} else {
									msg.append("'");
									msg.append(object.stringValue());
									msg.append("'");
								}
								context.violation(msg.toString());
							}
							if (failFast) {
								return ok;
							}
						}
						break;
						
					case Literal:
						if (!(object instanceof Literal)) {
							ok = false;
							if (context != null) {
								StringBuilder msg = new StringBuilder();
								msg.append("Expected a Literal but found ");
								if (object instanceof URI) {
									msg.append("<");
									msg.append(object.stringValue());
									msg.append('>');
								} else {
									msg.append("a BNode");
								}
								context.violation(msg.toString());
							}
							if (failFast) {
								return ok;
							}
						}
						break;
					
					}
				}
			}
			
			if (context!=null) {
				context.pop();
			}
		}
		
		return ok;
		
	}


	static class Context {
		PathImpl path;
		ValidationReport report;
		Vertex focusNode;
		Shape sourceShape;
		
		public Context(ValidationReport report, Vertex focusNode, Shape sourceShape) {
			this.focusNode = focusNode;
			this.sourceShape = sourceShape;
			this.report = report;
			path = new PathImpl();
			
		}
		
		public Vertex getVertex(Value object) {
			if (object instanceof Resource) {
				return focusNode.getGraph().getVertex((Resource) object);
			}
			return null;
		}

		public void push(URI predicate) {
			path.out(predicate);
		}
		
		public void pop() {
			path.remove(path.length()-1);
		}

		public Object curie(URI uri) {
			NamespaceManager nsManager = focusNode.getGraph().getNamespaceManager();
			if (nsManager == null) {
				return "<" + uri.stringValue() + ">";
			}
			return RdfUtil.optionalCurie(nsManager, uri);
		}
		
		void violation(String message) {
			violation().setMessage(message);
		}

		ValidationResult violation() {
			ValidationResult result = new ValidationResult();
			result.setFocusNode(focusNode.getId());
			result.setPath(path.copy());
			result.setSeverity(Severity.VIOLATION);
			result.setSourceShape(sourceShape.getId());
			report.addValidationResult(result);
			return result;
		}
		
		
	}
}
