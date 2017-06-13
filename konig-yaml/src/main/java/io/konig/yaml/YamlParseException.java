package io.konig.yaml;

public class YamlParseException extends Exception {
	private static final long serialVersionUID = 1L;

	public YamlParseException(String message) {
		super(message);
	}

	public YamlParseException(Throwable cause) {
		super(cause);
	}

	public YamlParseException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
