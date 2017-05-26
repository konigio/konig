package io.konig.transform.factory;

import java.util.HashSet;
import java.util.Set;

import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.PropertyConstraint;

abstract public class TargetProperty extends PropertyNode<TargetShape> {

	private Set<SourceProperty> matchSet = new HashSet<>();
	
	public TargetProperty(PropertyConstraint propertyConstraint) {
		super(propertyConstraint);
	}
	
	public void addMatch(SourceProperty node) {
		matchSet.add(node);
		node.setMatch(this);
	}
	
	public void removeMatch(SourceProperty node) {
		matchSet.remove(node);
		node.setMatch(null);
	}
	
	public Set<SourceProperty> getMatches() {
		return matchSet;
	}

	abstract public SourceProperty getPreferredMatch();

	abstract public void setPreferredMatch(SourceProperty preferredMatch);
	
	abstract public int totalPropertyCount();
	
	abstract public int mappedPropertyCount();


	@Override
	protected void printLocalFields(PrettyPrintWriter out) {
		out.field("hasPreferredMatch", getPreferredMatch()!=null);
	}

}
