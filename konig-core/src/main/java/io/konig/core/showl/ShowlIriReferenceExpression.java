package io.konig.core.showl;

import java.util.Set;

import org.openrdf.model.URI;

public class ShowlIriReferenceExpression implements ShowlExpression {

	private URI iriValue;
	private ShowlPropertyShape referencedBy;

	

	public ShowlIriReferenceExpression(URI iriValue, ShowlPropertyShape referencedBy) {
		this.iriValue = iriValue;
		this.referencedBy = referencedBy;
	}

	public URI getIriValue() {
		return iriValue;
	}

	/**
	 * The PropertyShape that contains the IRI reference.
	 */
	public ShowlPropertyShape getReferencedBy() {
		return referencedBy;
	}

	@Override
	public ShowlNodeShape rootNode() {
		return referencedBy.getRootNode();
	}

	@Override
	public String displayValue() {
		return "<" + iriValue.stringValue() + ">";
	}

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set) {
		
		// Do nothing

	}

}
