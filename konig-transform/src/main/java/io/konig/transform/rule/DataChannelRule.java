package io.konig.transform.rule;

import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;

public class DataChannelRule extends AbstractPrettyPrintable implements FromItem {

	private DataChannel channel;
	private BooleanExpression joinCondition;
	
	public DataChannelRule(DataChannel channel, BooleanExpression joinCondition) {
		this.channel = channel;
		this.joinCondition = joinCondition;
	}

	public DataChannelRule(DataChannel channel) {
		this.channel = channel;
	}

	public DataChannel getChannel() {
		return channel;
	}

	public BooleanExpression getJoinCondition() {
		return joinCondition;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.beginObject(this);
		out.field("channel", channel);
		out.field("joinCondition", joinCondition);
		out.endObject();
		
	}

	@Override
	public DataChannel primaryChannel() {
		return channel;
	}
	
}
