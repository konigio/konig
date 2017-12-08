package io.konig.estimator;

public class SizeEstimateException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public SizeEstimateException(String msg) {
		super(msg);
	}

	public SizeEstimateException(Throwable cause) {
		super(cause);
	}
}
