package io.konig.core.showl;

import java.text.MessageFormat;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.Context;
import io.konig.core.KonigException;
import io.konig.core.util.IriTemplate;
import io.konig.core.util.ValueFormat.Element;
import io.konig.core.util.ValueFormat.ElementType;
import io.konig.formula.Formula;
import io.konig.formula.FormulaVisitor;
import io.konig.formula.IriTemplateExpression;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.PropertyConstraint;

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


public class ShowlDerivedPropertyExpression extends ShowlPropertyExpression {

	public ShowlDerivedPropertyExpression(ShowlDerivedPropertyShape sourceProperty) {
		super(sourceProperty);
	}

	public ShowlDerivedPropertyShape getSourceProperty() {
		return (ShowlDerivedPropertyShape) super.getSourceProperty();
	}
	

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set)
	throws ShowlProcessingException {
		ShowlDerivedPropertyShape sourceProperty = getSourceProperty();
		if (sourceProperty.getRootNode() == sourceNodeShape.getRoot()) {
			PropertyConstraint constraint = sourceProperty.getPropertyConstraint();
			
			if (constraint == null) {
				throw new ShowlProcessingException("PropertyConstraint not found at " + sourceProperty.getPath());
			}
			
			QuantifiedExpression formula = constraint.getFormula();
			
			if (formula == null) {
				super.addDeclaredProperties(sourceNodeShape, set);
				return;
			}
			
			DeclarePropertiesVisitor visitor = new DeclarePropertiesVisitor(sourceProperty.getDeclaringShape(), set);
			
			try {
				formula.dispatch(visitor);
			} catch (Throwable oops) {
				String msg = MessageFormat.format(
						"Failed to get declared properties from {1} with formula {2}", sourceNodeShape.getPath(), formula.toSimpleString());
				throw new ShowlProcessingException(msg, oops);
			}
		}
		
		
	}
	
	
	private static class DeclarePropertiesVisitor implements FormulaVisitor {

		private ShowlNodeShape declaringNode;
		private Set<ShowlPropertyShape> sink;
		
		

		public DeclarePropertiesVisitor(ShowlNodeShape declaringNode, Set<ShowlPropertyShape> sink) {
			this.declaringNode = declaringNode;
			this.sink = sink;
		}

		@Override
		public void enter(Formula formula) {
			
			if (formula instanceof IriTemplateExpression) {
				IriTemplate template = ((IriTemplateExpression) formula).getTemplate();
				Context context = template.getContext();
				context.compile();
				for (Element e : template.toList()) {
					if (e.getType() == ElementType.VARIABLE) {
						
						
						URI predicate = new URIImpl(context.expandIRI(e.getText()));
						ShowlPropertyShape p = declaringNode.findOut(predicate);
						if (p == null) {
							throw new KonigException("Property not found: " + predicate.stringValue());
						}
						sink.add(p);
						
					}
				}
			}
			
		}

		@Override
		public void exit(Formula formula) {
			// do nothing
			
		}
		
	}
	
}
