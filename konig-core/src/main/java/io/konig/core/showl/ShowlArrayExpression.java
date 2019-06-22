package io.konig.core.showl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.OwlReasoner;

public class ShowlArrayExpression implements ShowlExpression {
	
	List<ShowlExpression> memberList = new ArrayList<>();

	public ShowlArrayExpression() {
	}
	
	public void addMember(ShowlExpression member) {
		memberList.add(member);
	}

	@Override
	public String displayValue() {
		StringBuilder builder = new StringBuilder();
		builder.append("Array[");
		String comma = "";
		for (ShowlExpression e : memberList) {
			builder.append(comma);
			comma = ", ";
			builder.append(e.displayValue());
		}
		
		builder.append(']');
		return builder.toString();
	}
	
	@Override
	public String toString() {
		return displayValue();
	}

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set)
			throws ShowlProcessingException {
		for (ShowlExpression e : memberList) {
			e.addDeclaredProperties(sourceNodeShape, set);
		}
		
	}

	public List<ShowlExpression> getMemberList() {
		return memberList;
	}

	@Override
	public void addProperties(Set<ShowlPropertyShape> set) {

		for (ShowlExpression e : memberList) {
			e.addProperties(set);
		}
		
	}

	@Override
	public URI valueType(OwlReasoner reasoner) {
		// For now, we assume that all members of the list return the same type.
		// We may need to do something more sophisticated.
		
		return memberList.get(0).valueType(reasoner);
	}

}
