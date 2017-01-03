package io.konig.sql;

public class SQLSchemaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SQLSchemaException() {
	}

	public SQLSchemaException(String arg0) {
		super(arg0);
	}

	public SQLSchemaException(Throwable arg0) {
		super(arg0);
	}

	public SQLSchemaException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
