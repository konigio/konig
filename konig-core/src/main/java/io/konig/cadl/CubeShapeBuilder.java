package io.konig.cadl;

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


import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.Context;
import io.konig.core.OwlReasoner;
import io.konig.core.Term;
import io.konig.core.impl.BasicContext;
import io.konig.core.impl.RdfUtil;
import io.konig.core.showl.ShowlManager;
import io.konig.core.showl.ShowlProperty;
import io.konig.core.showl.ShowlTraverser;
import io.konig.datasource.DataSource;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class CubeShapeBuilder {

	private ShapeManager shapeManager;
	private OwlReasoner reasoner;
	private String shapeNamespace;
	
	public CubeShapeBuilder(OwlReasoner reasoner, ShapeManager shapeManager, String shapeNamespace) {
		this.reasoner = reasoner;
		this.shapeManager = shapeManager;
		this.shapeNamespace = shapeNamespace;
	}

	public Shape buildShape(Cube cube) {
		Worker worker = new Worker();
		return worker.buildShape(cube);
	}
	
	private class Worker {
		
		private ShowlTraverser traverser;
		private PropertyConstraint sourceVariable;
		
		public Shape buildShape(Cube cube) {
			
			
			ShowlManager showlManager = new ShowlManager(shapeManager, reasoner);
			traverser = new ShowlTraverser(showlManager);
			
			
			URI shapeId = uri(shapeNamespace + cube.getId().getLocalName() + "RawShape");
			Shape shape = produceShape(shapeId);
			
			addVariable(shape, cube.getSource());
			
			for (Dimension dim : cube.getDimension()) {
				for (Level level : dim.getLevel()) {
					addLevel(shape, cube, dim, level);
				}
			}
			
			addDataSources(shape, cube);
			
			return shape;
		}
		
		private void addDataSources(Shape shape, Cube cube) {
			
			for (DataSource ds : cube.getStorage()) {
				shape.addShapeDataSource(ds);
			}
			
		}

		private void addVariable(Shape shape, Variable source) {
			
			PropertyConstraint p = shape.getVariableById(source.getId());
			if (p == null) {
				p = new PropertyConstraint(source.getId());
				shape.addVariable(p);
			}
			
			p.setValueClass(source.getValueType());
			
			sourceVariable = p;
			
			
		}

		private void addLevel(Shape shape, Cube cube, Dimension dim, Level level) {
			
			PropertyConstraint p = shape.getPropertyConstraint(level.getId());
			if (p == null) {
				p = new PropertyConstraint(level.getId());
				shape.add(p);
			}
			
			setLevelFormula(dim, level, p);
			
			if (!level.getAttribute().isEmpty()) {
				
				URI levelShapeId = uri(shapeNamespace + cube.getId().getLocalName() + "RawShape/level/" + level.getId().getLocalName() + "Shape");
				Shape levelShape = produceShape(levelShapeId);
				p.setShape(levelShape);
				addAttributes(levelShape, level, p.getFormula());
				
			}
			
		}


		private void addAttributes(Shape shape, Level level, QuantifiedExpression levelFormula) {
			
			for (Attribute attr : level.getAttribute()) {
				
				if (attr.getId().getLocalName().equals("id")) {
					shape.setNodeKind(NodeKind.IRI);
					continue;
				}
				
				PropertyConstraint p = shape.getPropertyConstraint(attr.getId());
				if (p == null) {
					p = new PropertyConstraint(attr.getId());
					shape.add(p);
				}
				
				QuantifiedExpression formula = attr.getFormula();
				
				if (formula == null) {
					formula = defaultAttrFormula(levelFormula, attr);
				}
				p.setFormula(formula);
				
			}
			
		}

		private QuantifiedExpression defaultAttrFormula(QuantifiedExpression levelFormula, Attribute attr) {
			
			String attributeName = attr.getId().getLocalName();
			
			Set<ShowlProperty> propertySet = traverser.traverse(sourceVariable.getPredicate(), RdfUtil.uri(sourceVariable.getValueClass()), levelFormula);
			
			propertySet = traverser.out(propertySet, attributeName);
			
			if (propertySet.size() == 1) {
				URI predicate = propertySet.iterator().next().getPredicate();
				Context context = new BasicContext("");
				copyContext(levelFormula.getContext(), context);
				context.addTerm(predicate.getLocalName(), predicate.stringValue());
				context.compile();
				StringBuilder builder = new StringBuilder();
				appendContext(builder, context);
				
				builder.append(levelFormula.toSimpleString());
				builder.append('.');
				builder.append(attributeName);
				String text = builder.toString();
				return QuantifiedExpression.fromString(text);
			}
			
			
			return null;
		}
		private Shape produceShape(URI shapeId) {
			Shape shape = shapeManager.getShapeById(shapeId);
			if (shape == null) {
				shape = new Shape(shapeId);
				shapeManager.addShape(shape);
			}
			return shape;
		}

		private void setLevelFormula(Dimension dim, Level level, PropertyConstraint p) {
			
			QuantifiedExpression levelFormula = level.getFormula();
			if (dim.getFormula() != null) {
				String levelFormulaText = levelFormula.toSimpleString();
				
				if (levelFormulaText.contains("$.") || levelFormulaText.contains("$^")) {
					
					String dimFormulaText = dim.getFormula().toSimpleString();
					levelFormulaText = levelFormulaText.replaceAll(
							Pattern.quote("$"), Matcher.quoteReplacement(dimFormulaText));
					
					Context context = new BasicContext("");
					copyContext(dim.getFormula().getContext(), context);
					copyContext(levelFormula.getContext(), context);
					
					StringBuilder builder = new StringBuilder();
					appendContext(builder, context);
					
					builder.append(levelFormulaText);
					String text = builder.toString();
					levelFormula = QuantifiedExpression.fromString(text);
				}
			}
			
			p.setFormula(levelFormula);
			
		}

		private void appendContext(StringBuilder builder, Context context) {
			context.sort();
			for (Term term : context.asList()) {
				switch (term.getKind()) {
				case NAMESPACE :
					builder.append("@prefix ");
					builder.append(term.getKey());
					builder.append(": <");
					builder.append(term.getId());
					builder.append("> .\n");
					break;
				default:
					builder.append("@term ");
					builder.append(term.getKey());
					builder.append(" ");
					if (term.getExpandedId().stringValue().equals(term.getId())) {
						builder.append('<');
						builder.append(term.getId());
						builder.append(">\n");
					} else {
						builder.append(term.getId());
						builder.append("\n");
					}
				}
			}
			builder.append("\n");
			
			
		}

		private void copyContext(Context source, Context target) {
			for (Term term : source.asList()) {
				target.add(term);
			}
			
		}

		private URI uri(String value) {
			return new URIImpl(value);
		}
	}

	

}
