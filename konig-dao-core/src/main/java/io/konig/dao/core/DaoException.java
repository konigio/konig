package io.konig.dao.core;

public class DaoException extends Exception {
	private static final long serialVersionUID = 1L;

	public DaoException(Throwable cause) {
		super(cause);
	}

	public DaoException(String msg) {
		super(msg);
	}
}
