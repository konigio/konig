package io.konig.transform.factory;

public class SharedSourceProperty {
	SourceProperty value;
	
	public void set(SourceProperty value) {
		this.value = value;
	}
	
	public SourceProperty get() {
		return value;
	}

}
