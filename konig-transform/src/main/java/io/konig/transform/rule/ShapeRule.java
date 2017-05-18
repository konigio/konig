package io.konig.transform.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
	
	private List<DataChannel> channels = new ArrayList<>();
	private Shape targetShape;
	private IdRule idRule;
	private Map<URI,PropertyRule> properties = new HashMap<>();

	private PropertyRule accessor;
	
	public ShapeRule(Shape targetShape) {
		this.targetShape = targetShape;
	}
	
	public List<DataChannel> getChannels() {
		return channels;
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
		properties.put(rule.getPredicate(), rule);
		rule.setContainer(this);
	}
	
	public Collection<PropertyRule> getPropertyRules() {
		return properties.values();
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
		if (!properties.isEmpty()) {
			out.beginArray("propertyRules");
			for (PropertyRule p : getPropertyRules()) {
				out.print(p);
			}
			
			out.endArray("propertyRules");
		}
		
	}

	public PropertyRule getProperty(URI predicate) {
		return properties.get(predicate);
	}
	
	
}
