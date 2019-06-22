package io.konig.core.showl;

import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.OwlReasoner;

/**
 * An expression which asserts that a mapping has been delegated to another expression.
 * This concept was introduced originally to handle mappings of repeated fields and so the delegate
 * is typically a ShowlArrayExpression.  However, the ShowlDelegationExpression may find other uses in the future.
 * @author Greg McFall
 *
 */
public class ShowlDelegationExpression implements ShowlExpression {
	

	private static final ShowlDelegationExpression instance = new ShowlDelegationExpression();
	
	public static ShowlDelegationExpression getInstance() {
		return instance;
	}
	
	private ShowlDelegationExpression() {
		
	}
	
	@Override
	public String displayValue() {
		
		return "DelegationExpression";
	}

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set)
			throws ShowlProcessingException {
		// Do nothing.  Mappings will be handled by the delegate.

	}

	@Override
	public void addProperties(Set<ShowlPropertyShape> set) {
		// Do nothing.  Mappings will be handled by the delegate.

	}

	@Override
	public URI valueType(OwlReasoner reasoner) {
		return null;
	}

}
