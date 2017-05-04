package io.konig.maven.google.download;

public class DownloadException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public DownloadException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
