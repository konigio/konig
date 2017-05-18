package io.konig.transform.rule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.Shape;

/**
 * A structure that describes the rules for transformation some shape (or set of shapes) to a given target shape.
 * @author Greg McFall
 */
public class ShapeRule extends AbstractPrettyPrintable {
	
	private Set<DataChannel> sourceShapes = new HashSet<>();
	private Shape targetShape;
	private IdRule idRule;
	private List<PropertyRule> propertyRules = new ArrayList<>();

	private PropertyRule accessor;
	
	public ShapeRule(Shape targetShape) {
		this.targetShape = targetShape;
	}
	
	public Set<DataChannel> getSourceShapes() {
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
		
		sourceShapes.add(rule.getDataChannel());
	}
	
	public List<PropertyRule> getPropertyRules() {
		return propertyRules;
	}
	
	public PropertyRule propertyRule(URI predicate) {
		for (PropertyRule rule : propertyRules) {
			if (rule.getPredicate().equals(predicate)) {
				return rule;
			}
		}
		return null;
	}

	public PropertyRule getAccessor() {
		return accessor;
	}

	public void setAccessor(PropertyRule accessor) {
		this.accessor = accessor;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.beginObject(this);
		out.beginObjectField("targetShape", targetShape);
		out.field("id", targetShape.getId());
		out.endObjectField(targetShape);
		if (!propertyRules.isEmpty()) {
			out.beginArray("propertyRules");
			for (PropertyRule p : propertyRules) {
				out.print(p);
			}
			
			out.endArray("propertyRules");
		}
		
	}
	
	
}
