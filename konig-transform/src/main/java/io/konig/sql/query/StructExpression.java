package io.konig.sql.query;

public class StructExpression extends BaseValueContainer
implements ItemExpression, ValueContainer {
	
	
	@Override
	public void append(StringBuilder builder) {
		
		builder.append("STRUCT(");
		String comma = "";
		for (QueryExpression field : getValues()) {
			builder.append(comma);
			field.append(builder);
			comma = ", ";
		}
		builder.append(')');
	}

}
