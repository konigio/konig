package io.konig.yaml;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlDeserializer implements YamlDeserializer {

	@Override
	public Object fromString(String data) {
		try {
			return new URL(data);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

}
