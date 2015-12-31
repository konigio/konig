package io.konig.core;

public enum Direction {

	IN("^"),
	OUT("!");
	
	private String n3Label;
	
	private Direction(String label) {
		n3Label = label;
	}
	
	public String n3Label() {
		return n3Label;
	}
}
