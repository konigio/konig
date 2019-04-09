package io.konig.cadl;

import java.text.MessageFormat;
import java.util.HashSet;

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
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Context;
import io.konig.core.OwlReasoner;
import io.konig.core.Term;
import io.konig.core.impl.BasicContext;
import io.konig.core.impl.RdfUtil;
import io.konig.core.showl.ShowlClass;
import io.konig.core.showl.ShowlManager;
import io.konig.core.showl.ShowlProperty;
import io.konig.core.showl.ShowlTraverser;
import io.konig.datasource.DataSource;
import io.konig.formula.FunctionExpression;
import io.konig.formula.PrimaryExpression;
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

	public Shape buildShape(Cube cube) throws CubeShapeException {
		Worker worker = new Worker();
		return worker.buildShape(cube);
	}
	
	private class Worker {
		
		private ShowlTraverser traverser;
		private PropertyConstraint sourceVariable;
		
		public Shape buildShape(Cube cube) throws CubeShapeException {
			
			
			ShowlManager showlManager = new ShowlManager(shapeManager, reasoner);
			traverser = new ShowlTraverser(showlManager);
			
			
			URI shapeId = uri(shapeNamespace + cube.getId().getLocalName() + "RawShape");
			Shape shape = produceShape(shapeId);
			shape.setNodeShapeCube(cube);
			
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

		private void addLevel(Shape shape, Cube cube, Dimension dim, Level level) throws CubeShapeException {
			
			PropertyConstraint p = shape.getPropertyConstraint(level.getId());
			if (p == null) {
				p = new PropertyConstraint(level.getId());
				shape.add(p);
			}
			p.setMinCount(0);
			p.setMaxCount(1);
			
			setLevelFormula(dim, level, p);
			
			if (!level.getAttribute().isEmpty()) {
				
				URI levelShapeId = uri(shapeNamespace + cube.getId().getLocalName() + "RawShape/level/" + level.getId().getLocalName() + "Shape");
				Shape levelShape = produceShape(levelShapeId);
				p.setShape(levelShape);
				addAttributes(levelShape, level, p.getFormula());
				
			} else {
				setDatatype(p, p.getFormula(), level.getId());
			}
			
		}


		private void addAttributes(Shape shape, Level level, QuantifiedExpression levelFormula) throws CubeShapeException {
			
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
					formula = defaultAttrFormula(levelFormula, attr, p);
				}
				p.setFormula(formula);
				p.setMinCount(0);
				p.setMaxCount(1);
				
				if (p.getDatatype() == null) {
					setDatatype(p, formula, attr.getId());
				}
				
			}
			
		}

		private void setDatatype(PropertyConstraint p, QuantifiedExpression formula, URI elementId) throws CubeShapeException {
			
			if (formula == null) {
				throw new CubeShapeException(
						MessageFormat.format("Formula must be defined for <{0}>", elementId.stringValue())
				);
			}
			
			PrimaryExpression primary = formula.asPrimaryExpression();
			if (primary instanceof FunctionExpression) {
				FunctionExpression func = (FunctionExpression) primary;
				URI datatype = func.getModel().getReturnType().getRdfType();
				p.setDatatype(datatype);
				return;
				
			}
			
			Set<ShowlProperty> propertySet =
					traverser.traverse(sourceVariable.getPredicate(), RdfUtil.uri(sourceVariable.getValueClass()), formula);
			
			if (propertySet.size()==1) {
				setDatatype(propertySet.iterator().next(), p, elementId, formula);
			} else if (propertySet.isEmpty()) {
				throw new CubeShapeException(
					MessageFormat.format("Datatype not found for <{0}> mapped by {1}", elementId.stringValue(), formula.toSimpleString())
				);
			} else {
				// propertySet contains multiple properties
				
				Set<URI> datatypeSet = new HashSet<>();
				for (ShowlProperty property : propertySet) {
					datatypeSet.addAll(property.rangeIncludes(reasoner));
				}
				
				if (datatypeSet.size() == 1) {
					URI datatype = datatypeSet.iterator().next();
					if (reasoner.isDatatype(datatype)) {
						p.setDatatype(datatype);
					} else {

						throw new CubeShapeException(
								MessageFormat.format(
									"<{0}> must have a Datatype value, but the formula {1} implies <{2}>", 
									elementId.stringValue(), 
									formula.toSimpleString(), 
									datatype.stringValue())
						);
					}
				} else if (datatypeSet.isEmpty()) {

					throw new CubeShapeException(
							MessageFormat.format(
									"Datatype for <{0}> is not known.  The datatype must be specified explicitly.", 
									elementId.stringValue()));
				} else {

					StringBuilder msg = new StringBuilder();
					msg.append("Datatype is ambiguous for <");
					msg.append(elementId.stringValue());
					msg.append(">.  The datatype must be specified explicitly.");
					throw new CubeShapeException(msg.toString());
				}
			}
			
			
		}

		private QuantifiedExpression defaultAttrFormula(QuantifiedExpression levelFormula, Attribute attr, PropertyConstraint p) throws CubeShapeException {
			
			String attributeName = attr.getId().getLocalName();
			
			Set<ShowlProperty> propertySet = traverser.traverse(sourceVariable.getPredicate(), RdfUtil.uri(sourceVariable.getValueClass()), levelFormula);
			
			propertySet = traverser.out(propertySet, attributeName);
			
			if (propertySet.size() == 1) {
				ShowlProperty property = propertySet.iterator().next();
				
				
				URI predicate = property.getPredicate();
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
				
				QuantifiedExpression formula = QuantifiedExpression.fromString(text);

				setDatatype(property, p, attr.getId(), formula);
				
				return formula;
			} else if (propertySet.isEmpty()) {
				throw new CubeShapeException(
					MessageFormat.format("Default mapping for <{0}> not found.", attr.getId().stringValue()));
			}
			
			StringBuilder msg = new StringBuilder();
			msg.append("Default mapping for <");
			msg.append(attr.getId().stringValue());
			msg.append("> is ambiguous.  Possible properties include:\n");
			for (ShowlProperty q : propertySet) {
				msg.append("   <");
				msg.append(q.getPredicate().stringValue());
				msg.append(">\n");
			}
			throw new CubeShapeException(msg.toString());
		}
		
		private void setDatatype(ShowlProperty property, PropertyConstraint p, URI element, QuantifiedExpression formula) throws CubeShapeException {
			
			ShowlClass range = property.getRange();
			URI datatype = null;
			if (range == null) {
				Set<URI> set = property.rangeIncludes(reasoner);
				if (set.size()==1) {
					datatype = set.iterator().next();
				} else if (set.size()>1) {
					StringBuilder msg = new StringBuilder();
					msg.append("Datatype is ambiguous for <");
					msg.append(element.stringValue());
					msg.append(">.  The datatype must be specified explicitly.");
					throw new CubeShapeException(msg.toString());
				}
			} else {
				datatype = range.getId();
			}
			
			if (datatype == null) {
				throw new CubeShapeException(
						MessageFormat.format(
								"Datatype for <{0}> is not known.  The datatype must be specified explicitly.", 
								element.stringValue()));
			}
			
			if (!reasoner.isDatatype(datatype)) {
				throw new CubeShapeException(
						MessageFormat.format(
							"<{0}> must have a Datatype value, but the formula {1} implies <{2}>", 
							element.stringValue(), 
							formula.toSimpleString(), 
							datatype.stringValue())
				);
			}
			
			p.setDatatype(datatype);
			
			
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
