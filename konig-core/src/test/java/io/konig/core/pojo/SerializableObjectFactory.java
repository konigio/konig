package io.konig.core.pojo;

public class SerializableObjectFactory {

	public SerializableObjectFactory() {
		
	}
	
	public SerializableObject createSerializableObject(String data) {
		SerializableObject object = new SerializableObject();
		object.read(data);
		return object;
	}

}
