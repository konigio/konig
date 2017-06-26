package io.konig.deploy.gcp;

public class DeploymentException extends Exception {
	private static final long serialVersionUID = 1L;

	public DeploymentException() {
	}

	public DeploymentException(String msg) {
		super(msg);
	}

	public DeploymentException(Throwable cause) {
		super(cause);
	}

	public DeploymentException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
