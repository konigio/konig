package io.konig.transform.rule;

public class Variable<T> implements TransformVariable {

	private String name;
	private T value;
	
	public Variable(String name, T value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public T getValue() {
		return value;
	}

}
