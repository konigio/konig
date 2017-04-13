package io.konig.datacatalog;

import java.util.List;

public class PathElementView extends Link {
	private String operator;
	private List<PropertyValuePair> filter;
	
	public PathElementView(List<PropertyValuePair> filter) {
		super(null, null);
		this.filter = filter;
	}
	
	public PathElementView(String operator, String name, String href) {
		super(name, href);
		this.operator = operator;
	}

	public String getOperator() {
		return operator;
	}

	public List<PropertyValuePair> getFilter() {
		return filter;
	}

}
