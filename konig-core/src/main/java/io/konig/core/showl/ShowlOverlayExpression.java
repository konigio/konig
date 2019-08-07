package io.konig.core.showl;

import java.util.ArrayList;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.OwlReasoner;

/**
 * An expression that has multiple alternatives based on different sources that must
 * "overlay" on top of each other.
 * @author Greg McFall
 *
 */
@SuppressWarnings("serial")
public class ShowlOverlayExpression extends ArrayList<ShowlExpression> implements ShowlExpression {

	public ShowlOverlayExpression() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public ShowlExpression transform() {
		
		ShowlOverlayExpression copy = new ShowlOverlayExpression();
		for (ShowlExpression e : this) {
			copy.add(e.transform());
		}
		return copy;
	}
	
	@Override
	public boolean add(ShowlExpression e) {
		if (e instanceof ShowlDelegationExpression) {
			return false;
		}
		return super.add(e);
	}

	@Override
	public String displayValue() {
		StringBuilder builder = new StringBuilder();
		builder.append("Overlay(");
		String comma = "";
		for (ShowlExpression e : this) {
			builder.append(comma);
			comma = ", ";
			builder.append(e.displayValue());
		}
		
		builder.append(")");
		return builder.toString();
	}

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set)
			throws ShowlProcessingException {
		for (ShowlExpression e : this) {
			e.addDeclaredProperties(sourceNodeShape, set);
		}

	}

	@Override
	public void addProperties(Set<ShowlPropertyShape> set) {
		
		for (ShowlExpression e : this) {
			e.addProperties(set);
		}

	}

	@Override
	public URI valueType(OwlReasoner reasoner) {
		// We assume that all expressions have the same value type
		// Is this a safe assumption?
		return get(0).valueType(reasoner);
	}

}
