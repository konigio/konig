package io.konig.core.showl;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;

import io.konig.core.impl.RdfUtil;
import io.konig.formula.DirectionStep;
import io.konig.formula.PathExpression;
import io.konig.formula.PathStep;
import io.konig.formula.PrimaryExpression;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.PropertyConstraint;

public class ShowlPropertyShape implements Traversable {
	private ShowlNodeShape declaringShape;
	private ShowlProperty property;
	private PropertyConstraint propertyConstraint;
	private ShowlNodeShape valueShape;
	private ShowlPropertyGroup group;
	
	
	public ShowlPropertyShape(ShowlNodeShape declaringShape, ShowlProperty property, PropertyConstraint propertyConstraint) {
		this.declaringShape = declaringShape;
		this.property = property;
		this.propertyConstraint = propertyConstraint;
		property.addPropertyShape(this);
	}
	
	public ShowlDerivedPropertyShape asDerivedPropertyShape() {
		return null;
	}
	
	public ShowlNodeShape getDeclaringShape() {
		return declaringShape;
	}
	
	public ShowlProperty getProperty() {
		return property;
	}
	
	public URI getPredicate() {
		return property.getPredicate();
	}
	public PropertyConstraint getPropertyConstraint() {
		return propertyConstraint;
	}
	
	public ShowlPropertyGroup getGroup() {
		return group;
	}
	
	public boolean isNestedAccordingToFormula() {
		if (propertyConstraint != null) {
			QuantifiedExpression formula = propertyConstraint.getFormula();
			if (formula != null) {
				PrimaryExpression primary = formula.asPrimaryExpression();
				if (primary instanceof PathExpression) {
					PathExpression path = (PathExpression) primary;
					int count = 0;
					for (PathStep step : path.getStepList()) {
						if (step instanceof DirectionStep) {
							if (++count==2) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	public String getPath() {
		
		List<String> elements = new ArrayList<>();
		ShowlNodeShape node = null;
		for (ShowlPropertyShape p=this; p!=null; p=node.getAccessor()) {
			elements.add(p.getPredicate().getLocalName());
			node = p.getDeclaringShape();
		}
		StringBuilder builder = new StringBuilder();
		builder.append('{');
		builder.append(RdfUtil.localName(node.getShape().getId()));
		builder.append('}');
		for (int i=elements.size()-1; i>=0; i--) {
			builder.append('.');
			builder.append(elements.get(i));
		}
		
		return builder.toString();
	}
	

	public String toString() {
		return getPath();
	}

	public ShowlNodeShape getValueShape() {
		return valueShape;
	}

	public void setValueShape(ShowlNodeShape valueShape) {
		this.valueShape = valueShape;
	}

	/**
	 * Should be used only by ShowlPropertyGroup
	 * @param group
	 */
	void group(ShowlPropertyGroup group) {
		this.group = group;
		
	}

	

}
