package io.konig.yaml;

import java.io.PushbackReader;
import java.io.Reader;

public class YamlReader {

	private PushbackReader reader;
	public YamlReader(Reader reader) {
		this.reader = new PushbackReader(reader, 2);
	}
	
	public Object readObject() {
		
		return null;
	}

}
