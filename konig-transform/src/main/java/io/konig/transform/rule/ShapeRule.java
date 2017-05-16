package io.konig.transform.rule;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;

import io.konig.shacl.Shape;

/**
 * A structure that describes the rules for transformation some shape (or set of shapes) to a given target shape.
 * @author Greg McFall
 */
public class ShapeRule {
	
	private List<Variable<Shape>> sourceShapes = new ArrayList<>();
	private Shape targetShape;
	private IdRule idRule;
	private List<PropertyRule> propertyRules = new ArrayList<>();
	
	public ShapeRule(Shape targetShape) {
		this.targetShape = targetShape;
	}
	
	public List<Variable<Shape>> getSourceShapes() {
		return sourceShapes;
	}
	
	public Shape getTargetShape() {
		return targetShape;
	}
	
	public IdRule getIdRule() {
		return idRule;
	}
	
	public void setIdRule(IdRule idRule) {
		this.idRule = idRule;
	}
	
	public void addPropertyRule(PropertyRule rule) {
		propertyRules.add(rule);
		rule.setContainer(this);
	}
	
	public List<PropertyRule> getPropertyRules() {
		return propertyRules;
	}
	
	public PropertyRule propertyRule(URI predicate) {
		for (PropertyRule rule : propertyRules) {
			if (rule.getFocusPredicate().equals(predicate)) {
				return rule;
			}
		}
		return null;
	}
}
