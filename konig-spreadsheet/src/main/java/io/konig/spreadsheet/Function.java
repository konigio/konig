package io.konig.spreadsheet;

import io.konig.core.util.ValueMap;

public class Function {
	
	private String name;
	private ValueMap parameters;
	
	public Function(String name, ValueMap parameters) {
		this.name = name;
		this.parameters = parameters;
	}
	public String getName() {
		return name;
	}
	public ValueMap getParameters() {
		return parameters;
	}
	
}
