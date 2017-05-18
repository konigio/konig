package io.konig.transform.rule;

import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;

public abstract class AbstractPropertyRule extends AbstractPrettyPrintable implements PropertyRule {
	
	protected ShapeRule container;
	protected DataChannel channel;
	protected ShapeRule nestedRule;

	public AbstractPropertyRule(DataChannel channel) {
		this.channel = channel;
	}
	
	@Override
	public ShapeRule getContainer() {
		return container;
	}
	
	@Override
	public void setContainer(ShapeRule container) {
		this.container = container;
	}

	@Override
	public DataChannel getDataChannel() {
		return channel;
	}

	@Override
	public ShapeRule getNestedRule() {
		return nestedRule;
	}

	public void setNestedRule(ShapeRule nestedRule) {
		nestedRule.setAccessor(this);
		this.nestedRule = nestedRule;
	}


	@Override
	public void print(PrettyPrintWriter out) {
		out.beginObject(this);
		out.field("predicate", getPredicate());
		if (channel != null) {
			out.beginObjectField("sourceShape", channel);
			out.field("name", channel.getName());
			out.field("value", channel.getShape().getId());
			out.endObjectField(channel);
		}
		out.field("nestedRule", nestedRule);
		printLocalFields(out);
		out.endObject();
		
	}

	abstract protected void printLocalFields(PrettyPrintWriter out);


}
