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



import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;

import io.konig.core.Context;
import io.konig.core.NameMap;
import io.konig.core.NamespaceManager;
import io.konig.core.Term;
import io.konig.core.Term.Kind;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.SH;
import io.konig.formula.CurieValue;
import io.konig.formula.Expression;
import io.konig.formula.Formula;
import io.konig.formula.FormulaVisitor;
import io.konig.formula.LocalNameTerm;

public class FormulaContextBuilder implements ShapeVisitor {
	
	private NamespaceManager nsManager;
	private NameMap map;
	private Map<String, Warning> warnings = new HashMap<>();
	private Worker worker = new Worker();
	
	public FormulaContextBuilder(NamespaceManager nsManager, NameMap map) {
		this.nsManager = nsManager;
		this.map = map;
	}
	

	@Override
	public void visit(Shape shape) {
		worker.setShape(shape);
		
		handleIriFormula(shape);
		visit(shape, shape.getProperty());
		visit(shape, shape.getDerivedProperty());

	}
	
	private void handleIriFormula(Shape shape) {
		
		Expression formula = shape.getIriFormula();
		if (formula != null) {
			worker.setProperty(null);
			formula.dispatch(worker);
		}
	}

	private void visit(Shape shape, List<PropertyConstraint> list) {
		if (list != null) {
			for (PropertyConstraint p : list) {
				Expression formula = p.getFormula();
				if (formula != null) {
					worker.setProperty(p);
					formula.dispatch(worker);
				}
				
			}
		}
		
	}

	public interface Warning {
	}
	
	static public class NameNotDefinedWarning implements Warning {
		private String name;
		private Shape shape;
		private PropertyConstraint property;
		public NameNotDefinedWarning(String name, Shape shape, PropertyConstraint property) {
			this.name = name;
			this.shape = shape;
			this.property = property;
		}
		public String getName() {
			return name;
		}
		public Shape getShape() {
			return shape;
		}
		public PropertyConstraint getProperty() {
			return property;
		}
		
	}
	
	static public class NamespaceNotFoundWarning implements Warning {
		private String namespacePrefix;
		private Shape shape;
		private PropertyConstraint property;
		public NamespaceNotFoundWarning(String prefix, Shape shape, PropertyConstraint property) {
			this.namespacePrefix = prefix;
			this.shape = shape;
			this.property = property;
		}
		public String getNamespacePrefix() {
			return namespacePrefix;
		}
		public Shape getShape() {
			return shape;
		}
		public PropertyConstraint getProperty() {
			return property;
		}
		
	}
	
	class Worker implements FormulaVisitor {
		
		
		private Shape shape;
		private PropertyConstraint property;
		
		void setShape(Shape shape) {
			this.shape = shape;
		}

		void setProperty(PropertyConstraint property) {
			this.property = property;
		}

		void namespaceNotFound(String prefix) {
			warnings.put(prefix, new NamespaceNotFoundWarning(prefix, shape, property));
		}

		@Override
		public void enter(Formula formula) {
		}

		@Override
		public void exit(Formula formula) {
			if (formula instanceof LocalNameTerm) {
				visitLocalNameTerm((LocalNameTerm) formula);
			} else if (formula instanceof CurieValue) {
				visitCurieTerm((CurieValue) formula);
			}
			
		}

		private void visitCurieTerm(CurieValue curie) {

			Context context = curie.getContext();
			String prefix = curie.getNamespacePrefix();
			
			Term term = context.getTerm(prefix);
			if (term == null) {
				Namespace ns = nsManager.findByPrefix(prefix);
				if (ns == null) {
					namespaceNotFound(prefix);
				} else {
					term = new Term(prefix, ns.getName(), Kind.NAMESPACE);
					context.add(term);
				}
			}
		}

		private void visitLocalNameTerm(LocalNameTerm formula) {
			
			String name = formula.getLocalName();
			URI uri = map.get(name);
			if (uri == null) {
				warnings.put(name, new NameNotDefinedWarning(name, shape, property));
			} else {
				Context context = formula.getContext();
				Term term = context.getTerm(name);
				if (term == null) {
					
					Namespace ns = nsManager.findByName(uri.getNamespace());
					if (ns != null) {
						
						Term nsTerm = context.getTerm(ns.getPrefix());
						if (nsTerm == null) {
							context.add(nsTerm=new Term(ns.getPrefix(), ns.getName(), Kind.NAMESPACE));
						}
						
						if (nsTerm.getId().equals(ns.getName())) {
							StringBuilder value = new StringBuilder();
							value.append(ns.getPrefix());
							value.append(':');
							value.append(name);
							
							context.addTerm(name, value.toString());
							return;
						}
					}
					context.addTerm(name, uri.stringValue());
					
				}
			}
			
		}
		
	}

}
