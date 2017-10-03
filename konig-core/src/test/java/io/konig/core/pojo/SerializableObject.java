package io.konig.core.pojo;

public class SerializableObject {
	private String name;

	public SerializableObject() {
	}
	
	public void read(String data) {
		name = data;
	}

	public String getName() {
		return name;
	}
}
