package io.konig.core.showl;

import java.util.Set;

public class ArrayFilterStatement implements ShowlStatement {
	
	private ShowlPropertyShape multiValuedProperty;
	private ShowlNodeShape enumTargetNode;

	public ArrayFilterStatement(ShowlPropertyShape multiValuedProperty, ShowlNodeShape enumTargetNode) {
		this.multiValuedProperty = multiValuedProperty;
		this.enumTargetNode = enumTargetNode;
	}

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNode, Set<ShowlPropertyShape> set) {
		multiValuedProperty.getSelectedExpression().addDeclaredProperties(sourceNode, set);

	}

	@Override
	public void addProperties(Set<ShowlPropertyShape> set) {
		
		multiValuedProperty.getSelectedExpression().addProperties(set);

	}

	public ShowlPropertyShape getMultiValuedProperty() {
		return multiValuedProperty;
	}

	public ShowlNodeShape getEnumTargetNode() {
		return enumTargetNode;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ArrayFilter(");
		builder.append(multiValuedProperty.getPath());
		builder.append(')');
		return builder.toString();
	}

}
