package io.konig.transform.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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
	
	private LinkedList<DataChannel> channels = new LinkedList<>();
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
	
	public void addChannel(DataChannel channel) {
		channels.addFirst(channel);
		channel.setParent(this);
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
		if (!channels.isEmpty()) {
			out.beginArray("channels");
			for (DataChannel dc : channels) {
				out.print(dc);
			}
			out.endArray("channels");
		}
		if (!properties.isEmpty()) {
			out.beginArray("propertyRules");
			List<PropertyRule> list = new ArrayList<>(getPropertyRules());
			Collections.sort(list);
			
			for (PropertyRule p : list) {
				out.print(p);
			}
			out.endArray("propertyRules");
		}
		
	}

	public PropertyRule getProperty(URI predicate) {
		return properties.get(predicate);
	}

	public List<DataChannel> getAllChannels() {
		Set<ShapeRule> ruleSet = new HashSet<>();
		addAll(ruleSet, this);
		
		Set<DataChannel> set = new HashSet<>();
		for (ShapeRule s : ruleSet) {
			set.addAll(s.getChannels());
		}
		List<DataChannel> list = new ArrayList<>(set);
		
		Collections.sort(list);
		
		return list;
	}

	private void addAll(Set<ShapeRule> ruleSet, ShapeRule shapeRule) {
		
		if (!ruleSet.contains(shapeRule)) {
			ruleSet.add(shapeRule);
			for (PropertyRule p : shapeRule.getPropertyRules()) {
				ShapeRule nested = p.getNestedRule();
				if (nested != null) {
					addAll(ruleSet, nested);
				}
			}
		}
		
	}
	
	
}
