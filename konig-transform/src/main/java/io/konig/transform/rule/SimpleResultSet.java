package io.konig.transform.rule;

import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.Value;

public class SimpleResultSet implements ResultSet {
	
	private DataChannel channel;
	private URI[] propertyNames;
	private List<Value[]> valueList;

	public SimpleResultSet(DataChannel channel, URI[] propertyNames, List<Value[]> valueList) {
		this.propertyNames = propertyNames;
		this.valueList = valueList;
		this.channel = channel;
	}

	public URI[] getPropertyNames() {
		return propertyNames;
	}


	public List<Value[]> getValueList() {
		return valueList;
	}

	@Override
	public DataChannel getChannel() {
		return channel;
	}



	
}
